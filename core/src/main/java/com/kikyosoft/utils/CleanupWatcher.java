package com.kikyosoft.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Java 8 compatible cleanup helper for objects which own non-JVM resources.
 *
 * The watched owner object is held only by WeakReference. The cleanup action
 * must not capture or reference the owner object, otherwise the owner cannot be
 * garbage collected.
 */
public class CleanupWatcher implements AutoCloseable {
   public interface CleanupAction {
      void cleanup() throws Exception;
   }

   public interface CleanupErrorHandler {
      void cleanupError(String p_name, Throwable p_error);
   }

   public static final class Registration implements AutoCloseable {
      private final CleanupWatcher watcher;
      private final CleanupReference reference;

      private Registration(CleanupWatcher p_watcher, CleanupReference p_reference) {
         watcher = p_watcher;
         reference = p_reference;
      }

      /**
       * Explicitly cleanup the registered resource and remove it from the
       * watcher. It is safe to call this method more than once.
       */
      public void close() {
         watcher.cleanup(reference, true);
      }

      /**
       * Remove this registration without running cleanup. Use this only when
       * the resource has already been released by other code.
       */
      public void cancel() {
         watcher.cancel(reference);
      }

      public boolean isClosed() {
         return reference.isClosed();
      }

      public String getName() {
         return reference.getName();
      }
   }

   private static final CleanupWatcher DEFAULT =
      new CleanupWatcher("CleanupWatcher", new CleanupErrorHandler() {
         public void cleanupError(String p_name, Throwable p_error) {
            LogUtil.log(1, "CleanupWatcher cleanup error [" + p_name + "]:" + p_error.toString());
         }
      });

   private final String name;
   private final ReferenceQueue<Object> queue;
   private final ConcurrentMap<CleanupReference, String> references;
   private final CleanupErrorHandler errorHandler;
   private final AtomicBoolean running;
   private final AtomicBoolean started;
   private final AtomicLong explicitCleanupCount;
   private final AtomicLong queueCleanupCount;
   private final AtomicLong failedCleanupCount;
   private volatile Thread worker;

   public CleanupWatcher(String p_name) {
      this(p_name, null);
   }

   public CleanupWatcher(String p_name, CleanupErrorHandler p_errorHandler) {
      name = p_name == null || p_name.length() == 0 ? "CleanupWatcher" : p_name;
      errorHandler = p_errorHandler;
      queue = new ReferenceQueue<Object>();
      references = new ConcurrentHashMap<CleanupReference, String>();
      running = new AtomicBoolean(true);
      started = new AtomicBoolean(false);
      explicitCleanupCount = new AtomicLong();
      queueCleanupCount = new AtomicLong();
      failedCleanupCount = new AtomicLong();
   }

   public static CleanupWatcher getDefault() {
      return DEFAULT;
   }

   public static Registration registerDefault(Object p_owner, CleanupAction p_cleanup) {
      return DEFAULT.register(p_owner, p_cleanup);
   }

   public static Registration registerDefault(Object p_owner, CleanupAction p_cleanup, String p_name) {
      return DEFAULT.register(p_owner, p_cleanup, p_name);
   }

   public static Registration registerDefault(Object p_owner, CleanupAction p_cleanup,
                                              String p_name, String p_value) {
      return DEFAULT.register(p_owner, p_cleanup, p_name, p_value);
   }

   public Registration register(Object p_owner, CleanupAction p_cleanup) {
      return register(p_owner, p_cleanup, null);
   }

   public Registration register(Object p_owner, CleanupAction p_cleanup, String p_name) {
      return register(p_owner, p_cleanup, p_name, p_name);
   }

   public Registration register(Object p_owner, CleanupAction p_cleanup, String p_name, String p_value) {
      if (p_owner == null) {
         throw new IllegalArgumentException("owner is null");
      }
      if (p_cleanup == null) {
         throw new IllegalArgumentException("cleanup is null");
      }
      if (!running.get()) {
         throw new IllegalStateException(name + " is shutdown");
      }
      startWorker();
      CleanupReference reference = new CleanupReference(p_owner, queue, p_cleanup, p_name);
      references.put(reference, p_value == null ? reference.getName() : p_value);
      return new Registration(this, reference);
   }

   public int getRegisteredCount() {
      return references.size();
   }

   public List<String> getRegisteredValues() {
      return new ArrayList<String>(references.values());
   }

   public long getExplicitCleanupCount() {
      return explicitCleanupCount.get();
   }

   public long getQueueCleanupCount() {
      return queueCleanupCount.get();
   }

   public long getFailedCleanupCount() {
      return failedCleanupCount.get();
   }

   /**
    * Stop the watcher thread. Registered resources are not cleaned by this
    * method because shutdown does not mean the watched owner objects are dead.
    */
   public void shutdown() {
      LogUtil.log(1, "CleanupWatcher termainated");
      running.set(false);
      Thread thread = worker;
      if (thread != null) {
         thread.interrupt();
      }
   }

   public void close() {
      shutdown();
   }

   private void startWorker() {
      if (!started.compareAndSet(false, true)) {
         return;
      }
      worker = new Thread(new Runnable() {
         public void run() {
            runWorker();
         }
      }, name);
      worker.setDaemon(true);
      worker.start();
   }

   private void runWorker() {
      LogUtil.log(1, "CleanupWatcher started");
      while (running.get()) {
         try {
            CleanupReference reference = (CleanupReference) queue.remove(30000L);
            if (reference != null) {
               cleanup(reference, false);
            }
            drainQueue();
         }
         catch (InterruptedException ex) {
            drainQueue();
         }
         catch (Throwable ex) {
            failedCleanupCount.incrementAndGet();
            reportError(name, ex);
         }
      }
      drainQueue();
   }

   private void drainQueue() {
      CleanupReference reference;
      while ((reference = (CleanupReference) queue.poll()) != null) {
         cleanup(reference, false);
      }
   }

   private void cleanup(CleanupReference p_reference, boolean p_explicit) {
      if (p_reference == null) {
         return;
      }
      if (!p_reference.closeStarted()) {
         return;
      }
      references.remove(p_reference);
      p_reference.clear();
      try {
         p_reference.cleanup();
         if (p_explicit) {
            explicitCleanupCount.incrementAndGet();
         }
         else {
            queueCleanupCount.incrementAndGet();
         }
      }
      catch (Throwable ex) {
         failedCleanupCount.incrementAndGet();
         reportError(p_reference.getName(), ex);
      }
   }

   private void cancel(CleanupReference p_reference) {
      if (p_reference == null) {
         return;
      }
      if (p_reference.closeStarted()) {
         references.remove(p_reference);
         p_reference.clear();
      }
   }

   private void reportError(String p_name, Throwable p_error) {
      if (errorHandler != null) {
         try {
            errorHandler.cleanupError(p_name, p_error);
            return;
         }
         catch (Throwable ignored) {
         }
      }
      LogUtil.log(1, "CleanupWatcher cleanup error [" + p_name + "]:" + p_error.toString());
   }

   private static final class CleanupReference extends WeakReference<Object> {
      private final CleanupAction cleanup;
      private final AtomicBoolean closed;
      private final String name;

      private CleanupReference(Object p_owner, ReferenceQueue<Object> p_queue,
                               CleanupAction p_cleanup, String p_name) {
         super(p_owner, p_queue);
         cleanup = p_cleanup;
         closed = new AtomicBoolean(false);
         name = p_name == null || p_name.length() == 0 ? p_owner.getClass().getName() : p_name;
      }

      private boolean closeStarted() {
         return closed.compareAndSet(false, true);
      }

      private boolean isClosed() {
         return closed.get();
      }

      private String getName() {
         return name;
      }

      private void cleanup() throws Exception {
         cleanup.cleanup();
      }
   }
}

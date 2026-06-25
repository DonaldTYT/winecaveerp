package com.kikyosoft.ai.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

public class AIJobQueueService<TRequest, TResult> {

    private final String queueName;
    private final AIJobHandler<TRequest, TResult> handler;
    private final ExecutorService worker;
    private final ConcurrentHashMap<String, AIJobRecord<TResult>> jobs = new ConcurrentHashMap<>();
    private final AtomicBoolean started = new AtomicBoolean(false);

    public AIJobQueueService(String queueName, AIJobHandler<TRequest, TResult> handler) {
        this.queueName = Objects.requireNonNull(queueName, "queueName");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.worker = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, queueName + "-worker");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        started.compareAndSet(false, true);
    }

    public String submit(TRequest request) {
        return submit(request, null);
    }

    public String submit(TRequest request, ResultNotifier<TResult> notifier) {
        ensureStarted();

        String jobId = UUID.randomUUID().toString();
        AIJobRecord<TResult> record =
                new AIJobRecord<>(jobId, queueName, System.currentTimeMillis(), notifier);

        jobs.put(jobId, record);

        Future<?> future = worker.submit(() -> process(jobId, request, record));
        record.setFuture(future);

        return jobId;
    }

    public AIJobRecord<TResult> getJob(String jobId) {
        AIJobRecord<TResult> record = jobs.get(jobId);
        if (record == null) {
            AIJobRecord<TResult> notFound = new AIJobRecord<>(jobId, queueName, 0, null);
            notFound.setState(AIJobState.NOT_FOUND);
            notFound.setErrorMessage("Job not found");
            return notFound;
        }
        return record;
    }

    public TResult waitForResult(String jobId, long timeout, TimeUnit unit) throws Exception {
        AIJobRecord<TResult> record = jobs.get(jobId);
        if (record == null) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }
        if (record.getFuture() == null) {
            throw new IllegalStateException("Job future not initialized: " + jobId);
        }

        record.getFuture().get(timeout, unit);

        if (record.getState() == AIJobState.FAILED) {
            throw new RuntimeException("Job failed: " + record.getErrorMessage());
        }

        return record.getResult();
    }

    public void shutdown() {
        worker.shutdown();
    }

    public void shutdownNow() {
        worker.shutdownNow();
    }

    private void process(String jobId, TRequest request, AIJobRecord<TResult> record) {
        record.setState(AIJobState.RUNNING);
        record.setStartedAtMillis(System.currentTimeMillis());

        try {
            TResult result = handler.execute(request);
            record.setResult(result);
            record.setState(AIJobState.SUCCESS);
            callSuccessNotifier(record, result);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = e.getClass().getName();
            }
            record.setErrorMessage(msg);
            record.setState(AIJobState.FAILED);
            callFailureNotifier(record, msg, e);
        } finally {
            record.setFinishedAtMillis(System.currentTimeMillis());
        }
    }

    private void callSuccessNotifier(AIJobRecord<TResult> record, TResult result) {
        ResultNotifier<TResult> notifier = record.getNotifier();
        if (notifier == null) return;

        try {
            notifier.notifySuccess(record.getJobId(), result);
        } catch (Exception notifyEx) {
            // swallow notifier failure so it doesn't corrupt job state
            notifyEx.printStackTrace();
        }
    }

    private void callFailureNotifier(AIJobRecord<TResult> record, String errorMessage, Throwable cause) {
        ResultNotifier<TResult> notifier = record.getNotifier();
        if (notifier == null) return;

        try {
            notifier.notifyFailure(record.getJobId(), errorMessage, cause);
        } catch (Exception notifyEx) {
            notifyEx.printStackTrace();
        }
    }

    private void ensureStarted() {
        if (!started.get()) {
            throw new IllegalStateException("Queue service not started");
        }
    }
}
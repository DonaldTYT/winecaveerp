package com.kikyosoft.tcp;

//Ensure this class is picked up by component scan (or declare as @Bean)

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TcpServersManager implements SmartLifecycle {

 private final List<TcpServerInstance> servers;
 private final AtomicBoolean running = new AtomicBoolean(false);

 public TcpServersManager(List<TcpServerInstance> servers) {
     this.servers = servers;
 }

 @Override
 public void start() {
     if (running.compareAndSet(false, true)) {
         System.out.println("[TCP] Manager starting " + servers.size() + " server(s)...");
         for (TcpServerInstance s : servers) s.start();
     }
 }

 @Override
 public void stop() {
     if (running.compareAndSet(true, false)) {
         System.out.println("[TCP] Manager stopping servers...");
         for (TcpServerInstance s : servers) s.stop();
     }
 }

 @Override public boolean isRunning() { return running.get(); }
 @Override public boolean isAutoStartup() { return true; }
 @Override public int getPhase() { return 0; }
}

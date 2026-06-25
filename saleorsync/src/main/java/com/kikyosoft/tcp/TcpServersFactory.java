package com.kikyosoft.tcp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class TcpServersFactory {

    @Bean
    public List<TcpServerInstance> tcpServers(TcpServersProperties props, ApplicationContext ctx) {
        List<TcpServerInstance> out = new ArrayList<>();
        for (TcpServersProperties.ServerSpec s : props.getServers()) {
            ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
            exec.setCorePoolSize(Math.max(2, s.getWorkerCore()));
            exec.setMaxPoolSize(Math.max(exec.getCorePoolSize(), s.getWorkerMax()));
            exec.setQueueCapacity(Math.max(1, s.getQueueCapacity()));
            exec.setKeepAliveSeconds(60);
            exec.setThreadNamePrefix("tcp-" + s.getName() + "-");
            exec.setWaitForTasksToCompleteOnShutdown(true);
            exec.setAwaitTerminationSeconds(5);
            exec.initialize();

            TcpHandler handler = ctx.getBean(s.getHandlerBean(), TcpHandler.class);

            TcpServerInstance instance = new TcpServerInstance(
                    s.getName(),
                    s.getPort(),
                    s.getBacklog(),
                    s.getClientReadTimeoutMs(),
                    exec,
                    handler
            );
            out.add(instance);
        }
        return out;
    }
}

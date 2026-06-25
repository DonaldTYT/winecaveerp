package com.kikyosoft.ai.queue;

public interface ResultNotifier<TResult> {

    void notifySuccess(String jobId, TResult result);

    void notifyFailure(String jobId, String errorMessage, Throwable cause);
    
    boolean notified();
}
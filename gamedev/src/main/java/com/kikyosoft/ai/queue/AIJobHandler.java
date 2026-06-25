package com.kikyosoft.ai.queue;

public interface AIJobHandler<TRequest, TResult> {
    TResult execute(TRequest request) throws Exception;
}
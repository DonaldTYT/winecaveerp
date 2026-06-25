package com.kikyosoft.ai.queue;

import java.util.concurrent.Future;

public class AIJobRecord<TResult> {
    private final String jobId;
    private final String jobType;
    private final long createdAtMillis;
    private final ResultNotifier<TResult> notifier;

    private volatile AIJobState state = AIJobState.QUEUED;
    private volatile long startedAtMillis;
    private volatile long finishedAtMillis;
    private volatile TResult result;
    private volatile String errorMessage;
    private volatile Future<?> future;

    public AIJobRecord(String jobId, String jobType, long createdAtMillis, ResultNotifier<TResult> notifier) {
        this.jobId = jobId;
        this.jobType = jobType;
        this.createdAtMillis = createdAtMillis;
        this.notifier = notifier;
    }

    public String getJobId() { return jobId; }
    public String getJobType() { return jobType; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public ResultNotifier<TResult> getNotifier() { return notifier; }

    public AIJobState getState() { return state; }
    public void setState(AIJobState state) { this.state = state; }

    public long getStartedAtMillis() { return startedAtMillis; }
    public void setStartedAtMillis(long startedAtMillis) { this.startedAtMillis = startedAtMillis; }

    public long getFinishedAtMillis() { return finishedAtMillis; }
    public void setFinishedAtMillis(long finishedAtMillis) { this.finishedAtMillis = finishedAtMillis; }

    public TResult getResult() { return result; }
    public void setResult(TResult result) { this.result = result; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Future<?> getFuture() { return future; }
    public void setFuture(Future<?> future) { this.future = future; }
}
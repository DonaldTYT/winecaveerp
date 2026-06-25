package com.kikyosoft.ai.comfy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sequential queue service for ComfyUIImageToTextClient.
 *
 * Features:
 * - accepts requests quickly
 * - executes them one by one in order
 * - returns a job id immediately
 * - lets caller poll status/result later
 *
 * Notes:
 * - Uses a single worker thread to guarantee sequential execution.
 * - Stores uploaded image bytes in memory so the original request stream
 *   can be closed immediately after submission.
 */
public class ComfyUIImageToTextQueueService {

    private final File workflowFile;
    private final ComfyUIImageToTextClient client;

    // single-thread executor => strictly sequential processing
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "comfy-image-to-text-worker");
        t.setDaemon(true);
        return t;
    });

    // Keep job states for polling
    private final ConcurrentHashMap<String, JobRecord> jobs = new ConcurrentHashMap<>();

    private final AtomicBoolean started = new AtomicBoolean(false);

    public ComfyUIImageToTextQueueService(File workflowFile) {
        this(workflowFile, new ComfyUIImageToTextClient());
    }

    public ComfyUIImageToTextQueueService(File workflowFile, ComfyUIImageToTextClient client) {
        this.workflowFile = Objects.requireNonNull(workflowFile, "workflowFile");
        this.client = Objects.requireNonNull(client, "client");
    }

    /**
     * Optional startup hook. Safe to call multiple times.
     */
    public void start() {
        started.compareAndSet(false, true);
    }

    /**
     * Submit a new job.
     *
     * The input stream is fully read during submission so the caller can close it immediately.
     */
    public String submit(InputStream imageStream, String fileName, String promptText) throws Exception {
        ensureStarted();

        byte[] imageBytes = imageStream.readAllBytes();
        String jobId = UUID.randomUUID().toString();

        JobRecord job = new JobRecord(
                jobId,
                fileName,
                promptText,
                imageBytes,
                Instant.now().toEpochMilli()
        );

        jobs.put(jobId, job);

        Future<?> future = worker.submit(() -> processJob(job));
        job.setFuture(future);

        return jobId;
    }

    /**
     * Convenience overload for byte[].
     */
    public String submit(byte[] imageBytes, String fileName, String promptText) {
        ensureStarted();

        String jobId = UUID.randomUUID().toString();

        JobRecord job = new JobRecord(
                jobId,
                fileName,
                promptText,
                imageBytes,
                Instant.now().toEpochMilli()
        );

        jobs.put(jobId, job);

        Future<?> future = worker.submit(() -> processJob(job));
        job.setFuture(future);

        return jobId;
    }

    /**
     * Returns a snapshot of the job status.
     */
    public JobStatus getStatus(String jobId) {
        JobRecord job = jobs.get(jobId);
        if (job == null) {
            return JobStatus.notFound(jobId);
        }

        return new JobStatus(
                job.getJobId(),
                job.getState(),
                job.getFileName(),
                job.getPromptText(),
                job.getResultText(),
                job.getErrorMessage(),
                job.getCreatedAtMillis(),
                job.getStartedAtMillis(),
                job.getFinishedAtMillis()
        );
    }

    /**
     * Wait for completion and return result text.
     */
    public String waitForResult(String jobId, long timeout, TimeUnit unit) throws Exception {
        JobRecord job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }

        Future<?> future = job.getFuture();
        if (future == null) {
            throw new IllegalStateException("Job future not initialized: " + jobId);
        }

        future.get(timeout, unit);

        if (job.getState() == JobState.FAILED) {
            throw new RuntimeException("Job failed: " + job.getErrorMessage());
        }

        return job.getResultText();
    }

    /**
     * Optional cleanup for old finished jobs.
     */
    public int removeFinishedJobsOlderThan(long age, TimeUnit unit) {
        long cutoff = System.currentTimeMillis() - unit.toMillis(age);
        int removed = 0;

        for (JobRecord job : jobs.values()) {
            boolean finished = job.getState() == JobState.SUCCESS || job.getState() == JobState.FAILED;
            long finishedAt = job.getFinishedAtMillis();

            if (finished && finishedAt > 0 && finishedAt < cutoff) {
                if (jobs.remove(job.getJobId(), job)) {
                    removed++;
                }
            }
        }

        return removed;
    }

    public void shutdown() {
        worker.shutdown();
    }

    public void shutdownNow() {
        worker.shutdownNow();
    }

    private void processJob(JobRecord job) {
        job.setState(JobState.RUNNING);
        job.setStartedAtMillis(System.currentTimeMillis());

        try (InputStream is = new ByteArrayInputStream(job.getImageBytes())) {
            String result = client.runWorkflow(workflowFile, is, job.getFileName(), job.getPromptText());
            job.setResultText(result);
            job.setState(JobState.SUCCESS);
        } catch (Exception e) {
            job.setErrorMessage(buildErrorMessage(e));
            job.setState(JobState.FAILED);
        } finally {
            job.setFinishedAtMillis(System.currentTimeMillis());
            // optional: free memory after completion
            job.clearImageBytes();
        }
    }

    private static String buildErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = e.getClass().getName();
        }
        return msg;
    }

    private void ensureStarted() {
        if (!started.get()) {
            throw new IllegalStateException("Queue service not started");
        }
    }

    public enum JobState {
        QUEUED,
        RUNNING,
        SUCCESS,
        FAILED,
        NOT_FOUND
    }

    public static class JobStatus {
        private final String jobId;
        private final JobState state;
        private final String fileName;
        private final String promptText;
        private final String resultText;
        private final String errorMessage;
        private final long createdAtMillis;
        private final long startedAtMillis;
        private final long finishedAtMillis;

        public JobStatus(String jobId,
                         JobState state,
                         String fileName,
                         String promptText,
                         String resultText,
                         String errorMessage,
                         long createdAtMillis,
                         long startedAtMillis,
                         long finishedAtMillis) {
            this.jobId = jobId;
            this.state = state;
            this.fileName = fileName;
            this.promptText = promptText;
            this.resultText = resultText;
            this.errorMessage = errorMessage;
            this.createdAtMillis = createdAtMillis;
            this.startedAtMillis = startedAtMillis;
            this.finishedAtMillis = finishedAtMillis;
        }

        public static JobStatus notFound(String jobId) {
            return new JobStatus(jobId, JobState.NOT_FOUND, null, null, null, "Job not found", 0, 0, 0);
        }

        public String getJobId() { return jobId; }
        public JobState getState() { return state; }
        public String getFileName() { return fileName; }
        public String getPromptText() { return promptText; }
        public String getResultText() { return resultText; }
        public String getErrorMessage() { return errorMessage; }
        public long getCreatedAtMillis() { return createdAtMillis; }
        public long getStartedAtMillis() { return startedAtMillis; }
        public long getFinishedAtMillis() { return finishedAtMillis; }
    }

    private static class JobRecord {
        private final String jobId;
        private final String fileName;
        private final String promptText;
        private volatile byte[] imageBytes;
        private final long createdAtMillis;

        private volatile long startedAtMillis;
        private volatile long finishedAtMillis;
        private volatile String resultText;
        private volatile String errorMessage;
        private volatile JobState state = JobState.QUEUED;
        private volatile Future<?> future;

        JobRecord(String jobId, String fileName, String promptText, byte[] imageBytes, long createdAtMillis) {
            this.jobId = jobId;
            this.fileName = fileName;
            this.promptText = promptText;
            this.imageBytes = imageBytes;
            this.createdAtMillis = createdAtMillis;
        }

        public String getJobId() { return jobId; }
        public String getFileName() { return fileName; }
        public String getPromptText() { return promptText; }
        public byte[] getImageBytes() { return imageBytes; }
        public long getCreatedAtMillis() { return createdAtMillis; }
        public long getStartedAtMillis() { return startedAtMillis; }
        public long getFinishedAtMillis() { return finishedAtMillis; }
        public String getResultText() { return resultText; }
        public String getErrorMessage() { return errorMessage; }
        public JobState getState() { return state; }
        public Future<?> getFuture() { return future; }

        public void setStartedAtMillis(long startedAtMillis) { this.startedAtMillis = startedAtMillis; }
        public void setFinishedAtMillis(long finishedAtMillis) { this.finishedAtMillis = finishedAtMillis; }
        public void setResultText(String resultText) { this.resultText = resultText; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public void setState(JobState state) { this.state = state; }
        public void setFuture(Future<?> future) { this.future = future; }

        public void clearImageBytes() { this.imageBytes = null; }
    }
}
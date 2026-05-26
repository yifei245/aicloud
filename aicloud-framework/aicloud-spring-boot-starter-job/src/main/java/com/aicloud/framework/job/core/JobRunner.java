package com.aicloud.framework.job.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Shared async job runner.
 *
 * @author yifei
 */
public class JobRunner {

    private final Executor executor;

    public JobRunner(Executor executor) {
        this.executor = executor;
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    public Executor getExecutor() {
        return executor;
    }
}

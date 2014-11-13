package org.nixxed.clusterfilepoller.timer;

import org.nixxed.clusterfilepoller.FilePollJob;
import org.nixxed.clusterfilepoller.zk.MasterTask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Controls the scheduling of threads to handle executing the task of polling.
 */
public class FilePollScheduler implements MasterTask {
    private ScheduledExecutorService scheduler;
    private Set<FilePollJob> jobs;

    public FilePollScheduler(FilePollJob job) {
        jobs = new HashSet<FilePollJob>();
        jobs.add(job);
    }

    public FilePollScheduler(Set<FilePollJob> jobs) {
        this.jobs = jobs;
    }

    @Override
    public void start() {
        scheduler = new ScheduledThreadPoolExecutor(jobs.size());

        for (FilePollJob job : jobs) {
            CustomFileFilter filter = new CustomFileFilter(job.getListener(), job.getRegex());
            Runnable task = new FilePollTimerTask(job.getPath(), filter);

            scheduler.scheduleAtFixedRate(task, 0, job.getIntervalSeconds(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();

            scheduler = null;
        }
    }
}

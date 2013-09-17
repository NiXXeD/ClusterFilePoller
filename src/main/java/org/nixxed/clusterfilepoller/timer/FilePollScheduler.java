package org.nixxed.clusterfilepoller.timer;

import org.nixxed.clusterfilepoller.ClusterFilePollJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Controls the scheduling of threads to handle executing the task of polling.
 */
@Component
public class FilePollScheduler {
    private ScheduledExecutorService service;
    private Set<ClusterFilePollJob> jobs;
    private boolean started = false;

    @Resource
    public void setJobs(Set<ClusterFilePollJob> jobs) {
        this.jobs = jobs;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        if (!started) {
            service = new ScheduledThreadPoolExecutor(jobs.size());
            started = true;

            for (ClusterFilePollJob job : jobs) {
                CustomFileFilter filter = new CustomFileFilter(job.getListener(), job.getRegex());
                Runnable task = new FilePollTimerTask(job.getPath(), filter);

                service.scheduleAtFixedRate(task, 0, job.getIntervalSeconds(), TimeUnit.SECONDS);
            }
        }
    }

    public void stop() {
        if (started) {
            service.shutdownNow();

            service = null;
            started = false;
        }
    }
}

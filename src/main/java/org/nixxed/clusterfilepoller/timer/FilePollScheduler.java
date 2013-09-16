package org.nixxed.clusterfilepoller.timer;

import org.nixxed.clusterfilepoller.ClusterFilePollJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 */
@Component
public class FilePollScheduler {
	private final Set<Timer> timers = new HashSet<Timer>();

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
			for(ClusterFilePollJob job : jobs) {
				CustomFileFilter filter = new CustomFileFilter(job.getListener(), job.getRegex());
				TimerTask task = new FilePollTimerTask(job.getPath(), filter);
	
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(task, 0, job.getIntervalSeconds() * 1000);
				timers.add(timer);
			}
		}
	}
	
	public void stop() {
		if (started) {
			for(Timer timer : timers) {
				timer.cancel();
				timer.purge();
			}
			timers.clear();
		}
	}
}

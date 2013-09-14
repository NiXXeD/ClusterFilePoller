package org.nixxed.clusterfilepoller.timer;

import org.nixxed.clusterfilepoller.ClusterFilePollJob;
import org.nixxed.clusterfilepoller.FilePollListener;

import java.util.*;

/**
 * 
 */
public class FilePollScheduler {
	private final FilePollListener listener;	
	private final ClusterFilePollJob[] jobs;
	private final Set<Timer> timers = new HashSet<Timer>();
	
	private boolean started = false;

	public FilePollScheduler(FilePollListener listener, ClusterFilePollJob[] jobs) {
		this.listener = listener;
		this.jobs = jobs;
	}
	
	public boolean isStarted() {
		return started;
	}

	public void start() {
		if (!started) {
			for(ClusterFilePollJob job : jobs) {
				CustomFileFilter filter = new CustomFileFilter(listener, job.getRegex());
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

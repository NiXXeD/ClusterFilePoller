package org.nixxed.clusterfilepoller;

/**
 * Describe a job for the cluster file poller. Will create one timer job / thread.
 */
public class ClusterFilePollJob {
	private String path;
	private String regex;
	private int intervalSeconds;
    private FilePollListener listener;

	public ClusterFilePollJob(String path, String regex, int intervalSeconds, FilePollListener listener) {
		this.path = path;
		this.regex = regex;
		this.intervalSeconds = intervalSeconds;
        this.listener = listener;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public int getIntervalSeconds() {
		return intervalSeconds;
	}

	public void setIntervalSeconds(int intervalSeconds) {
		this.intervalSeconds = intervalSeconds;
	}

    public FilePollListener getListener() {
        return listener;
    }

    public void setListener(FilePollListener listener) {
        this.listener = listener;
    }
}

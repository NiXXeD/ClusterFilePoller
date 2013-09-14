package org.nixxed.clusterfilepoller.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.TimerTask;

/**
 * A timer job to poll files.
 */
public class FilePollTimerTask extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(FilePollTimerTask.class);
	
	private String path;
	private CustomFileFilter filter;

	public FilePollTimerTask(String path, CustomFileFilter filter) {
		this.path = path;
		this.filter = filter;
	}

	@Override
	public void run() {
		logger.trace("Starting polling path: {}", path);

		File dir = new File(path);
		if (dir.exists() && dir.isDirectory()) {
			listFiles(dir, filter);
		} else {
			logger.info("Directory does not exist or is not a directory: {}", path);
		}

		logger.trace("Finished polling path: {}", path);
	}
	
	private void listFiles(File dir, CustomFileFilter filter) {
		File[] subDirs = dir.listFiles(filter);
		
		if (subDirs != null) {
			for (File subDir : subDirs) {
				listFiles(subDir, filter);
			}
		}
	}

	
}

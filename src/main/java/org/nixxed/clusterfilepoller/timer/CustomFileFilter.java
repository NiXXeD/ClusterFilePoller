package org.nixxed.clusterfilepoller.timer;

import org.nixxed.clusterfilepoller.FilePollListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;

/**
 * A custom file filter to allow files to be worked mid-poll.
 */
public class CustomFileFilter implements FileFilter {
	private static final Logger logger = LoggerFactory.getLogger(CustomFileFilter.class);
	
	private final FilePollListener listener;
	private final String regex;

	public CustomFileFilter(FilePollListener listener, String regex) {
		this.listener = listener;
		this.regex = regex;
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		} else {
			String path = file.getPath();
			if (path.matches(regex)) {
				logger.trace("File found: {}", path);
				listener.fileFound(file);
			}
			return false;
		}
	}
}

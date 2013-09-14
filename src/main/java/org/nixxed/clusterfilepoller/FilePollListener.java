package org.nixxed.clusterfilepoller;

import java.io.File;

/**
 * A listener to handle files found by FilePollTimerTask.
 */
public interface FilePollListener {
	public void fileFound(File file);
}

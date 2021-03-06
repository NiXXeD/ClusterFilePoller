package org.nixxed.clusterfilepoller.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A single polling task job to poll files.
 */
public class FilePollTimerTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(FilePollTimerTask.class);

    private String path;
    private CustomFileFilter filter;

    public FilePollTimerTask(String path, CustomFileFilter filter) {
        this.path = path;
        this.filter = filter;
    }

    @Override
    public void run() {
        logger.trace("Starting Polling path={}", path);

        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            listFiles(dir, filter);
        } else {
            logger.warn("Directory does not exist or is not a directory: {}", path);
        }

        logger.trace("Finished Polling path={}", path);
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

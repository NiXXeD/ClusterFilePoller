package org.nixxed.clusterfilepoller;

import org.nixxed.clusterfilepoller.timer.FilePollScheduler;
import org.nixxed.clusterfilepoller.zk.MasterHelper;

import java.io.File;

public class ClusterIntegrationTest {

    public static void main(String[] args) throws Exception {
        FilePollListener listener = new FilePollListener() {
            @Override
            public void fileFound(File file) {
                file.renameTo(new File(file.getPath() + "_running"));
            }
        };

        FilePollJob job = new FilePollJob("/test/path", "(?i)^.*\\.(txt|zip)$", 5, listener);

        FilePollScheduler scheduler = new FilePollScheduler(job);

        String connectionString = "localhost:2181";
        String electionPath = "/clusterFilePollerTest";
        String description = "TestPoller";
        MasterHelper masterHelper = new MasterHelper(scheduler, connectionString, electionPath, description);
        masterHelper.start();

        Thread.sleep(25000);

        masterHelper.stop();
    }

}

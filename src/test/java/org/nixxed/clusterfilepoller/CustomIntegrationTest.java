package org.nixxed.clusterfilepoller;

import org.nixxed.clusterfilepoller.zk.MasterHelper;
import org.nixxed.clusterfilepoller.zk.MasterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(CustomIntegrationTest.class);

    public static void main(String[] args) throws Exception {
        MasterTask task = new MasterTask() {
            @Override
            public void start() {
                logger.debug("CustomTask started...");
            }

            @Override
            public void stop() {
                logger.debug("CustomTask stopped...");
            }
        };

        String connectionString = "localhost:2181";
        String electionPath = "/clusterFilePollerTest";
        String description = "TestPoller";
        MasterHelper masterHelper = new MasterHelper(task, connectionString, electionPath, description);
        masterHelper.start();

        Thread.sleep(25000);

        masterHelper.stop();
    }

}

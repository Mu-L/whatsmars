package org.hongxi.whatsmars.curator;

import org.apache.curator.test.TestingServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by shenhongxi on 2021/4/24.
 */
public class EmbeddedZookeeper {

    public static void main(String[] args) throws Exception {
        start(2181);
    }

    private static TestingServer testingServer;

    public static void start(int port) throws Exception {
        String dataDir = System.getProperty("user.home") + File.separator + "test_zk_data";
        Path path = Paths.get(dataDir);
        Files.createDirectories(path);

        File dir = new File(dataDir + File.separator + System.nanoTime());
        try {
            testingServer = new TestingServer(port, dir);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Thread.sleep(1000L);
                    testingServer.close();
                } catch (InterruptedException | IOException ignore) {
                }
            }));
        }
    }
}
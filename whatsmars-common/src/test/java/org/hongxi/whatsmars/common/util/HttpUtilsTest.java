package org.hongxi.whatsmars.common.util;

import java.io.FileOutputStream;
import java.io.InputStream;

public class HttpUtilsTest {

//    @Test
    public void testGetAsStream() throws Exception {
        String url = "https://gitee.com/javahongxi/whatsmars/raw/master/whatsmars-mq/whatsmars-mq-rocketmq/RMQ.png";
        try (InputStream inputStream = HttpUtils.getAsStream(url, null, null)) {
            FileOutputStream fileOutputStream = new FileOutputStream(System.getProperty("user.home") + "/Downloads/RMQ.png");
            while (true) {
                int i = inputStream.read();
                if (i == -1) {
                    break;
                }
                fileOutputStream.write((byte) i);
            }
            fileOutputStream.close();
        }
    }
}

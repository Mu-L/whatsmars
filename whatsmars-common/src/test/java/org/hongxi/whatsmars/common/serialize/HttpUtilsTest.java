package org.hongxi.whatsmars.common.serialize;

import org.hongxi.whatsmars.common.util.HttpUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class HttpUtilsTest {

    @Test
    public void t() throws Exception {
        String url = "http://stat.whatsmars.com/ds/x2/f00qvewaxsqdnrzazhhyguhduc7wd8sv.png";
        InputStream inputStream = HttpUtils.httpGetStream(url, null);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File("/Users/javahongxi/Documents/test22222.png"));
            while (true) {
                int i = inputStream.read();
                if( i == -1) {
                    break;
                }
                fileOutputStream.write((byte)i);
            }
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
    }
}

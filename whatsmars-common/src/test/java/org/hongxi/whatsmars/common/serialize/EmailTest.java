package org.hongxi.whatsmars.common.serialize;

import org.hongxi.whatsmars.common.mail.EmailSenderClient;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EmailTest {

//    @Test
    public void t() throws Exception {
        EmailSenderClient client = new EmailSenderClient();
        client.setSmtpPort(25);
        client.setHostName("smtp.qq.com");
        client.setUsername("service@whatsmars.com");
        client.setPassword("whatsmars99");//您的邮箱密码
        client.setSslOn(true);
        client.setFromAddress("service@whatsmars.com");

        String targetAddress = "javahongxi@163.com";
        //client.sendTextEmail("javahongxi@163.com","测试邮件","是否可以收到邮件！");
        Map<String, URL> attaches = new HashMap<String, URL>();
        attaches.put("logo",new URL("http://www.baidu.com/img/bd_logo1.png"));
        attaches.put("logo2",new URL("http://commons.apache.org/proper/commons-email/images/commons-logo.png"));
        client.sendMultipartEmail(targetAddress, "测试邮件", "test", attaches);
        System.out.println("发送成功！");
    }
}

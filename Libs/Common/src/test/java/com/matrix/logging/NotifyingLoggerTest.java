package com.matrix.logging;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import com.google.common.collect.ImmutableMap;
import com.matrix.logging.NotifyingLogger;

import rapture.common.CallingContext;
import rapture.common.RaptureConstants;
import rapture.common.RaptureURI;
import rapture.common.Scheme;
import rapture.common.impl.jackson.JacksonUtil;
import rapture.config.ConfigLoader;
import rapture.config.RaptureConfig;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;
import rapture.mail.Mailer;
import rapture.mail.SMTPConfig;

@Deprecated
public class NotifyingLoggerTest {

    static final Wiser wiser = new Wiser();
    static final CallingContext context = ContextFactory.getKernelUser();
    static final String configStr = "CONFIG";
    static final String geezer = "geezer";

    static String saveRaptureRepo;
    static String saveInitSysConfig;
    static CallingContext rootContext;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RaptureConfig.setLoadYaml(false);
        RaptureConfig config = ConfigLoader.getConf();

        saveRaptureRepo = config.RaptureRepo;
        saveInitSysConfig = config.InitSysConfig;

        System.setProperty("LOGSTASH-ISENABLED", "false");

        Kernel.INSTANCE.restart();
        Kernel.initBootstrap();
        rootContext = ContextFactory.getKernelUser();

        Kernel.getAudit().createAuditLog(ContextFactory.getKernelUser(), new RaptureURI(RaptureConstants.DEFAULT_AUDIT_URI, Scheme.LOG).getAuthority(),
                "LOG {} using MEMORY {}");
        Kernel.getLock().createLockManager(ContextFactory.getKernelUser(), "lock://kernel", "LOCKING USING DUMMY {}", "");

        wiser.setPort(2525);
        wiser.start();

        SMTPConfig emailCfg = new SMTPConfig().setHost("localhost").setPort(2525).setUsername("").setPassword("")
                .setFrom("Incapture <support@incapturetechnologies.com>").setAuthentication(false).setTlsenable(false).setTlsrequired(false);
        Kernel.getSys().writeSystemConfig(context, configStr, Mailer.SMTP_CONFIG_URL, JacksonUtil.jsonFromObject(emailCfg));

        wiser.getServer().setRequireTLS(false);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        wiser.stop();
    }

    @Test
    public void testEmail() throws MessagingException {
        Map<String, Object> map = ImmutableMap.of("NOTIFY_TYPE", "EMAIL", "MESSAGE_SUBJECT", "TestEmail" + System.currentTimeMillis(), "EMAIL_RECIPIENTS",
                "support@incapturetechnologies.com");

        NotifyingLogger.setAlertLevel(Level.ERROR);
        NotifyingLogger.setConfiguration(map);
        NotifyingLogger.getLogger("Test").fatal("This is a test");

        boolean found = false;
        for (WiserMessage message : wiser.getMessages()) {
            String envelopeSender = message.getEnvelopeSender();
            String envelopeReceiver = message.getEnvelopeReceiver();
            MimeMessage mess = message.getMimeMessage();
            System.out.println("got message " + mess.getSubject());
            if (mess.getSubject().equals(map.get("MESSAGE_SUBJECT"))) found = true;
        }
        assertTrue(found);
    }
}

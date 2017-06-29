package com.matrix.alerts;

import com.matrix.Constants;
import com.matrix.common.Config;
import com.matrix.common.ServiceLocator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STWriter;
import rapture.mail.Mailer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {Mailer.class})
public class EmailAlertPublisherTest {

    Config config = mock(Config.class);

    @Before
    public void before() throws Exception {
        mockStatic(Mailer.class);
    }

    @Test
    public void test() throws Exception {
        expect(config.getString("MESSAGE_SUBJECT", null)).andReturn("Alert: Success");
        expect(config.getString("EMAIL_RECIPIENTS", null)).andReturn("a@a.com, b@a.com");
        expect(config.get("PREFIX_ENV_TO_MAIL_SUBJECT", Boolean.class, true)).andReturn(false);
        expect(config.get("INJECT_HOST_NAME_IN_MESSAGE", Boolean.class, true)).andReturn(false);


        Mailer.email(aryEq(new String[] {"a@a.com", "b@a.com"}), eq("Alert: Success"), eq("Processing complete"));
        expectLastCall();

        replayAll();
        replay(config);
        new EMailAlertPublisher().sendAlert(config, "Processing complete");
        verifyAll();
        verify(config);

    }
}

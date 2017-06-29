package com.matrix.logging;

import com.matrix.alerts.AlertService;
import com.matrix.common.ServiceLocator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ServiceLocator.class, NotifyingLogger2.class, NotifyingAppender.class, Logger.class })
public class NotifyingLogger2Test {

    AlertService alertService = mock(AlertService.class);
    NotifyingAppender notifyingAppender = mock(NotifyingAppender.class);


    @Before
    public void before() throws Exception {
        mockStatic(ServiceLocator.class);
        mockStatic(NotifyingAppender.class);
        mockStatic(Logger.class);

    }

    @Test
    public void defaultAlertLevel() throws Exception {
        Logger logger = mock(Logger.class);
        expect(logger.getLogger("java.lang.String")).andReturn(logger);
        logger.removeAppender("NotifyingLogger");
        expectLastCall();
        expect(ServiceLocator.getAlertService()).andReturn(alertService);
        expectNew(NotifyingAppender.class, Level.ERROR, alertService).andReturn(notifyingAppender);
        logger.addAppender(notifyingAppender);
        expectLastCall();

        replayAll();
        replay(logger);
        NotifyingLogger2.getLogger(String.class);
        verifyAll();
        verify(logger);
    }

    @Test
    public void changeAlertLevel() throws Exception {
        Logger logger = mock(Logger.class);
        expect(logger.getLogger("java.lang.String")).andReturn(logger);
        logger.removeAppender("NotifyingLogger");
        expectLastCall();
        expect(ServiceLocator.getAlertService()).andReturn(alertService);
        expectNew(NotifyingAppender.class, Level.WARN, alertService).andReturn(notifyingAppender);
        logger.addAppender(notifyingAppender);
        expectLastCall();

        replayAll();
        replay(logger);
        NotifyingLogger2.getLogger(String.class, Level.WARN);
        verifyAll();
        verify(logger);
    }

    @Test
    public void changeDefaultAlertLevel() throws Exception {
        Logger logger = mock(Logger.class);
        expect(logger.getLogger("java.lang.String")).andReturn(logger);
        logger.removeAppender("NotifyingLogger");
        expectLastCall();
        expect(ServiceLocator.getAlertService()).andReturn(alertService);
        expectNew(NotifyingAppender.class, Level.WARN, alertService).andReturn(notifyingAppender);
        logger.addAppender(notifyingAppender);
        expectLastCall();

        replayAll();
        replay(logger);
        NotifyingLogger2.setDefaultAlertLevel(Level.WARN);
        NotifyingLogger2.getLogger(String.class);
        verifyAll();
        verify(logger);
    }
}

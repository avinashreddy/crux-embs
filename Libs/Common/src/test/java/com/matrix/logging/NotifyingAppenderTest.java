package com.matrix.logging;

import com.matrix.alerts.AlertService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

public class NotifyingAppenderTest {

    AlertService alertService = mock(AlertService.class);

    NotifyingAppender notifyingAppender = new NotifyingAppender(Level.ERROR, alertService);

    @Test
    public void sendAlert() throws Exception {
        Logger logger = mock(Logger.class);
        LoggingEvent event = mock(LoggingEvent.class);
        ThrowableInformation throwableInformation = mock(ThrowableInformation.class);


        expect(event.getLevel()).andReturn(Level.FATAL);
        expect(event.getMessage()).andReturn("error in processing!!!");
        expect(event.getThrowableInformation()).andReturn(throwableInformation);
        expect(throwableInformation.getThrowableStrRep()).andReturn(new String [] {"a", "b"});

        alertService.sendAlert("error in processing!!!" + System.lineSeparator() + "a" + System.lineSeparator() + "b");
        expectLastCall();
        expectLastCall();
        replayAll();
        replay(logger, event, throwableInformation);
        notifyingAppender.append(event);
        verifyAll();
        verify(logger, event, throwableInformation);
    }


    @Test
    public void doNotSendAlert() throws Exception {
        Logger logger = mock(Logger.class);
        LoggingEvent event = mock(LoggingEvent.class);
        expect(event.getLevel()).andReturn(Level.INFO); // Default alert level is ERROR. So no alert should be sent.
        expect(event.getMessage()).andReturn("error in processing!!!");
        replayAll();
        replay(logger, event);
        notifyingAppender.append(event);
        verifyAll();
        verify(logger, event);
    }
}

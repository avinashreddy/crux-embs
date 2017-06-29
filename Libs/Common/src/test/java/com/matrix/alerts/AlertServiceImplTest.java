package com.matrix.alerts;

import com.matrix.common.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.eq;
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
@PrepareForTest( { EMailAlertPublisher.class, SlackAlertPublisher.class, WorkflowAlertPublisher.class, AlertServiceImpl.class})
public class AlertServiceImplTest {

    Config config = mock(Config.class);

    EMailAlertPublisher eMailAlertPublisher = mock(EMailAlertPublisher.class);
    SlackAlertPublisher slackAlertPublisher = mock(SlackAlertPublisher.class);
    WorkflowAlertPublisher workflowAlertPublisher = mock(WorkflowAlertPublisher.class);

    @Before
    public void before() throws Exception {
        mockStatic(EMailAlertPublisher.class);
        mockStatic(SlackAlertPublisher.class);
        mockStatic(WorkflowAlertPublisher.class);
        mockStatic(AlertServiceImpl.class);
        expectNew(EMailAlertPublisher.class).andReturn(eMailAlertPublisher);
        expectNew(SlackAlertPublisher.class).andReturn(slackAlertPublisher);
        expectNew(WorkflowAlertPublisher.class).andReturn(workflowAlertPublisher);
    }

    @Test
    public void sendInfoAlert() {
        expect(config.getString("NOTIFY_TYPE")).andReturn("EMAIL, SLACK");

        eMailAlertPublisher.sendAlert(config, "Processing complete");
        expectLastCall();
        slackAlertPublisher.sendAlert(config, "Processing complete");
        expectLastCall();

        replayAll();
        replay(eMailAlertPublisher, slackAlertPublisher, config);

        new AlertServiceImpl(config).sendAlert("Processing complete");

        verifyAll();
        verify(eMailAlertPublisher, slackAlertPublisher, config);
    }

    @Test
    public void sendErrorAlert() {
        expect(config.getString("NOTIFY_TYPE")).andReturn("EMAIL");

        eMailAlertPublisher.sendAlert(eq(config),  contains("Processing failed" + System.lineSeparator() + System.lineSeparator() + "java.lang.IllegalStateException: DB Not available."));
        expectLastCall();

        replayAll();
        replay(eMailAlertPublisher, config);

        new AlertServiceImpl(config).sendAlert("Processing failed", new IllegalStateException("DB Not available."));

        verifyAll();
        verify(eMailAlertPublisher, config);
    }
}

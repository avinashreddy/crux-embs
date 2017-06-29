package com.matrix.alerts;

import com.matrix.common.Config;
import com.matrix.common.KernelServices;
import com.matrix.common.ServiceLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import rapture.common.CallingContext;
import rapture.common.CreateResponse;
import rapture.common.dp.Workflow;
import rapture.kernel.ContextFactory;
import rapture.kernel.DecisionApiImplWrapper;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {ContextFactory.class, KernelServices.class, HashMap.class, WorkflowAlertPublisher.class})
public class WorkflowAlertPublisherTest {

    DecisionApiImplWrapper decisionApi = mock(DecisionApiImplWrapper.class);
    CallingContext callingContext = mock(CallingContext.class);
    Workflow workflow = mock(Workflow.class);
    Config config = mock(Config.class);

    @Before
    public void before() throws Exception {
        mockStatic(KernelServices.class);
        mockStatic(ContextFactory.class);
        mockStatic(HashMap.class);
        mockStatic(WorkflowAlertPublisher.class);
    }

    @Test
    public void test() throws Exception {
        expect(config.getString("NOTIFY_WORKFLOW", "workflow://workflows/notification/general"))
                .andReturn("workflow://workflows/notification/general");
        HashMap<String, String> args = mock(HashMap.class);
        expectNew(HashMap.class).andReturn(args);

        expect(args.put("message", "Processing complete")).andReturn("");


        expect(KernelServices.getDecision()).andReturn(decisionApi);
        expect(ContextFactory.getKernelUser()).andReturn(callingContext);

        expect(decisionApi.getWorkflow(callingContext, "workflow://workflows/notification/general"))
                .andReturn(workflow);

        CreateResponse response = new CreateResponse();
        response.setIsCreated(true);

        expect(decisionApi.createWorkOrderP(callingContext, "workflow://workflows/notification/general",
                args, null)).andReturn(response);

        replayAll();
        replay(decisionApi, callingContext, workflow, config, args);
        new WorkflowAlertPublisher().sendAlert(config, "Processing complete");
        verifyAll();
        verify(decisionApi, callingContext, workflow, config, args);

    }
}

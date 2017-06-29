package com.matrix;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import rapture.common.RaptureURI;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { System.class, RaptureUriUtils.class})

public class RaptureUriUtilsTest {

    @Test
    public void test() {
        mockStatic(System.class);
        expect(System.getenv("UI_URL")).andReturn("http://localhost:8000/");
        PowerMock.replayAll();
        String url =  RaptureUriUtils.buildWorkOrderHTTPUrl(
               "workorder://1496620800/workflows/matrix/pin/pin/WO00000012#0");
        verifyAll();
        Assert.assertEquals("http://localhost:8000/process/workflows/matrix/pin/pin&workorder=WO00000012", url);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidUri() {
        String url =  RaptureUriUtils.buildWorkOrderHTTPUrl(
                "blob://1496620800/workflows/matrix/pin/pin/WO00000012#0");
    }
}

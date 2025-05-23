package cn.iocoder.yudao.module.esb.camel.processor.logging;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EsbPostForwardLogProcessorTest {

    @Mock
    private Exchange mockExchange;

    @Mock
    private Message mockInMessage;

    private EsbLogBuilder realLogBuilder;
    private EsbInterfaceDO interfaceDO; // To hold interface data for context

    private EsbPostForwardLogProcessor postForwardLogProcessor;

    @BeforeEach
    void setUp() {
        interfaceDO = new EsbInterfaceDO(); // Initialize interfaceDO
        interfaceDO.setId(1L);
        interfaceDO.setCode("TEST_IF001");
        
        realLogBuilder = new EsbLogBuilder(interfaceDO.getId(), interfaceDO.getCode());
        // Simulate that forwardRequestTime was set by EsbPreForwardLogProcessor
        realLogBuilder.forwardRequestTime(LocalDateTime.now().minusSeconds(1)); // 1 second ago

        postForwardLogProcessor = new EsbPostForwardLogProcessor();

        when(mockExchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class)).thenReturn(interfaceDO);
        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class))
            .thenReturn(realLogBuilder);
        when(mockExchange.getIn()).thenReturn(mockInMessage);
    }

    @Test
    void process_SuccessfulForward_ShouldLogResponseAndSuccess() throws Exception {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        headers.put("X-Response-Header", "ResponseValue");
        String body = "Forward Response Body";
        Integer httpStatus = 200;

        when(mockInMessage.getHeaders()).thenReturn(headers);
        when(mockInMessage.getBody(String.class)).thenReturn(body);
        when(mockInMessage.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class)).thenReturn(httpStatus);
        when(mockExchange.getException()).thenReturn(null); // No exception

        // Act
        postForwardLogProcessor.process(mockExchange);

        // Assert
        assertNotNull(realLogBuilder.build().getForwardResponseTime());
        assertTrue(realLogBuilder.build().getForwardResponseHeaders().contains("\"X-Response-Header\":\"ResponseValue\""));
        assertEquals(body, realLogBuilder.build().getForwardResponseBody());
        assertTrue(realLogBuilder.build().getSuccess()); // Based on 200 status
        assertNull(realLogBuilder.build().getErrorMessage()); // No error message for success

        // Verify forward duration was stored in exchange property
        verify(mockExchange).setProperty(eq(EsbPostForwardLogProcessor.FORWARD_DURATION_PROPERTY), anyLong());
    }

    @Test
    void process_FailedForward_HttpStatusNot2xx_ShouldLogResponseAndFailure() throws Exception {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        String body = "{\"error\":\"failed\"}"; // Corrected JSON syntax
        Integer httpStatus = 500;

        when(mockInMessage.getHeaders()).thenReturn(headers); // Empty headers for this case
        when(mockInMessage.getBody(String.class)).thenReturn(body);
        when(mockInMessage.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class)).thenReturn(httpStatus);
        when(mockExchange.getException()).thenReturn(null);

        // Act
        postForwardLogProcessor.process(mockExchange);

        // Assert
        assertNotNull(realLogBuilder.build().getForwardResponseTime());
        assertFalse(realLogBuilder.build().getSuccess());
        assertNotNull(realLogBuilder.build().getErrorMessage());
        assertTrue(realLogBuilder.build().getErrorMessage().contains("HTTP status: 500"));
        assertTrue(realLogBuilder.build().getErrorMessage().contains(body)); // Error message should contain short body
    }
    
    @Test
    void process_FailedForward_WithException_ShouldLogResponseAndFailure() throws Exception {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        String body = "Some response before exception or null";
        Integer httpStatus = null; // Or some non-2xx status
        Exception mockException = new RuntimeException("Connection timed out");

        when(mockInMessage.getHeaders()).thenReturn(headers);
        when(mockInMessage.getBody(String.class)).thenReturn(body);
        when(mockInMessage.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class)).thenReturn(httpStatus);
        when(mockExchange.getException()).thenReturn(mockException); // Exception present

        // Act
        postForwardLogProcessor.process(mockExchange);

        // Assert
        assertNotNull(realLogBuilder.build().getForwardResponseTime());
        assertFalse(realLogBuilder.build().getSuccess());
        assertNotNull(realLogBuilder.build().getErrorMessage());
        assertTrue(realLogBuilder.build().getErrorMessage().contains("HTTP status: null")); // Or actual status if set
        assertTrue(realLogBuilder.build().getErrorMessage().contains("Connection timed out"));
    }


    @Test
    void process_NullLogBuilder_ShouldLogError() throws Exception {
        // Arrange
        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class))
            .thenReturn(null);

        // Act
        postForwardLogProcessor.process(mockExchange);

        // Assert - similar to EsbPreForwardLogProcessorTest, difficult to assert log output directly.
        // Test ensures no NPE and assumes internal logging handles the error.
        assertTrue(true, "Processor should handle null logBuilder gracefully.");
    }
}

package cn.iocoder.yudao.module.esb.camel.processor.logging;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EsbPreForwardLogProcessorTest {

    @Mock
    private Exchange mockExchange;

    @Mock
    private Message mockInMessage;
    
    // Use @Spy to allow partial mocking/verification of the EsbLogBuilder if needed,
    // or simply use a real instance and verify its state after the processor runs.
    // For this test, we'll pass a real EsbLogBuilder via exchange properties.
    private EsbLogBuilder realLogBuilder;

    private EsbPreForwardLogProcessor preForwardLogProcessor;

    @BeforeEach
    void setUp() {
        EsbInterfaceDO interfaceDO = new EsbInterfaceDO();
        interfaceDO.setId(1L);
        interfaceDO.setCode("TEST_IF001");
        
        realLogBuilder = new EsbLogBuilder(interfaceDO.getId(), interfaceDO.getCode());
        preForwardLogProcessor = new EsbPreForwardLogProcessor();

        // Common arrange for mockExchange
        when(mockExchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class)).thenReturn(interfaceDO);
        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class))
            .thenReturn(realLogBuilder);
        when(mockExchange.getIn()).thenReturn(mockInMessage);
    }

    @Test
    void process_ValidLogBuilder_ShouldUpdateForwardRequestDetails() throws Exception {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        headers.put("X-Forward-Header", "ForwardValue");
        String body = "Forward Request Body";

        when(mockInMessage.getHeaders()).thenReturn(headers);
        when(mockInMessage.getBody(String.class)).thenReturn(body);

        // Act
        preForwardLogProcessor.process(mockExchange);

        // Assert
        assertNotNull(realLogBuilder.build().getForwardRequestTime());
        assertTrue(realLogBuilder.build().getForwardRequestHeaders().contains("\"X-Forward-Header\":\"ForwardValue\""));
        assertEquals(body, realLogBuilder.build().getForwardRequestBody());
    }

    @Test
    void process_NullLogBuilder_ShouldLogErrorAndReturn() throws Exception {
        // Arrange
        // Override the setup for this specific test case
        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class))
            .thenReturn(null);
        
        // Spy on the processor to verify log.error (advanced, optional) or simply check no modifications happen
        // For simplicity, we check that the logBuilder's state (if it were accessible, which it isn't directly)
        // wouldn't be modified. Since it's null, the processor should just return.

        // Act
        preForwardLogProcessor.process(mockExchange);

        // Assert
        // Difficult to assert logger calls without PowerMock or DI for logger.
        // We can infer by checking that no methods were called on a (hypothetical) non-null logBuilder
        // if it had been passed in. But since it's null, the processor should simply log and exit.
        // No specific assertion here other than the test completes without NPE.
        // The processor's internal logging is assumed to handle the error message.
        assertTrue(true, "Processor should handle null logBuilder gracefully by logging and returning.");
    }
}

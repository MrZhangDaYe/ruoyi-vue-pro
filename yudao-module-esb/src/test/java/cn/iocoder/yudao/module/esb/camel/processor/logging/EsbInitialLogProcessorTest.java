package cn.iocoder.yudao.module.esb.camel.processor.logging;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EsbInitialLogProcessorTest {

    @Mock
    private Exchange mockExchange;

    @Mock
    private Message mockInMessage;

    @InjectMocks
    private EsbInitialLogProcessor initialLogProcessor;

    @Test
    void process_ValidExchange_ShouldInitializeLogBuilder() throws Exception {
        // Arrange
        EsbInterfaceDO interfaceDO = new EsbInterfaceDO();
        interfaceDO.setId(1L);
        interfaceDO.setCode("TEST_IF001");

        Map<String, Object> headers = new HashMap<>();
        headers.put("X-Test-Header", "TestValue");
        String body = "Test Request Body";

        when(mockExchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class)).thenReturn(interfaceDO);
        when(mockExchange.getIn()).thenReturn(mockInMessage);
        when(mockInMessage.getHeaders()).thenReturn(headers);
        when(mockInMessage.getBody(String.class)).thenReturn(body);

        // Act
        initialLogProcessor.process(mockExchange);

        // Assert
        verify(mockExchange).setProperty(eq(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY), any(EsbLogBuilder.class));
        
        // Optionally, capture the EsbLogBuilder and verify its content
        org.mockito.ArgumentCaptor<EsbLogBuilder> logBuilderCaptor = org.mockito.ArgumentCaptor.forClass(EsbLogBuilder.class);
        verify(mockExchange).setProperty(eq(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY), logBuilderCaptor.capture());
        EsbLogBuilder capturedBuilder = logBuilderCaptor.getValue();
        
        assertNotNull(capturedBuilder.build().getRequestTime());
        assertEquals(interfaceDO.getId(), capturedBuilder.build().getInterfaceId());
        assertEquals(interfaceDO.getCode(), capturedBuilder.build().getInterfaceCode());
        assertTrue(capturedBuilder.build().getRequestHeaders().contains("\"X-Test-Header\":\"TestValue\""));
        assertEquals(body, capturedBuilder.build().getRequestBody());
    }

    @Test
    void process_NullInterfaceDO_ShouldLogErrorAndReturn() throws Exception {
        // Arrange
        when(mockExchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class)).thenReturn(null);

        // Act
        initialLogProcessor.process(mockExchange);

        // Assert
        // Verify that EsbLogBuilder was NOT set in exchange properties
        verify(mockExchange, never()).setProperty(eq(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY), any(EsbLogBuilder.class));
        // Further logging verification can be done if a mock logger is injected into the processor,
        // but for now, this interaction check is sufficient.
    }
}

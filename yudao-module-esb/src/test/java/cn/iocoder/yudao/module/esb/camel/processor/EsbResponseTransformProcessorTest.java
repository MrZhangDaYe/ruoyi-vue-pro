package cn.iocoder.yudao.module.esb.camel.processor;

import cn.iocoder.yudao.module.esb.camel.processor.logging.EsbInitialLogProcessor;
import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.dataobject.mapping.EsbMappingDO;
import cn.iocoder.yudao.module.esb.service.mapping.EsbMappingService;
import org.apache.camel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EsbResponseTransformProcessorTest {

    @Mock
    private Exchange mockExchange;

    @Mock
    private Message mockInMessage;

    @Mock
    private EsbMappingService mockMappingService;
    
    @Mock
    private CamelContext mockCamelContext;

    @Mock
    private TypeConverter mockTypeConverter;
    
    private EsbInterfaceDO realInterfaceDO;
    private EsbLogBuilder realLogBuilder;
    private EsbResponseTransformProcessor responseTransformProcessor;

    @BeforeEach
    void setUp() {
        realInterfaceDO = new EsbInterfaceDO();
        realInterfaceDO.setId(1L);
        realInterfaceDO.setCode("IF_RESP_TEST");

        realLogBuilder = new EsbLogBuilder(realInterfaceDO.getId(), realInterfaceDO.getCode());
        
        responseTransformProcessor = new EsbResponseTransformProcessor(realInterfaceDO, mockMappingService);

        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class))
            .thenReturn(realLogBuilder);
        when(mockExchange.getIn()).thenReturn(mockInMessage);
        when(mockExchange.getContext()).thenReturn(mockCamelContext);
        when(mockCamelContext.getTypeConverter()).thenReturn(mockTypeConverter);
    }

    @Test
    void process_NoMappingRule_ShouldPassThrough() throws Exception {
        String originalBody = "{\"status\":\"success\"}";
        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(null);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        responseTransformProcessor.process(mockExchange);

        verify(mockInMessage, never()).setBody(any());
        assertEquals(originalBody, mockInMessage.getBody(String.class));
    }

    @Test
    void process_MappingRuleDisabled_ShouldPassThrough() throws Exception {
        String originalBody = "{\"status\":\"success\"}";
        EsbMappingDO disabledMapping = new EsbMappingDO();
        disabledMapping.setStatus(0); // Disabled
        disabledMapping.setMappingTemplate("$uppercase(status)");
        disabledMapping.setContentType("application/json");
        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(disabledMapping);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        responseTransformProcessor.process(mockExchange);

        verify(mockInMessage, never()).setBody(any());
        assertEquals(originalBody, mockInMessage.getBody(String.class));
    }
    
    @Test
    void process_MappingRuleNoTemplate_ShouldPassThrough() throws Exception {
        String originalBody = "{\"status\":\"success\"}";
        EsbMappingDO mappingWithNoTemplate = new EsbMappingDO();
        mappingWithNoTemplate.setStatus(1);
        mappingWithNoTemplate.setMappingTemplate(""); // Blank template
        mappingWithNoTemplate.setContentType("application/json");
        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingWithNoTemplate);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        responseTransformProcessor.process(mockExchange);
        verify(mockInMessage, never()).setBody(any());
    }
    
    @Test
    void process_MappingRuleNotJson_ShouldPassThrough() throws Exception {
        String originalBody = "<xmlstatus>success</xmlstatus>";
        EsbMappingDO mappingNotJson = new EsbMappingDO();
        mappingNotJson.setStatus(1);
        mappingNotJson.setMappingTemplate("$uppercase(status)");
        mappingNotJson.setContentType("application/xml"); // Not JSON
        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingNotJson);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);
        
        responseTransformProcessor.process(mockExchange);
        verify(mockInMessage, never()).setBody(any());
    }

    @Test
    void process_BlankBody_ShouldSkipTransformation() throws Exception {
        String originalBody = ""; // Blank body
        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("application/json");
        mappingRule.setMappingTemplate("$uppercase(status)");

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        responseTransformProcessor.process(mockExchange);
        verify(mockInMessage, never()).setBody(any());
    }

    @Test
    void process_ValidJsonataTransformation_ShouldTransformBody() throws Exception {
        String originalBody = "{\"data\":{\"value\": \"test\"}}";
        // Example: JSONata expression to extract and uppercase 'value'
        String jsonataExpression = "$uppercase(data.value)"; 
        String transformedBody = "TEST";     // Expected output

        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("application/json");
        mappingRule.setMappingTemplate(jsonataExpression);

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        Object mockJsonataResult = "TEST"; // Simulated result from JsonataLanguage.evaluate()
        when(mockTypeConverter.convertTo(eq(String.class), eq(mockExchange), any())).thenAnswer(invocation -> {
            Object bodyToConvert = invocation.getArgument(2);
            if (bodyToConvert.equals(mockJsonataResult)) { // Check if the input to convertTo is our mockJsonataResult
                return transformedBody;
            }
            return bodyToConvert.toString();
        });

        responseTransformProcessor.process(mockExchange);

        verify(mockInMessage).setBody(transformedBody);
        // Success flag in logBuilder might have been set by EsbPostForwardLogProcessor.
        // This transformation success shouldn't wrongly set it to true if it was false.
        // And if transformation fails, it should set it to false.
    }

    @Test
    void process_JsonataTransformationError_ShouldSetLogAndThrowException() throws Exception {
        String originalBody = "{\"data\":\"test\"}";
        String jsonataExpression = "$invalidJsonataFunction(data)";

        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("application/json");
        mappingRule.setMappingTemplate(jsonataExpression);

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        // Let real JsonataLanguage attempt to parse and fail
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            responseTransformProcessor.process(mockExchange);
        });
        
        assertTrue(thrownException.getMessage().contains("Response transformation failed"));
        assertFalse(realLogBuilder.build().getSuccess()); // Explicitly check success is false
        assertNotNull(realLogBuilder.build().getErrorMessage());
        assertTrue(realLogBuilder.build().getErrorMessage().contains("Response transformation failed"));
    }
}

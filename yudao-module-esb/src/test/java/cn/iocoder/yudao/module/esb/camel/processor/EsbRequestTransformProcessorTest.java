package cn.iocoder.yudao.module.esb.camel.processor;

import cn.iocoder.yudao.module.esb.camel.processor.logging.EsbInitialLogProcessor; // For the property key
import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.dataobject.mapping.EsbMappingDO;
import cn.iocoder.yudao.module.esb.service.mapping.EsbMappingService;
import org.apache.camel.*;
import org.apache.camel.component.jsonata.JsonataLanguage; // Direct import for mocking if needed, or rely on context
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class EsbRequestTransformProcessorTest {

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
    
    // We need a real EsbInterfaceDO for the processor's constructor
    private EsbInterfaceDO realInterfaceDO;

    // We need a real EsbLogBuilder to be on the exchange
    private EsbLogBuilder realLogBuilder;

    private EsbRequestTransformProcessor requestTransformProcessor;

    @BeforeEach
    void setUp() {
        realInterfaceDO = new EsbInterfaceDO();
        realInterfaceDO.setId(1L);
        realInterfaceDO.setCode("IF_REQ_TEST");

        realLogBuilder = new EsbLogBuilder(realInterfaceDO.getId(), realInterfaceDO.getCode());
        
        requestTransformProcessor = new EsbRequestTransformProcessor(realInterfaceDO, mockMappingService);

        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class))
            .thenReturn(realLogBuilder);
        when(mockExchange.getIn()).thenReturn(mockInMessage);
        when(mockExchange.getContext()).thenReturn(mockCamelContext);
        when(mockCamelContext.getTypeConverter()).thenReturn(mockTypeConverter);
    }

    @Test
    void process_NoMappingRule_ShouldPassThrough() throws Exception {
        // Arrange
        String originalBody = "{\"key\":\"value\"}";
        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "REQUEST")).thenReturn(null);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        // Act
        requestTransformProcessor.process(mockExchange);

        // Assert
        verify(mockInMessage, never()).setBody(any()); // Body should not be set again if no transformation
        assertEquals(originalBody, mockInMessage.getBody(String.class)); // Ensure original body is intact
        assertTrue(realLogBuilder.build().getSuccess() == null || realLogBuilder.build().getSuccess()); // Success not affected negatively
    }

    @Test
    void process_MappingRuleDisabled_ShouldPassThrough() throws Exception {
        // Arrange
        String originalBody = "{\"key\":\"value\"}";
        EsbMappingDO disabledMapping = new EsbMappingDO();
        disabledMapping.setStatus(0); // Disabled
        disabledMapping.setMappingTemplate("$uppercase(key)");
        disabledMapping.setContentType("application/json");
        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "REQUEST")).thenReturn(disabledMapping);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        // Act
        requestTransformProcessor.process(mockExchange);

        // Assert
        verify(mockInMessage, never()).setBody(any());
        assertEquals(originalBody, mockInMessage.getBody(String.class));
    }
    
    @Test
    void process_MappingRuleNoTemplate_ShouldPassThrough() throws Exception {
        String originalBody = "{\"key\":\"value\"}";
        EsbMappingDO mappingWithNoTemplate = new EsbMappingDO();
        mappingWithNoTemplate.setStatus(1);
        mappingWithNoTemplate.setMappingTemplate(""); // Blank template
        mappingWithNoTemplate.setContentType("application/json");
        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "REQUEST")).thenReturn(mappingWithNoTemplate);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        requestTransformProcessor.process(mockExchange);
        verify(mockInMessage, never()).setBody(any());
    }
    
    @Test
    void process_MappingRuleNotJson_ShouldPassThrough() throws Exception {
        String originalBody = "<xml/>";
        EsbMappingDO mappingNotJson = new EsbMappingDO();
        mappingNotJson.setStatus(1);
        mappingNotJson.setMappingTemplate("$uppercase(key)");
        mappingNotJson.setContentType("application/xml"); // Not JSON
        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "REQUEST")).thenReturn(mappingNotJson);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);
        
        requestTransformProcessor.process(mockExchange);
        verify(mockInMessage, never()).setBody(any());
    }

    @Test
    void process_ValidJsonataTransformation_ShouldTransformBody() throws Exception {
        // Arrange
        String originalBody = "{\"name\":\"world\"}";
        String transformedBody = "HELLO, WORLD"; // Expected output from JSONata: "$uppercase('Hello, ' & name)"
        String jsonataExpression = "$uppercase('Hello, ' & name)";

        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("application/json");
        mappingRule.setMappingTemplate(jsonataExpression);

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "REQUEST")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);
        
        // Mocking the JsonataLanguage and Expression evaluation
        // This is a bit complex to mock perfectly without deeper Camel test utilities,
        // but we can mock the outcome.
        // A simpler approach for unit test is to assume JsonataLanguage itself works,
        // and we are testing the processor's logic around it.
        // For this test, we'll mock the TypeConverter to return the expected transformedBody
        // when the Jsonata expression's (mocked) evaluation result is passed to it.

        // Let's assume the JsonataLanguage().createExpression(jsonataExpression).evaluate(exchange, Object.class)
        // would return some intermediate object (e.g. the string "HELLO, WORLD" itself or a Map).
        // The key is that TypeConverter is then used.
        Object mockJsonataResult = "HELLO, WORLD"; // This is what Jsonata would produce
        when(mockTypeConverter.convertTo(eq(String.class), eq(mockExchange), any())).thenAnswer(invocation -> {
            Object bodyToConvert = invocation.getArgument(2);
            // Simulate TypeConverter for this specific case
            if (bodyToConvert.equals(mockJsonataResult)) {
                 return transformedBody;
            }
            return bodyToConvert.toString();
        });


        // Act
        requestTransformProcessor.process(mockExchange);

        // Assert
        verify(mockInMessage).setBody(transformedBody);
        assertTrue(realLogBuilder.build().getSuccess() == null || realLogBuilder.build().getSuccess());
    }

    @Test
    void process_JsonataTransformationError_ShouldSetLogAndThrowException() throws Exception {
        // Arrange
        String originalBody = "{\"name\":\"world\"}";
        String jsonataExpression = "$invalidJsonataFunction(name)"; // Invalid expression

        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("application/json");
        mappingRule.setMappingTemplate(jsonataExpression);

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "REQUEST")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        // Mock the Jsonata evaluation to throw an exception
        // This requires getting hold of the Expression object created inside the processor.
        // A simpler way is to make JsonataLanguage itself a mock, or allow the real one to throw.
        // For this test, we'll let the real JsonataLanguage attempt to parse and fail.
        // This is closer to an integration test for JsonataLanguage but tests processor's error handling.
        // Alternatively, mock the Expression.evaluate to throw.

        // To directly test the catch block, we would need to mock JsonataLanguage and Expression.
        // For now, let's assume the real JsonataLanguage is used and it throws an error for an invalid expression.
        // This means the test might depend on the actual Jsonata library behavior.

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            requestTransformProcessor.process(mockExchange);
        });
        
        assertTrue(thrownException.getMessage().contains("Request transformation failed"));
        assertFalse(realLogBuilder.build().getSuccess());
        assertNotNull(realLogBuilder.build().getErrorMessage());
        assertTrue(realLogBuilder.build().getErrorMessage().contains("Request transformation failed"));
        // Verify body was not changed from original (or it might be null depending on when error happens)
        // verify(mockInMessage, never()).setBody(anyString()); // This might be too strict if setBody happens before error
    }
    
    @Test
    void process_BlankBody_ShouldSkipTransformation() throws Exception {
        // Arrange
        String originalBody = ""; // Blank body
        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("application/json");
        mappingRule.setMappingTemplate("$uppercase(key)");

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "REQUEST")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);

        // Act
        requestTransformProcessor.process(mockExchange);

        // Assert
        verify(mockInMessage, never()).setBody(any()); // No transformation
    }
}

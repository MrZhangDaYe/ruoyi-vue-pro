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

import com.fasterxml.jackson.core.JsonProcessingException; // Example import for Jackson

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
    void process_ValidJsonataTransformation_ShouldTransformBody_WhenForwardNotSoap() throws Exception {
        // Arrange
        realInterfaceDO.setForwardProtocol("HTTP"); // Or any non-SOAP to ensure JSONata path
        realInterfaceDO.setProtocol("HTTP"); // Assuming ESB serves HTTP

        String originalBody = "{\"data\":{\"value\": \"test\"}}";
        String jsonataExpression = "$uppercase(data.value)"; 
        String transformedBody = "TEST";

        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("application/json"); // Expects JSON input, produces JSON
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
    void process_JsonataTransformationError_ShouldSetLogAndThrowException_WhenForwardNotSoap() throws Exception {
        // Arrange
        realInterfaceDO.setForwardProtocol("HTTP"); // Ensure JSONata path for this error
        realInterfaceDO.setProtocol("HTTP");

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

    // New tests for SOAP XML to JSON
    @Test
    void process_SoapXmlToJson_Jackson_Success() throws Exception {
        // Arrange
        String originalSoapXmlBody = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:greetResponse xmlns:ns2=\"http://example.com/types\"><ns2:greeting>Hello Pikachu</ns2:greeting></ns2:greetResponse></soap:Body></soap:Envelope>";
        // Expected JSON might depend on how XmlMapper converts (e.g., handling of namespaces, SOAP envelope).
        // Let's assume a simple case where the body content is extracted and converted.
        // The processor's XmlMapper is new XmlMapper(), then readValue(xml, Map.class), then new ObjectMapper().writeValueAsString(map).
        // This would typically convert the <greetResponse> part.
        // For a more direct test, we can mock what xmlMapper.readValue would return.
        
        // What a simple XmlMapper().readValue(xml, Map.class) might produce for the <greetResponse> part:
        // Map<String, Object> expectedMap = Map.of("greetResponse", Map.of("greeting", "Hello Pikachu"));
        // Then ObjectMapper().writeValueAsString(expectedMap) would be:
        // String expectedJson = "{\"greetResponse\":{\"greeting\":\"Hello Pikachu\"}}";


        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        // This content type indicates the *input* to this processor is XML
        mappingRule.setContentType("text/xml"); // Or application/soap+xml, application/xml
        // mappingRule.setMappingTemplate(""); // Template not used for direct XML->JSON

        // Simulate conditions: ESB served HTTP, but got response from SOAP backend
        realInterfaceDO.setProtocol("HTTP");
        realInterfaceDO.setForwardProtocol("SOAP");


        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalSoapXmlBody);

        // To avoid testing the real XmlMapper and ObjectMapper deeply here,
        // we can mock the behavior of the processor's internal mappers if they were injectable,
        // or, more simply, provide an XML that we know the default XmlMapper/ObjectMapper combo will convert to expectedJson.
        // For this test, we will rely on the processor's internal XmlMapper and ObjectMapper to work.
        // This means the test is slightly an integration test of Jackson's capabilities.

        // Act
        responseTransformProcessor.process(mockExchange);

        // Assert
        // Due to the complexity of predicting exact JSON output from arbitrary XML with default mappers (namespaces, etc.),
        // a more robust test would be to verify the Content-Type and that setBody was called.
        // For a more precise assertion on body, one might need to parse the resulting JSON and check specific fields.
        // For now, let's check for a key part of the expected JSON.
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockInMessage).setBody(bodyCaptor.capture());
        String actualJsonBody = bodyCaptor.getValue();
        
        // A simple check, actual structure depends on XmlMapper's default for SOAP.
        // This will likely fail if SOAP envelope is not stripped before XmlMapper.readValue(Map.class)
        // The processor currently doesn't strip SOAP envelope.
        // So, the Map will contain "Envelope", then "Body", then "greetResponse".
        // Let's adjust expected JSON or processor logic.
        // Assuming the processor's goal is to convert the *entire* XML to JSON for now:
        assertTrue(actualJsonBody.contains("Envelope")); 
        assertTrue(actualJsonBody.contains("Body"));
        assertTrue(actualJsonBody.contains("greetResponse"));
        assertTrue(actualJsonBody.contains("Hello Pikachu"));

        verify(mockInMessage).setHeader(Exchange.CONTENT_TYPE, "application/json; charset=utf-8");
        assertTrue(realLogBuilder.build().getSuccess() == null || realLogBuilder.build().getSuccess());
    }
    
    @Test
    void process_SoapXmlToJson_BlankBody_ShouldSetEmptyJson() throws Exception {
        // Arrange
        String originalSoapXmlBody = ""; // Blank XML body

        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("text/xml");

        realInterfaceDO.setProtocol("HTTP");
        realInterfaceDO.setForwardProtocol("SOAP");

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalSoapXmlBody);
        
        // Act
        responseTransformProcessor.process(mockExchange);

        // Assert
        verify(mockInMessage).setBody("{}"); // Or null or empty string, depends on desired behavior in processor
        verify(mockInMessage).setHeader(Exchange.CONTENT_TYPE, "application/json; charset=utf-8");
    }


    @Test
    void process_SoapXmlToJson_XmlParseException() throws Exception {
        // Arrange
        String invalidXmlBody = "<soap:Envelope><malformed>"; // Invalid XML

        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("text/xml");

        realInterfaceDO.setProtocol("HTTP");
        realInterfaceDO.setForwardProtocol("SOAP");

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(invalidXmlBody);

        // This test relies on the internal XmlMapper throwing an exception.

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            responseTransformProcessor.process(mockExchange);
        });

        assertTrue(thrownException.getMessage().contains("Response transformation failed (XML to JSON)"));
        assertFalse(realLogBuilder.build().getSuccess());
        assertNotNull(realLogBuilder.build().getErrorMessage());
        assertTrue(realLogBuilder.build().getErrorMessage().contains("Response transformation failed (XML to JSON)"));
    }
    
    // Example pass-through test for when SOAP to JSON is expected but content type is wrong
    @Test
    void process_SoapXmlToJson_ContentTypeNotXml_ShouldPassThrough() throws Exception {
        String originalBody = "{\"this\":\"isjson\"}"; // Body is JSON, but forward was SOAP
        EsbMappingDO mappingRule = new EsbMappingDO();
        mappingRule.setStatus(1);
        mappingRule.setContentType("application/json"); // Rule says input is JSON, but we want to test SOAP path
        
        realInterfaceDO.setProtocol("HTTP");
        realInterfaceDO.setForwardProtocol("SOAP"); // This makes it try the SOAP to JSON path

        when(mockMappingService.getMappingByInterfaceIdAndType(realInterfaceDO.getId(), "RESPONSE")).thenReturn(mappingRule);
        when(mockInMessage.getBody(String.class)).thenReturn(originalBody);
        
        responseTransformProcessor.process(mockExchange);

        // Because mappingRule.contentType is application/json, it will skip the SOAP to JSON path.
        // And because forwardProtocol is SOAP, it will skip the JSONata path.
        // So, it should pass through.
        verify(mockInMessage, never()).setBody(anyString());
        assertEquals(originalBody, mockInMessage.getBody(String.class));
    }
}

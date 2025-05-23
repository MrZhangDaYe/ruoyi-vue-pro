package cn.iocoder.yudao.module.esb.camel.processor;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils; // For ObjectMapper
import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.dataobject.mapping.EsbMappingDO;
import cn.iocoder.yudao.module.esb.service.mapping.EsbMappingService;
import cn.iocoder.yudao.module.esb.camel.processor.logging.EsbInitialLogProcessor;
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper; // Jackson XML Mapper
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.jsonata.JsonataLanguage;
import org.apache.camel.Expression;
import cn.hutool.core.util.StrUtil;

import java.util.Locale;
import java.util.Map; // For parsing XML to Map

@Slf4j
public class EsbResponseTransformProcessor implements Processor {
    private final EsbInterfaceDO interfaceDO;
    private final EsbMappingService mappingService;
    private final XmlMapper xmlMapper; // For XML to Map/Object
    private final ObjectMapper jsonMapper; // For Map/Object to JSON String

    public EsbResponseTransformProcessor(EsbInterfaceDO interfaceDO, EsbMappingService mappingService) {
        this.interfaceDO = interfaceDO;
        this.mappingService = mappingService;
        this.xmlMapper = new XmlMapper();
        // Configure xmlMapper if needed (e.g., default options are usually fine for simple cases)
        // xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.jsonMapper = JsonUtils.getObjectMapper(); // Reuse ObjectMapper from JsonUtils if it's configured globally
                                                  // Or new ObjectMapper() if JsonUtils not suitable/available
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) throws Exception {
        EsbLogBuilder logBuilder = exchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class);
        String originalBody = exchange.getIn().getBody(String.class);

        EsbMappingDO responseMapping = mappingService.getMappingByInterfaceIdAndType(interfaceDO.getId(), "RESPONSE");

        // Condition for SOAP XML to JSON transformation
        boolean shouldTransformSoapXmlToJson = "SOAP".equalsIgnoreCase(interfaceDO.getForwardProtocol()) && // Response is from a SOAP service
                ("HTTP".equalsIgnoreCase(interfaceDO.getProtocol()) || "HTTPS".equalsIgnoreCase(interfaceDO.getProtocol())) && // Original request to ESB was HTTP/REST
                responseMapping != null &&
                Integer.valueOf(1).equals(responseMapping.getStatus()) &&
                responseMapping.getContentType() != null &&
                (responseMapping.getContentType().toLowerCase(Locale.ROOT).contains("text/xml") ||
                 responseMapping.getContentType().toLowerCase(Locale.ROOT).contains("application/soap+xml") ||
                 responseMapping.getContentType().toLowerCase(Locale.ROOT).contains("application/xml"));
                 // And for now, we ignore mappingTemplate for this direct XML->JSON path.
                 // If mappingTemplate is present and is JSONata, it could be a second-stage transform on the JSON.

        // Condition for JSON to JSON (JSONata) transformation (existing logic)
        boolean shouldTransformJsonToJsonata = responseMapping != null &&
                Integer.valueOf(1).equals(responseMapping.getStatus()) &&
                StrUtil.isNotBlank(responseMapping.getMappingTemplate()) &&
                responseMapping.getContentType() != null &&
                responseMapping.getContentType().toLowerCase(Locale.ROOT).contains("application/json") &&
                !"SOAP".equalsIgnoreCase(interfaceDO.getForwardProtocol()); // Ensure this is for non-SOAP responses primarily


        if (shouldTransformSoapXmlToJson) {
            if (StrUtil.isBlank(originalBody)) {
                log.warn("Interface {}: Response body (SOAP XML) is blank, skipping XML to JSON transformation.", interfaceDO.getCode());
                // It might be valid for a SOAP service to return an empty body for some operations (e.g. HTTP 204)
                // but then there's nothing to transform.
                // Set body to empty JSON or null based on requirements.
                exchange.getIn().setBody("{}"); // Or null, or empty string
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json; charset=utf-8");
                return;
            }
            log.info("Interface {}: Attempting SOAP XML to JSON response transformation.", interfaceDO.getCode());
            try {
                // 1. Parse XML string to Map (or a generic JsonNode/Object)
                // This will convert the XML structure into a Java Map/List structure.
                // TODO: Need to consider SOAP Envelope. CXF PAYLOAD mode might return only Body content.
                // If it's full envelope, XmlMapper needs to be configured or pre-processing needed to extract Body.
                // For now, assume originalBody is the XML content to be converted (e.g. Body content).
                Object xmlAsObject = xmlMapper.readValue(originalBody, Map.class); // Using Map.class for generic structure

                // 2. Convert Map to JSON String
                String jsonResponseBody = jsonMapper.writeValueAsString(xmlAsObject);

                exchange.getIn().setBody(jsonResponseBody);
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json; charset=utf-8");
                log.info("Interface {}: Response body transformed successfully from SOAP XML to JSON. New Content-Type: {}", interfaceDO.getCode(), exchange.getIn().getHeader(Exchange.CONTENT_TYPE));

            } catch (Exception e) {
                log.error("Interface {}: Error during SOAP XML to JSON response transformation. Input XML body: '{}'. Error: {}",
                        interfaceDO.getCode(), StrUtil.truncate(originalBody, 1000), e.getMessage(), e);
                if (logBuilder != null) {
                    logBuilder.success(false);
                    logBuilder.errorMessage("Response transformation failed (XML to JSON): " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                throw new RuntimeException("Response transformation failed (XML to JSON) for interface " + interfaceDO.getCode(), e);
            }

        } else if (shouldTransformJsonToJsonata) {
            // ... (existing JSONata logic from Turn 62 - keep this)
            // For brevity, assuming that logic is here.
             if (StrUtil.isBlank(originalBody)) {
                log.warn("Interface {}: Response body is blank, skipping JSONata transformation.", interfaceDO.getCode());
                return;
            }
            String jsonataExpression = responseMapping.getMappingTemplate();
            log.info("Interface {}: Attempting JSONata response transformation. Expression: '{}'", interfaceDO.getCode(), jsonataExpression);
            try {
                JsonataLanguage jsonataLang = new JsonataLanguage();
                Expression expression = jsonataLang.createExpression(jsonataExpression);
                Object transformedBodyObj = expression.evaluate(exchange, Object.class);
                String transformedBody = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, transformedBodyObj);
                exchange.getIn().setBody(transformedBody);
                log.info("Interface {}: Response body transformed successfully using JSONata. New body: {}", interfaceDO.getCode(), transformedBody);
            } catch (Exception e) {
                 log.error("Interface {}: Error during JSONata response transformation. Expression: '{}'. Input body: '{}'. Error: {}",
                        interfaceDO.getCode(), jsonataExpression, StrUtil.truncate(originalBody, 500), e.getMessage(), e);
                if (logBuilder != null) {
                    logBuilder.success(false); 
                    logBuilder.errorMessage("Response transformation failed (JSONata): " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                throw new RuntimeException("Response transformation failed (JSONata) for interface " + interfaceDO.getCode(), e);
            }
        } else {
            log.info("Interface {}: No active response mapping rule found for current scenario (XML-to-JSON or JSONata) or conditions not met. Skipping response transformation.", interfaceDO.getCode());
        }
    }
}

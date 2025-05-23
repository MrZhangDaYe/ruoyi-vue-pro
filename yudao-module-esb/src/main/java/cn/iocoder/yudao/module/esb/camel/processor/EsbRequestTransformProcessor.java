package cn.iocoder.yudao.module.esb.camel.processor;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils; // Assuming this is available and works for String to Map
import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.dataobject.mapping.EsbMappingDO;
import cn.iocoder.yudao.module.esb.service.mapping.EsbMappingService;
import cn.iocoder.yudao.module.esb.camel.processor.logging.EsbInitialLogProcessor;
import freemarker.template.Configuration; // Import FreeMarker
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.jsonata.JsonataLanguage;
import org.apache.camel.Expression;
import cn.hutool.core.util.StrUtil;

import java.io.StringWriter;
import java.util.HashMap; // Import HashMap
import java.util.Locale;
import java.util.Map;

@Slf4j
public class EsbRequestTransformProcessor implements Processor {
    private final EsbInterfaceDO interfaceDO;
    private final EsbMappingService mappingService;
    private final Configuration freemarkerCfg; // FreeMarker Configuration

    public EsbRequestTransformProcessor(EsbInterfaceDO interfaceDO, EsbMappingService mappingService) {
        this.interfaceDO = interfaceDO;
        this.mappingService = mappingService;

        // Initialize FreeMarker Configuration once
        this.freemarkerCfg = new Configuration(Configuration.VERSION_2_3_31); // Or your desired version
        // Configure for loading templates from string, or other settings as needed
        // For security, consider using a specific ClassTemplateLoader if templates were from classpath
        // For templates from DB, StringTemplateLoader can be used but new Template(...) is simpler here.
        freemarkerCfg.setDefaultEncoding("UTF-8");
        // Crucial for allowing map keys like "user-name" to be accessed as dataModel.user_name or dataModel["user-name"]
        DefaultObjectWrapper ow = new DefaultObjectWrapper(Configuration.VERSION_2_3_31);
        ow.setUseAdaptersForContainers(true); // This is important for maps and collections
        freemarkerCfg.setObjectWrapper(ow);
    }

    @Override
    @SuppressWarnings("unchecked") // For JsonUtils.parseObject if it returns raw Map
    public void process(Exchange exchange) throws Exception {
        EsbLogBuilder logBuilder = exchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class);
        String originalBody = exchange.getIn().getBody(String.class);

        EsbMappingDO requestMapping = mappingService.getMappingByInterfaceIdAndType(interfaceDO.getId(), "REQUEST");

        boolean shouldTransformAsJsonToJsonata = requestMapping != null &&
                Integer.valueOf(1).equals(requestMapping.getStatus()) &&
                StrUtil.isNotBlank(requestMapping.getMappingTemplate()) &&
                requestMapping.getContentType() != null &&
                requestMapping.getContentType().toLowerCase(Locale.ROOT).contains("application/json") &&
                !"SOAP".equalsIgnoreCase(interfaceDO.getForwardProtocol()); // Only JSONata for non-SOAP forward

        boolean shouldTransformAsJsonToSoap = requestMapping != null &&
                Integer.valueOf(1).equals(requestMapping.getStatus()) &&
                StrUtil.isNotBlank(requestMapping.getMappingTemplate()) &&
                requestMapping.getContentType() != null &&
                requestMapping.getContentType().toLowerCase(Locale.ROOT).contains("application/json") &&
                "SOAP".equalsIgnoreCase(interfaceDO.getForwardProtocol());

        if (shouldTransformAsJsonToJsonata) {
            if (StrUtil.isBlank(originalBody)) {
                log.warn("Interface {}: Request body is blank, skipping JSONata transformation.", interfaceDO.getCode());
                return;
            }
            String jsonataExpression = requestMapping.getMappingTemplate();
            log.info("Interface {}: Attempting JSONata request transformation. Expression: '{}'", interfaceDO.getCode(), jsonataExpression);
            try {
                JsonataLanguage jsonataLang = new JsonataLanguage();
                Expression expression = jsonataLang.createExpression(jsonataExpression);
                Object transformedBodyObj = expression.evaluate(exchange, Object.class);
                String transformedBody = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, transformedBodyObj);
                exchange.getIn().setBody(transformedBody);
                log.info("Interface {}: Request body transformed successfully using JSONata. New body: {}", interfaceDO.getCode(), transformedBody);
            } catch (Exception e) {
                log.error("Interface {}: Error during JSONata request transformation. Expression: '{}'. Input body: '{}'. Error: {}",
                        interfaceDO.getCode(), jsonataExpression, StrUtil.truncate(originalBody, 500), e.getMessage(), e);
                if (logBuilder != null) {
                    logBuilder.success(false);
                    logBuilder.errorMessage("Request transformation failed (JSONata): " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                throw new RuntimeException("Request transformation failed (JSONata) for interface " + interfaceDO.getCode(), e);
            }

        } else if (shouldTransformAsJsonToSoap) {
            if (StrUtil.isBlank(originalBody)) {
                log.warn("Interface {}: Request body is blank, skipping JSON to SOAP transformation.", interfaceDO.getCode());
                return;
            }
            String freemarkerTemplateContent = requestMapping.getMappingTemplate();
            log.info("Interface {}: Attempting JSON to SOAP (FreeMarker) request transformation.", interfaceDO.getCode());

            try {
                // 1. Parse JSON string to Map (data model for FreeMarker)
                Map<String, Object> dataModel;
                try {
                    dataModel = JsonUtils.parseObject(originalBody, Map.class);
                    if (dataModel == null) { // parseObject might return null for "null" string literal
                        dataModel = new HashMap<>(); // Provide empty map instead of null
                    }
                } catch (Exception parseEx) {
                    log.error("Interface {}: Failed to parse JSON request body for FreeMarker transformation. Body: '{}'. Error: {}",
                            interfaceDO.getCode(), StrUtil.truncate(originalBody, 500), parseEx.getMessage(), parseEx);
                    if (logBuilder != null) {
                        logBuilder.success(false);
                        logBuilder.errorMessage("Request transformation failed: Invalid JSON input. " + parseEx.getMessage());
                    }
                    throw new RuntimeException("Request transformation failed: Invalid JSON input for interface " + interfaceDO.getCode(), parseEx);
                }


                // 2. Process FreeMarker template
                StringWriter out = new StringWriter();
                Template template = new Template("jsonToSoapRequest_" + interfaceDO.getCode(), freemarkerTemplateContent, freemarkerCfg);
                template.process(dataModel, out);
                String soapXmlBody = out.toString();

                exchange.getIn().setBody(soapXmlBody);
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/xml; charset=utf-8"); // Or application/soap+xml
                log.info("Interface {}: Request body transformed successfully from JSON to SOAP XML. New Content-Type: {}", interfaceDO.getCode(), exchange.getIn().getHeader(Exchange.CONTENT_TYPE));

            } catch (Exception e) {
                log.error("Interface {}: Error during JSON to SOAP (FreeMarker) request transformation. Template snippet: '{}'. Error: {}",
                        interfaceDO.getCode(), StrUtil.truncate(freemarkerTemplateContent, 100), e.getMessage(), e);
                if (logBuilder != null) {
                    logBuilder.success(false);
                    logBuilder.errorMessage("Request transformation failed (FreeMarker to SOAP): " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                throw new RuntimeException("Request transformation failed (FreeMarker to SOAP) for interface " + interfaceDO.getCode(), e);
            }
        } else {
            log.info("Interface {}: No active request mapping rule found for current scenario (JSONata or JSON-to-SOAP) or conditions not met. Skipping request transformation.", interfaceDO.getCode());
        }
    }
}

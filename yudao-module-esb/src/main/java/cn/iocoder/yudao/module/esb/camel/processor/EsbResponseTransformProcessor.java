package cn.iocoder.yudao.module.esb.camel.processor;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.dataobject.mapping.EsbMappingDO;
import cn.iocoder.yudao.module.esb.service.mapping.EsbMappingService;
import cn.iocoder.yudao.module.esb.camel.processor.logging.EsbInitialLogProcessor; // For constant
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.jsonata.JsonataLanguage;
import org.apache.camel.Expression;
import cn.hutool.core.util.StrUtil;

import java.util.Locale;

@Slf4j
public class EsbResponseTransformProcessor implements Processor {
    private final EsbInterfaceDO interfaceDO;
    private final EsbMappingService mappingService;

    public EsbResponseTransformProcessor(EsbInterfaceDO interfaceDO, EsbMappingService mappingService) {
        this.interfaceDO = interfaceDO;
        this.mappingService = mappingService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        EsbLogBuilder logBuilder = exchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class);

        // 1. Get the mapping rule for RESPONSE
        EsbMappingDO responseMapping = mappingService.getMappingByInterfaceIdAndType(interfaceDO.getId(), "RESPONSE");

        // 2. Check if transformation should be applied
        if (responseMapping != null &&
            Integer.valueOf(1).equals(responseMapping.getStatus()) && // Assuming 1 is enabled status
            StrUtil.isNotBlank(responseMapping.getMappingTemplate()) &&
            responseMapping.getContentType() != null &&
            responseMapping.getContentType().toLowerCase(Locale.ROOT).contains("application/json")) {

            // The body here is the response from the forwarded service (or an intermediate response)
            String originalBody = exchange.getIn().getBody(String.class);
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
                // exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json"); // Assuming output is JSON
                log.info("Interface {}: Response body transformed successfully using JSONata. New body: {}", interfaceDO.getCode(), transformedBody);

            } catch (Exception e) {
                log.error("Interface {}: Error during JSONata response transformation. Expression: '{}'. Input body: '{}'. Error: {}",
                        interfaceDO.getCode(), jsonataExpression, StrUtil.truncate(originalBody, 500), e.getMessage(), e);
                if (logBuilder != null) {
                    // If a transformation error occurs, it usually means the overall process is not successful.
                    // However, the 'success' flag in logBuilder might have been set by EsbPostForwardLogProcessor
                    // based on HTTP status. This transformation failure should override it.
                    logBuilder.success(false); 
                    logBuilder.errorMessage("Response transformation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                // Rethrow to be handled by Camel's error handler
                throw new RuntimeException("Response transformation failed for interface " + interfaceDO.getCode(), e);
            }
        } else {
            log.info("Interface {}: No active JSON response mapping rule found or conditions not met. Skipping response transformation.", interfaceDO.getCode());
        }
    }
}

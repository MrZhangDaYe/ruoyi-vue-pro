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
import org.apache.camel.Expression; // Import Expression
import cn.hutool.core.util.StrUtil; // Import StrUtil

import java.util.Locale; // For toLowerCase

@Slf4j
public class EsbRequestTransformProcessor implements Processor {
    private final EsbInterfaceDO interfaceDO;
    private final EsbMappingService mappingService;

    public EsbRequestTransformProcessor(EsbInterfaceDO interfaceDO, EsbMappingService mappingService) {
        this.interfaceDO = interfaceDO;
        this.mappingService = mappingService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        EsbLogBuilder logBuilder = exchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class);

        // 1. Get the mapping rule for REQUEST
        EsbMappingDO requestMapping = mappingService.getMappingByInterfaceIdAndType(interfaceDO.getId(), "REQUEST");

        // 2. Check if transformation should be applied
        if (requestMapping != null &&
            Integer.valueOf(1).equals(requestMapping.getStatus()) && // Assuming 1 is enabled status
            StrUtil.isNotBlank(requestMapping.getMappingTemplate()) &&
            requestMapping.getContentType() != null &&
            requestMapping.getContentType().toLowerCase(Locale.ROOT).contains("application/json")) {

            String originalBody = exchange.getIn().getBody(String.class);
            if (StrUtil.isBlank(originalBody)) {
                log.warn("Interface {}: Request body is blank, skipping JSONata transformation.", interfaceDO.getCode());
                return;
            }

            String jsonataExpression = requestMapping.getMappingTemplate();
            log.info("Interface {}: Attempting JSONata request transformation. Expression: '{}'", interfaceDO.getCode(), jsonataExpression);

            try {
                JsonataLanguage jsonataLang = new JsonataLanguage();
                // Configure the language instance if needed (e.g. to allow java calls etc - but keep it simple for now)
                Expression expression = jsonataLang.createExpression(jsonataExpression);
                
                // Evaluate the expression against the exchange (body is implicitly used by JsonataLanguage if not other source specified)
                Object transformedBodyObj = expression.evaluate(exchange, Object.class);
                
                // Convert the transformed object (often a Map or List for JSON output) to String
                String transformedBody = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, transformedBodyObj);

                exchange.getIn().setBody(transformedBody);
                // Assuming the output of JSONata is JSON, ensure Content-Type is set if not already correct.
                // exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
                log.info("Interface {}: Request body transformed successfully using JSONata. New body: {}", interfaceDO.getCode(), transformedBody);

            } catch (Exception e) {
                log.error("Interface {}: Error during JSONata request transformation. Expression: '{}'. Input body: '{}'. Error: {}",
                        interfaceDO.getCode(), jsonataExpression, StrUtil.truncate(originalBody, 500), e.getMessage(), e);
                if (logBuilder != null) {
                    logBuilder.success(false);
                    // Using a more specific error message for transformation failure
                    logBuilder.errorMessage("Request transformation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                // Rethrow the exception to be handled by Camel's error handler (e.g., deadLetterChannel)
                // This ensures the main route processing stops or is redirected as per error handling strategy.
                throw new RuntimeException("Request transformation failed for interface " + interfaceDO.getCode(), e);
            }
        } else {
            log.info("Interface {}: No active JSON request mapping rule found or conditions not met. Skipping request transformation.", interfaceDO.getCode());
        }
    }
}

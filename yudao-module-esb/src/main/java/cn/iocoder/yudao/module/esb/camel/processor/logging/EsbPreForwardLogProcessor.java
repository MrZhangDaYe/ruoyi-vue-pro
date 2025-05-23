package cn.iocoder.yudao.module.esb.camel.processor.logging;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO; // Added for context in logging
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class EsbPreForwardLogProcessor implements Processor {

    public EsbPreForwardLogProcessor() {
        // Constructor
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        EsbLogBuilder logBuilder = exchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class);
        EsbInterfaceDO interfaceDO = exchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class); // For logging context

        String interfaceCode = (interfaceDO != null) ? interfaceDO.getCode() : "UnknownInterface";

        if (logBuilder == null) {
            log.error("EsbLogBuilder not found in exchange properties for interface code: {}. Cannot record pre-forward log details.", interfaceCode);
            // If logBuilder is null, it's a critical issue from previous steps.
            // We might want to create a new one here to capture at least the error,
            // but for now, we'll assume it must be present.
            return;
        }

        // Record forward request time
        logBuilder.forwardRequestTime(LocalDateTime.now());

        // Record headers that will be sent to the target
        Map<String, Object> forwardRequestHeaders = exchange.getIn().getHeaders();
        logBuilder.forwardRequestHeaders(forwardRequestHeaders);

        // Record body that will be sent to the target
        String forwardRequestBody = exchange.getIn().getBody(String.class);
        logBuilder.forwardRequestBody(forwardRequestBody);

        log.debug("EsbPreForwardLogProcessor: Recorded forwarding request details for interface code: {}", interfaceCode);
    }
}

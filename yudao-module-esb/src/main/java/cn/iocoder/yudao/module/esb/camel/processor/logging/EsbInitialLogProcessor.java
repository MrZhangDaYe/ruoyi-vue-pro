package cn.iocoder.yudao.module.esb.camel.processor.logging;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component; // Import Component for potential Spring management if needed, though processors are often new'd up in routes

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
// @Component // Typically processors used in dynamic routes are instantiated directly via new() in the RouteBuilder.
// If this processor were to be used as a Spring bean lookup in a route, then @Component would be needed.
// For our current dynamic route building approach (new EsbInitialLogProcessor()), @Component is not strictly necessary.
public class EsbInitialLogProcessor implements Processor {

    public static final String ESB_LOG_BUILDER_PROPERTY = "esbLogBuilder";

    public EsbInitialLogProcessor() {
        // Constructor can be empty if no external services like EsbLogService are directly needed here.
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        EsbInterfaceDO interfaceDO = exchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class);

        if (interfaceDO == null) {
            log.error("EsbInterfaceDO not found in exchange properties. Cannot initialize ESB log.");
            // Potentially throw an exception or handle this scenario as an error in the route
            return;
        }

        EsbLogBuilder logBuilder = new EsbLogBuilder(interfaceDO.getId(), interfaceDO.getCode());

        // Record request time
        logBuilder.requestTime(LocalDateTime.now());

        // Record request headers
        Map<String, Object> requestHeaders = exchange.getIn().getHeaders();
        logBuilder.requestHeaders(requestHeaders); // EsbLogBuilder handles Map -> String conversion

        // Record request body
        // Ensure stream caching is enabled on the route if the body needs to be read multiple times.
        // For platform-http, it's often buffered and can be re-read.
        String requestBody = exchange.getIn().getBody(String.class);
        logBuilder.requestBody(requestBody);

        exchange.setProperty(ESB_LOG_BUILDER_PROPERTY, logBuilder);

        log.debug("EsbInitialLogProcessor: Initialized log for interface code: {}, request time: {}",
                interfaceDO.getCode(), logBuilder.build().getRequestTime());
    }
}

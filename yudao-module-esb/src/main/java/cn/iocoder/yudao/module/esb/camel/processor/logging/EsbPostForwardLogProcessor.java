package cn.iocoder.yudao.module.esb.camel.processor.logging;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.time.Duration; // Import Duration
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class EsbPostForwardLogProcessor implements Processor {

    // Exchange property to store the duration of the forward call specifically
    public static final String FORWARD_DURATION_PROPERTY = "esbForwardDurationMillis";

    public EsbPostForwardLogProcessor() {
        // Constructor
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        EsbLogBuilder logBuilder = exchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class);
        EsbInterfaceDO interfaceDO = exchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class);
        String interfaceCode = (interfaceDO != null) ? interfaceDO.getCode() : "UnknownInterface";

        if (logBuilder == null) {
            log.error("EsbLogBuilder not found in exchange properties for interface code: {}. Cannot record post-forward log details.", interfaceCode);
            return;
        }

        LocalDateTime forwardResponseTime = LocalDateTime.now();
        logBuilder.forwardResponseTime(forwardResponseTime);

        // Record headers received from the target
        // Note: Depending on the Camel version and components, response headers might be in exchange.getIn().getHeaders()
        // or exchange.getMessage().getHeaders() if using .getMessage() explicitly. getIn() is usually correct.
        Map<String, Object> forwardResponseHeaders = exchange.getIn().getHeaders();
        logBuilder.forwardResponseHeaders(forwardResponseHeaders);

        // Record body received from the target
        String forwardResponseBody = exchange.getIn().getBody(String.class);
        logBuilder.forwardResponseBody(forwardResponseBody);

        // Calculate and store duration of the forward call specifically
        LocalDateTime forwardRequestTime = logBuilder.build().getForwardRequestTime(); // .build() to get a snapshot of current logVO
        if (forwardRequestTime != null) {
            long durationMillis = Duration.between(forwardRequestTime, forwardResponseTime).toMillis();
            exchange.setProperty(FORWARD_DURATION_PROPERTY, durationMillis); // Store for potential reference
            log.debug("Forward call for interface {} took {} ms", interfaceCode, durationMillis);
            // We will set the *overall* duration in EsbFinalLogProcessor using the initial request time.
        }

        // Preliminary success/failure determination based on HTTP status code
        // This might be overridden later by successFlagJsonPath logic or overall route status
        Integer httpStatusCode = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        boolean currentStepSuccess = httpStatusCode != null && httpStatusCode >= 200 && httpStatusCode < 300;
        
        logBuilder.success(currentStepSuccess); // Set success based on this step for now. EsbFinalLogProcessor will make the final call.

        if (!currentStepSuccess) {
            String errorMessage = "Forwarding target responded with HTTP status: " + httpStatusCode;
            // Check if there's an exception on the exchange, which might provide more details
            Exception ex = exchange.getException();
            if (ex != null) {
                errorMessage += ". Exception: " + ex.getMessage();
            } else if (forwardResponseBody != null && forwardResponseBody.length() < 500) { // Append response body if short
                 errorMessage += ". Response Body: " + forwardResponseBody;
            }
            logBuilder.errorMessage(errorMessage); // This might be overridden by a route-level exception later
            log.warn("Forwarding for interface {} failed with HTTP status: {}. Body: {}", interfaceCode, httpStatusCode, forwardResponseBody);
        } else {
             // Clear any previous error message if this step was successful but a prior one (like transformation) failed
             // However, this logic is tricky. Let's assume error messages accumulate or are set by exception handlers.
             // For now, if HTTP call is 2xx, we don't set error message here from HTTP status.
        }
        
        log.debug("EsbPostForwardLogProcessor: Recorded forwarded response details for interface code: {}", interfaceCode);
    }
}

package cn.iocoder.yudao.module.esb.camel.processor.logging;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.service.log.EsbLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.exception.ExceptionUtils; // For stack trace

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class EsbFinalLogProcessor implements Processor {

    private final EsbLogService esbLogService;

    public EsbFinalLogProcessor(EsbLogService esbLogService) {
        this.esbLogService = esbLogService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        EsbLogBuilder logBuilder = exchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class);
        EsbInterfaceDO interfaceDO = exchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class);
        String interfaceCode = (interfaceDO != null) ? interfaceDO.getCode() : "UnknownInterface";
        Long interfaceId = (interfaceDO != null) ? interfaceDO.getId() : null;


        if (logBuilder == null) {
            // If logBuilder was never initialized, create a minimal one for error logging if possible
            if (interfaceDO != null) {
                logBuilder = new EsbLogBuilder(interfaceId, interfaceCode);
                logBuilder.requestTime(LocalDateTime.now()); // Approximate request time
                log.warn("EsbLogBuilder was null for interface {}. Created a new minimal log for error reporting.", interfaceCode);
            } else {
                log.error("EsbLogBuilder and EsbInterfaceDO are both null in EsbFinalLogProcessor. Cannot save any log.");
                // This is a critical failure in the logging chain.
                return;
            }
        }

        // Set response time if not already set (e.g., if route failed before external call)
        if (logBuilder.build().getResponseTime() == null) {
            logBuilder.responseTime(LocalDateTime.now());
        }
        
        // Calculate total duration
        LocalDateTime initialRequestTime = logBuilder.build().getRequestTime();
        if (initialRequestTime != null) {
            long totalDurationMillis = Duration.between(initialRequestTime, logBuilder.build().getResponseTime()).toMillis();
            logBuilder.durationMilliseconds((int) totalDurationMillis);
        }

        // Finalize success status and error message based on exchange exception
        Exception exception = exchange.getException();
        if (exception != null) {
            logBuilder.success(false);
            logBuilder.errorMessage(buildErrorMessageFromException(exception));
            // Log the exception with stack trace for application logs
            log.error("Exception in ESB route for interface {}: ", interfaceCode, exception);
        } else {
            // If no exception, the success status should have been set by EsbPostForwardLogProcessor
            // or defaults to true if not set (e.g. if no forward happened but also no exception)
            if (logBuilder.build().getSuccess() == null) {
                logBuilder.success(true); // Default to success if no exception and not set otherwise
            }
        }

        try {
            esbLogService.createLog(logBuilder.build());
            log.debug("EsbFinalLogProcessor: Successfully saved ESB log for interface code: {}", interfaceCode);
        } catch (Exception e) {
            log.error("Failed to save ESB log for interface code: {}. LogData: {}. Error: {}",
                    interfaceCode, logBuilder.build().toString(), e.getMessage(), e);
        }
    }

    private String buildErrorMessageFromException(Exception ex) {
        if (ex == null) {
            return null;
        }
        String message = "Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage();
        // Optionally add a short stack trace, e.g., ExceptionUtils.getStackTrace(ex)
        // Be mindful of log field size.
        String stackTrace = ExceptionUtils.getStackTrace(ex);
        if (stackTrace.length() > 1500) { // Keep it somewhat brief for the DB log message
            stackTrace = stackTrace.substring(0, 1500) + "... (truncated)";
        }
        message += "\nStackTrace (summary): " + stackTrace;
        return message;
    }
}

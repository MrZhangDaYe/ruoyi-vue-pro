package cn.iocoder.yudao.module.esb.camel.util;

// Assuming JsonUtils is NOT available for map to json string, will use a simple converter.
// import cn.iocoder.yudao.framework.common.util.json.JsonUtils; 
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogCreateReqVO;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public class EsbLogBuilder {

    private final EsbLogCreateReqVO logVO;

    public EsbLogBuilder(Long interfaceId, String interfaceCode) {
        this.logVO = new EsbLogCreateReqVO();
        this.logVO.setInterfaceId(interfaceId);
        this.logVO.setInterfaceCode(interfaceCode);
    }

    public EsbLogBuilder requestTime(LocalDateTime requestTime) {
        this.logVO.setRequestTime(requestTime);
        return this;
    }

    public EsbLogBuilder requestHeaders(Map<String, Object> headersMap) {
        this.logVO.setRequestHeaders(convertHeadersToString(headersMap));
        return this;
    }
    
    public EsbLogBuilder requestHeaders(String headersString) {
        this.logVO.setRequestHeaders(headersString);
        return this;
    }

    public EsbLogBuilder requestBody(String requestBody) {
        this.logVO.setRequestBody(requestBody);
        return this;
    }

    public EsbLogBuilder responseTime(LocalDateTime responseTime) {
        this.logVO.setResponseTime(responseTime);
        return this;
    }

    public EsbLogBuilder responseHeaders(Map<String, Object> headersMap) {
        this.logVO.setResponseHeaders(convertHeadersToString(headersMap));
        return this;
    }

    public EsbLogBuilder responseHeaders(String headersString) {
        this.logVO.setResponseHeaders(headersString);
        return this;
    }
    
    public EsbLogBuilder responseBody(String responseBody) {
        this.logVO.setResponseBody(responseBody);
        return this;
    }

    public EsbLogBuilder forwardRequestTime(LocalDateTime forwardRequestTime) {
        this.logVO.setForwardRequestTime(forwardRequestTime);
        return this;
    }

    public EsbLogBuilder forwardRequestHeaders(Map<String, Object> headersMap) {
        this.logVO.setForwardRequestHeaders(convertHeadersToString(headersMap));
        return this;
    }
    
    public EsbLogBuilder forwardRequestHeaders(String headersString) {
        this.logVO.setForwardRequestHeaders(headersString);
        return this;
    }

    public EsbLogBuilder forwardRequestBody(String forwardRequestBody) {
        this.logVO.setForwardRequestBody(forwardRequestBody);
        return this;
    }

    public EsbLogBuilder forwardResponseTime(LocalDateTime forwardResponseTime) {
        this.logVO.setForwardResponseTime(forwardResponseTime);
        return this;
    }

    public EsbLogBuilder forwardResponseHeaders(Map<String, Object> headersMap) {
        this.logVO.setForwardResponseHeaders(convertHeadersToString(headersMap));
        return this;
    }

    public EsbLogBuilder forwardResponseHeaders(String headersString) {
        this.logVO.setForwardResponseHeaders(headersString);
        return this;
    }
    
    public EsbLogBuilder forwardResponseBody(String forwardResponseBody) {
        this.logVO.setForwardResponseBody(forwardResponseBody);
        return this;
    }

    public EsbLogBuilder success(Boolean success) {
        this.logVO.setSuccess(success);
        return this;
    }

    public EsbLogBuilder errorMessage(String errorMessage) {
        // Truncate if too long for DB field, or ensure DB field is large enough (e.g., TEXT)
        if (errorMessage != null && errorMessage.length() > 2000) { // Example truncation
            errorMessage = errorMessage.substring(0, 1990) + "... (truncated)"; // Adjusted for safety
        }
        this.logVO.setErrorMessage(errorMessage);
        return this;
    }

    public EsbLogBuilder durationMilliseconds(Integer durationMilliseconds) {
        this.logVO.setDurationMilliseconds(durationMilliseconds);
        return this;
    }

    public EsbLogCreateReqVO build() {
        return this.logVO;
    }

    private String convertHeadersToString(Map<String, Object> headersMap) {
        if (headersMap == null || headersMap.isEmpty()) {
            return null;
        }
        // Simple Map.toString() like conversion, avoiding external dependencies if not confirmed.
        // This might not be perfect JSON, but is a safe fallback.
        return headersMap.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    String valueStr;
                    if (value instanceof java.util.Collection) {
                        // Simple join for collections
                        valueStr = ((java.util.Collection<?>) value).stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                    } else {
                        valueStr = value != null ? value.toString() : "null";
                    }
                    // Basic escaping for quotes in key/value to avoid breaking a simple JSON-like structure if manually parsed
                    key = key.replace("\"", "\\\"");
                    valueStr = valueStr.replace("\"", "\\\"");
                    return "\"" + key + "\": \"" + valueStr + "\"";
                })
                .collect(Collectors.joining(", ", "{", "}"));
    }
}

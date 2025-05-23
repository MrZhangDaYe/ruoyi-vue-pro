package cn.iocoder.yudao.module.esb.camel.util;

import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogCreateReqVO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class EsbLogBuilderTest {

    @Test
    void testFullBuild() {
        Long interfaceId = 1L;
        String interfaceCode = "IF001";
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Request-ID", "12345");
        headers.put("Numbers", Arrays.asList(1,2,3));


        EsbLogBuilder builder = new EsbLogBuilder(interfaceId, interfaceCode);
        EsbLogCreateReqVO logVO = builder
            .requestTime(now.minusSeconds(10))
            .requestHeaders(headers)
            .requestBody("{\"key\":\"value\"}")
            .responseTime(now.minusSeconds(5)) // Initial response from ESB itself, if applicable
            // .responseHeaders(headers) // Let's use a different map for response
            // .responseBody("{"status":"received"}")
            .forwardRequestTime(now.minusSeconds(4))
            .forwardRequestHeaders(headers)
            .forwardRequestBody("{\"transformed_key\":\"transformed_value\"}")
            .forwardResponseTime(now.minusSeconds(1))
            .forwardResponseHeaders(headers)
            .forwardResponseBody("{\"external_status\":\"success\"}")
            .success(true)
            .errorMessage(null)
            .durationMilliseconds(9000) // Example: 10s (request) - 1s (fwd_response) = 9s
            .build();

        assertEquals(interfaceId, logVO.getInterfaceId());
        assertEquals(interfaceCode, logVO.getInterfaceCode());
        assertEquals(now.minusSeconds(10), logVO.getRequestTime());
        assertNotNull(logVO.getRequestHeaders());
        assertTrue(logVO.getRequestHeaders().contains("\"Content-Type\":\"application/json\""));
        assertTrue(logVO.getRequestHeaders().contains("\"X-Request-ID\":\"12345\""));
        assertTrue(logVO.getRequestHeaders().contains("\"Numbers\":\"1, 2, 3\"")); // Note space due to simple join
        assertEquals("{\"key\":\"value\"}", logVO.getRequestBody());
        
        assertEquals(now.minusSeconds(5), logVO.getResponseTime());
        // assertNotNull(logVO.getResponseHeaders()); // Not set in this specific chain
        // assertNull(logVO.getResponseBody()); // Not set

        assertEquals(now.minusSeconds(4), logVO.getForwardRequestTime());
        assertNotNull(logVO.getForwardRequestHeaders());
        assertEquals("{\"transformed_key\":\"transformed_value\"}", logVO.getForwardRequestBody());
        assertEquals(now.minusSeconds(1), logVO.getForwardResponseTime());
        assertNotNull(logVO.getForwardResponseHeaders());
        assertEquals("{\"external_status\":\"success\"}", logVO.getForwardResponseBody());
        assertTrue(logVO.getSuccess());
        assertNull(logVO.getErrorMessage());
        assertEquals(9000, logVO.getDurationMilliseconds());
    }

    @Test
    void testMinimalBuild() {
        EsbLogBuilder builder = new EsbLogBuilder(2L, "IF002");
        EsbLogCreateReqVO logVO = builder.build();

        assertEquals(2L, logVO.getInterfaceId());
        assertEquals("IF002", logVO.getInterfaceCode());
        assertNull(logVO.getRequestTime());
        assertNull(logVO.getRequestBody());
        // ... assert other fields are null or default
        assertNull(logVO.getResponseTime());
        assertNull(logVO.getForwardRequestTime());
        assertNull(logVO.getForwardResponseTime());
        assertNull(logVO.getSuccess());
        assertNull(logVO.getErrorMessage());
        assertNull(logVO.getDurationMilliseconds());
    }
    
    @Test
    void testHeaderConversion_EmptyAndNull() {
        EsbLogBuilder builder = new EsbLogBuilder(3L, "IF003");
        builder.requestHeaders((Map<String, Object>) null);
        assertNull(builder.build().getRequestHeaders());

        builder.requestHeaders(new HashMap<>());
        assertNull(builder.build().getRequestHeaders());
    }

    @Test
    void testHeaderConversion_StringHeaders() {
        EsbLogBuilder builder = new EsbLogBuilder(4L, "IF004");
        String preformattedHeaders = "{\"X-Custom\":\"direct\"}";
        builder.requestHeaders(preformattedHeaders);
        assertEquals(preformattedHeaders, builder.build().getRequestHeaders());
    }
    
    @Test
    void testErrorMessageTruncation() {
        EsbLogBuilder builder = new EsbLogBuilder(5L, "IF005");
        String longMessage = "a".repeat(3000);
        builder.errorMessage(longMessage);
        EsbLogCreateReqVO logVO = builder.build();
        assertNotNull(logVO.getErrorMessage());
        assertTrue(logVO.getErrorMessage().length() <= 2000); // Adjusted to check less than or equal to 2000
        assertTrue(logVO.getErrorMessage().endsWith("... (truncated)"));
    }
}

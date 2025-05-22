package cn.iocoder.yudao.module.esb.controller.admin.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ESB请求日志 Response VO")
@Data
public class EsbLogRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "接口ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long interfaceId;

    @Schema(description = "接口编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "USER_QUERY_001")
    private String interfaceCode;

    @Schema(description = "请求时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime requestTime;

    @Schema(description = "请求头", example = "{'Content-Type': 'application/json'}")
    private String requestHeaders;

    @Schema(description = "请求体", example = "{'userId': '123'}")
    private String requestBody;

    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

    @Schema(description = "响应头", example = "{'Content-Type': 'application/json'}")
    private String responseHeaders;

    @Schema(description = "响应体", example = "{'status': 'success'}")
    private String responseBody;

    @Schema(description = "转发请求时间")
    private LocalDateTime forwardRequestTime;

    @Schema(description = "转发请求头")
    private String forwardRequestHeaders;

    @Schema(description = "转发请求体")
    private String forwardRequestBody;

    @Schema(description = "转发响应时间")
    private LocalDateTime forwardResponseTime;

    @Schema(description = "转发响应头")
    private String forwardResponseHeaders;

    @Schema(description = "转发响应体")
    private String forwardResponseBody;

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "错误信息", example = "Connection timed out")
    private String errorMessage;

    @Schema(description = "总耗时 (毫秒)", example = "500")
    private Integer durationMilliseconds;

    @Schema(description = "租户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long tenantId;

    @Schema(description = "创建者", example = "admin_system") // Usually system or a specific user if applicable
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // No updater/updateTime for logs typically, as they are immutable once created.
    // If they are part of BaseDO and automatically populated, they will be present.
    // For this VO, we'll only include what's explicitly in EsbLogDO and relevant for display.
}

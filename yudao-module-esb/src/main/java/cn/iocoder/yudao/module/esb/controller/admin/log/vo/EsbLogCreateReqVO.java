package cn.iocoder.yudao.module.esb.controller.admin.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ESB请求日志创建 Request VO")
@Data
public class EsbLogCreateReqVO {

    @Schema(description = "接口ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "接口ID不能为空")
    private Long interfaceId;

    @Schema(description = "接口编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "USER_QUERY_001")
    @NotEmpty(message = "接口编码不能为空")
    private String interfaceCode;

    @Schema(description = "请求时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请求时间不能为空")
    private LocalDateTime requestTime;

    @Schema(description = "请求头")
    private String requestHeaders;

    @Schema(description = "请求体")
    private String requestBody;

    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

    @Schema(description = "响应头")
    private String responseHeaders;

    @Schema(description = "响应体")
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

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "总耗时 (毫秒)")
    private Integer durationMilliseconds;

    // tenant_id will be handled by TenantContextHolder and TenantBaseDO
    // creator, create_time, etc. will be handled by BaseDO
}

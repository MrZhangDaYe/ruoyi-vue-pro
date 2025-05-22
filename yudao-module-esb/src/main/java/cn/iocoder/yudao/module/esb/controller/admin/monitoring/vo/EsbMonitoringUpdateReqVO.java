package cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ESB接口监控更新 Request VO (used by monitoring mechanism)")
@Data
public class EsbMonitoringUpdateReqVO {

    @Schema(description = "接口ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    @NotNull(message = "接口ID不能为空")
    private Long interfaceId;

    @Schema(description = "上次检查时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "上次检查时间不能为空")
    private LocalDateTime lastCheckTime;

    @Schema(description = "是否可用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否可用不能为空")
    private Boolean isAvailable;

    @Schema(description = "HTTP状态码或其他状态标识", example = "200")
    private Integer statusCode;

    @Schema(description = "错误信息 (如果不可用)", example = "Connection refused")
    private String errorMessage;

    @Schema(description = "响应时间 (毫秒)", example = "120")
    private Integer responseTimeMilliseconds;
}

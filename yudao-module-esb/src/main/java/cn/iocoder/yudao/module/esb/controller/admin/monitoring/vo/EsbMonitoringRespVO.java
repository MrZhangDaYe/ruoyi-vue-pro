package cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ESB接口监控 Response VO")
@Data
public class EsbMonitoringRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "接口ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long interfaceId;

    @Schema(description = "接口名称", example = "用户查询接口")
    private String interfaceName;

    @Schema(description = "接口编码", example = "USER_QUERY_001")
    private String interfaceCode;

    @Schema(description = "上次检查时间")
    private LocalDateTime lastCheckTime;

    @Schema(description = "是否可用", example = "true")
    private Boolean isAvailable;

    @Schema(description = "HTTP状态码或其他状态标识", example = "200")
    private Integer statusCode;

    @Schema(description = "错误信息 (如果不可用)", example = "Timeout")
    private String errorMessage;

    @Schema(description = "响应时间 (毫秒)", example = "150")
    private Integer responseTimeMilliseconds;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

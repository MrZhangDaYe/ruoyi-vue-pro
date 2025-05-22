package cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ESB接口监控分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbMonitoringPageReqVO extends PageParam {

    @Schema(description = "接口ID列表 (用于关联查询)", hidden = true) // This will be populated by the service layer, not directly by user
    private java.util.Collection<Long> interfaceIds;

    @Schema(description = "是否可用", example = "true")
    private Boolean isAvailable;

    // Add other potential filter fields if needed, e.g., based on interface name/code from a JOIN,
    // but the mapper method provided only uses interfaceIds and isAvailable.
    // For now, keeping it simple as per the mapper's requirement.
}

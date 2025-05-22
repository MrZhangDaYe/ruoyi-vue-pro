package cn.iocoder.yudao.module.esb.controller.admin.mapping.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ESB参数映射分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbMappingPageReqVO extends PageParam {

    @Schema(description = "接口ID", example = "101")
    private Long interfaceId;

    @Schema(description = "映射类型 (REQUEST, RESPONSE)", example = "REQUEST")
    private String type;

    @Schema(description = "状态 (0: 禁用, 1: 启用)", example = "1")
    private Integer status;

}

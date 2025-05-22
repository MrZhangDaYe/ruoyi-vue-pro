package cn.iocoder.yudao.module.esb.controller.admin.mapping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class EsbMappingBaseVO {

    @Schema(description = "接口ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    @NotNull(message = "接口ID不能为空")
    private Long interfaceId;

    @Schema(description = "映射类型 (REQUEST, RESPONSE)", requiredMode = Schema.RequiredMode.REQUIRED, example = "REQUEST")
    @NotEmpty(message = "映射类型不能为空")
    @Size(max = 50, message = "映射类型长度不能超过50个字符")
    // TODO: Consider an Enum for validation (e.g., REQUEST, RESPONSE)
    private String type;

    @Schema(description = "内容类型, e.g., application/json, application/xml", example = "application/json")
    @Size(max = 100, message = "内容类型长度不能超过100个字符")
    private String contentType;

    @Schema(description = "参数转换模板", example = "<user><name>{{name}}</name></user>")
    private String mappingTemplate; // Can be large, so no size limit for now

    @Schema(description = "状态 (0: 禁用, 1: 启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    // TODO: Consider an Enum (e.g., CommonStatusEnum)
    private Integer status;

    @Schema(description = "备注", example = "请求参数映射")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

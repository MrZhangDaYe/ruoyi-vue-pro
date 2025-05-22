package cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Data
public class EsbInterfaceBaseVO {

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户查询接口")
    @NotEmpty(message = "接口名称不能为空")
    @Size(max = 255, message = "接口名称长度不能超过255个字符")
    private String name;

    @Schema(description = "接口编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "USER_QUERY_001")
    @NotEmpty(message = "接口编码不能为空")
    @Size(max = 255, message = "接口编码长度不能超过255个字符")
    private String code;

    @Schema(description = "接口路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "/esb/user/query")
    @NotEmpty(message = "接口路径不能为空")
    @Size(max = 255, message = "接口路径长度不能超过255个字符")
    // Consider adding a custom validator for path format if needed
    private String path;

    @Schema(description = "接口协议, e.g., HTTP, HTTPS, MLLP", requiredMode = Schema.RequiredMode.REQUIRED, example = "HTTP")
    @NotEmpty(message = "接口协议不能为空")
    @Size(max = 50, message = "接口协议长度不能超过50个字符")
    private String protocol;

    @Schema(description = "转发接口名称", example = "HIS用户查询接口")
    @Size(max = 255, message = "转发接口名称长度不能超过255个字符")
    private String forwardInterfaceName;

    @Schema(description = "转发接口地址", example = "http://his.internal/api/user/info")
    @URL(message = "转发接口地址必须是有效的URL")
    @Size(max = 1000, message = "转发接口地址长度不能超过1000个字符")
    private String forwardInterfaceAddress;

    @Schema(description = "转发接口协议", example = "HTTP")
    @Size(max = 50, message = "转发接口协议长度不能超过50个字符")
    private String forwardProtocol;

    @Schema(description = "重试次数", example = "3")
    @PositiveOrZero(message = "重试次数必须大于等于0")
    private Integer retryCount;

    @Schema(description = "超时时间 (毫秒)", example = "60000")
    @PositiveOrZero(message = "超时时间必须大于等于0")
    private Integer timeoutMilliseconds;

    @Schema(description = "同步或异步 (true: 同步, false: 异步)", example = "true")
    @NotNull(message = "同步异步标识不能为空")
    private Boolean isSynchronous;

    @Schema(description = "接口成功标识 (JSONPath expression to check in response)", example = "$.success")
    @Size(max = 255, message = "接口成功标识长度不能超过255个字符")
    private String successFlagJsonPath;

    @Schema(description = "状态 (0: 禁用, 1: 启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    // Consider adding a custom validator for enum values (e.g., 0 or 1)
    private Integer status;

    @Schema(description = "备注", example = "这是一个用于查询用户信息的ESB接口")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

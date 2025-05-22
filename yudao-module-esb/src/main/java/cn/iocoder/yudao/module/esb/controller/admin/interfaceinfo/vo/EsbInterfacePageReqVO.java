package cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ESB接口分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbInterfacePageReqVO extends PageParam {

    @Schema(description = "接口名称", example = "用户查询接口")
    private String name;

    @Schema(description = "接口编码", example = "USER_QUERY_001")
    private String code;

    @Schema(description = "接口路径", example = "/esb/user/query")
    private String path;

    @Schema(description = "接口协议", example = "HTTP")
    private String protocol;

    @Schema(description = "状态 (0: 禁用, 1: 启用)", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}

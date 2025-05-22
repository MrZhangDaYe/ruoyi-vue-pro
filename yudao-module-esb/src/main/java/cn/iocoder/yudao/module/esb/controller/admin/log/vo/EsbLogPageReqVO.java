package cn.iocoder.yudao.module.esb.controller.admin.log.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ESB请求日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbLogPageReqVO extends PageParam {

    @Schema(description = "接口ID", example = "101")
    private Long interfaceId;

    @Schema(description = "接口编码", example = "USER_QUERY_001")
    private String interfaceCode;

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "请求时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] requestTime;

}

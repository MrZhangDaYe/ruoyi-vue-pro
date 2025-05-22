package cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ESB接口创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbInterfaceCreateReqVO extends EsbInterfaceBaseVO {

}

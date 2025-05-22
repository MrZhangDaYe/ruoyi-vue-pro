package cn.iocoder.yudao.module.esb.dal.dataobject.mapping;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * ESB参数映射表 DO
 *
 * @author 芋道源码
 */
@TableName("esb_mapping")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbMappingDO extends TenantBaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接口ID
     * Foreign key to esb_interface.id
     */
    @TableField("interface_id")
    private Long interfaceId;

    /**
     * 映射类型 (REQUEST, RESPONSE)
     * TODO: Consider an Enum
     */
    private String type;

    /**
     * 内容类型, e.g., application/json, application/xml
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 参数转换模板 (e.g., FreeMarker, Velocity, XSLT)
     */
    @TableField("mapping_template")
    private String mappingTemplate;

    /**
     * 状态 (0: 禁用, 1: 启用)
     * TODO: Consider an Enum (e.g., CommonStatusEnum)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}

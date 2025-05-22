package cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * ESB接口配置表 DO
 *
 * @author 芋道源码
 */
@TableName("esb_interface")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbInterfaceDO extends TenantBaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口编码
     */
    private String code;

    /**
     * 接口路径
     */
    private String path;

    /**
     * 接口协议, e.g., HTTP, HTTPS, MLLP
     */
    private String protocol;

    /**
     * Camel路由ID
     */
    @TableField("camel_route_id")
    private String camelRouteId;

    /**
     * 转发接口名称
     */
    @TableField("forward_interface_name")
    private String forwardInterfaceName;

    /**
     * 转发接口地址
     */
    @TableField("forward_interface_address")
    private String forwardInterfaceAddress;

    /**
     * 转发接口协议
     */
    @TableField("forward_protocol")
    private String forwardProtocol;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 超时时间 (毫秒)
     */
    @TableField("timeout_milliseconds")
    private Integer timeoutMilliseconds;

    /**
     * 同步或异步 (true: 同步, false: 异步)
     */
    @TableField("is_synchronous")
    private Boolean isSynchronous;

    /**
     * 接口成功标识 (JSONPath expression to check in response)
     */
    @TableField("success_flag_json_path")
    private String successFlagJsonPath;

    /**
     * 状态 (0: 禁用, 1: 启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}

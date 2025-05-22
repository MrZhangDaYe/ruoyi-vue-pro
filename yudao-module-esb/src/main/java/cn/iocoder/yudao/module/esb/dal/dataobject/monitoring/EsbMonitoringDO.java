package cn.iocoder.yudao.module.esb.dal.dataobject.monitoring;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * ESB接口监控表 DO
 *
 * @author 芋道源码
 */
@TableName("esb_monitoring")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbMonitoringDO extends TenantBaseDO {

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
     * 上次检查时间
     */
    @TableField("last_check_time")
    private LocalDateTime lastCheckTime;

    /**
     * 是否可用
     */
    @TableField("is_available")
    private Boolean isAvailable;

    /**
     * HTTP状态码或其他状态标识
     */
    @TableField("status_code")
    private Integer statusCode;

    /**
     * 错误信息 (如果不可用)
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 响应时间 (毫秒)
     */
    @TableField("response_time_milliseconds")
    private Integer responseTimeMilliseconds;

    // tenant_id is inherited from TenantBaseDO
    // creator, create_time, updater, update_time, deleted are inherited from BaseDO (via TenantBaseDO)
}

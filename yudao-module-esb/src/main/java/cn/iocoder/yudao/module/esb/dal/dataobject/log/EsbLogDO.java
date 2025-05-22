package cn.iocoder.yudao.module.esb.dal.dataobject.log;

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
 * ESB请求日志表 DO
 *
 * @author 芋道源码
 */
@TableName("esb_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbLogDO extends TenantBaseDO {

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
     * 接口编码
     */
    @TableField("interface_code")
    private String interfaceCode;

    /**
     * 请求时间
     */
    @TableField("request_time")
    private LocalDateTime requestTime;

    /**
     * 请求头
     */
    @TableField("request_headers")
    private String requestHeaders;

    /**
     * 请求体
     */
    @TableField("request_body")
    private String requestBody;

    /**
     * 响应时间
     */
    @TableField("response_time")
    private LocalDateTime responseTime;

    /**
     * 响应头
     */
    @TableField("response_headers")
    private String responseHeaders;

    /**
     * 响应体
     */
    @TableField("response_body")
    private String responseBody;

    /**
     * 转发请求时间
     */
    @TableField("forward_request_time")
    private LocalDateTime forwardRequestTime;

    /**
     * 转发请求头
     */
    @TableField("forward_request_headers")
    private String forwardRequestHeaders;

    /**
     * 转发请求体
     */
    @TableField("forward_request_body")
    private String forwardRequestBody;

    /**
     * 转发响应时间
     */
    @TableField("forward_response_time")
    private LocalDateTime forwardResponseTime;

    /**
     * 转发响应头
     */
    @TableField("forward_response_headers")
    private String forwardResponseHeaders;

    /**
     * 转发响应体
     */
    @TableField("forward_response_body")
    private String forwardResponseBody;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 总耗时 (毫秒)
     */
    @TableField("duration_milliseconds")
    private Integer durationMilliseconds;

    // tenant_id is inherited from TenantBaseDO
    // creator, create_time, updater, update_time, deleted are inherited from BaseDO (via TenantBaseDO)
}

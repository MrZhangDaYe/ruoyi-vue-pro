package cn.iocoder.yudao.module.esb.service.monitoring;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringPageReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringRespVO;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringUpdateReqVO;

import jakarta.validation.Valid;

/**
 * ESB接口监控 Service 接口
 *
 * @author 芋道源码
 */
public interface EsbMonitoringService {

    /**
     * 记录接口监控结果
     *
     * @param updateReqVO 监控结果信息
     */
    void recordMonitoringResult(@Valid EsbMonitoringUpdateReqVO updateReqVO);

    /**
     * 获取指定接口的当前监控状态
     *
     * @param interfaceId 接口编号
     * @return 监控状态
     */
    EsbMonitoringRespVO getMonitoringStatus(Long interfaceId);

    /**
     * 获取接口监控分页数据
     *
     * @param pageReqVO 分页查询参数
     * @return 监控分页结果
     */
    PageResult<EsbMonitoringRespVO> getMonitoringPage(EsbMonitoringPageReqVO pageReqVO);

    /**
     * (计划任务/手动触发) 执行指定接口的健康检查
     *
     * @param interfaceId 接口编号
     */
    void performInterfaceCheck(Long interfaceId);

    /**
     * (计划任务) 调度所有相关接口的周期性健康检查
     */
    void schedulePeriodicChecks();

}

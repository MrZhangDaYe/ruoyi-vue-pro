package cn.iocoder.yudao.module.esb.service.log;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogCreateReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogPageReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogRespVO;

import jakarta.validation.Valid;

/**
 * ESB请求日志 Service 接口
 *
 * @author 芋道源码
 */
public interface EsbLogService {

    /**
     * 创建ESB请求日志
     *
     * @param createReqVO 创建信息
     */
    void createLog(@Valid EsbLogCreateReqVO createReqVO);

    /**
     * 获得ESB请求日志
     *
     * @param id 编号
     * @return ESB请求日志
     */
    EsbLogRespVO getLog(Long id);

    /**
     * 获得ESB请求日志分页
     *
     * @param pageReqVO 分页查询
     * @return ESB请求日志分页
     */
    PageResult<EsbLogRespVO> getLogPage(EsbLogPageReqVO pageReqVO);

}

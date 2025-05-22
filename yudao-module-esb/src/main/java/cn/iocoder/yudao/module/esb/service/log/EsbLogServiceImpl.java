package cn.iocoder.yudao.module.esb.service.log;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogCreateReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogPageReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogRespVO;
import cn.iocoder.yudao.module.esb.convert.log.EsbLogConvert;
import cn.iocoder.yudao.module.esb.dal.dataobject.log.EsbLogDO;
import cn.iocoder.yudao.module.esb.dal.mysql.log.EsbLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * ESB请求日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class EsbLogServiceImpl implements EsbLogService {

    @Resource
    private EsbLogMapper esbLogMapper;

    @Override
    public void createLog(EsbLogCreateReqVO createReqVO) {
        EsbLogDO logDO = EsbLogConvert.INSTANCE.convert(createReqVO);
        esbLogMapper.insert(logDO);
    }

    @Override
    public EsbLogRespVO getLog(Long id) {
        EsbLogDO logDO = esbLogMapper.selectById(id);
        return EsbLogConvert.INSTANCE.convert(logDO);
    }

    @Override
    public PageResult<EsbLogRespVO> getLogPage(EsbLogPageReqVO pageReqVO) {
        PageResult<EsbLogDO> pageResult = esbLogMapper.selectPage(pageReqVO);
        return EsbLogConvert.INSTANCE.convertPage(pageResult);
    }

}

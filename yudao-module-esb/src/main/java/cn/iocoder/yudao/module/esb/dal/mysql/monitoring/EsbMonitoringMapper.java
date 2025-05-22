package cn.iocoder.yudao.module.esb.dal.mysql.monitoring;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringPageReqVO;
import cn.iocoder.yudao.module.esb.dal.dataobject.monitoring.EsbMonitoringDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * ESB接口监控表 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface EsbMonitoringMapper extends BaseMapperX<EsbMonitoringDO> {

    default EsbMonitoringDO selectByInterfaceId(Long interfaceId) {
        return selectOne(EsbMonitoringDO::getInterfaceId, interfaceId);
    }

    default PageResult<EsbMonitoringDO> selectPage(EsbMonitoringPageReqVO reqVO, Collection<Long> interfaceIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EsbMonitoringDO>()
                .inIfPresent(EsbMonitoringDO::getInterfaceId, interfaceIds) // Filter by a list of interface IDs
                .eqIfPresent(EsbMonitoringDO::getIsAvailable, reqVO.getIsAvailable())
                .orderByDesc(EsbMonitoringDO::getLastCheckTime));
    }

}

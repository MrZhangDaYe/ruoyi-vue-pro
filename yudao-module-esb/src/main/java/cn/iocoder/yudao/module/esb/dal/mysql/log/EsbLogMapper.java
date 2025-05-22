package cn.iocoder.yudao.module.esb.dal.mysql.log;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogPageReqVO;
import cn.iocoder.yudao.module.esb.dal.dataobject.log.EsbLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ESB请求日志表 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface EsbLogMapper extends BaseMapperX<EsbLogDO> {

    default PageResult<EsbLogDO> selectPage(EsbLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EsbLogDO>()
                .eqIfPresent(EsbLogDO::getInterfaceId, reqVO.getInterfaceId())
                .likeIfPresent(EsbLogDO::getInterfaceCode, reqVO.getInterfaceCode())
                .eqIfPresent(EsbLogDO::getSuccess, reqVO.getSuccess())
                .betweenIfPresent(EsbLogDO::getRequestTime, reqVO.getRequestTime())
                .orderByDesc(EsbLogDO::getRequestTime)); // Typically order by request_time or id
    }

}

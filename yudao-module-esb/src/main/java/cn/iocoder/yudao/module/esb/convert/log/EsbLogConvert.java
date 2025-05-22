package cn.iocoder.yudao.module.esb.convert.log;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogCreateReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogRespVO;
import cn.iocoder.yudao.module.esb.dal.dataobject.log.EsbLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EsbLogConvert {

    EsbLogConvert INSTANCE = Mappers.getMapper(EsbLogConvert.class);

    EsbLogDO convert(EsbLogCreateReqVO bean);

    EsbLogRespVO convert(EsbLogDO bean);

    PageResult<EsbLogRespVO> convertPage(PageResult<EsbLogDO> page);

}

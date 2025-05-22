package cn.iocoder.yudao.module.esb.convert.mapping;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.mapping.vo.EsbMappingCreateReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.mapping.vo.EsbMappingRespVO;
import cn.iocoder.yudao.module.esb.controller.admin.mapping.vo.EsbMappingUpdateReqVO;
import cn.iocoder.yudao.module.esb.dal.dataobject.mapping.EsbMappingDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface EsbMappingConvert {

    EsbMappingConvert INSTANCE = Mappers.getMapper(EsbMappingConvert.class);

    EsbMappingDO convert(EsbMappingCreateReqVO bean);

    EsbMappingDO convert(EsbMappingUpdateReqVO bean);

    EsbMappingRespVO convert(EsbMappingDO bean);

    List<EsbMappingRespVO> convertList(List<EsbMappingDO> list);

    PageResult<EsbMappingRespVO> convertPage(PageResult<EsbMappingDO> page);

}

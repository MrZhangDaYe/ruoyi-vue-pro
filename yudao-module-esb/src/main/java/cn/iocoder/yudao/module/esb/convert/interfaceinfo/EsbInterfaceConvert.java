package cn.iocoder.yudao.module.esb.convert.interfaceinfo;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.EsbInterfaceCreateReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.EsbInterfaceRespVO;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.EsbInterfaceUpdateReqVO;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface EsbInterfaceConvert {

    EsbInterfaceConvert INSTANCE = Mappers.getMapper(EsbInterfaceConvert.class);

    EsbInterfaceDO convert(EsbInterfaceCreateReqVO bean);

    EsbInterfaceDO convert(EsbInterfaceUpdateReqVO bean);

    EsbInterfaceRespVO convert(EsbInterfaceDO bean);

    List<EsbInterfaceRespVO> convertList(List<EsbInterfaceDO> list);

    PageResult<EsbInterfaceRespVO> convertPage(PageResult<EsbInterfaceDO> page);

}

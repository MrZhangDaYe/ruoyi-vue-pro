package cn.iocoder.yudao.module.esb.convert.monitoring;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringRespVO;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringUpdateReqVO;
import cn.iocoder.yudao.module.esb.dal.dataobject.monitoring.EsbMonitoringDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring") // No specific uses for EsbInterfaceConvert here, manual population is better for joined fields
public interface EsbMonitoringConvert {

    EsbMonitoringConvert INSTANCE = Mappers.getMapper(EsbMonitoringConvert.class);

    EsbMonitoringDO convert(EsbMonitoringUpdateReqVO bean);

    @Mapping(target = "interfaceName", ignore = true) // Will be populated manually in service
    @Mapping(target = "interfaceCode", ignore = true) // Will be populated manually in service
    EsbMonitoringRespVO convert(EsbMonitoringDO bean);

    List<EsbMonitoringRespVO> convertList(List<EsbMonitoringDO> list);

    PageResult<EsbMonitoringRespVO> convertPage(PageResult<EsbMonitoringDO> page);

}

package cn.iocoder.yudao.module.esb.dal.mysql.mapping;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.esb.dal.dataobject.mapping.EsbMappingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ESB参数映射表 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface EsbMappingMapper extends BaseMapperX<EsbMappingDO> {

    default List<EsbMappingDO> selectByInterfaceId(Long interfaceId) {
        return selectList(EsbMappingDO::getInterfaceId, interfaceId);
    }

    default EsbMappingDO selectByInterfaceIdAndType(Long interfaceId, String type) {
        return selectOne(new LambdaQueryWrapperX<EsbMappingDO>()
                .eq(EsbMappingDO::getInterfaceId, interfaceId)
                .eq(EsbMappingDO::getType, type));
    }

}

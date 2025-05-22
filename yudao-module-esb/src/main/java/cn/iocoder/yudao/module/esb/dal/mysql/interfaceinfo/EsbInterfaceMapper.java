package cn.iocoder.yudao.module.esb.dal.mysql.interfaceinfo;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ESB接口配置表 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface EsbInterfaceMapper extends BaseMapperX<EsbInterfaceDO> {

    default EsbInterfaceDO selectByCode(String code) {
        return selectOne(EsbInterfaceDO::getCode, code);
    }

    default EsbInterfaceDO selectByPath(String path) {
        return selectOne(EsbInterfaceDO::getPath, path);
    }

}

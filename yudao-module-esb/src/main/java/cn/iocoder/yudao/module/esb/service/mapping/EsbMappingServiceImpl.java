package cn.iocoder.yudao.module.esb.service.mapping;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.mapping.vo.*;
import cn.iocoder.yudao.module.esb.convert.mapping.EsbMappingConvert;
import cn.iocoder.yudao.module.esb.dal.dataobject.mapping.EsbMappingDO;
import cn.iocoder.yudao.module.esb.dal.mysql.mapping.EsbMappingMapper;
import cn.iocoder.yudao.module.esb.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.esb.service.interfaceinfo.EsbInterfaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * ESB参数映射 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class EsbMappingServiceImpl implements EsbMappingService {

    @Resource
    private EsbMappingMapper esbMappingMapper;

    @Resource
    private EsbInterfaceService esbInterfaceService; // To validate interfaceId

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMapping(EsbMappingCreateReqVO createReqVO) {
        // 1. 校验接口存在
        validateInterfaceExists(createReqVO.getInterfaceId());
        // 2. 校验映射唯一性 (同一接口下，同一类型只能有一个映射)
        validateMappingUnique(null, createReqVO.getInterfaceId(), createReqVO.getType());

        // 3. 插入
        EsbMappingDO esbMapping = EsbMappingConvert.INSTANCE.convert(createReqVO);
        esbMappingMapper.insert(esbMapping);
        return esbMapping.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMapping(EsbMappingUpdateReqVO updateReqVO) {
        // 1. 校验待更新的映射存在
        validateMappingExists(updateReqVO.getId());
        // 2. 校验接口存在
        validateInterfaceExists(updateReqVO.getInterfaceId());
        // 3. 校验映射唯一性
        validateMappingUnique(updateReqVO.getId(), updateReqVO.getInterfaceId(), updateReqVO.getType());

        // 4. 更新
        EsbMappingDO updateObj = EsbMappingConvert.INSTANCE.convert(updateReqVO);
        esbMappingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMapping(Long id) {
        // 1. 校验存在
        validateMappingExists(id);
        // 2. 删除
        esbMappingMapper.deleteById(id);
    }

    private void validateInterfaceExists(Long interfaceId) {
        if (esbInterfaceService.getInterface(interfaceId) == null) {
            throw exception(ErrorCodeConstants.INTERFACE_NOT_EXISTS);
        }
    }

    private EsbMappingDO validateMappingExists(Long id) {
        EsbMappingDO mapping = esbMappingMapper.selectById(id);
        if (mapping == null) {
            throw exception(ErrorCodeConstants.MAPPING_NOT_EXISTS);
        }
        return mapping;
    }

    private void validateMappingUnique(Long id, Long interfaceId, String type) {
        EsbMappingDO byInterfaceIdAndType = esbMappingMapper.selectByInterfaceIdAndType(interfaceId, type);
        if (byInterfaceIdAndType != null && !Objects.equals(byInterfaceIdAndType.getId(), id)) {
            throw exception(ErrorCodeConstants.MAPPING_TYPE_EXISTS_FOR_INTERFACE, type, interfaceId);
        }
    }

    @Override
    public EsbMappingRespVO getMapping(Long id) {
        EsbMappingDO mapping = esbMappingMapper.selectById(id);
        return EsbMappingConvert.INSTANCE.convert(mapping);
    }

    @Override
    public PageResult<EsbMappingRespVO> getMappingPage(EsbMappingPageReqVO pageReqVO) {
        PageResult<EsbMappingDO> pageResult = esbMappingMapper.selectPage(pageReqVO);
        return EsbMappingConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<EsbMappingRespVO> getMappingsByInterfaceId(Long interfaceId) {
        List<EsbMappingDO> list = esbMappingMapper.selectByInterfaceId(interfaceId);
        return EsbMappingConvert.INSTANCE.convertList(list);
    }

    @Override
    public EsbMappingRespVO getMappingByInterfaceIdAndType(Long interfaceId, String type) {
        EsbMappingDO mapping = esbMappingMapper.selectByInterfaceIdAndType(interfaceId, type);
        return EsbMappingConvert.INSTANCE.convert(mapping);
    }
}

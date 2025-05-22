package cn.iocoder.yudao.module.esb.service.mapping;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.mapping.vo.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * ESB参数映射 Service 接口
 *
 * @author 芋道源码
 */
public interface EsbMappingService {

    /**
     * 创建ESB参数映射
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMapping(@Valid EsbMappingCreateReqVO createReqVO);

    /**
     * 更新ESB参数映射
     *
     * @param updateReqVO 更新信息
     */
    void updateMapping(@Valid EsbMappingUpdateReqVO updateReqVO);

    /**
     * 删除ESB参数映射
     *
     * @param id 编号
     */
    void deleteMapping(Long id);

    /**
     * 获得ESB参数映射
     *
     * @param id 编号
     * @return ESB参数映射
     */
    EsbMappingRespVO getMapping(Long id);

    /**
     * 获得ESB参数映射分页
     *
     * @param pageReqVO 分页查询
     * @return ESB参数映射分页
     */
    PageResult<EsbMappingRespVO> getMappingPage(EsbMappingPageReqVO pageReqVO);

    /**
     * 获得指定接口的所有参数映射列表
     *
     * @param interfaceId 接口编号
     * @return ESB参数映射列表
     */
    List<EsbMappingRespVO> getMappingsByInterfaceId(Long interfaceId);

    /**
     * 获得指定接口和类型的参数映射
     *
     * @param interfaceId 接口编号
     * @param type 映射类型 (REQUEST, RESPONSE)
     * @return ESB参数映射
     */
    EsbMappingRespVO getMappingByInterfaceIdAndType(Long interfaceId, String type);

}

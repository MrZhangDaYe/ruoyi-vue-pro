package cn.iocoder.yudao.module.esb.service.interfaceinfo;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * ESB接口配置 Service 接口
 *
 * @author 芋道源码
 */
public interface EsbInterfaceService {

    /**
     * 创建ESB接口
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInterface(@Valid EsbInterfaceCreateReqVO createReqVO);

    /**
     * 更新ESB接口
     *
     * @param updateReqVO 更新信息
     */
    void updateInterface(@Valid EsbInterfaceUpdateReqVO updateReqVO);

    /**
     * 删除ESB接口
     *
     * @param id 编号
     */
    void deleteInterface(Long id);

    /**
     * 获得ESB接口
     *
     * @param id 编号
     * @return ESB接口
     */
    EsbInterfaceRespVO getInterface(Long id);

    /**
     * 获得ESB接口分页
     *
     * @param pageReqVO 分页查询
     * @return ESB接口分页
     */
    PageResult<EsbInterfaceRespVO> getInterfacePage(EsbInterfacePageReqVO pageReqVO);

    /**
     * 获得ESB接口列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return ESB接口列表
     */
    List<EsbInterfaceRespVO> getInterfaceList(EsbInterfaceExportReqVO exportReqVO);

    /**
     * 启动指定接口的Camel路由
     *
     * @param id 接口编号
     */
    void startInterfaceRoute(Long id);

    /**
     * 停止指定接口的Camel路由
     *
     * @param id 接口编号
     */
    void stopInterfaceRoute(Long id);

    /**
     * 刷新（更新）指定接口的Camel路由
     *
     * @param id 接口编号
     */
    void refreshInterfaceRoute(Long id);

    /**
     * Dynamically adds a Camel route based on the interface definition and saves the generated route ID.
     * If the interface is not enabled, this method might choose not to add the route or log a warning.
     *
     * @param interfaceDO The interface definition.
     */
    void addCamelRouteAndSave(cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO interfaceDO);

}

package cn.iocoder.yudao.module.esb.service.interfaceinfo;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.*;
import cn.iocoder.yudao.module.esb.convert.interfaceinfo.EsbInterfaceConvert;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.mysql.interfaceinfo.EsbInterfaceMapper;
import cn.iocoder.yudao.module.esb.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * ESB接口配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class EsbInterfaceServiceImpl implements EsbInterfaceService {

    @Resource
    private EsbInterfaceMapper esbInterfaceMapper;

    @Resource
    private CamelContext camelContext; // For future Camel route management

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInterface(EsbInterfaceCreateReqVO createReqVO) {
        // 校验唯一性
        validateInterfaceUnique(null, createReqVO.getCode(), createReqVO.getPath());

        // 插入
        EsbInterfaceDO esbInterface = EsbInterfaceConvert.INSTANCE.convert(createReqVO);
        esbInterfaceMapper.insert(esbInterface);

        // 尝试添加 Camel 路由
        addCamelRoute(esbInterface);
        return esbInterface.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInterface(EsbInterfaceUpdateReqVO updateReqVO) {
        // 校验存在
        validateInterfaceExists(updateReqVO.getId());
        // 校验唯一性
        validateInterfaceUnique(updateReqVO.getId(), updateReqVO.getCode(), updateReqVO.getPath());

        // 更新
        EsbInterfaceDO updateObj = EsbInterfaceConvert.INSTANCE.convert(updateReqVO);
        esbInterfaceMapper.updateById(updateObj);

        // 尝试更新 Camel 路由
        updateCamelRoute(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInterface(Long id) {
        // 校验存在
        EsbInterfaceDO esbInterface = validateInterfaceExists(id);

        // 尝试移除 Camel 路由
        removeCamelRoute(esbInterface);

        // 删除
        esbInterfaceMapper.deleteById(id);
    }

    private EsbInterfaceDO validateInterfaceExists(Long id) {
        EsbInterfaceDO esbInterface = esbInterfaceMapper.selectById(id);
        if (esbInterface == null) {
            throw exception(ErrorCodeConstants.INTERFACE_NOT_EXISTS);
        }
        return esbInterface;
    }

    private void validateInterfaceUnique(Long id, String code, String path) {
        EsbInterfaceDO byCode = esbInterfaceMapper.selectByCode(code);
        if (byCode != null && !Objects.equals(byCode.getId(), id)) {
            throw exception(ErrorCodeConstants.INTERFACE_CODE_EXISTS, code);
        }

        EsbInterfaceDO byPath = esbInterfaceMapper.selectByPath(path);
        if (byPath != null && !Objects.equals(byPath.getId(), id)) {
            throw exception(ErrorCodeConstants.INTERFACE_PATH_EXISTS, path);
        }
    }

    @Override
    public EsbInterfaceRespVO getInterface(Long id) {
        EsbInterfaceDO esbInterface = esbInterfaceMapper.selectById(id);
        return EsbInterfaceConvert.INSTANCE.convert(esbInterface);
    }

    @Override
    public PageResult<EsbInterfaceRespVO> getInterfacePage(EsbInterfacePageReqVO pageReqVO) {
        PageResult<EsbInterfaceDO> pageResult = esbInterfaceMapper.selectPage(pageReqVO);
        return EsbInterfaceConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<EsbInterfaceRespVO> getInterfaceList(EsbInterfaceExportReqVO exportReqVO) {
        List<EsbInterfaceDO> list = esbInterfaceMapper.selectList(exportReqVO);
        return EsbInterfaceConvert.INSTANCE.convertList(list);
    }

    // =================== Camel Route Management (Placeholders) ===================

    @Override
    public void startInterfaceRoute(Long id) {
        EsbInterfaceDO esbInterface = validateInterfaceExists(id);
        // TODO: Implement actual Camel route starting logic
        log.info("[startInterfaceRoute][interfaceId({})] Starting Camel route for interface: {}", id, esbInterface.getCode());
        // Example: if (esbInterface.getStatus() == CommonStatusEnum.ENABLE.getStatus()) { addCamelRoute(esbInterface); }
    }

    @Override
    public void stopInterfaceRoute(Long id) {
        EsbInterfaceDO esbInterface = validateInterfaceExists(id);
        // TODO: Implement actual Camel route stopping logic
        log.info("[stopInterfaceRoute][interfaceId({})] Stopping Camel route for interface: {}", id, esbInterface.getCode());
        // Example: removeCamelRoute(esbInterface);
    }

    @Override
    public void refreshInterfaceRoute(Long id) {
        EsbInterfaceDO esbInterface = validateInterfaceExists(id);
         // TODO: Implement actual Camel route refreshing logic
        log.info("[refreshInterfaceRoute][interfaceId({})] Refreshing Camel route for interface: {}", id, esbInterface.getCode());
        // Example: updateCamelRoute(esbInterface);
    }

    private void addCamelRoute(EsbInterfaceDO interfaceDO) {
        if (interfaceDO == null) {
            log.warn("[addCamelRoute] interfaceDO is null, skipping.");
            return;
        }
        // In a real scenario, check if (interfaceDO.getStatus() == CommonStatusEnum.ENABLE.getStatus())
        log.info("[addCamelRoute][interfaceId({})] Placeholder: Attempting to add Camel route for interface code: {}", interfaceDO.getId(), interfaceDO.getCode());
        // Actual Camel route addition logic will be complex and involve:
        // 1. Building a RouteDefinition based on interfaceDO properties (protocol, path, forwardAddress etc.)
        // 2. Adding the RouteDefinition to CamelContext: camelContext.addRouteDefinition(routeDefinition);
        // 3. Starting the route: camelContext.getRouteController().startRoute(routeId);
        // For now, we just log. The actual implementation is deferred.
        // Store routeId in interfaceDO.setCamelRouteId("generatedRouteId"); and update DB if needed.
        if (interfaceDO.getStatus() != null && interfaceDO.getStatus() == 1) { // Assuming 1 means enabled
            try {
                String routeId = "esb-route-" + interfaceDO.getCode();
                // Placeholder: In real implementation, build and start the route using CamelContext
                log.info("[addCamelRoute][interfaceId({})] Successfully prepared to add Camel route: {}. Current status: ENABLED", interfaceDO.getId(), routeId);
                // Simulating route ID assignment for now
                if (interfaceDO.getCamelRouteId() == null) {
                     interfaceDO.setCamelRouteId(routeId);
                     // esbInterfaceMapper.updateById(interfaceDO); // Update DB with routeId
                     log.info("[addCamelRoute][interfaceId({})] Assigned camelRouteId: {}", interfaceDO.getId(), routeId);
                }
            } catch (Exception e) {
                log.error("[addCamelRoute][interfaceId({})] Failed to add Camel route for interface code: {}. Error: {}", interfaceDO.getId(), interfaceDO.getCode(), e.getMessage(), e);
            }
        } else {
            log.info("[addCamelRoute][interfaceId({})] Interface code: {} is not enabled, skipping Camel route addition.", interfaceDO.getId(), interfaceDO.getCode());
        }
    }

    private void updateCamelRoute(EsbInterfaceDO interfaceDO) {
        if (interfaceDO == null) {
            log.warn("[updateCamelRoute] interfaceDO is null, skipping.");
            return;
        }
        log.info("[updateCamelRoute][interfaceId({})] Placeholder: Attempting to update Camel route for interface code: {}", interfaceDO.getId(), interfaceDO.getCode());
        // Actual Camel route update logic:
        // 1. Stop and remove the old route if it exists (use interfaceDO.getCamelRouteId())
        //    camelContext.getRouteController().stopRoute(oldRouteId);
        //    camelContext.removeRoute(oldRouteId);
        // 2. If interfaceDO.getStatus() is enabled, then add the new/updated route (similar to addCamelRoute)
        removeCamelRoute(interfaceDO); // Remove existing first
        if (interfaceDO.getStatus() != null && interfaceDO.getStatus() == 1) { // Assuming 1 means enabled
            addCamelRoute(interfaceDO); // Add the new or updated route
        } else {
             log.info("[updateCamelRoute][interfaceId({})] Interface code: {} is not enabled, route will remain stopped/removed.", interfaceDO.getId(), interfaceDO.getCode());
        }
    }

    private void removeCamelRoute(EsbInterfaceDO interfaceDO) {
        if (interfaceDO == null || interfaceDO.getCamelRouteId() == null) {
            log.warn("[removeCamelRoute] interfaceDO or camelRouteId is null, skipping. Interface ID: {}", interfaceDO != null ? interfaceDO.getId() : "null");
            return;
        }
        log.info("[removeCamelRoute][interfaceId({})] Placeholder: Attempting to remove Camel route: {}", interfaceDO.getId(), interfaceDO.getCamelRouteId());
        // Actual Camel route removal logic:
        // 1. Stop the route: camelContext.getRouteController().stopRoute(interfaceDO.getCamelRouteId());
        // 2. Remove the route: camelContext.removeRoute(interfaceDO.getCamelRouteId());
        // For now, we just log.
         try {
            // Placeholder: In real implementation, stop and remove the route using CamelContext
            log.info("[removeCamelRoute][interfaceId({})] Successfully prepared to remove Camel route: {}", interfaceDO.getId(), interfaceDO.getCamelRouteId());
            // interfaceDO.setCamelRouteId(null); // Clear routeId from DO
            // esbInterfaceMapper.updateById(interfaceDO); // Update DB
        } catch (Exception e) {
            log.error("[removeCamelRoute][interfaceId({})] Failed to remove Camel route {} for interface code: {}. Error: {}",
                interfaceDO.getId(), interfaceDO.getCamelRouteId(), interfaceDO.getCode(), e.getMessage(), e);
        }
    }

}

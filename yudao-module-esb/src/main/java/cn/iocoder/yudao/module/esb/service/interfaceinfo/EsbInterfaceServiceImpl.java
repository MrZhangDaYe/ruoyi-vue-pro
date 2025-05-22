package cn.iocoder.yudao.module.esb.service.interfaceinfo;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.*;
import cn.iocoder.yudao.module.esb.convert.interfaceinfo.EsbInterfaceConvert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.mysql.interfaceinfo.EsbInterfaceMapper;
import cn.iocoder.yudao.module.esb.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
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
    private CamelContext camelContext;

    @Resource
    private cn.iocoder.yudao.module.esb.service.mapping.EsbMappingService esbMappingService; // Added EsbMappingService

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInterface(EsbInterfaceCreateReqVO createReqVO) {
        // 校验唯一性
        validateInterfaceUnique(null, createReqVO.getCode(), createReqVO.getPath());

        // 插入
        EsbInterfaceDO esbInterface = EsbInterfaceConvert.INSTANCE.convert(createReqVO);
        esbInterfaceMapper.insert(esbInterface); // esbInterface gets ID populated by MyBatis

        if (Integer.valueOf(1).equals(esbInterface.getStatus())) { // Assuming 1 is enabled
            addCamelRouteAndSave(esbInterface);
        }
        return esbInterface.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInterface(EsbInterfaceUpdateReqVO updateReqVO) {
        // 校验存在
        validateInterfaceExists(updateReqVO.getId());
        // 校验唯一性
        validateInterfaceUnique(updateReqVO.getId(), updateReqVO.getCode(), updateReqVO.getPath());

        EsbInterfaceDO updatedInterfaceDO = EsbInterfaceConvert.INSTANCE.convert(updateReqVO);
        EsbInterfaceDO oldInterfaceDO = esbInterfaceMapper.selectById(updatedInterfaceDO.getId());
        if (oldInterfaceDO == null) { // Should be caught by validateInterfaceExists, but as a safeguard
            throw exception(ErrorCodeConstants.INTERFACE_NOT_EXISTS);
        }

        // If old route ID exists, remove it
        if (StrUtil.isNotEmpty(oldInterfaceDO.getCamelRouteId())) {
            removeCamelRouteLogic(oldInterfaceDO.getCamelRouteId(), oldInterfaceDO.getCode());
        }

        esbInterfaceMapper.updateById(updatedInterfaceDO); // Perform the actual update of other fields

        // If the updated interface is enabled, add the new route
        if (Integer.valueOf(1).equals(updatedInterfaceDO.getStatus())) {
            // Fetch again to get the fully updated DO if needed, or pass updatedInterfaceDO
            // addCamelRouteAndSave will update the camelRouteId in DB
            addCamelRouteAndSave(updatedInterfaceDO);
        } else { // If not enabled, ensure no route ID is stored
            EsbInterfaceDO clearRouteIdDO = new EsbInterfaceDO();
            clearRouteIdDO.setId(updatedInterfaceDO.getId());
            clearRouteIdDO.setCamelRouteId(null);
            esbInterfaceMapper.updateById(clearRouteIdDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInterface(Long id) {
        // 校验存在
        EsbInterfaceDO interfaceDO = validateInterfaceExists(id); // Fetches the DO

        if (interfaceDO != null && StrUtil.isNotEmpty(interfaceDO.getCamelRouteId())) {
            removeCamelRouteLogic(interfaceDO.getCamelRouteId(), interfaceDO.getCode());
        }
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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.LoggingLevel;
// import org.apache.camel.model.RouteDefinition; // May not be needed if adding directly
import cn.iocoder.yudao.module.esb.camel.processor.EsbRequestTransformProcessor; // Import new processors
import cn.iocoder.yudao.module.esb.camel.processor.EsbResponseTransformProcessor; // Import new processors

// ... inside EsbInterfaceServiceImpl ...
    // =================== Camel Route Management ===================

    private String buildAndStartCamelRoute(EsbInterfaceDO interfaceDO) {
       if (interfaceDO == null || !Integer.valueOf(1).equals(interfaceDO.getStatus())) {
           log.warn("Interface {} is not enabled or is null, skipping Camel route creation.", interfaceDO != null ? interfaceDO.getCode() : "null");
           return null;
       }

       final String routeId = interfaceDO.getCode(); // Use code as route ID for uniqueness and readability
       final String sourceUri;
       // Assuming HTTP/HTTPS for now. TODO: Expand for other protocols.
       if ("HTTP".equalsIgnoreCase(interfaceDO.getProtocol()) || "HTTPS".equalsIgnoreCase(interfaceDO.getProtocol())) {
           // platform-http component is better for exposing REST services in Spring Boot
           sourceUri = "platform-http:/" + interfaceDO.getPath().replaceAll("^/+", "") // Ensure path doesn't start with multiple slashes
                   + "?httpMethodRestrict=GET,POST"; // TODO: Make methods configurable from interfaceDO
       } else {
           log.error("Unsupported protocol {} for interface code: {}", interfaceDO.getProtocol(), interfaceDO.getCode());
           return null;
       }

       final String forwardUri;
       if (StrUtil.isNotEmpty(interfaceDO.getForwardInterfaceAddress())) {
           if ("HTTP".equalsIgnoreCase(interfaceDO.getForwardProtocol()) || "HTTPS".equalsIgnoreCase(interfaceDO.getForwardProtocol())) {
               String address = interfaceDO.getForwardInterfaceAddress();
               // Ensure address starts with http:// or https://
               if (!address.toLowerCase().startsWith("http://") && !address.toLowerCase().startsWith("https://")) {
                    address = interfaceDO.getForwardProtocol().toLowerCase() + "://" + address;
               }
               forwardUri = address
                       + "?bridgeEndpoint=true" // Important for acting as a transparent proxy
                       + "&throwExceptionOnFailure=false"; // Handle errors manually to enable logging/custom responses
                       // TODO: Add timeouts: "&httpClient.connectTimeout=" + interfaceDO.getTimeoutMilliseconds() + "&httpClient.socketTimeout=" + interfaceDO.getTimeoutMilliseconds()
           } else {
               log.error("Unsupported forward protocol {} for interface code: {}", interfaceDO.getForwardProtocol(), interfaceDO.getCode());
               return null;
           }
       } else {
            // If forward address is not set, this route might be a direct response or error.
            // For now, we'll assume a forward URI is mandatory for this example structure.
            // A more robust implementation would handle routes without a .toD() if not forwarding.
            log.warn("Forward address is not configured for interface code: {}. Route will not forward.", interfaceDO.getCode());
            forwardUri = null; // Or handle as a direct response route
       }


       try {
           RouteBuilder routeBuilder = new RouteBuilder() {
               @Override
               public void configure() throws Exception {
                   // Define error handler (basic for now, TODO: enhance with retries)
                   errorHandler(deadLetterChannel("log:esb.error?level=ERROR&showAll=true")
                           .useOriginalMessage().maximumRedeliveries(interfaceDO.getRetryCount() != null ? interfaceDO.getRetryCount() : 0)
                           .redeliveryDelay(1000)); // TODO: Make delay configurable

                   from(sourceUri)
                       .routeId(routeId)
                       .log(LoggingLevel.INFO, "ESB Route " + routeId + " received request. Headers: ${headers}")
                       // TODO: Integrate with EsbLogService for request logging (headers, body)

                       .process(new EsbRequestTransformProcessor(interfaceDO, esbMappingService)); // Request Transformation
                   
                   if (StrUtil.isNotEmpty(forwardUri)) {
                       getRouteCollection().getRoutes().get(getRouteCollection().getRoutes().size()-1) // Get the current route definition
                           .log(LoggingLevel.INFO, "ESB Route " + routeId + " forwarding to: " + forwardUri)
                           .to(forwardUri)
                           .log(LoggingLevel.INFO, "ESB Route " + routeId + " received response from target. Status: ${header.CamelHttpResponseCode}. Body: ${body}")
                           // TODO: Integrate with EsbLogService for response logging
                           .process(new EsbResponseTransformProcessor(interfaceDO, esbMappingService)); // Response Transformation
                   } else {
                        // Handle cases where there is no forward URI (e.g., respond directly or error)
                        getRouteCollection().getRoutes().get(getRouteCollection().getRoutes().size()-1)
                           .log(LoggingLevel.WARN, "ESB Route " + routeId + " has no forward URI configured. Ending route.")
                           // .transform().simple("Mock response as no forward URI set for ${routeId}") // Example direct response
                           .process(new EsbResponseTransformProcessor(interfaceDO, esbMappingService)); // Still process response (e.g. to set a default)
                   }
                       // TODO: Success/failure determination based on interfaceDO.getSuccessFlagJsonPath()
               }
           };
           camelContext.addRoutes(routeBuilder); // This starts routes by default if CamelContext is started.
                                               // No need to explicitly start route if autoStartup is true (default) for routes.
           log.info("Successfully defined and added Camel route with ID: {} for interface code: {}", routeId, interfaceDO.getCode());
           return routeId;

       } catch (Exception e) {
           log.error("Failed to build or add Camel route: {} for interface code: {}. Error: {}", routeId, interfaceDO.getCode(), e.getMessage(), e);
           // Attempt to clean up if route definition was partially added or failed
           try {
               if (camelContext.getRoute(routeId) != null) {
                   camelContext.getRouteController().stopRoute(routeId);
                   camelContext.removeRoute(routeId);
               }
           } catch (Exception cleanupEx) {
               log.error("Error during cleanup of failed route {}: {}", routeId, cleanupEx.getMessage(), cleanupEx);
           }
           return null;
       }
    }

    @Override
    public void addCamelRouteAndSave(EsbInterfaceDO interfaceDO) {
        Assert.notNull(interfaceDO, "InterfaceDO cannot be null for adding Camel route.");

        String existingCamelRouteId = interfaceDO.getCamelRouteId();
        if (StrUtil.isNotEmpty(existingCamelRouteId)) {
            log.warn("Interface {} already has a Camel route ID: {}. Consider removing it first or use refresh.", interfaceDO.getCode(), existingCamelRouteId);
            // Optionally, try to remove it first if the logic implies add should be idempotent or replace
            // removeCamelRouteLogic(existingCamelRouteId, interfaceDO.getCode());
        }

        String newCamelRouteId = buildAndStartCamelRoute(interfaceDO);
        if (StrUtil.isNotEmpty(newCamelRouteId)) {
            EsbInterfaceDO updateDO = new EsbInterfaceDO();
            updateDO.setId(interfaceDO.getId());
            updateDO.setCamelRouteId(newCamelRouteId);
            esbInterfaceMapper.updateById(updateDO);
            // Update the passed in object as well, so the caller has the routeId
            interfaceDO.setCamelRouteId(newCamelRouteId);
            log.info("Successfully added Camel route {} for interface {} and updated database.", newCamelRouteId, interfaceDO.getCode());
        } else {
            log.warn("Failed to build or start Camel route for interface {}. No route ID assigned.", interfaceDO.getCode());
        }
    }

    private void removeCamelRouteLogic(String camelRouteId, String interfaceCode) {
        if (StrUtil.isEmpty(camelRouteId)) {
            log.info("No Camel route ID provided for interface code: {}. Nothing to remove.", interfaceCode);
            return;
        }
        try {
            if (camelContext.getRouteController().getRouteStatus(camelRouteId) == ServiceStatus.Started) {
                camelContext.getRouteController().stopRoute(camelRouteId);
                log.info("Successfully stopped Camel route: {} for interface code: {}", camelRouteId, interfaceCode);
            }
            boolean removed = camelContext.removeRoute(camelRouteId);
            if (removed) {
                log.info("Successfully removed Camel route definition: {} for interface code: {}", camelRouteId, interfaceCode);
            } else {
                log.warn("Could not remove Camel route definition: {} (it might not exist).", camelRouteId);
            }
        } catch (Exception e) {
            log.error("Error while stopping or removing Camel route: {} for interface code: {}. Error: {}", camelRouteId, interfaceCode, e.getMessage(), e);
        }
    }

    public void removeCamelRouteAndUpdateDO(EsbInterfaceDO interfaceDO) {
        Assert.notNull(interfaceDO, "InterfaceDO cannot be null for removing Camel route.");
        removeCamelRouteLogic(interfaceDO.getCamelRouteId(), interfaceDO.getCode());

        EsbInterfaceDO updateDO = new EsbInterfaceDO();
        updateDO.setId(interfaceDO.getId());
        updateDO.setCamelRouteId(null); // Clear the route ID
        esbInterfaceMapper.updateById(updateDO);
        interfaceDO.setCamelRouteId(null); // Update the passed in object as well
        log.info("Cleared Camel route ID for interface {} in database.", interfaceDO.getCode());
    }


    @Override
    public void startInterfaceRoute(Long id) {
        EsbInterfaceDO interfaceDO = esbInterfaceMapper.selectById(id);
        if (interfaceDO == null) {
            throw exception(ErrorCodeConstants.INTERFACE_NOT_EXISTS);
        }

        // Update status to enabled
        interfaceDO.setStatus(1); // Assuming 1 is enabled
        EsbInterfaceDO updateStatusDO = new EsbInterfaceDO();
        updateStatusDO.setId(id);
        updateStatusDO.setStatus(1);
        esbInterfaceMapper.updateById(updateStatusDO);

        // Remove old route if any
        if (StrUtil.isNotEmpty(interfaceDO.getCamelRouteId())) {
            removeCamelRouteLogic(interfaceDO.getCamelRouteId(), interfaceDO.getCode());
            interfaceDO.setCamelRouteId(null); // Clear from current DO instance
        }
        addCamelRouteAndSave(interfaceDO); // This will update interfaceDO with new routeId and save to DB
    }

    @Override
    public void stopInterfaceRoute(Long id) {
        EsbInterfaceDO interfaceDO = esbInterfaceMapper.selectById(id);
        if (interfaceDO == null) {
            throw exception(ErrorCodeConstants.INTERFACE_NOT_EXISTS);
        }

        removeCamelRouteAndUpdateDO(interfaceDO); // This method now also clears camelRouteId in DB

        // Optionally, also set interface status to disabled
        EsbInterfaceDO updateStatusDO = new EsbInterfaceDO();
        updateStatusDO.setId(id);
        updateStatusDO.setStatus(0); // Assuming 0 is disabled
        esbInterfaceMapper.updateById(updateStatusDO);
        interfaceDO.setStatus(0); // Update local object status
        log.info("Interface {} status set to disabled.", interfaceDO.getCode());
    }

    @Override
    public void refreshInterfaceRoute(Long id) {
        EsbInterfaceDO interfaceDO = esbInterfaceMapper.selectById(id);
        if (interfaceDO == null) {
            throw exception(ErrorCodeConstants.INTERFACE_NOT_EXISTS);
        }

        // Remove old route if any
        if (StrUtil.isNotEmpty(interfaceDO.getCamelRouteId())) {
            removeCamelRouteLogic(interfaceDO.getCamelRouteId(), interfaceDO.getCode());
            interfaceDO.setCamelRouteId(null); // Clear it from the current DO instance
        }

        if (Integer.valueOf(1).equals(interfaceDO.getStatus())) { // Only add if it's enabled
            addCamelRouteAndSave(interfaceDO);
        } else {
            log.warn("Interface {} is not enabled. Refresh will only remove the old route if it existed.", interfaceDO.getCode());
            // Ensure route ID is cleared in DB if it was disabled
            EsbInterfaceDO clearRouteIdDO = new EsbInterfaceDO();
            clearRouteIdDO.setId(interfaceDO.getId());
            clearRouteIdDO.setCamelRouteId(null);
            esbInterfaceMapper.updateById(clearRouteIdDO);
        }
    }

}

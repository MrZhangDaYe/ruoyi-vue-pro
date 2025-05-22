package cn.iocoder.yudao.module.esb.listener;

import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.mysql.interfaceinfo.EsbInterfaceMapper;
import cn.iocoder.yudao.module.esb.service.interfaceinfo.EsbInterfaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource; // Use jakarta.annotation.Resource for Spring 3+
import java.util.List;

/**
 * Listener to initialize and start ESB routes on application startup.
 */
@Component
@Slf4j
public class EsbRouteApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private EsbInterfaceMapper esbInterfaceMapper; // Using mapper directly to get all enabled interfaces

    @Resource
    private EsbInterfaceService esbInterfaceService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ensure this runs only once and for the root application context
        if (event.getApplicationContext().getParent() == null) {
            log.info("Application startup detected, initializing ESB routes...");
            try {
                // Fetch all enabled interfaces
                // Using status 1 for enabled as CommonStatusEnum was not found.
                List<EsbInterfaceDO> enabledInterfaces = esbInterfaceMapper.selectList(
                        EsbInterfaceDO::getStatus, 1
                );

                if (enabledInterfaces == null || enabledInterfaces.isEmpty()) {
                    log.info("No enabled ESB interfaces found to initialize.");
                    return;
                }

                log.info("Found {} enabled ESB interfaces. Attempting to start routes...", enabledInterfaces.size());
                for (EsbInterfaceDO interfaceDO : enabledInterfaces) {
                    try {
                        log.debug("Initializing route for interface code: {}", interfaceDO.getCode());
                        // This method (addCamelRouteAndSave) will be implemented in a future step.
                        // It's responsible for building the route, adding it to CamelContext,
                        // and updating the interfaceDO with the camelRouteId.
                        esbInterfaceService.addCamelRouteAndSave(interfaceDO);
                        log.info("Successfully initialized and started route for interface code: {}", interfaceDO.getCode());
                    } catch (Exception e) {
                        log.error("Failed to initialize route for interface code: {}. Error: {}", interfaceDO.getCode(), e.getMessage(), e);
                        // Depending on requirements, might want to update interface status or store error
                    }
                }
                log.info("ESB routes initialization complete.");
            } catch (Exception e) {
                log.error("Error during ESB routes initialization: {}", e.getMessage(), e);
            }
        }
    }
}

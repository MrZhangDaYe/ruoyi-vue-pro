package cn.iocoder.yudao.module.esb.service.monitoring;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.EsbInterfaceRespVO; // For getting interface details
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringPageReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringRespVO;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringUpdateReqVO;
import cn.iocoder.yudao.module.esb.convert.monitoring.EsbMonitoringConvert;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.dataobject.monitoring.EsbMonitoringDO;
import cn.iocoder.yudao.module.esb.dal.mysql.interfaceinfo.EsbInterfaceMapper;
import cn.iocoder.yudao.module.esb.dal.mysql.monitoring.EsbMonitoringMapper;
import cn.iocoder.yudao.module.esb.service.interfaceinfo.EsbInterfaceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EsbMonitoringServiceImpl implements EsbMonitoringService {

    @Resource
    private EsbMonitoringMapper esbMonitoringMapper;

    @Resource
    private EsbMonitoringConvert esbMonitoringConvert;

    @Resource
    private EsbInterfaceService esbInterfaceService;

    @Resource
    private EsbInterfaceMapper esbInterfaceMapper; // For fetching all enabled interfaces

    @Override
    @Transactional
    public void recordMonitoringResult(EsbMonitoringUpdateReqVO updateReqVO) {
        EsbMonitoringDO existingMonitoring = esbMonitoringMapper.selectByInterfaceId(updateReqVO.getInterfaceId());
        EsbMonitoringDO monitoringDO = esbMonitoringConvert.convert(updateReqVO);

        if (existingMonitoring != null) {
            monitoringDO.setId(existingMonitoring.getId());
            // Ensure createTime and creator are not overwritten if they exist
            monitoringDO.setCreateTime(existingMonitoring.getCreateTime());
            monitoringDO.setCreator(existingMonitoring.getCreator());
            esbMonitoringMapper.updateById(monitoringDO);
        } else {
            // For new entries, creator and createTime will be handled by BaseDO
            esbMonitoringMapper.insert(monitoringDO);
        }
    }

    @Override
    public EsbMonitoringRespVO getMonitoringStatus(Long interfaceId) {
        EsbMonitoringDO monitoringDO = esbMonitoringMapper.selectByInterfaceId(interfaceId);
        if (monitoringDO == null) {
            return null;
        }
        EsbMonitoringRespVO respVO = esbMonitoringConvert.convert(monitoringDO);
        EsbInterfaceRespVO interfaceInfo = esbInterfaceService.getInterface(interfaceId); // Using service layer for consistency
        if (interfaceInfo != null) {
            respVO.setInterfaceName(interfaceInfo.getName());
            respVO.setInterfaceCode(interfaceInfo.getCode());
        }
        return respVO;
    }

    @Override
    public PageResult<EsbMonitoringRespVO> getMonitoringPage(EsbMonitoringPageReqVO pageReqVO) {
        // If pageReqVO.interfaceIds is null or empty, and no other interface-specific filters are present,
        // this will fetch all monitoring data. If specific interface filters are needed (e.g., by name),
        // we'd first query EsbInterfaceDOs. For now, assuming interfaceIds can be directly provided or all are fetched.
        // The mapper's selectPage already handles the collection of interfaceIds.
        PageResult<EsbMonitoringDO> pageResult = esbMonitoringMapper.selectPage(pageReqVO, pageReqVO.getInterfaceIds());
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }

        PageResult<EsbMonitoringRespVO> respPageResult = esbMonitoringConvert.convertPage(pageResult);

        // Populate interfaceName and interfaceCode
        List<Long> interfaceIdsFromPage = respPageResult.getList().stream()
                .map(EsbMonitoringRespVO::getInterfaceId)
                .distinct()
                .collect(Collectors.toList());

        if (!interfaceIdsFromPage.isEmpty()) {
            List<EsbInterfaceDO> interfaces = esbInterfaceMapper.selectBatchIds(interfaceIdsFromPage);
            Map<Long, EsbInterfaceDO> interfaceMap = interfaces.stream()
                    .collect(Collectors.toMap(EsbInterfaceDO::getId, i -> i));

            respPageResult.getList().forEach(monitoringResp -> {
                EsbInterfaceDO interfaceDO = interfaceMap.get(monitoringResp.getInterfaceId());
                if (interfaceDO != null) {
                    monitoringResp.setInterfaceName(interfaceDO.getName());
                    monitoringResp.setInterfaceCode(interfaceDO.getCode());
                }
            });
        }
        return respPageResult;
    }

    @Override
    public void performInterfaceCheck(Long interfaceId) {
        EsbInterfaceRespVO interfaceInfo = esbInterfaceService.getInterface(interfaceId);
        if (interfaceInfo == null) {
            log.warn("[performInterfaceCheck] Interface with ID {} not found. Skipping check.", interfaceId);
            return;
        }
        log.info("[performInterfaceCheck] Performing health check for interface ID: {}, Code: {}", interfaceId, interfaceInfo.getCode());
        // TODO: Implement actual health check logic (e.g., HTTP call, MLLP ping) based on interfaceInfo.getProtocol() etc.
        // This logic will be complex and specific to the protocol.
        // After the check, call recordMonitoringResult.
        // Example placeholder result:
        EsbMonitoringUpdateReqVO result = new EsbMonitoringUpdateReqVO();
        result.setInterfaceId(interfaceId);
        result.setLastCheckTime(java.time.LocalDateTime.now());
        // Simulate a successful check
        result.setIsAvailable(true);
        result.setStatusCode(200); // Example for HTTP
        result.setResponseTimeMilliseconds(150); // Example
        // recordMonitoringResult(result);
        log.info("[performInterfaceCheck] Placeholder: Check for interface ID {} completed. Would call recordMonitoringResult.", interfaceId);
    }

    @Override
    // @Scheduled(cron = "0 * * * * ?") // Example: Run every minute. Configure appropriately.
    public void schedulePeriodicChecks() {
        log.info("[schedulePeriodicChecks] Starting periodic health checks for all enabled interfaces.");
        // Assuming status 1 means enabled as in EsbInterfaceDO
        List<EsbInterfaceDO> enabledInterfaces = esbInterfaceMapper.selectList(
                new LambdaQueryWrapper<EsbInterfaceDO>().eq(EsbInterfaceDO::getStatus, 1)
        );

        if (enabledInterfaces.isEmpty()) {
            log.info("[schedulePeriodicChecks] No enabled interfaces found to check.");
            return;
        }

        log.info("[schedulePeriodicChecks] Found {} enabled interfaces to check.", enabledInterfaces.size());
        for (EsbInterfaceDO interfaceDO : enabledInterfaces) {
            try {
                log.debug("[schedulePeriodicChecks] Triggering check for interface ID: {}, Code: {}", interfaceDO.getId(), interfaceDO.getCode());
                performInterfaceCheck(interfaceDO.getId());
            } catch (Exception e) {
                log.error("[schedulePeriodicChecks] Error performing check for interface ID: {}. Error: {}", interfaceDO.getId(), e.getMessage(), e);
                // Optionally record a failure for this specific check attempt if performInterfaceCheck doesn't handle it
                EsbMonitoringUpdateReqVO errorResult = new EsbMonitoringUpdateReqVO();
                errorResult.setInterfaceId(interfaceDO.getId());
                errorResult.setLastCheckTime(java.time.LocalDateTime.now());
                errorResult.setIsAvailable(false);
                errorResult.setErrorMessage("Scheduler failed to dispatch check: " + e.getMessage());
                // recordMonitoringResult(errorResult);
                 log.info("[schedulePeriodicChecks] Placeholder: Check for interface ID {} failed. Would call recordMonitoringResult.", interfaceDO.getId());
            }
        }
        log.info("[schedulePeriodicChecks] Finished periodic health checks.");
    }
}

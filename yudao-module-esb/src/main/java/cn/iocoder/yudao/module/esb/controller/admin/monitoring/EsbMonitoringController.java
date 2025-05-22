package cn.iocoder.yudao.module.esb.controller.admin.monitoring;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringPageReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.monitoring.vo.EsbMonitoringRespVO;
import cn.iocoder.yudao.module.esb.service.monitoring.EsbMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ESB接口监控状态")
@RestController
@RequestMapping("/esb/monitoring")
@Validated
public class EsbMonitoringController {

    @Resource
    private EsbMonitoringService esbMonitoringService;

    @GetMapping("/get-status")
    @Operation(summary = "获得指定ESB接口的监控状态")
    @Parameter(name = "interfaceId", description = "接口编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:monitoring:query')")
    public CommonResult<EsbMonitoringRespVO> getMonitoringStatus(@RequestParam("interfaceId") Long interfaceId) {
        EsbMonitoringRespVO status = esbMonitoringService.getMonitoringStatus(interfaceId);
        return success(status);
    }

    @GetMapping("/page")
    @Operation(summary = "获得ESB接口监控状态分页")
    @PreAuthorize("@ss.hasPermission('esb:monitoring:query')")
    public CommonResult<PageResult<EsbMonitoringRespVO>> getMonitoringPage(@Valid EsbMonitoringPageReqVO pageVO) {
        PageResult<EsbMonitoringRespVO> pageResult = esbMonitoringService.getMonitoringPage(pageVO);
        return success(pageResult);
    }

    @PostMapping("/perform-check")
    @Operation(summary = "手动触发指定ESB接口的健康检查")
    @Parameter(name = "interfaceId", description = "接口编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:monitoring:check')")
    public CommonResult<Boolean> performInterfaceCheck(@RequestParam("interfaceId") Long interfaceId) {
        esbMonitoringService.performInterfaceCheck(interfaceId);
        return success(true);
    }

}

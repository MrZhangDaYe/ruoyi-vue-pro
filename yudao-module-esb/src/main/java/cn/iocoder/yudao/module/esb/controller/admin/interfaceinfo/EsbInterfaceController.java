package cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.*;
import cn.iocoder.yudao.module.esb.convert.interfaceinfo.EsbInterfaceConvert;
import cn.iocoder.yudao.module.esb.service.interfaceinfo.EsbInterfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - ESB接口信息")
@RestController
@RequestMapping("/esb/interface")
@Validated
public class EsbInterfaceController {

    @Resource
    private EsbInterfaceService esbInterfaceService;

    @Resource
    private EsbInterfaceConvert esbInterfaceConvert; // Typically not used directly in controller if service handles DTOs

    @PostMapping("/create")
    @Operation(summary = "创建ESB接口")
    @PreAuthorize("@ss.hasPermission('esb:interface:create')")
    public CommonResult<Long> createInterface(@Valid @RequestBody EsbInterfaceCreateReqVO createReqVO) {
        return success(esbInterfaceService.createInterface(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ESB接口")
    @PreAuthorize("@ss.hasPermission('esb:interface:update')")
    public CommonResult<Boolean> updateInterface(@Valid @RequestBody EsbInterfaceUpdateReqVO updateReqVO) {
        esbInterfaceService.updateInterface(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ESB接口")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:interface:delete')")
    public CommonResult<Boolean> deleteInterface(@RequestParam("id") Long id) {
        esbInterfaceService.deleteInterface(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ESB接口")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:interface:query')")
    public CommonResult<EsbInterfaceRespVO> getInterface(@RequestParam("id") Long id) {
        EsbInterfaceRespVO respVO = esbInterfaceService.getInterface(id);
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得ESB接口分页")
    @PreAuthorize("@ss.hasPermission('esb:interface:query')")
    public CommonResult<PageResult<EsbInterfaceRespVO>> getInterfacePage(@Valid EsbInterfacePageReqVO pageVO) {
        PageResult<EsbInterfaceRespVO> pageResult = esbInterfaceService.getInterfacePage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ESB接口 Excel")
    @PreAuthorize("@ss.hasPermission('esb:interface:export')")
    @OperateLog(type = EXPORT)
    public void exportInterfaceExcel(@Valid EsbInterfaceExportReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        List<EsbInterfaceRespVO> list = esbInterfaceService.getInterfaceList(exportReqVO);
        ExcelUtils.write(response, "ESB接口.xls", "数据", EsbInterfaceRespVO.class, list);
    }

    // =================== Camel Route Management Endpoints ===================

    @PostMapping("/start-route")
    @Operation(summary = "启动ESB接口路由")
    @Parameter(name = "id", description = "接口编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:interface:start')")
    public CommonResult<Boolean> startInterfaceRoute(@RequestParam("id") Long id) {
        esbInterfaceService.startInterfaceRoute(id);
        return success(true);
    }

    @PostMapping("/stop-route")
    @Operation(summary = "停止ESB接口路由")
    @Parameter(name = "id", description = "接口编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:interface:stop')")
    public CommonResult<Boolean> stopInterfaceRoute(@RequestParam("id") Long id) {
        esbInterfaceService.stopInterfaceRoute(id);
        return success(true);
    }

    @PostMapping("/refresh-route")
    @Operation(summary = "刷新ESB接口路由")
    @Parameter(name = "id", description = "接口编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:interface:refresh')")
    public CommonResult<Boolean> refreshInterfaceRoute(@RequestParam("id") Long id) {
        esbInterfaceService.refreshInterfaceRoute(id);
        return success(true);
    }

}

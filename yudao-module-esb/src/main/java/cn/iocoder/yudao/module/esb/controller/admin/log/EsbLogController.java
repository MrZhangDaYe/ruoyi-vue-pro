package cn.iocoder.yudao.module.esb.controller.admin.log;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogPageReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogRespVO;
import cn.iocoder.yudao.module.esb.service.log.EsbLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ESB接口日志")
@RestController
@RequestMapping("/esb/log")
@Validated
public class EsbLogController {

    @Resource
    private EsbLogService esbLogService;

    @GetMapping("/get")
    @Operation(summary = "获得ESB接口日志")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:log:query')")
    public CommonResult<EsbLogRespVO> getLog(@RequestParam("id") Long id) {
        EsbLogRespVO log = esbLogService.getLog(id);
        return success(log);
    }

    @GetMapping("/page")
    @Operation(summary = "获得ESB接口日志分页")
    @PreAuthorize("@ss.hasPermission('esb:log:query')")
    public CommonResult<PageResult<EsbLogRespVO>> getLogPage(@Valid EsbLogPageReqVO pageVO) {
        PageResult<EsbLogRespVO> pageResult = esbLogService.getLogPage(pageVO);
        return success(pageResult);
    }

}

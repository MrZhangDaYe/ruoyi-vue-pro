package cn.iocoder.yudao.module.esb.controller.admin.mapping;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.esb.controller.admin.mapping.vo.*;
import cn.iocoder.yudao.module.esb.service.mapping.EsbMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ESB接口数据映射")
@RestController
@RequestMapping("/esb/mapping")
@Validated
public class EsbMappingController {

    @Resource
    private EsbMappingService esbMappingService;

    @PostMapping("/create")
    @Operation(summary = "创建ESB接口数据映射")
    @PreAuthorize("@ss.hasPermission('esb:mapping:create')")
    public CommonResult<Long> createMapping(@Valid @RequestBody EsbMappingCreateReqVO createReqVO) {
        return success(esbMappingService.createMapping(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ESB接口数据映射")
    @PreAuthorize("@ss.hasPermission('esb:mapping:update')")
    public CommonResult<Boolean> updateMapping(@Valid @RequestBody EsbMappingUpdateReqVO updateReqVO) {
        esbMappingService.updateMapping(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ESB接口数据映射")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:mapping:delete')")
    public CommonResult<Boolean> deleteMapping(@RequestParam("id") Long id) {
        esbMappingService.deleteMapping(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ESB接口数据映射")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('esb:mapping:query')")
    public CommonResult<EsbMappingRespVO> getMapping(@RequestParam("id") Long id) {
        EsbMappingRespVO respVO = esbMappingService.getMapping(id);
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得ESB接口数据映射分页")
    @PreAuthorize("@ss.hasPermission('esb:mapping:query')")
    public CommonResult<PageResult<EsbMappingRespVO>> getMappingPage(@Valid EsbMappingPageReqVO pageVO) {
        PageResult<EsbMappingRespVO> pageResult = esbMappingService.getMappingPage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/list-by-interface")
    @Operation(summary = "获得指定接口的所有ESB接口数据映射列表")
    @Parameter(name = "interfaceId", description = "接口编号", required = true, example = "2048")
    @PreAuthorize("@ss.hasPermission('esb:mapping:query')")
    public CommonResult<List<EsbMappingRespVO>> getMappingsByInterfaceId(@RequestParam("interfaceId") Long interfaceId) {
        List<EsbMappingRespVO> list = esbMappingService.getMappingsByInterfaceId(interfaceId);
        return success(list);
    }

}

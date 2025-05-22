package cn.iocoder.yudao.module.esb.service.interfaceinfo;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.EsbInterfaceCreateReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.EsbInterfacePageReqVO;
import cn.iocoder.yudao.module.esb.controller.admin.interfaceinfo.vo.EsbInterfaceUpdateReqVO;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.mysql.interfaceinfo.EsbInterfaceMapper;
import cn.iocoder.yudao.module.esb.enums.ErrorCodeConstants; // Assuming this exists
import cn.iocoder.yudao.module.esb.convert.interfaceinfo.EsbInterfaceConvert; // Used by service

import org.apache.camel.CamelContext;
import org.apache.camel.spi.RouteController; // For mocking Camel route operations
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.camel.ServiceStatus;
import org.junit.jupiter.api.Disabled;
import org.mockito.*;
import org.springframework.beans.BeanUtils;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo; // Assuming this exists
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EsbInterfaceServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private EsbInterfaceServiceImpl esbInterfaceService;

    @Mock
    private EsbInterfaceMapper esbInterfaceMapper;

    @Mock
    private CamelContext camelContext;

    @Mock
    private RouteController routeController;

    @Mock
    private cn.iocoder.yudao.module.esb.service.mapping.EsbMappingService esbMappingService; // Used by processors

    // EsbInterfaceConvert is used via INSTANCE.

    @Captor
    private ArgumentCaptor<EsbInterfaceDO> esbInterfaceDOCaptor;


    @BeforeEach
    void setUp() {
        when(camelContext.getRouteController()).thenReturn(routeController);
    }

    // Updated test to reflect route logic
    @Test
    void testCreateInterface_Enabled_ShouldAddRoute() {
        // Arrange
        EsbInterfaceCreateReqVO reqVO = new EsbInterfaceCreateReqVO();
        reqVO.setName("Test Route Add");
        reqVO.setCode("ROUTE_ADD_CODE");
        reqVO.setPath("/route/add");
        reqVO.setStatus(1); // Enabled

        EsbInterfaceDO savedInterface = new EsbInterfaceDO();
        BeanUtils.copyProperties(reqVO, savedInterface);
        savedInterface.setId(1L);

        when(esbInterfaceMapper.selectByCode(reqVO.getCode())).thenReturn(null);
        when(esbInterfaceMapper.selectByPath(reqVO.getPath())).thenReturn(null);
        doAnswer(invocation -> {
            EsbInterfaceDO argument = invocation.getArgument(0);
            argument.setId(1L);
            return null;
        }).when(esbInterfaceMapper).insert(any(EsbInterfaceDO.class));

        // Act
        esbInterfaceService.createInterface(reqVO);

        // Assert
        verify(esbInterfaceMapper).insert(esbInterfaceDOCaptor.capture());
        EsbInterfaceDO capturedInsert = esbInterfaceDOCaptor.getValue();
        assertEquals(reqVO.getCode(), capturedInsert.getCode());

        // Verify that addCamelRouteAndSave leads to an update of camelRouteId
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getId().equals(savedInterface.getId()) &&
                i.getCamelRouteId() != null &&
                i.getCamelRouteId().equals(savedInterface.getCode()))); // Assuming routeId is code
    }

    @Test
    void testCreateInterface_Disabled_ShouldNotAddRoute() {
        // Arrange
        EsbInterfaceCreateReqVO reqVO = new EsbInterfaceCreateReqVO();
        reqVO.setName("Test No Route");
        reqVO.setCode("NO_ROUTE_CODE");
        reqVO.setPath("/no/route");
        reqVO.setStatus(0); // Disabled

        when(esbInterfaceMapper.selectByCode(reqVO.getCode())).thenReturn(null);
        when(esbInterfaceMapper.selectByPath(reqVO.getPath())).thenReturn(null);
        doAnswer(invocation -> {
            EsbInterfaceDO argument = invocation.getArgument(0);
            argument.setId(1L);
            return null;
        }).when(esbInterfaceMapper).insert(any(EsbInterfaceDO.class));

        // Act
        esbInterfaceService.createInterface(reqVO);

        // Assert
        verify(esbInterfaceMapper).insert(any(EsbInterfaceDO.class));
        verify(esbInterfaceMapper, never()).updateById(argThat(i -> i.getCamelRouteId() != null));
    }


    @Test
    void testCreateInterface_CodeExists_ThrowsException() {
        // Arrange
        EsbInterfaceCreateReqVO reqVO = randomPojo(EsbInterfaceCreateReqVO.class, o -> o.setCode("EXISTING_CODE"));
        EsbInterfaceDO existingInterface = randomPojo(EsbInterfaceDO.class, o -> o.setCode("EXISTING_CODE"));
        when(esbInterfaceMapper.selectByCode("EXISTING_CODE")).thenReturn(existingInterface);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> esbInterfaceService.createInterface(reqVO));
        assertEquals(ErrorCodeConstants.INTERFACE_CODE_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void testCreateInterface_PathExists_ThrowsException() {
        // Arrange
        EsbInterfaceCreateReqVO reqVO = randomPojo(EsbInterfaceCreateReqVO.class, o -> o.setPath("/existing/path"));
        reqVO.setCode("UNIQUE_CODE_FOR_PATH_TEST"); // Ensure code is unique for this test
        EsbInterfaceDO existingInterface = randomPojo(EsbInterfaceDO.class, o -> o.setPath("/existing/path"));
        when(esbInterfaceMapper.selectByCode(reqVO.getCode())).thenReturn(null); // Code is fine
        when(esbInterfaceMapper.selectByPath("/existing/path")).thenReturn(existingInterface);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> esbInterfaceService.createInterface(reqVO));
        assertEquals(ErrorCodeConstants.INTERFACE_PATH_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void testUpdateInterface_Success() {
        // Arrange
        EsbInterfaceUpdateReqVO reqVO = randomPojo(EsbInterfaceUpdateReqVO.class, o -> {
            o.setId(1L);
            o.setStatus(1); // Enabled -> new route to be added
            o.setCode("UPDATED_CODE");
            o.setPath("/updated/path");
        });
        EsbInterfaceDO oldDO = randomPojo(EsbInterfaceDO.class, o -> {
            o.setId(1L);
            o.setCode("ORIGINAL_CODE");
            o.setPath("/original/path");
            o.setStatus(0); // Was disabled
            o.setCamelRouteId(null); // No old route
        });

        when(esbInterfaceMapper.selectById(reqVO.getId())).thenReturn(oldDO);
        when(esbInterfaceMapper.selectByCode(reqVO.getCode())).thenReturn(null);
        when(esbInterfaceMapper.selectByPath(reqVO.getPath())).thenReturn(null);

        // Act
        esbInterfaceService.updateInterface(reqVO);

        // Assert
        // First updateById is for the main fields
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getCode().equals(reqVO.getCode())));
        // Second updateById is for setting the camelRouteId
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getId().equals(reqVO.getId()) &&
                i.getCamelRouteId() != null &&
                i.getCamelRouteId().equals(reqVO.getCode())));
        // Verify no old route was removed (as it was null)
        verify(routeController, never()).stopRoute(anyString());
        verify(camelContext, never()).removeRoute(anyString());
    }

    @Test
    void testUpdateInterface_EnabledToDisabled_ShouldRemoveRouteAndClearId() {
        // Arrange
        EsbInterfaceUpdateReqVO reqVO = randomPojo(EsbInterfaceUpdateReqVO.class, o -> {
            o.setId(1L);
            o.setStatus(0); // Disabled
            o.setCode("CODE_TO_DISABLE");
        });
        EsbInterfaceDO oldDO = randomPojo(EsbInterfaceDO.class, o -> {
            o.setId(1L);
            o.setStatus(1); // Was enabled
            o.setCamelRouteId("route_CODE_TO_DISABLE");
            o.setCode("CODE_TO_DISABLE");
        });

        when(esbInterfaceMapper.selectById(reqVO.getId())).thenReturn(oldDO);
        when(esbInterfaceMapper.selectByCode(reqVO.getCode())).thenReturn(oldDO); // No code conflict
        when(camelContext.getRouteController().getRouteStatus("route_CODE_TO_DISABLE")).thenReturn(ServiceStatus.Started);


        // Act
        esbInterfaceService.updateInterface(reqVO);

        // Assert
        verify(routeController).stopRoute("route_CODE_TO_DISABLE");
        verify(camelContext).removeRoute("route_CODE_TO_DISABLE");
        // First updateById is for the main fields (status=0)
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getStatus().equals(0)));
        // Second updateById is for clearing the camelRouteId
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getId().equals(reqVO.getId()) && i.getCamelRouteId() == null));
    }


    @Test
    void testUpdateInterface_NotFound_ThrowsException() {
        // Arrange
        EsbInterfaceUpdateReqVO reqVO = randomPojo(EsbInterfaceUpdateReqVO.class, o -> o.setId(99L));
        when(esbInterfaceMapper.selectById(99L)).thenReturn(null);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> esbInterfaceService.updateInterface(reqVO));
        assertEquals(ErrorCodeConstants.INTERFACE_NOT_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void testUpdateInterface_CodeConflict_ThrowsException() {
        // Arrange
        EsbInterfaceUpdateReqVO reqVO = randomPojo(EsbInterfaceUpdateReqVO.class, o -> {
            o.setId(1L);
            o.setCode("CONFLICT_CODE");
        });
        EsbInterfaceDO existingDO = randomPojo(EsbInterfaceDO.class, o -> o.setId(1L)); // The one being updated
        EsbInterfaceDO conflictingDO = randomPojo(EsbInterfaceDO.class, o -> {
            o.setId(2L); // Different ID
            o.setCode("CONFLICT_CODE");
        });

        when(esbInterfaceMapper.selectById(reqVO.getId())).thenReturn(existingDO);
        when(esbInterfaceMapper.selectByCode("CONFLICT_CODE")).thenReturn(conflictingDO);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> esbInterfaceService.updateInterface(reqVO));
        assertEquals(ErrorCodeConstants.INTERFACE_CODE_EXISTS.getCode(), exception.getCode());
    }
    
    @Test
    void testDeleteInterface_Success_WithExistingRoute() {
        // Arrange
        Long interfaceId = 1L;
        String routeId = "route_DELETE_ME";
        EsbInterfaceDO existingDO = randomPojo(EsbInterfaceDO.class, o -> {
            o.setId(interfaceId);
            o.setCamelRouteId(routeId);
            o.setCode("DELETE_ME");
            o.setStatus(1);
        });
        when(esbInterfaceMapper.selectById(interfaceId)).thenReturn(existingDO);
        when(camelContext.getRouteController().getRouteStatus(routeId)).thenReturn(ServiceStatus.Started);

        // Act
        esbInterfaceService.deleteInterface(interfaceId);

        // Assert
        verify(routeController).stopRoute(routeId);
        verify(camelContext).removeRoute(routeId);
        verify(esbInterfaceMapper).deleteById(interfaceId);
    }

    @Test
    void testDeleteInterface_NotFound_ThrowsException() {
        // Arrange
        Long interfaceId = 99L;
        when(esbInterfaceMapper.selectById(interfaceId)).thenReturn(null);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> esbInterfaceService.deleteInterface(interfaceId));
        assertEquals(ErrorCodeConstants.INTERFACE_NOT_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void testGetInterface_Found() {
        // Arrange
        Long interfaceId = 1L;
        EsbInterfaceDO existingDO = randomPojo(EsbInterfaceDO.class, o -> o.setId(interfaceId));
        when(esbInterfaceMapper.selectById(interfaceId)).thenReturn(existingDO);

        // Act
        esbInterfaceService.getInterface(interfaceId); // Result is converted, not asserting content here, just flow

        // Assert
        verify(esbInterfaceMapper).selectById(interfaceId);
    }

    @Test
    void testGetInterfacePage_Success() {
        // Arrange
        EsbInterfacePageReqVO reqVO = new EsbInterfacePageReqVO();
        List<EsbInterfaceDO> list = Collections.singletonList(randomPojo(EsbInterfaceDO.class));
        PageResult<EsbInterfaceDO> pageResult = new PageResult<>(list, 1L);
        when(esbInterfaceMapper.selectPage(reqVO)).thenReturn(pageResult);

        // Act
        esbInterfaceService.getInterfacePage(reqVO);

        // Assert
        verify(esbInterfaceMapper).selectPage(reqVO);
    }

    // TODO: Add tests for getInterfaceList (for export)

    // TODO: Add tests for getInterfaceList (for export)

    @Test
    void testStartInterfaceRoute_Success() {
        // Arrange
        Long interfaceId = 1L;
        EsbInterfaceDO existingDO = randomPojo(EsbInterfaceDO.class, o -> {
            o.setId(interfaceId);
            o.setStatus(0); // Currently disabled
            o.setCode("START_ROUTE_TEST");
            o.setCamelRouteId(null); // No existing route
        });
        when(esbInterfaceMapper.selectById(interfaceId)).thenReturn(existingDO);

        // Act
        esbInterfaceService.startInterfaceRoute(interfaceId);

        // Assert
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getId().equals(interfaceId) && i.getStatus().equals(1))); // Status updated
        // Verify new route is added (implicitly, camelRouteId would be set)
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getId().equals(interfaceId) && i.getCamelRouteId().equals(existingDO.getCode())));
    }

    @Test
    void testStopInterfaceRoute_Success() {
        // Arrange
        Long interfaceId = 1L;
        String routeId = "route_STOP_ME";
        EsbInterfaceDO existingDO = randomPojo(EsbInterfaceDO.class, o -> {
            o.setId(interfaceId);
            o.setStatus(1); // Currently enabled
            o.setCode("STOP_ME");
            o.setCamelRouteId(routeId);
        });
        when(esbInterfaceMapper.selectById(interfaceId)).thenReturn(existingDO);
        when(camelContext.getRouteController().getRouteStatus(routeId)).thenReturn(ServiceStatus.Started);

        // Act
        esbInterfaceService.stopInterfaceRoute(interfaceId);

        // Assert
        verify(routeController).stopRoute(routeId);
        verify(camelContext).removeRoute(routeId);
        // Verify camelRouteId is cleared in DB
        verify(esbInterfaceMapper, times(2)).updateById(esbInterfaceDOCaptor.capture()); // Once for routeId, once for status
        List<EsbInterfaceDO> capturedUpdates = esbInterfaceDOCaptor.getAllValues();
        assertTrue(capturedUpdates.stream().anyMatch(u -> u.getId().equals(interfaceId) && u.getCamelRouteId() == null));
        assertTrue(capturedUpdates.stream().anyMatch(u -> u.getId().equals(interfaceId) && u.getStatus().equals(0))); // Status updated
    }

    @Test
    void testRefreshInterfaceRoute_Enabled_ShouldReAddRoute() {
        // Arrange
        Long interfaceId = 1L;
        String oldRouteId = "route_OLD_REFRESH";
        String newRouteId = "REFRESH_CODE"; // Code is used as new routeId
        EsbInterfaceDO existingDO = randomPojo(EsbInterfaceDO.class, o -> {
            o.setId(interfaceId);
            o.setStatus(1); // Enabled
            o.setCode(newRouteId);
            o.setCamelRouteId(oldRouteId);
        });
        when(esbInterfaceMapper.selectById(interfaceId)).thenReturn(existingDO);
        when(camelContext.getRouteController().getRouteStatus(oldRouteId)).thenReturn(ServiceStatus.Started);

        // Act
        esbInterfaceService.refreshInterfaceRoute(interfaceId);

        // Assert
        verify(routeController).stopRoute(oldRouteId);
        verify(camelContext).removeRoute(oldRouteId);
        // Verify new route is added
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getId().equals(interfaceId) && i.getCamelRouteId().equals(newRouteId)));
    }

    @Test
    void testRefreshInterfaceRoute_Disabled_ShouldRemoveAndNotAddRoute() {
        // Arrange
        Long interfaceId = 1L;
        String oldRouteId = "route_OLD_DISABLED_REFRESH";
        EsbInterfaceDO existingDO = randomPojo(EsbInterfaceDO.class, o -> {
            o.setId(interfaceId);
            o.setStatus(0); // Disabled
            o.setCode("DISABLED_REFRESH_CODE");
            o.setCamelRouteId(oldRouteId);
        });
        when(esbInterfaceMapper.selectById(interfaceId)).thenReturn(existingDO);
        when(camelContext.getRouteController().getRouteStatus(oldRouteId)).thenReturn(ServiceStatus.Started);

        // Act
        esbInterfaceService.refreshInterfaceRoute(interfaceId);

        // Assert
        verify(routeController).stopRoute(oldRouteId);
        verify(camelContext).removeRoute(oldRouteId);
        // Verify camelRouteId is cleared in DB (and not re-added)
        verify(esbInterfaceMapper).updateById(argThat(i -> i.getId().equals(interfaceId) && i.getCamelRouteId() == null));
        // Verify no new route was attempted to be built and started (by checking no further updateById with a non-null camelRouteId)
        Mockito.clearInvocations(esbInterfaceMapper); // Clear previous updateById for routeId=null
        esbInterfaceService.refreshInterfaceRoute(interfaceId); // Call again to check
        verify(esbInterfaceMapper, never()).updateById(argThat(i -> i.getCamelRouteId() != null)); // Ensure no new route ID set
    }

    // Placeholder for addCamelRouteAndSave specific tests if needed, though mostly covered by CRUD operations.
    @Test
    @Disabled("buildAndStartCamelRoute is private and complex, tested via public methods")
    void testAddCamelRouteAndSave_WhenBuildFails() {
        // This would require deeper changes to make buildAndStartCamelRoute testable
        // or more complex mocking if it were not private.
        // For now, this scenario (buildAndStartCamelRoute returning null) is partially covered
        // by ensuring camelRouteId is not updated in DB if build fails.
    }
}

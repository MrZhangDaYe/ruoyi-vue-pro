package cn.iocoder.yudao.module.esb.listener;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.dal.mysql.interfaceinfo.EsbInterfaceMapper;
import cn.iocoder.yudao.module.esb.service.interfaceinfo.EsbInterfaceService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.ApplicationContext; // Import ApplicationContext

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class EsbRouteApplicationListenerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private EsbRouteApplicationListener esbRouteApplicationListener;

    @Mock
    private EsbInterfaceMapper esbInterfaceMapper;

    @Mock
    private EsbInterfaceService esbInterfaceService;

    @Mock
    private ContextRefreshedEvent mockEvent;

    @Mock
    private ApplicationContext mockApplicationContext;


    @Test
    void onApplicationEvent_WithEnabledInterfaces_ShouldAddRoutes() {
        // Arrange
        EsbInterfaceDO interface1 = new EsbInterfaceDO(); interface1.setId(1L); interface1.setCode("IFACE1"); interface1.setStatus(1);
        EsbInterfaceDO interface2 = new EsbInterfaceDO(); interface2.setId(2L); interface2.setCode("IFACE2"); interface2.setStatus(1);
        List<EsbInterfaceDO> enabledInterfaces = Arrays.asList(interface1, interface2);

        when(mockEvent.getApplicationContext()).thenReturn(mockApplicationContext);
        when(mockApplicationContext.getParent()).thenReturn(null); // Root context
        when(esbInterfaceMapper.selectList(eq(EsbInterfaceDO::getStatus), eq(1))).thenReturn(enabledInterfaces);

        // Act
        esbRouteApplicationListener.onApplicationEvent(mockEvent);

        // Assert
        verify(esbInterfaceService, times(1)).addCamelRouteAndSave(interface1);
        verify(esbInterfaceService, times(1)).addCamelRouteAndSave(interface2);
    }

    @Test
    void onApplicationEvent_NoEnabledInterfaces_ShouldNotAddRoutes() {
        // Arrange
        when(mockEvent.getApplicationContext()).thenReturn(mockApplicationContext);
        when(mockApplicationContext.getParent()).thenReturn(null); // Root context
        when(esbInterfaceMapper.selectList(eq(EsbInterfaceDO::getStatus), eq(1))).thenReturn(Collections.emptyList());

        // Act
        esbRouteApplicationListener.onApplicationEvent(mockEvent);

        // Assert
        verify(esbInterfaceService, never()).addCamelRouteAndSave(any(EsbInterfaceDO.class));
    }
    
    @Test
    void onApplicationEvent_NotRootContext_ShouldDoNothing() {
        // Arrange
        when(mockEvent.getApplicationContext()).thenReturn(mockApplicationContext);
        when(mockApplicationContext.getParent()).thenReturn(mock(ApplicationContext.class)); // Not root context

        // Act
        esbRouteApplicationListener.onApplicationEvent(mockEvent);

        // Assert
        verifyNoInteractions(esbInterfaceMapper);
        verifyNoInteractions(esbInterfaceService);
    }
}

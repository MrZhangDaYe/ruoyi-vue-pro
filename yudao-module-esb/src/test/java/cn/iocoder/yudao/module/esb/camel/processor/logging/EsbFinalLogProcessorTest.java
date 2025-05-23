package cn.iocoder.yudao.module.esb.camel.processor.logging;

import cn.iocoder.yudao.module.esb.camel.util.EsbLogBuilder;
import cn.iocoder.yudao.module.esb.controller.admin.log.vo.EsbLogCreateReqVO;
import cn.iocoder.yudao.module.esb.dal.dataobject.interfaceinfo.EsbInterfaceDO;
import cn.iocoder.yudao.module.esb.service.log.EsbLogService;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EsbFinalLogProcessorTest {

    @Mock
    private Exchange mockExchange;

    @Mock
    private EsbLogService mockEsbLogService;

    private EsbLogBuilder realLogBuilder;
    private EsbInterfaceDO interfaceDO;
    private EsbFinalLogProcessor finalLogProcessor;

    @BeforeEach
    void setUp() {
        interfaceDO = new EsbInterfaceDO();
        interfaceDO.setId(1L);
        interfaceDO.setCode("TEST_IF001");

        realLogBuilder = new EsbLogBuilder(interfaceDO.getId(), interfaceDO.getCode());
        // Simulate EsbInitialLogProcessor has run
        realLogBuilder.requestTime(LocalDateTime.now().minusSeconds(10)); 

        finalLogProcessor = new EsbFinalLogProcessor(mockEsbLogService);

        when(mockExchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class)).thenReturn(interfaceDO);
        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class))
            .thenReturn(realLogBuilder);
    }

    @Test
    void process_SuccessfulExchange_ShouldSaveLog() throws Exception {
        // Arrange
        // Simulate EsbPostForwardLogProcessor has set success
        realLogBuilder.success(true);
        realLogBuilder.responseTime(LocalDateTime.now().minusSeconds(1)); // Manually set response time for test

        when(mockExchange.getException()).thenReturn(null);

        // Act
        finalLogProcessor.process(mockExchange);

        // Assert
        ArgumentCaptor<EsbLogCreateReqVO> logVOCaptor = ArgumentCaptor.forClass(EsbLogCreateReqVO.class);
        verify(mockEsbLogService).createLog(logVOCaptor.capture());
        EsbLogCreateReqVO capturedLog = logVOCaptor.getValue();

        assertTrue(capturedLog.getSuccess());
        assertNull(capturedLog.getErrorMessage());
        assertNotNull(capturedLog.getDurationMilliseconds());
        assertTrue(capturedLog.getDurationMilliseconds() > 0); // Should be around 9000ms based on setup
        assertEquals(interfaceDO.getId(), capturedLog.getInterfaceId());
    }

    @Test
    void process_ExchangeWithException_ShouldSaveLogAsFailure() throws Exception {
        // Arrange
        Exception testException = new RuntimeException("Test Camel Exception");
        when(mockExchange.getException()).thenReturn(testException);
        realLogBuilder.responseTime(LocalDateTime.now().minusSeconds(1)); // Manually set for duration calc

        // Act
        finalLogProcessor.process(mockExchange);

        // Assert
        ArgumentCaptor<EsbLogCreateReqVO> logVOCaptor = ArgumentCaptor.forClass(EsbLogCreateReqVO.class);
        verify(mockEsbLogService).createLog(logVOCaptor.capture());
        EsbLogCreateReqVO capturedLog = logVOCaptor.getValue();

        assertFalse(capturedLog.getSuccess());
        assertNotNull(capturedLog.getErrorMessage());
        assertTrue(capturedLog.getErrorMessage().contains("Test Camel Exception"));
        assertTrue(capturedLog.getErrorMessage().contains(ExceptionUtils.getStackTrace(testException).substring(0,100))); // Check part of stack trace
        assertNotNull(capturedLog.getDurationMilliseconds());
    }

    @Test
    void process_NullLogBuilderAndInterfaceDO_ShouldNotCallLogService() throws Exception {
        // Arrange
        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class)).thenReturn(null);
        when(mockExchange.getProperty("esbInterfaceDO", EsbInterfaceDO.class)).thenReturn(null);
        
        // Act
        finalLogProcessor.process(mockExchange);

        // Assert
        verify(mockEsbLogService, never()).createLog(any(EsbLogCreateReqVO.class));
    }
    
    @Test
    void process_NullLogBuilderButInterfaceDOPresent_ShouldAttemptMinimalLogForError() throws Exception {
        // Arrange
        when(mockExchange.getProperty(EsbInitialLogProcessor.ESB_LOG_BUILDER_PROPERTY, EsbLogBuilder.class)).thenReturn(null);
        // interfaceDO is still available from setUp() via mockExchange.getProperty("esbInterfaceDO")
        Exception testException = new IllegalStateException("Early failure");
        when(mockExchange.getException()).thenReturn(testException); // Exception occurred

        // Act
        finalLogProcessor.process(mockExchange);

        // Assert
        ArgumentCaptor<EsbLogCreateReqVO> logVOCaptor = ArgumentCaptor.forClass(EsbLogCreateReqVO.class);
        verify(mockEsbLogService).createLog(logVOCaptor.capture());
        EsbLogCreateReqVO capturedLog = logVOCaptor.getValue();

        assertEquals(interfaceDO.getId(), capturedLog.getInterfaceId());
        assertEquals(interfaceDO.getCode(), capturedLog.getInterfaceCode());
        assertFalse(capturedLog.getSuccess());
        assertNotNull(capturedLog.getErrorMessage());
        assertTrue(capturedLog.getErrorMessage().contains("Early failure"));
        assertNotNull(capturedLog.getRequestTime()); // Should be set to 'now' by the processor
        assertNotNull(capturedLog.getResponseTime()); // Should be set to 'now'
        assertNotNull(capturedLog.getDurationMilliseconds()); // Should be small or zero
    }
    
    @Test
    void process_SuccessNotSetAndNoException_ShouldDefaultToSuccessTrue() throws Exception {
        // Arrange
        // realLogBuilder.success() is not called, so it's null
        realLogBuilder.responseTime(LocalDateTime.now().minusSeconds(1));
        when(mockExchange.getException()).thenReturn(null);

        // Act
        finalLogProcessor.process(mockExchange);
        
        // Assert
        ArgumentCaptor<EsbLogCreateReqVO> logVOCaptor = ArgumentCaptor.forClass(EsbLogCreateReqVO.class);
        verify(mockEsbLogService).createLog(logVOCaptor.capture());
        EsbLogCreateReqVO capturedLog = logVOCaptor.getValue();
        
        assertTrue(capturedLog.getSuccess()); // Defaulted to true
    }
}

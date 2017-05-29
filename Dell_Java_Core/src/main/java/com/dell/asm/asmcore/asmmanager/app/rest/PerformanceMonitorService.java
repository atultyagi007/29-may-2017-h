package com.dell.asm.asmcore.asmmanager.app.rest;



import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import javax.ws.rs.Path;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.perfmonitoring.IPerformanceMonitorService;
import com.dell.asm.asmcore.asmmanager.client.perfmonitoring.PerformanceMetric;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.i18n2.exception.AsmRuntimeException;


@Path("/PerformanceMetric")
public class PerformanceMonitorService implements IPerformanceMonitorService { 
    private static final Logger logger = Logger.getLogger(PerformanceMonitorService.class);

@Override
public PerformanceMetric[] getDevicePerformanceMonitoring(String refId, String duration, String time) {
    try {
        String required = "System_Board_CPU_Usage,System_Board_IO_Usage,System_Board_MEM_Usage,System_Board_SYS_Usage";
        return ProxyUtil.getAsmDeployerProxy().performanceMonitoring(refId, duration, time, required);

    } catch (WebApplicationException wex) {
        throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(refId));
    } catch (Exception e) {
        logger.error("Error occurred in getPerformanceMonitoringMethod:", e);
        throw new AsmRuntimeException(AsmManagerMessages.internalError(), e);
    }

}
}



package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.ApplicationContextHolder;
import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.util.discovery.DeviceTypeCheckUtil;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryProviders;
import com.dell.asm.asmcore.asmmanager.util.discovery.IDiscoveryProvider;
import com.dell.asm.asmcore.asmmanager.util.discovery.InfrastructureDevice;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;

/**
 * Worked thread to invoke chassis or server RA to discover
 *
 * @author Bapu_Patil
 *
 */
public class DiscoverDeviceCallable implements Callable<DiscoveredDevices> {

    InfrastructureDevice device;
    String parentJob;
    DiscoverDeviceType deviceType;

    private static final Logger logger = Logger.getLogger(DiscoverDeviceCallable.class);

    public DiscoverDeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DiscoverDeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public DiscoverDeviceCallable(InfrastructureDevice deviceInfo, String jobName) {
        this.device = deviceInfo;
        parentJob = jobName;
        this.device.setParentJob(parentJob);
    }

    @Override
    public DiscoveredDevices call() throws Exception
    {
        logger.info( "Discovering device at IP: " + device.getIpAddress());
        IDiscoveryProvider discoveryProvider = null;

        DiscoveryProviders discoveryProviders = ApplicationContextHolder.getContext().getBean(DiscoveryProviders.class);

        // if call from inventory we already know device type.
        // Exception is for servers/ioms discovery triggered by chassis inventory job
        if (!device.isFromInventoryJob() || getDeviceType()==null) {
            DiscoverDeviceType dt = DeviceTypeCheckUtil.checkDeviceType(device);
            setDeviceType(dt);
            device.setDeviceType(dt);
        }

        for (IDiscoveryProvider dDevice : discoveryProviders.getDiscoverableDevices()) {
            if (dDevice.getDiscoverDeviceType().equals(getDeviceType())) {
                discoveryProvider = dDevice;
                break;
            }
        }

        if (discoveryProvider==null) {
            String msg = " Device IP " + device.getIpAddress() + " unknown. Captured discovery response: " + device.getDiscoveryResponse();
            logger.info(msg);
            DiscoveredDevices result = getDefaultResult();
            EEMILocalizableMessage eemiMessage = AsmManagerMessages.unsupportedDeviceType(device.getIpAddress());
            result.setStatusMessage(eemiMessage.getDisplayMessage().localize());
            result.setStatus(DiscoveryStatus.UNSUPPORTED);
            return result;
        }

        return discoveryProvider.discoverDevices(device);
    }

    private DiscoveredDevices getDefaultResult() {
        DiscoveredDevices result = new DiscoveredDevices();
        result.setParentJobId(parentJob);
        result.setUnmanaged(device.isUnmanaged());
        result.setReserved(device.isReserved());
        result.setServerPoolId(device.getServerPoolId());
        result.setConfig(device.getConfig());
        result.setDiscoverDeviceType(device.getDeviceType());
        result.setIpAddress(device.getIpAddress());
        result.setDeviceType(DeviceType.unknown);
        return result;
    }

}

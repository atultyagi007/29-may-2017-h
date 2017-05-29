package com.dell.asm.asmcore.asmmanager.tasks;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.MapRazorNodeNameToSerialNumberDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.MapRazorNodeNameToSerialNumberEntity;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorDevice;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorUtil;
import com.dell.pg.orion.jobmgr.JobStatus;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Recurring job that runs inventory on all devices and checks their compliance if they have been configured.
 * Job scheduled by the JRAF core Job Manager
 *
 * @author Ayesha Iqbal
 */

public class RazorSyncJob extends AsmDefaultJob {
    public static final String RECURRING_RAZOR_JOB_KEY_NAME = "Razor.JobKey.name";
    public static final String RECURRING_RAZOR_JOB_KEY_GROUP = "Razor.JobKey.group";

    private static final Logger logger = Logger.getLogger(RazorSyncJob.class);

    private DeviceInventoryDAO deviceInventoryDAO;

    @Override
    protected void executeSafely(JobExecutionContext context) {
        logger.debug(" Executing Razor Sync Job");
        initializeFromJobContext(context);

        JobKey jobKey = context.getJobDetail().getKey();
        addJobDetail(RECURRING_RAZOR_JOB_KEY_GROUP, jobKey.getGroup());
        addJobDetail(RECURRING_RAZOR_JOB_KEY_NAME, jobKey.getName());
        setJobStatus(JobStatus.IN_PROGRESS);

        try {
            List<? extends Trigger> triggers = getJobManager().getTriggersOfJob(jobKey);
            if (triggers != null && triggers.size() > 0) {
                logger.info("Next time razor sync job is run: " + triggers.get(0).getNextFireTime());
            }
        } catch (SchedulerException e1) {
            logger.error("Unable to get job info for razor sync job", e1);
        }

        SortedMap<String, RazorDevice> razorDevices;
        try {
            razorDevices = RazorUtil.getRazorDevicesHelper();
            if (razorDevices != null) {
                for (String key : razorDevices.keySet()) {
                    RazorDevice device = razorDevices.get(key);
                    if (device != null && device.getFacts() != null && device.getFacts().size() > 0) {

                        String sNodeName = key;
                        String sSerialNumber = device.getFacts().get("serialnumber");
                        String sManufacturer = device.getFacts().get("manufacturer");
                        String sIP = device.getFacts().get("ipaddress");
                        String uuid = device.getFacts().get("uuid");

                        logger.debug("RazorSyncJob: nodeName = " + sNodeName);
                        logger.debug("RazorSyncJob: manufacturer = " + sManufacturer);
                        logger.debug("RazorSyncJob: serialnumber = " + sSerialNumber);
                        logger.debug("RazorSyncJob: sIP = " + sIP);
                        logger.debug("RazorSyncJob: UUID = " + uuid);
                        if (StringUtils.isEmpty(uuid)) {
                            uuid = UUID.randomUUID().toString();
                            logger.info("No UUID found for razor device " + sSerialNumber + "; using " + uuid);
                        }

                        if (sNodeName != null && sNodeName.length() > 0 && sSerialNumber != null && sSerialNumber.length() > 0 && sManufacturer != null && sManufacturer.length() > 0) {
                            // Create a record in ASM DB for mapping serviceTag( or serial number for UCS/VMWare ) to a razor node.
                            MapRazorNodeNameToSerialNumberEntity entityInDB = MapRazorNodeNameToSerialNumberDAO.getInstance().getRazorNodeBySerialNumber(sSerialNumber);
                            if (entityInDB == null) // create a new record ONLY if it does not already exist
                            {
                                MapRazorNodeNameToSerialNumberEntity entity = new MapRazorNodeNameToSerialNumberEntity();
                                entity.setId(sNodeName);
                                entity.setSerialNumber(sSerialNumber);
                                logger.info("RazorSyncJob: Adding map " + sNodeName + " : " + sSerialNumber + " to razorNodeNameToSerialNumber table");
                                MapRazorNodeNameToSerialNumberDAO.getInstance().createRazorNode(entity);
                            } else {
                                logger.debug("RazorSyncJob: Razor node with ID " + sNodeName + " already exists in ASM MAP table");
                            }

                            String sManufacturerForComparison = sManufacturer.toLowerCase();
                            if (sManufacturerForComparison.indexOf("dell") >= 0 || sManufacturerForComparison.indexOf("vmware") >= 0 || sManufacturerForComparison.indexOf("microsoft") >= 0) {
                                logger.debug("Skipping node " + key + " since manufacturer is " + sManufacturer);
                            } else {
                                DeviceInventoryEntity deviceEntityInDB = getDeviceInventoryDAO().getDeviceInventoryByServiceTag(sSerialNumber);
                                if (deviceEntityInDB == null) {
                                    DeviceInventoryEntity deviceEntity = new DeviceInventoryEntity();
                                    deviceEntity.setDeviceType(DeviceType.Server);
                                    deviceEntity.setIpAddress(sIP);
                                    deviceEntity.setModel(device.getFacts().get("productname"));
                                    deviceEntity.setVendor(sManufacturer);
                                    deviceEntity.setRefId(uuid);
                                    deviceEntity.setServiceTag(sSerialNumber);
                                    deviceEntity.setHealth(DeviceHealth.UNKNOWN); // should change this to an enum

                                    logger.info("RazorSyncJob: Adding server " + sNodeName + ", " + sSerialNumber + ", " + sManufacturer + " to DeviceInventory table");
                                    getDeviceInventoryDAO().createDeviceInventory(deviceEntity);

                                } else {
                                    if (device.getIpAddress().compareToIgnoreCase(sIP) != 0) {
                                        deviceEntityInDB.setIpAddress(sIP);
                                        getDeviceInventoryDAO().updateDeviceInventory(deviceEntityInDB);
                                    }
                                }
                            }

                        }
                    }
                }

            }

        } catch (Exception e) {
            logger.error("Unable to get devices from razor", e);
        }

        setJobStatus(JobStatus.SUCCESSFUL);
        logger.debug("scheduled razor sync job successful for the job name:" + getJobName());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public DeviceInventoryDAO getDeviceInventoryDAO() {
        if (deviceInventoryDAO == null) {
            deviceInventoryDAO = new DeviceInventoryDAO();
        }
        return deviceInventoryDAO;
    }

    public void setDeviceInventoryDAO(DeviceInventoryDAO deviceInventoryDAO) {
        this.deviceInventoryDAO = deviceInventoryDAO;
    }
}

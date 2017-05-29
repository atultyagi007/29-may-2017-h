package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;


@XmlRootElement(name = "FirmwareUpdateRequest")
@ApiModel()
public class FirmwareUpdateRequest implements Serializable
{
	public enum UpdateType { SERVICE, DEVICE };
	private static final long serialVersionUID = -3765575454464568518L;
    private List<String> idList;

    //updatenow, nextreboot, forcereboot, schedule
    private String scheduleType;
    
    private boolean exitMaintenanceMode;
    
    private Date scheduleDate;
    
    private UpdateType updateType;

	public List<String> getIdList() {
		return idList;
	}

	public void setIdList(List<String> idList) {
		this.idList = idList;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public boolean isExitMaintenanceMode() {
		return exitMaintenanceMode;
	}

	public void setExitMaintenanceMode(boolean exitMaintenanceMode) {
		this.exitMaintenanceMode = exitMaintenanceMode;
	}

	public Date getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(Date scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public void setUpdateType(UpdateType updateType) {
		this.updateType = updateType;
	}   
}
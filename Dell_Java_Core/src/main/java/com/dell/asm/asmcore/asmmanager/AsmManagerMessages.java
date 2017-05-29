/**************************************************************************
 *   Copyright (c) 2013 - 2015 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager;

import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.i18n2.EEMILocalizableMessage.EEMICategory;
import com.dell.asm.i18n2.EEMILocalizableMessage.EEMISeverity;
import com.dell.asm.i18n2.LocalizableMessage;
import com.dell.asm.i18n2.ResourceBundleLocalizableMessage;

public final class AsmManagerMessages {
	
    private AsmManagerMessages() {
    }

    public enum MsgCodes {
        ASM0001, ASM0002, ASM0003, ASM0004, ASM0005, ASM0006, ASM0007, ASM0008, ASM0009, ASM0010,
        ASM0011, ASM0012, ASM0013, ASM0014, ASM0015, ASM0016, ASM0017, ASM0018, ASM0019, ASM0020,
        ASM0021, ASM0022, ASM0023, ASM0024, ASM0025, ASM0026, ASM0027, ASM0028, ASM0029, ASM0030,
        ASM0031, ASM0032, ASM0033, ASM0034, ASM0035, ASM0036, ASM0037, ASM0038, ASM0039, ASM0040,
        ASM0041, ASM0042, ASM0043, ASM0044, ASM0045, ASM0046, ASM0047, ASM0048, ASM0049, ASM0050,
        ASM0051, ASM0052, ASM0053, ASM0054, ASM0055, ASM0056, ASM0057, ASM0058, ASM0059, ASM0060,
        ASM0061, ASM0062, ASM0063, ASM0064, ASM0065, ASM0066, ASM0067, ASM0068, ASM0069, ASM0070,
        ASM0071, ASM0072, ASM0073, ASM0074, ASM0075, ASM0076, ASM0077, ASM0078, ASM0079, ASM0080,
        ASM0081, ASM0082, ASM0083, ASM0084, ASM0085, ASM0086, ASM0087, ASM0088, ASM0089, ASM0090,
        ASM0091, ASM0092, ASM0093, ASM0094, ASM0095, ASM0096, ASM0097, ASM0098, ASM0099, ASM00100,
        ASM00101, ASM00102, ASM00103, ASM00104, ASM00105, ASM00106, ASM00107, ASM00108, ASM00109,
        ASM00110, ASM00111, ASM00112, ASM00113, ASM00114, ASM00115, ASM00116, ASM00117, ASM00118,
        ASM00119, ASM00120, ASM00122, ASM00123, ASM00124, ASM00125, ASM00126, ASM00127,
        ASM00128, ASM00129, ASM00130, ASM00131, ASM00132, ASM00133, ASM00134, ASM00135, ASM00136,
        ASM00137, ASM00138, ASM00139, ASM00140, ASM00141, ASM00142, ASM00143, ASM00144, ASM00145,
        ASM00146, ASM00148, ASM00149, ASM00150, ASM00151, ASM00152, ASM00153, ASM00154,
        ASM00155, ASM00156, ASM00157, ASM00158, ASM00159, ASM00160, ASM00161, ASM00162, ASM00163,
        ASM00164, ASM00165, ASM00166, ASM00167, ASM00168, ASM00169, ASM00170, ASM00171, ASM00172,
        ASM00173, ASM00174, ASM00175, ASM00176, ASM00177, ASM00178, ASM00179, ASM00180, ASM00181,
        ASM00182, ASM00183, ASM00184, ASM00185, ASM00186, ASM00187, ASM00188, ASM00189, ASM00190, ASM00191,
        ASM00192, ASM00193, ASM00194, ASM00195, ASM00196, ASM00197, ASM00198, ASM00199, ASM00200,
        ASM00201, ASM00202, ASM00203, ASM00204, ASM00205, ASM00206, ASM00207, ASM00208, ASM00209,
        ASM00210, ASM00211, ASM00212, ASM00213, ASM00214, ASM00215, ASM00216, ASM00217, ASM00218,
        ASM00219, ASM00220, ASM00221, ASM00222, ASM00223, ASM00224, ASM00225, ASM00226, ASM00227,
        ASM00229, ASM00228, ASM00230, ASM00231, ASM00232, ASM00233, ASM00234, ASM00235, ASM00236,
        ASM00237, ASM00238, ASM00239, ASM00240, ASM00241,ASM00242, ASM00243, ASM00244, ASM00245,
        ASM00246, ASM00247, ASM00248, ASM00249, ASM00250, ASM00251, ASM00252, ASM00253, ASM00254,
        ASM00255, ASM00256, ASM00257, ASM00258, ASM00259, ASM00260, ASM00261, ASM00262, ASM00263,
        ASM00264, ASM00265, ASM00266, ASM00267, ASM00268, ASM00269, ASM00270, ASM00271, ASM00272,
        ASM00273, ASM00274, ASM00275, ASM00276, ASM00277, ASM00278, ASM00279, ASM00280, ASM00281,
        ASM00282, ASM00283, ASM00284, ASM00285, ASM00286, ASM00287, ASM00288, ASM00289, ASM00290, 
        ASM00291, ASM00292, ASM00293, ASM00294, ASM00295, ASM00296, ASM00297, ASM00298, ASM00299, 
        ASM00300, ASM00301, ASM00302, ASM00303, ASM00304, ASM00305, ASM00306, ASM00307, ASM00308,
        ASM00309, ASM00310, ASM00311, ASM00312, ASM00313, ASM00314, ASM00315, ASM00316, ASM00317,
        ASM00318, ASM00319, ASM00320, ASM00321, ASM00322, ASM00323, ASM00324, ASM00325, ASM00326,
        ASM00327, ASM00328, ASM00329, ASM00330, ASM00331, ASM00332, ASM00333, ASM00334, ASM00335,
        ASM00336, ASM00337, ASM00338, ASM00339, ASM00340, ASM00341, ASM00342, ASM00343, ASM00344,
        ASM00345, ASM00346, ASM00347
    }

    private static final String MESSAGE_BUNDLE_NAME = "AsmManagerMessages";

    private static final String AGENT_ID = "ASM Manager";

    private static EEMILocalizableMessage buildDetailedMessage(MsgCodes msgCode,
                                                               EEMISeverity severity,
                                                               EEMICategory category,
                                                               Object... params) {
        return new EEMILocalizableMessage(new ResourceBundleLocalizableMessage(MESSAGE_BUNDLE_NAME, msgCode.name(), params),
                severity, category, AGENT_ID);
    }

    private static LocalizableMessage buildLocalizableMessage(MsgCodes msgCode, Object... params) {
        return new ResourceBundleLocalizableMessage(MESSAGE_BUNDLE_NAME, msgCode.name(), params);
    }

    public static EEMILocalizableMessage duplicateRecord(String refId) {
        return buildDetailedMessage(
                MsgCodes.ASM0001,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refId);
    }

    public static EEMILocalizableMessage duplicateRefId(String refId) {
        return buildDetailedMessage(
                MsgCodes.ASM0001,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refId);
    }

    public static EEMILocalizableMessage duplicateServiceTag(String serviceTag) {
        return buildDetailedMessage(
                MsgCodes.ASM0005,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serviceTag);
    }

    public static EEMILocalizableMessage notFound(String refId) {
        return buildDetailedMessage(
                MsgCodes.ASM0002,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refId);
    }

    public static EEMILocalizableMessage deviceIdsNotFound(String refIds) {
        return buildDetailedMessage(
                MsgCodes.ASM0054,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refIds);
    }

    public static EEMILocalizableMessage invalidDeviceGroupRequest(String refIds) {
        return buildDetailedMessage(
                MsgCodes.ASM0056,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refIds);
    }

    public static EEMILocalizableMessage deviceGroupNotFound(String refIds) {
        return buildDetailedMessage(
                MsgCodes.ASM0057,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refIds);
    }

    public static EEMILocalizableMessage userIdsNotFound(String refIds) {
        return buildDetailedMessage(
                MsgCodes.ASM0055,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refIds);
    }

    public static EEMILocalizableMessage mismatchRefId(String refId, String deviceRefId) {
        return buildDetailedMessage(
                MsgCodes.ASM0003,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refId, deviceRefId);
    }

    public static EEMILocalizableMessage internalError() {
        return buildDetailedMessage(
                MsgCodes.ASM0004,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage missingRequired(String message) {
        return buildDetailedMessage(
                MsgCodes.ASM0006,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message);
    }

    public static EEMILocalizableMessage invalidIPRange(String startIP, String endIP) {
        return buildDetailedMessage(
                MsgCodes.ASM0007,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                startIP, endIP);
    }
    
    public static EEMILocalizableMessage ipRangeIsOverlapping(String startIP, String endIP) {
        return buildDetailedMessage(
                MsgCodes.ASM0077,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                startIP, endIP);
    }

    public static EEMILocalizableMessage invalidIPSubnet(String startIp, String endIp) {
        return buildDetailedMessage(
                MsgCodes.ASM0008,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                startIp, endIp);
    }

    public static EEMILocalizableMessage invalidCredentials(String message) {
        return buildDetailedMessage(
                MsgCodes.ASM0009,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message);
    }

    public static EEMILocalizableMessage jobSchedulingError(String message) {
        return buildDetailedMessage(
                MsgCodes.ASM0010,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message);
    }

    public static EEMILocalizableMessage unsupportedDeviceType(String deviceType) {
        return buildDetailedMessage(
                MsgCodes.ASM0011,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                deviceType);
    }

    public static EEMILocalizableMessage discoveryServiceException(String deviceIP) {
        return buildDetailedMessage(
                MsgCodes.ASM0012,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                deviceIP);
    }

    public static EEMILocalizableMessage puppetException(String deviceIP, String moduleName) {
        return buildDetailedMessage(
                MsgCodes.ASM00237,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                deviceIP, moduleName);
    }

    public static LocalizableMessage discoveryJobStarted(String jobName) {
        return buildLocalizableMessage(MsgCodes.ASM0014, jobName);
    }

    public static LocalizableMessage discoveryJobCompleted(String jobName) {
        return buildLocalizableMessage(MsgCodes.ASM0015, jobName);
    }

    public static LocalizableMessage discoveryJobFailedLog(String jobName) {
        return buildLocalizableMessage(MsgCodes.ASM0016, jobName);
    }

    public static EEMILocalizableMessage discoveryJobFailed(String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM0016,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,
                jobName);
    }

    public static EEMILocalizableMessage InvalidTemplateName(String templateName) {
        return buildDetailedMessage(
                MsgCodes.ASM0017,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                templateName);
    }

    public static EEMILocalizableMessage InvalidTemplateNameLength(String templateName) {
        return buildDetailedMessage(
                MsgCodes.ASM0018,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                templateName);
    }

    public static EEMILocalizableMessage InvalidTemplateType() {
        return buildDetailedMessage(
                MsgCodes.ASM0019,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING
        );
    }

    public static EEMILocalizableMessage UserPasswordNullEmpty(String userName) {
        return buildDetailedMessage(
                MsgCodes.ASM0020,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                userName);
    }

    public static EEMILocalizableMessage InvalidUserNameLength(String userName) {
        return buildDetailedMessage(
                MsgCodes.ASM0021,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                userName);
    }

    public static EEMILocalizableMessage InvalidUserPasswordPattern() {
        return buildDetailedMessage(
                MsgCodes.ASM0022,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING
        );
    }

    public static EEMILocalizableMessage userLimitCrossed() {
        return buildDetailedMessage(
                MsgCodes.ASM0023,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING
        );
    }

    public static EEMILocalizableMessage duplicateTemplateName(String message) {
        return buildDetailedMessage(
                MsgCodes.ASM0024,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message);
    }

    public static EEMILocalizableMessage raProblemCreate(String message, String messageFromRA) {
        return buildDetailedMessage(
                MsgCodes.ASM0025,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message,
                messageFromRA);
    }

    public static EEMILocalizableMessage invalidCredentialId(String message) {
        return buildDetailedMessage(
                MsgCodes.ASM0026,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message);
    }

    public static EEMILocalizableMessage invalidIpAddressId(String message) {
        return buildDetailedMessage(
                MsgCodes.ASM0027,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message);
    }

    public static EEMILocalizableMessage missingNetworkId() {
        return buildDetailedMessage(
                MsgCodes.ASM0028,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage missingCredential() {
        return buildDetailedMessage(
                MsgCodes.ASM0029,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage raProblemUpdate(String message, String messageFromRA) {
        return buildDetailedMessage(
                MsgCodes.ASM0030,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message,
                messageFromRA);
    }

    public static EEMILocalizableMessage invalidSmtpEmailAddress(String email) {
        return buildDetailedMessage(
                MsgCodes.ASM0031,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                email);
    }

    public static EEMILocalizableMessage invalidSmtpUser(String userName) {
        return buildDetailedMessage(
                MsgCodes.ASM0032,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                userName);
    }

    public static EEMILocalizableMessage maxTrapServerLimit(int size) {
        return buildDetailedMessage(
                MsgCodes.ASM0033,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                size);
    }

    public static EEMILocalizableMessage invalidTrapServer(String snmpDestination) {
        return buildDetailedMessage(
                MsgCodes.ASM0034,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                snmpDestination);
    }

    public static EEMILocalizableMessage maxEmailServerLimit(int size) {
        return buildDetailedMessage(
                MsgCodes.ASM0035,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                size);
    }

    public static EEMILocalizableMessage noNTPserverSpecified() {
        return buildDetailedMessage(
                MsgCodes.ASM0036,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING
        );
    }

    public static EEMILocalizableMessage invalidNTPserver(String ntpServer) {
        return buildDetailedMessage(
                MsgCodes.ASM0037,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                ntpServer);
    }

    public static EEMILocalizableMessage invalidTimezone(int timeZone) {
        return buildDetailedMessage(
                MsgCodes.ASM0038,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                timeZone);
    }

    public static EEMILocalizableMessage invalidSyslogServer(String syslog) {
        return buildDetailedMessage(
                MsgCodes.ASM0039,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                syslog);
    }

    public static EEMILocalizableMessage invalidEmailAddress(String emailAddress) {
        return buildDetailedMessage(
                MsgCodes.ASM0040,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                emailAddress);
    }

    public static EEMILocalizableMessage invalidSmtpAddress(String smtp) {
        return buildDetailedMessage(
                MsgCodes.ASM0041,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                smtp);
    }

    public static EEMILocalizableMessage templateNotFound(String templateId) {

        return buildDetailedMessage(
                MsgCodes.ASM0042,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                templateId);
    }

    public static EEMILocalizableMessage inValidDeviceRef(String deviceRef) {

        return buildDetailedMessage(
                MsgCodes.ASM0043,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                deviceRef);
    }


    public static EEMILocalizableMessage inValidStateForConfigure(String state) {

        return buildDetailedMessage(
                MsgCodes.ASM0044,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                state);
    }


    public static EEMILocalizableMessage inValidTemplateAndDevice(String type, String invType) {

        return buildDetailedMessage(
                MsgCodes.ASM0045,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                type, invType);
    }

    public static EEMILocalizableMessage inValidTemplateType(String reqDeviceType, String invDeviceType) {

        return buildDetailedMessage(
                MsgCodes.ASM0046,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                invDeviceType, reqDeviceType);
    }

    public static EEMILocalizableMessage dhcpNotSupported() {

        return buildDetailedMessage(
                MsgCodes.ASM0047,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static LocalizableMessage configureJobStarted(String jobName) {
        return buildLocalizableMessage(MsgCodes.ASM0048, jobName);
    }

    public static LocalizableMessage configureJobCompleted(String jobName) {
        return buildLocalizableMessage(MsgCodes.ASM0049, jobName);
    }

    public static EEMILocalizableMessage configureJobFailed(String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM0050,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,
                jobName);
    }

    public static EEMILocalizableMessage invalidTemplateState(boolean state) {
        return buildDetailedMessage(
                MsgCodes.ASM0051,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,
                state);
    }

    public static EEMILocalizableMessage missingCredentials() {
        return buildDetailedMessage(
                MsgCodes.ASM0052,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage connectionError(String raType) {
        return buildDetailedMessage(
                MsgCodes.ASM0053,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage TemplateIdNull(String templateId) {
        return buildDetailedMessage(
                MsgCodes.ASM0063,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage TemplateNull() {
        return buildDetailedMessage(
                MsgCodes.ASM0064,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage DeviceTypeNull(String deviceType) {
        return buildDetailedMessage(
                MsgCodes.ASM0065,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage TempateStateNull(String templateId) {
        return buildDetailedMessage(
                MsgCodes.ASM0066,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING);
    }


    public static EEMILocalizableMessage duplicateDeviceGroupName(String refId) {
        return buildDetailedMessage(
                MsgCodes.ASM0058,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refId);
    }

    public static EEMILocalizableMessage updateDeviceGroupError(String refId) {
        return buildDetailedMessage(
                MsgCodes.ASM0059,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refId);
    }

    public static EEMILocalizableMessage deviceGroupsDataNotFound(String refIds) {
        return buildDetailedMessage(
                MsgCodes.ASM0060,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                refIds);
    }

    public static EEMILocalizableMessage noCredentialSpecified() {
        return buildDetailedMessage(
                MsgCodes.ASM0061,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage templateIsInUse(String templateName) {
        return buildDetailedMessage(
                MsgCodes.ASM0062,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                templateName);
    }

    public static EEMILocalizableMessage duplicateEmailAddress(String message) {
        return buildDetailedMessage(
                MsgCodes.ASM0067,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message);
    }

    public static EEMILocalizableMessage duplicateSnmpAddress(String message) {
        return buildDetailedMessage(
                MsgCodes.ASM0068,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                message);
    }

    public static EEMILocalizableMessage invalidFirmwareRepositoryId(String repositoryId) {
        return buildDetailedMessage(
                MsgCodes.ASM0069,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                repositoryId);
    }

    public static EEMILocalizableMessage credentialInUse(String label) {
        return buildDetailedMessage(
                MsgCodes.ASM0070,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                label);
    }
    
    public static EEMILocalizableMessage licenseExpired() {
        return buildDetailedMessage(
                MsgCodes.ASM0071,
                EEMILocalizableMessage.EEMISeverity.ERROR,
                EEMILocalizableMessage.EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage licenseNotUploaded() {
        return buildDetailedMessage(
                MsgCodes.ASM0072,
                EEMILocalizableMessage.EEMISeverity.ERROR,
                EEMILocalizableMessage.EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage licenseNotAvailable(String required, String available) {
        
        int free = Integer.parseInt(available);
        if(free == 0){
            return buildDetailedMessage(
                    MsgCodes.ASM0075,
                    EEMILocalizableMessage.EEMISeverity.ERROR,
                    EEMILocalizableMessage.EEMICategory.USER_FACING,
                    required, available);
        }
        return buildDetailedMessage(
                MsgCodes.ASM0073,
                EEMILocalizableMessage.EEMISeverity.ERROR,
                EEMILocalizableMessage.EEMICategory.USER_FACING,
                required, available);
    }

    public static EEMILocalizableMessage scheduleError() {
        return buildDetailedMessage(
                MsgCodes.ASM0074,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage networkResourceInUse(String networkName, String policyName) {
        return buildDetailedMessage(
                MsgCodes.ASM0076, 
                EEMISeverity.ERROR, 
                EEMICategory.USER_FACING, 
                networkName, policyName);
    }
    
    public static EEMILocalizableMessage deviceStateNotSupportedForDeleteOperation(String serviceTag,String deviceStatus) {
        return buildDetailedMessage(
                MsgCodes.ASM0078,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,serviceTag,deviceStatus);
    }
    
    public static EEMILocalizableMessage deleteTemplateError(String templateName) {
        return buildDetailedMessage(
                MsgCodes.ASM0079,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                templateName);
    }
    
    public static LocalizableMessage deleteDeviceJobStarted(String serviceTag) {
        return buildLocalizableMessage(MsgCodes.ASM0080, serviceTag);
    }

    public static LocalizableMessage deleteDeviceJobCompleted(String serviceTag) {
        return buildLocalizableMessage(MsgCodes.ASM0081, serviceTag);
    }
    
    public static LocalizableMessage deletedServerSuccessfully(String serviceTag) {
        return buildLocalizableMessage(MsgCodes.ASM0082, serviceTag);
    }
    
    public static LocalizableMessage deletedChassisSuccessfully(String serviceTag) {
        return buildLocalizableMessage(MsgCodes.ASM0083, serviceTag);
    }
    
    public static LocalizableMessage deletedIOMSuccessfully(String serviceTag) {
        return buildLocalizableMessage(MsgCodes.ASM0084, serviceTag);
    }
    
    public static LocalizableMessage deleteDeviceJobFailed(String serviceTag) {
        return buildLocalizableMessage(MsgCodes.ASM0085, serviceTag);
    }
    
    
    public static LocalizableMessage rerunInventoryJobStarted(String jobName) {
        return buildLocalizableMessage(MsgCodes.ASM0086, jobName);
    }

    public static LocalizableMessage rerunInventoryJobCompleted(String jobName) {
        return buildLocalizableMessage(MsgCodes.ASM0087, jobName);
    }

    public static LocalizableMessage rerunInventoryJobFailed(String jobName) {
        return buildLocalizableMessage(MsgCodes.ASM0088, jobName);
    }

    public static EEMILocalizableMessage chassisDeviceStateNotSupportedForDeleteOperation(String serviceTag,String deviceStatus) {
        return buildDetailedMessage(
                MsgCodes.ASM0090,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,serviceTag,deviceStatus);
    }
    
    public static EEMILocalizableMessage bladeServerOrIOMDeviceStateNotSupportedForDeleteOperation(String serviceTag,String deviceStatus) {
        return buildDetailedMessage(
                MsgCodes.ASM0091,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,serviceTag,deviceStatus);
    }

    public static EEMILocalizableMessage serviceNotImplemented() {
        return buildDetailedMessage(
                MsgCodes.ASM0093,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serversNotAvailableForDeployment(String sPoolName) {
        return buildDetailedMessage(
                MsgCodes.ASM0094,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, sPoolName);
    }

    public static EEMILocalizableMessage renameToDuplicateName(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM0096,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }
    public static EEMILocalizableMessage noServiceTemplateFoundForDeployment() {
        return buildDetailedMessage(
                MsgCodes.ASM0095,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    public static EEMILocalizableMessage duplicateVolumeNameFound(String volumeName) {
        return buildDetailedMessage(
                MsgCodes.ASM0097,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,volumeName);
    }
    public static EEMILocalizableMessage invalidVolumeSizeEql(String volumeSize) {
        return buildDetailedMessage(
                MsgCodes.ASM0098,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,volumeSize);
    }
    public static EEMILocalizableMessage invalidVolumeSizeCmpl(String volumeSize) {
        return buildDetailedMessage(
                MsgCodes.ASM0099,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,volumeSize);
    }
    public static EEMILocalizableMessage invalidPercentageFormat(String percentage) {
        return buildDetailedMessage(
                MsgCodes.ASM00100,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,percentage);
    }
    public static EEMILocalizableMessage invalidCHAPUserName(String userName) {
        return buildDetailedMessage(
                MsgCodes.ASM00101,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,userName);
    }
    public static EEMILocalizableMessage invalidCHAPPassword() {
        return buildDetailedMessage(
                MsgCodes.ASM00102,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    public static EEMILocalizableMessage invalidFolderName(String folderName) {
        return buildDetailedMessage(
                MsgCodes.ASM00103,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,folderName);
    }
    public static EEMILocalizableMessage invalidServerWWN(String serverWWN) {
        return buildDetailedMessage(
                MsgCodes.ASM00104,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,serverWWN);
    }
    public static EEMILocalizableMessage invalidVolumeName(String volumeName) {
        return buildDetailedMessage(
                MsgCodes.ASM00105,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,volumeName);
    }
    public static EEMILocalizableMessage invalidServerIPorIQN(String ipOriqn) {
        return buildDetailedMessage(
                MsgCodes.ASM00106,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,ipOriqn);
    }
    public static EEMILocalizableMessage serviceTemplateCreated(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00107,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,name);
    }
    public static EEMILocalizableMessage serviceTemplateUpdated(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00108,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,name);
    }
    public static EEMILocalizableMessage serviceTemplateDeleted(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00109,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,name);
    }
    public static EEMILocalizableMessage deployedServiceTemplate(String name, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00110,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,name,jobName);
    }
    public static EEMILocalizableMessage migratedServer(String deploymentName, String oldServer, String newServer) {
        return buildDetailedMessage(
                MsgCodes.ASM00140,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,deploymentName,oldServer,newServer);
    }

    public static LocalizableMessage inventoryStartedOnHostMsg(String sServiceTag) 
    {
        return buildLocalizableMessage(MsgCodes.ASM00111, sServiceTag);
    }

    public static LocalizableMessage inventoryCompletedOnHostMsg(String sServiceTag) 
    {
        return buildLocalizableMessage(MsgCodes.ASM00112, sServiceTag);
    }

    public static LocalizableMessage inventoryFailedOnHostMsg(String sServiceTag, String sErrorMsg) 
    {
        return buildLocalizableMessage(MsgCodes.ASM00113, sServiceTag, sErrorMsg);
    }
    
    public static EEMILocalizableMessage moreThanOneOfSameStorageTypeInTemplate() {
        return buildDetailedMessage(
                MsgCodes.ASM00114,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage templateHasApplicationWithoutVM() {
        return buildDetailedMessage(
                MsgCodes.ASM00115,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage templateHasVMWithoutCluster() {
        return buildDetailedMessage(
                MsgCodes.ASM00116,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage deleteDefaultCredential(String label) {
        return buildDetailedMessage(
                MsgCodes.ASM00117,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,label);
    }
    
    public static EEMILocalizableMessage passwordMismatch() {
        return buildDetailedMessage(
                MsgCodes.ASM00118,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage missingDeviceInfo(String template, String component) {
        return buildDetailedMessage(
                MsgCodes.ASM00119,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,template, component);
    }
    
    public static EEMILocalizableMessage deletedDeployment(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00120,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,name);
    }

    public static EEMILocalizableMessage invalidHostname(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00123,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }

    public static EEMILocalizableMessage noUserInContext() {
        return buildDetailedMessage(
                MsgCodes.ASM00124,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage noIQNorIP() {
        return buildDetailedMessage(
                MsgCodes.ASM00125,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage storageMustHave2EQL() {
        return buildDetailedMessage(
                MsgCodes.ASM00127,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage storageSizeLess512() {
        return buildDetailedMessage(
                MsgCodes.ASM00128,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage clusterMustBeHyperV() {
        return buildDetailedMessage(
                MsgCodes.ASM00129,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage invalidNetappVolumeName(String volumeName) {
        return buildDetailedMessage(
                MsgCodes.ASM00136,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,volumeName);
    }
    
    public static EEMILocalizableMessage clusterDuplicate(final String clusterName) {
        return buildDetailedMessage(
                MsgCodes.ASM00268,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, clusterName);
    }

    public static EEMILocalizableMessage memMustHaveStatic() {
        return buildDetailedMessage(
                MsgCodes.ASM00130,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage haMustHave2StorComponents() {
        return buildDetailedMessage(
                MsgCodes.ASM00131,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverIsInMoreThan1Cluster() {
        return buildDetailedMessage(
                MsgCodes.ASM00132,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage invalidIPforNFS(String ipOriqn) {
        return buildDetailedMessage(
                MsgCodes.ASM00133,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,ipOriqn);
    }

    public static EEMILocalizableMessage serverMustHaveStorageNetwork() {
        return buildDetailedMessage(
                MsgCodes.ASM00134,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage deviceInUse(String deviceType, String serviceTag) {
        return buildDetailedMessage(
                MsgCodes.ASM00135,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, deviceType, serviceTag);
    }

    public static EEMILocalizableMessage duplicateHostname(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00152,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }
    
    public static EEMILocalizableMessage duplicateVolumeName(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00156,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }
    
    
    public static EEMILocalizableMessage duplicateVMName(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00137,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }

    public static EEMILocalizableMessage PasswordNullEmpty(String passField) {
        return buildDetailedMessage(
                MsgCodes.ASM00138,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, passField);
    }

    public static EEMILocalizableMessage iscsiMustHaveStatic() {
        return buildDetailedMessage(
                MsgCodes.ASM00139,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverMustHavePXE() {
        return buildDetailedMessage(
                MsgCodes.ASM00141,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverMissedNetwork(String osImage, String netName) {
        return buildDetailedMessage(
                MsgCodes.ASM00142,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, osImage, netName);
    }

    public static EEMILocalizableMessage storageCompellentAndISCSI() {
        return buildDetailedMessage(
                MsgCodes.ASM00143,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverWithHyperVPartitioned() {
        return buildDetailedMessage(
                MsgCodes.ASM00144,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverWithESXNotPartitioned() {
        return buildDetailedMessage(
                MsgCodes.ASM00145,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage wrongPartition(String netName, int partnum) {
        return buildDetailedMessage(
                MsgCodes.ASM00146,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, netName, partnum);
    }

    public static EEMILocalizableMessage serverWithNetappMustHaveRequiredNetwork() {
        return buildDetailedMessage(
                MsgCodes.ASM00122,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage iscsiMustBeOnPort1() {
        return buildDetailedMessage(
                MsgCodes.ASM00148,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage iscsiMustBeTheOnlyNetwork() {
        return buildDetailedMessage(
                MsgCodes.ASM00150,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage workloadMustBeDHCP() {
        return buildDetailedMessage(
                MsgCodes.ASM00151,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage mustSetBIOSForHypervisor() {
        return buildDetailedMessage(
                MsgCodes.ASM00153,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverMustBeHyperV() {
        return buildDetailedMessage(
                MsgCodes.ASM00154,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static LocalizableMessage ipAddressInUse(String ipAddress) {
        return buildLocalizableMessage(MsgCodes.ASM00155, ipAddress);
    }
    
    public static EEMILocalizableMessage notEnoughIPs(String network) {
        return buildDetailedMessage(
                MsgCodes.ASM00157,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,network);
    }
    public static EEMILocalizableMessage PuppetDeviceExtractionFailed(String deviceId) {
        return buildDetailedMessage(
                MsgCodes.ASM00158,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,deviceId);
    }

    public static EEMILocalizableMessage vsanRequired3Hosts() {
        return buildDetailedMessage(
                MsgCodes.ASM00159,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage deploymentNotFound(String id) {

        return buildDetailedMessage(
                MsgCodes.ASM00160,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                id);
    }

    public static EEMILocalizableMessage serverPoolNotFound(String id) {

        return buildDetailedMessage(
                MsgCodes.ASM00161,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                id);
    }

    public static EEMILocalizableMessage cannotOpenRemoteFile(String fileType, String fileName) {

        return buildDetailedMessage(
                MsgCodes.ASM00162,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                fileType,
                fileName);
    }
    public static EEMILocalizableMessage cannotCreateDownloadDir(String fileType, String localDir) {

        return buildDetailedMessage(
                MsgCodes.ASM00163,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                fileType,
                localDir);
    }
    public static EEMILocalizableMessage cannotCreateLocalFile(String fileType,String fileName) {

        return buildDetailedMessage(
                MsgCodes.ASM00164,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                fileType,
                fileName);
    }
    public static EEMILocalizableMessage unableToAccessRemoteFile(String fileType,String fileName) {

        return buildDetailedMessage(
                MsgCodes.ASM00165,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                fileType,
                fileName);
    }
    public static EEMILocalizableMessage cannotSaveLocalFile(String fileType,String fileName) {

        return buildDetailedMessage(
                MsgCodes.ASM00166,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                fileType,
                fileName);
    }
    public static EEMILocalizableMessage cannotExpandRemoteFile(String fileType, String fileName) {

        return buildDetailedMessage(
                MsgCodes.ASM00167,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                fileType,
                fileName);
    }
    public static EEMILocalizableMessage remoteFileDoesNotExist(String fileType, String fileName) {

        return buildDetailedMessage(
                MsgCodes.ASM00168,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                fileType,
                fileName);
    }

    public static EEMILocalizableMessage couldNotConnectToPath(String location) {

        return buildDetailedMessage(
                MsgCodes.ASM00169,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                location);
    }

    public static EEMILocalizableMessage razorRepoAlreadyExists(String repoName) {

        return buildDetailedMessage(
                MsgCodes.ASM00170,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                "OS",
                repoName);
    }

    public static EEMILocalizableMessage invalidFileFormat(String expectedFormat){
        return buildDetailedMessage(
                MsgCodes.ASM00171,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                expectedFormat);
    }

    public static LocalizableMessage discoveryError(String error) {
        return buildLocalizableMessage(MsgCodes.ASM00172, error);
    }

    public static EEMILocalizableMessage credentialsNotSupported() {
        return buildDetailedMessage(
                MsgCodes.ASM00173,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING
        );
    }

    public static EEMILocalizableMessage catalogNotSupported() {
        return buildDetailedMessage(
                MsgCodes.ASM00174,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING
        );
    }

    public static EEMILocalizableMessage networkIdMissed() {
        return buildDetailedMessage(
                MsgCodes.ASM00175,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING
        );
    }

    public static EEMILocalizableMessage applyConfigurationFailed(String msg) {
        return buildDetailedMessage(
                MsgCodes.ASM00176,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                msg);

    }

    public static EEMILocalizableMessage startedConfigurationJob(String serviceTag, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00177,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,jobName,serviceTag);
    }

    public static EEMILocalizableMessage completedConfigurationJob(String serviceTag, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00191,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,jobName,serviceTag);
    }
    public static EEMILocalizableMessage startedScheduledInventoryJob(String serviceTag, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00188,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,jobName,serviceTag);
    }
    public static EEMILocalizableMessage completedScheduledInventoryJob(String serviceTag, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00189,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,jobName, serviceTag);
    }
    public static EEMILocalizableMessage errorScheduledInventoryJob(String serviceTag, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00190,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,jobName, serviceTag);
    }


    /**
     * Supplied firmware repository catalog source location string is invalid. Supported formats
     * are CIFS, NFS and FTP.
     *
     * @return The message
     */
    public static EEMILocalizableMessage invalidCatalogSourceLocation() {
        return buildDetailedMessage(MsgCodes.ASM00178, EEMISeverity.ERROR, EEMICategory.USER_FACING);
    }

    /**
     * The supplied firmware repository catalog file name is not valid. Only .xml or .cab files
     * are supported.
     *
     * @return The message
     */
    public static EEMILocalizableMessage invalidCatalogFileName() {
        return buildDetailedMessage(MsgCodes.ASM00179, EEMISeverity.ERROR, EEMICategory.USER_FACING);
    }

    /**
     * The firmware repository catalog file contained a baseLocation field that is a valid URL
     * but is not supported by ASM firmware repository management. Only CIFS, NFS or FTP
     * locations are supported.
     *
     * @return The message
     */
    public static EEMILocalizableMessage unsupportedCatalogBaseLocationUrl() {
        return buildDetailedMessage(MsgCodes.ASM00180, EEMISeverity.ERROR, EEMICategory.USER_FACING);
    }

    /**
     * The firmware repository catalog file contained a baseLocation field that appears to be
     * a host name but it cannot be resolved to an IP address.
     *
     * @param hostname The hostname contained in the catalog file
     * @return The message
     */
    public static EEMILocalizableMessage catalogBaseLocationHostUnknown(String hostname) {
        return buildDetailedMessage(MsgCodes.ASM00181, EEMISeverity.ERROR,
                EEMICategory.USER_FACING, hostname);
    }

    public static EEMILocalizableMessage startedDownloadingFirmwareRepository(String repoName) {
        return buildDetailedMessage(MsgCodes.ASM00182, EEMISeverity.INFO,
                EEMICategory.USER_FACING, repoName);
    }

    public static EEMILocalizableMessage firmwareDownloadFailed(String repoName, String firmwareLocation) {
        return buildDetailedMessage(MsgCodes.ASM00183, EEMISeverity.ERROR,
                EEMICategory.USER_FACING, repoName, firmwareLocation);
    }

    public static EEMILocalizableMessage completedDownloadingFirmwareRepository(String repoName) {
        return buildDetailedMessage(MsgCodes.ASM00184, EEMISeverity.INFO,
                EEMICategory.USER_FACING, repoName);
    }

    public static EEMILocalizableMessage createdFirmwareRepository(String repoName) {
        return buildDetailedMessage(MsgCodes.ASM00185, EEMISeverity.INFO,
                EEMICategory.USER_FACING, repoName);
    }

    public static EEMILocalizableMessage deletedFirmwareRepository(String repoName) {
        return buildDetailedMessage(MsgCodes.ASM00186, EEMISeverity.INFO,
                EEMICategory.USER_FACING, repoName);
    }

    public static EEMILocalizableMessage firmwareRepositoryNotAvailable(String repoName) {
        return buildDetailedMessage(MsgCodes.ASM00255, EEMISeverity.ERROR,
                EEMICategory.USER_FACING, repoName);
    }

    public static EEMILocalizableMessage scheduleDateIsPast() {
        return buildDetailedMessage(MsgCodes.ASM00187, EEMISeverity.ERROR, EEMICategory.USER_FACING);
    }

    public static LocalizableMessage resourceDiscovered(String name) {
        return buildLocalizableMessage(MsgCodes.ASM00192, name);
    }

    public static LocalizableMessage resourceAdded(String name) {
        return buildLocalizableMessage(MsgCodes.ASM00193, name);
    }

    public static EEMILocalizableMessage invalidRepoName() {
        return buildDetailedMessage(MsgCodes.ASM00194, EEMISeverity.ERROR, EEMICategory.USER_FACING);
    }
    
    /* Firmware updates
     */
    public static EEMILocalizableMessage firmwareUpdateCompleted(String svc_tag) {
        return buildDetailedMessage(
				    MsgCodes.ASM00232,
				    EEMISeverity.INFO,
				    EEMICategory.USER_FACING,
				    svc_tag);
    }
    public static EEMILocalizableMessage firmwareUpdateFailed(String svc_tag) {
        return buildDetailedMessage(
				    MsgCodes.ASM00233,
				    EEMISeverity.ERROR,
				    EEMICategory.USER_FACING,
				    svc_tag);
    }
    

    public static EEMILocalizableMessage deploymentTeardownMustIncludeServers() {
        return buildDetailedMessage(MsgCodes.ASM00195, EEMISeverity.ERROR, EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage hypervRequiresStatic(String netName) {
        return buildDetailedMessage(
                MsgCodes.ASM00196,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, netName);
    }

    public static EEMILocalizableMessage serverMustHaveOsImage() {
        return buildDetailedMessage(
                MsgCodes.ASM00197,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage exportConfigurationFailed() {
        return buildDetailedMessage(
                MsgCodes.ASM00198,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage noTemplateFile() {
        return buildDetailedMessage(
                MsgCodes.ASM00199,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage badTemplateFile(String reason) {
        return buildDetailedMessage(
                MsgCodes.ASM00200,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, reason);
    }

    public static EEMILocalizableMessage noBackupPassword() {
        return buildDetailedMessage(
                MsgCodes.ASM00201,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage uploadTemplateValidationFailedHasNetworks() {
        return buildDetailedMessage(
                MsgCodes.ASM00202,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage uploadTemplateValidationFailedHasGUID() {
        return buildDetailedMessage(
                MsgCodes.ASM00203,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage uploadTemplateValidationFailedHasOption() {
        return buildDetailedMessage(
                MsgCodes.ASM00204,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage invalidHostnameTemplate(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00205,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }

    public static EEMILocalizableMessage mustSetAutoGenerateHostname() {
        return buildDetailedMessage(
                MsgCodes.ASM00206,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage hostnameTooLong(String hostname) {
        return buildDetailedMessage(
                MsgCodes.ASM00207,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, hostname);
    }

    public static EEMILocalizableMessage nonServerComponentsDetected() {
        return buildDetailedMessage(
                MsgCodes.ASM00208,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serversNotAvailableForOneOfDeployments(String sPoolName, int serviceNumber) {
        return buildDetailedMessage(
                MsgCodes.ASM00209,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, sPoolName, serviceNumber);
    }

    public static EEMILocalizableMessage vmMustHaveNetwork(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00210,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }

    public static EEMILocalizableMessage invalidRaidConfiguration() {
        return buildDetailedMessage(
                MsgCodes.ASM00211,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage badParametersForFiltering() {
        return buildDetailedMessage(
                MsgCodes.ASM00212,
                EEMILocalizableMessage.EEMISeverity.ERROR,
                EEMILocalizableMessage.EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage deploymentFailed(String name, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00213,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,name,jobName);
    }

    public static EEMILocalizableMessage deploymentCompleted(String name, String jobName) {
	return buildDetailedMessage(
                MsgCodes.ASM00214,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,name,jobName);
    }

    public static EEMILocalizableMessage missingTemplateRequired(String componentName, String fieldName) {
        return buildDetailedMessage(
                    MsgCodes.ASM00215,
                    EEMISeverity.ERROR,
                    EEMICategory.USER_FACING,
                    componentName, fieldName);        
    }

    public static EEMILocalizableMessage reverseDnsLookupFailed(String ipAddress, String primaryDns) {
        return buildDetailedMessage(
                MsgCodes.ASM00216,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                ipAddress, primaryDns);
    }

    public static EEMILocalizableMessage staticIpRequiredForDnsHostnameTemplate() {
        return buildDetailedMessage(
                MsgCodes.ASM00217,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage sameChapUsernames() {
        return buildDetailedMessage(
                MsgCodes.ASM00218,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage invalidManagementIp(String ipAddress) {
        return buildDetailedMessage(
                MsgCodes.ASM00219,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                ipAddress);
    }

    public static EEMILocalizableMessage noManagementNetworkToSet(String serverName, String ipAddress) {
        return buildDetailedMessage(
                MsgCodes.ASM00220,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serverName, ipAddress);
    }

    public static EEMILocalizableMessage invalidManagementIpForNetwork(String serverName, String networkName, String ipAddress) {
        return buildDetailedMessage(
                MsgCodes.ASM00221,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serverName, networkName, ipAddress);
    }

    public static EEMILocalizableMessage managementIpInUse(String ipAddress) {
        return buildDetailedMessage(
                MsgCodes.ASM00222,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                ipAddress);
    }

    public static EEMILocalizableMessage managementIpNotAvailable(String serverName, String networkName, String ipAddress) {
        return buildDetailedMessage(
                MsgCodes.ASM00223,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serverName, networkName, ipAddress);
    }

    public static EEMILocalizableMessage dnsHostnameAndDnsIpOptionsConflict(String serverName) {
        return buildDetailedMessage(
                MsgCodes.ASM00224,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serverName);
    }

    public static EEMILocalizableMessage noDnsServersForManagementNetwork(String serverName) {
        return buildDetailedMessage(
                MsgCodes.ASM00225,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serverName);
    }

    public static EEMILocalizableMessage dnsLookupFailed(String hostName, String dnsServer) {
        return buildDetailedMessage(
                MsgCodes.ASM00226,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                hostName, dnsServer);
    }
    public static EEMILocalizableMessage invalidSoftwareBundleId(String bundleId) {
        return buildDetailedMessage(
                MsgCodes.ASM00227,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                bundleId);
    }
    public static EEMILocalizableMessage duplicateBundleName(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00228,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                name);
    }

    public static EEMILocalizableMessage duplicateServersChosen(Set<String> componentNames, String serviceTag) {
        return buildDetailedMessage(
                MsgCodes.ASM00229,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                componentNames, serviceTag);
    }

    public static EEMILocalizableMessage mustSetNtpForHypervisor() {
        return buildDetailedMessage(
                MsgCodes.ASM00230,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage unableImportConfig() {
        return buildDetailedMessage(
                MsgCodes.ASM00231,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage invalidHostnameTemplateForVMs() {
        return buildDetailedMessage(
                MsgCodes.ASM00234,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage uploadConfigurationFailed() {
        return buildDetailedMessage(
                MsgCodes.ASM00235,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverNotFoundForDeployment(String componentName) {
        return buildDetailedMessage(
                MsgCodes.ASM00236,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                componentName);
    }

    public static EEMILocalizableMessage rejectedServers(String serversList, String reason) {
        return buildDetailedMessage(
                MsgCodes.ASM00238,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serversList, reason);
    }

    public static EEMILocalizableMessage serverMustHave2ISCSI() {
        return buildDetailedMessage(
                MsgCodes.ASM00239,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverRejectForDeployment(Map<String, String> reason) {
        return buildDetailedMessage(
                MsgCodes.ASM00240,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                reason);
    }

    public static EEMILocalizableMessage invalidFirmwareBundleVersion(String newVersion, String minVersion) {
        return buildDetailedMessage(
                MsgCodes.ASM00241,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                newVersion,
                minVersion);
    }

    public static LocalizableMessage createOsRepoStarted(String repoName) {
        return buildLocalizableMessage(MsgCodes.ASM00242, repoName);
    }

    public static LocalizableMessage createOsRepoFinished(String repoName) {
        return buildLocalizableMessage(MsgCodes.ASM00243, repoName);
    }

    public static LocalizableMessage createOsRepoDownloadFailed(String repoName) {
        return buildLocalizableMessage(MsgCodes.ASM00244, repoName);
    }

    public static LocalizableMessage createOsRepoRazorFailed(String repoName) {
        return buildLocalizableMessage(MsgCodes.ASM00245, repoName);
    }

    public static EEMILocalizableMessage unknownOsRepoRazorImageType(String type) {
        return buildDetailedMessage(MsgCodes.ASM00318,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, type);
    }

    public static EEMILocalizableMessage importConfigNotSupported() {
        return buildDetailedMessage(
                MsgCodes.ASM00246,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
	public static EEMILocalizableMessage userDoesNotHaveAccessToPool(
	        String userId) {
		return buildDetailedMessage(MsgCodes.ASM00247, 
				EEMISeverity.ERROR,
		        EEMICategory.USER_FACING, userId);
	}

	public static EEMILocalizableMessage noServersInPool() {
		return buildDetailedMessage(MsgCodes.ASM00248, 
				EEMISeverity.ERROR,
		        EEMICategory.USER_FACING);
	}
	
    public static EEMILocalizableMessage unsupportedPathFormat(String path) {
        return buildDetailedMessage(
                MsgCodes.ASM00249,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                path);
    }


    public static EEMILocalizableMessage networkDoesNotExist(String networkId) {
        return buildDetailedMessage(MsgCodes.ASM00250, 
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, networkId);
    }

    public static EEMILocalizableMessage integratedSdCheck() {
        return buildDetailedMessage(
                MsgCodes.ASM00251,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage integratedRaidCheck() {
        return buildDetailedMessage(
                MsgCodes.ASM00252,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage storageVolumeNameAlreadyExists(final String volumeName) {
        return buildDetailedMessage(
                MsgCodes.ASM00253,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                volumeName);
    }
    
    public static EEMILocalizableMessage bootModeCheck() {
        return buildDetailedMessage(
                MsgCodes.ASM00256,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage firmwareRepositoryUsedByTemplate(String templateName) {
        return buildDetailedMessage(
                MsgCodes.ASM00259,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                templateName);
    }

    public static EEMILocalizableMessage deploymentStatusSyncFailed() {
        return buildDetailedMessage(
                MsgCodes.ASM00257,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage deploymentStatusSyncCompleted() {
        return buildDetailedMessage(
                MsgCodes.ASM00258,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage networksDuplicate() {
        return buildDetailedMessage(
                MsgCodes.ASM00260,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage networkTypeDuplicate() {
        return buildDetailedMessage(
                MsgCodes.ASM00261,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage workloadNetworksNotSame() {
        return buildDetailedMessage(
                MsgCodes.ASM00263,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage execDisableCheck() {
        return buildDetailedMessage(
                MsgCodes.ASM00264,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage fcNwtworksValidation() {
        return buildDetailedMessage(
                MsgCodes.ASM00265,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage manualServerError() {
        return buildDetailedMessage(
                MsgCodes.ASM00266,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage osRepositoryUsedByTemplate(String osRepoName, String templatesName, String deploymentsName) {
        return buildDetailedMessage(
                MsgCodes.ASM00267,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                osRepoName,
                templatesName,
                deploymentsName);
    }

    public static EEMILocalizableMessage osRepositoryStillInProgress() {
        return buildDetailedMessage(
                MsgCodes.ASM00311,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage invalidAuthenticationForStorage() {
        return buildDetailedMessage(
                MsgCodes.ASM00269,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage invalidMixedAuthenticationForStorage() {
        return buildDetailedMessage(
                MsgCodes.ASM00270,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage invalidIscsiNetworkConfigurationForServerWithEqualLogicStorageUsingIqnIp() {
        return buildDetailedMessage(
                MsgCodes.ASM00271,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage hyperVClusterNamesWithSpaces() {
        return buildDetailedMessage(
                MsgCodes.ASM00272,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage eqlMemUsedWithWrongStorage() {
        return buildDetailedMessage(
                MsgCodes.ASM00273,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage insufficientNumberOfIsciNetworksForStorageComponent() {
        return buildDetailedMessage(
                MsgCodes.ASM00274,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage firmwareRepositoryInUseByService() {
        return buildDetailedMessage(
                MsgCodes.ASM00275,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage insufficientNumberOfIsciNetworksForStorageComponentBootFromSanTemplate() {
        return buildDetailedMessage(
                MsgCodes.ASM00276,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage invalidNotes(String notes) {
        return buildDetailedMessage(
                MsgCodes.ASM00277,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,notes);
    }

    public static EEMILocalizableMessage duplicateVMNameDeployed(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00278,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                name);
    }

    public static EEMILocalizableMessage invalidNumberOfClustersForVM() {
        return buildDetailedMessage(
                MsgCodes.ASM00279,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage invalidVMAssociation() {
        return buildDetailedMessage(
                MsgCodes.ASM00280,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage resourceIsSharedAcrossServices(String resourceName) {
        return buildDetailedMessage(
                MsgCodes.ASM00281,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                resourceName);
    }
    
    public static EEMILocalizableMessage serverMissingPxeNetworkForBootDevice() {
        return buildDetailedMessage(
                MsgCodes.ASM00282,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
     }

    public static EEMILocalizableMessage invalidAddOnModuleId(final String addOnModuleId) {
    	return buildDetailedMessage(
    	        MsgCodes.ASM00283,
    	        EEMISeverity.ERROR,
    	        EEMICategory.USER_FACING,
    	        addOnModuleId);
    }
    
    public static EEMILocalizableMessage invalidVmName(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00284,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }
    
    public static EEMILocalizableMessage hyperVDomainSettingsMissing() {
        return buildDetailedMessage(
                MsgCodes.ASM00285,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage invalidDefaultGatewayNetwork() {
        return buildDetailedMessage(
                MsgCodes.ASM00286,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage noEmInInventory() {
        return buildDetailedMessage(
                MsgCodes.ASM00287,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage mustHaveESXForSD() {
        return buildDetailedMessage(
                MsgCodes.ASM00262,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
       
    public static EEMILocalizableMessage incorrectNetworkConfForBareMetalAndLinux2() {
        return buildDetailedMessage(
                MsgCodes.ASM00290,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serviceNameInUse(String serviceName) {
        return buildDetailedMessage(
                MsgCodes.ASM00295,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serviceName);
    }
    
    public static EEMILocalizableMessage noVcenterFoundForRefId(String vcenterRefId) {
        return buildDetailedMessage(
                MsgCodes.ASM00296,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                vcenterRefId);
    }
    
    public static EEMILocalizableMessage noClusterFound(String vcenterRefId, String clusterName) {
        return buildDetailedMessage(
                MsgCodes.ASM00297,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                vcenterRefId,
                clusterName);
    }
    
    public static EEMILocalizableMessage noServersAvailableForDeployment(String serviceName) {
        return buildDetailedMessage(
                MsgCodes.ASM00298,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, serviceName);
    }    
    
    public static EEMILocalizableMessage invalidParam(String paramId) {
        return buildDetailedMessage(
                MsgCodes.ASM00299,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                paramId);
    }

    public static EEMILocalizableMessage addOnModuleUploadMissingRequiredFile(final String requiredFileName) {
        return buildDetailedMessage(
                MsgCodes.ASM00300,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                requiredFileName);
    }
    
    public static EEMILocalizableMessage addOnModuleUploadMissingRequiredFields(final String... requiredFields) {
        return buildDetailedMessage(
                MsgCodes.ASM00301,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                StringUtils.arrayToCommaDelimitedString(requiredFields));
    }
    
    public static EEMILocalizableMessage addOnModuleAlreadyExists(final String moduleName) {
        return buildDetailedMessage(
                MsgCodes.ASM00302,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                moduleName);
    }

    public static EEMILocalizableMessage addOnModuleComponentAlreadyExists(final String... componentNames) {
        return buildDetailedMessage(
                MsgCodes.ASM00307,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                StringUtils.arrayToCommaDelimitedString(componentNames));
    }

    
    public static EEMILocalizableMessage addOnModuleUploadErrorOrInvalidFormat() {
        return buildDetailedMessage(
                MsgCodes.ASM00303,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }    
    
    public static EEMILocalizableMessage addOnModuleUploadInvalidFieldValues(final String... invalidFieldValues) {
        return buildDetailedMessage(
                MsgCodes.ASM00304,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                StringUtils.arrayToCommaDelimitedString(invalidFieldValues));
    }
    
    public static EEMILocalizableMessage addOnModuleUploadMissingRequiredFieldValues(final String... requiredFieldValues) {
        return buildDetailedMessage(
                MsgCodes.ASM00305,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                StringUtils.arrayToCommaDelimitedString(requiredFieldValues));
    }

    public static EEMILocalizableMessage addOnModuleUnsupportedVersionRequirements(final String... unsupportedVersions) {
        return buildDetailedMessage(
                MsgCodes.ASM00306,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                StringUtils.arrayToCommaDelimitedString(unsupportedVersions));
    }

    public static EEMILocalizableMessage osInstallationUsingStaticNetworkNotSupported() {
        return buildDetailedMessage(
                MsgCodes.ASM00308,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serviceUpdateInProgressError(String serviceName) {
        return buildDetailedMessage(
                MsgCodes.ASM00309,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serviceName);
    }

    public static EEMILocalizableMessage osRepositoryInvalid(final String repoName) {
        return buildDetailedMessage(
                MsgCodes.ASM00310,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                repoName);
    }
    
    public static EEMILocalizableMessage invalidEsxHostnameTemplate(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00312,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }
    
    public static EEMILocalizableMessage invalidEsxHostname(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00313,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }

    public static EEMILocalizableMessage dnsLookupAlreadySelected(String componentName, String networkName) {
        return buildDetailedMessage(
                MsgCodes.ASM00314,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                componentName, networkName);
    }
    
    public static EEMILocalizableMessage noRepositoryFoundForService(String serviceName) {
        return buildDetailedMessage(
                MsgCodes.ASM00315,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serviceName);
    }    
    
    public static EEMILocalizableMessage serviceIsNotManagingFirmware(String serviceName) {
        return buildDetailedMessage(
                MsgCodes.ASM00316,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serviceName);
    }
    
    public static EEMILocalizableMessage embeddedFirmwareRepositoryCannotBeDefault() {
        return buildDetailedMessage(
                MsgCodes.ASM00317,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage insufficientNumberOfHostsPerCluster(int value, int expected) {
        return buildDetailedMessage(
                MsgCodes.ASM00319,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, value, expected);
    }

    public static EEMILocalizableMessage esxTemplateMustHaveCluster() {
        return buildDetailedMessage(
                MsgCodes.ASM00320,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage invalidEqlVolName(final String volumeName) {
        return buildDetailedMessage(
                MsgCodes.ASM00321,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, volumeName);
    }

    public static EEMILocalizableMessage invalidNonRaidConfiguration() {
        return buildDetailedMessage(
                MsgCodes.ASM00322,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    public static EEMILocalizableMessage hypervEqlChapValidation() {
        return buildDetailedMessage(
                MsgCodes.ASM00323,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    public static EEMILocalizableMessage windowsServerWithVMWareCluster() {
        return buildDetailedMessage(
                MsgCodes.ASM00324,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    public static EEMILocalizableMessage unableToProcessCatalogXml() {
        return buildDetailedMessage(
                MsgCodes.ASM00325,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage uploadedServiceTemplateExists() {
        return buildDetailedMessage(
                MsgCodes.ASM00326,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage addOnModuleInUse() {
        return buildDetailedMessage(
                MsgCodes.ASM00327,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverMustHave2HVMNetworks() {
        return buildDetailedMessage(
                MsgCodes.ASM00328,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage fibreChannelInterfaceRequired(String volumeName) {
        return buildDetailedMessage(
                MsgCodes.ASM00329,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                volumeName);
    }

    public static EEMILocalizableMessage bootVolumeChecked() {
        return buildDetailedMessage(
                MsgCodes.ASM00330,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage bootVolumeNotChecked() {
        return buildDetailedMessage(
                MsgCodes.ASM00331,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage esxiHostNotFound() {
        return buildDetailedMessage(
                MsgCodes.ASM00332,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage needDefaultFirmwareCatalog() {
        return buildDetailedMessage(
                MsgCodes.ASM00333,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage canNotDeleteDefaultFirmwareCatalog() {
        return buildDetailedMessage(
                MsgCodes.ASM00334,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }
    
    public static EEMILocalizableMessage fcStorageWithServer() {
        return buildDetailedMessage(
                MsgCodes.ASM00335,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage invalidPartitionForNetwork(String serverName) {
        return buildDetailedMessage(
                MsgCodes.ASM00336,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                serverName);
    }

    public static EEMILocalizableMessage invalidRepositoryForResynchronization() {
        return buildDetailedMessage(
                MsgCodes.ASM00337,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage serverConfigurationJobStarted(String serviceTag, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00338,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,jobName,serviceTag);
    }

    public static EEMILocalizableMessage serverConfigurationJobCompleted(String serviceTag, String jobName) {
        return buildDetailedMessage(
                MsgCodes.ASM00339,
                EEMISeverity.INFO,
                EEMICategory.USER_FACING,jobName,serviceTag);
    }

    public static EEMILocalizableMessage serverConfigurationJobFailed(String msg) {
        return buildDetailedMessage(
                MsgCodes.ASM00340,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING,
                msg);

    }

    public static EEMILocalizableMessage nonexistentVolumeName(final String volumeName) {
        return buildDetailedMessage(
                MsgCodes.ASM00341,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, volumeName);
    }
    
    public static EEMILocalizableMessage duplicatePodAndVolName(final String volumeName) {
        return buildDetailedMessage(
                MsgCodes.ASM00342,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, volumeName);
    }

    public static EEMILocalizableMessage windows2008WithDomainSettings() {
        return buildDetailedMessage(
                MsgCodes.ASM00343,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage incorrectNumberOfVSANNetworks() {
        return buildDetailedMessage(
                MsgCodes.ASM00344,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage winDomainFieldsCheck() {
        return buildDetailedMessage(
                MsgCodes.ASM00345,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING);
    }

    public static EEMILocalizableMessage duplicatePortGroupName(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00346,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }

    public static EEMILocalizableMessage duplicateVDSName(String name) {
        return buildDetailedMessage(
                MsgCodes.ASM00347,
                EEMISeverity.ERROR,
                EEMICategory.USER_FACING, name);
    }
}

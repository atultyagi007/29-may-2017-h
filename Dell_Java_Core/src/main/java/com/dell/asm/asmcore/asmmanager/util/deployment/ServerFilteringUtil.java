/**************************************************************************
 *   Copyright (c) 2013 - 2015 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;


import com.dell.asm.asmcore.asmmanager.client.deployment.SelectedNIC;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.NicFQDD;
import com.dell.pg.asm.server.client.device.Controller;
import com.dell.pg.asm.server.client.device.PhysicalDisk;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.admin.rest.UserResource;
import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.app.rest.FirmwareRepositoryService;
import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentFilterResponse;
import com.dell.asm.asmcore.asmmanager.client.deployment.RejectedServer;
import com.dell.asm.asmcore.asmmanager.client.deployment.SelectedServer;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.hardware.RAIDConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerNotEnoughDisksException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.user.model.IUserResource;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.server.app.rest.ServerDeviceService;
import com.dell.pg.asm.server.client.device.IServerDeviceService;
import com.dell.pg.asm.server.client.device.LogicalNetworkInterface;
import com.dell.pg.asm.server.client.device.Server.SDCardState;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Performs server filtering for given networking settings
 */
public class ServerFilteringUtil {
    private static final Logger logger = Logger.getLogger(ServerFilteringUtil.class);

    private static final String ASM_MANAGER_CONFIG_FILE = "asm_manager.properties";
    private static final String QLOGIC_57840 ="57840";
    private static final String INTEL_QUAD ="4P";
    private static final String QLOGIC_57800 ="57800";
    private static final String FILTER_FLAG = "filter";
    private static final String SUPPORTED_EMBEDDED_NICS_PATTERN = ".*57810.*|.*X520.*";
    private static final String UNSUPPORTED_NICS_PATTERN = ".*(Broadcom|QLogic).*(5719|5720).*";
    private static final String INTEL_X520_I350 ="X520/I350";
    private static final String INTEL_X710_I350 ="X710/I350";
    private static final String INTEL_X540_I350 ="X540/I350";

    // bios attributes
    private static final String ONE_TIME_HDD_SEQ = "OneTimeHddSeqDev";
    private static final String EMB_SATA = "EmbSata";

    // Instance members exposed for mocking
    private final DeviceGroupDAO groupDao;
    private final DeviceInventoryDAO inventoryDao;
    private final HardwareUtil hardwareUtil;
    private final LocalizableMessageService logService;
    private final FirmwareRepositoryService firmwareRepositoryService;
    private final IServerDeviceService serverDeviceService;
    private Map<String, com.dell.pg.asm.server.client.device.Server> serverMap =
    				new HashMap<>();

    private Map<String, List<Map>> serverBiosMap = new HashMap<>();

    private enum Vendor {

        Broadcom, Intel, QLogic;

        public String value() {
            return name();
        }
    }

    public ServerFilteringUtil() {
        // will fail for local unit tests as it requires DB
        this.logService = LocalizableMessageService.getInstance();
        this.hardwareUtil = HardwareUtil.getInstance();
        this.groupDao = DeviceGroupDAO.getInstance();
        this.inventoryDao = new DeviceInventoryDAO();
        this.firmwareRepositoryService = new FirmwareRepositoryService();
        this.serverDeviceService = new ServerDeviceService();
    }

    public ServerFilteringUtil(DeviceGroupDAO groupDao, DeviceInventoryDAO inventoryDao,
                               HardwareUtil hardwareUtil,
                               LocalizableMessageService logService,
                               FirmwareRepositoryService firmwareRepositoryService,
                               IServerDeviceService serverDeviceService) {
        this.groupDao = groupDao;
        this.inventoryDao = inventoryDao;
        this.hardwareUtil = hardwareUtil;
        this.logService = logService;
        this.firmwareRepositoryService = firmwareRepositoryService;
        this.serverDeviceService = serverDeviceService;
    }

    /**
     * For disabled NICs wsman returns empty identity list
     * @param nic
     * @return
     */
    public boolean isNICEnabled(LogicalNetworkInterface nic, String biosString, Map<String, List<Map>> bioStringMappingCache) throws IOException {
        if (nic.getIdentityList()== null || nic.getIdentityList().size()==0) {
            logger.warn("NIC considered disabled as it has no identities: " + nic.getFqdd());
            return false;
        }

        String fqdd = nic.getFqdd();
        if (fqdd==null) return false; // sanity check
        if (!fqdd.startsWith("NIC.")) {
            return true; // don't have rules for FC
        }

        // for Embedded NIC we only support 57810
        if (fqdd.contains(NicFQDD.LOCATOR_EMBEDDED) && nic.getProductName()!=null &&
                !nic.getProductName().matches(SUPPORTED_EMBEDDED_NICS_PATTERN)) {
            return false;
        }

        // skip the all-1Gb 5720
        if (nic.getProductName()!=null && nic.getProductName().matches(UNSUPPORTED_NICS_PATTERN)) {
            return false;
        }

        Pattern pattern = Pattern.compile("NIC.(\\S+)\\.(\\S+)-(\\d+)-(\\d+)");
        Matcher matcher = pattern.matcher(fqdd);
        if (!matcher.find()) {
            logger.warn("Not checking for disabled NIC: " + fqdd);
            return true; // weird NIC fqdd
        }
        String g0 = matcher.group(1);
        String g1 = matcher.group(2);
        if (g0!=null && g1!=null) {
            String displayName = null;
            switch (g0) {
                case "Mezzanine":
                    displayName = "Mezzanine Slot " + g1;
                    break;
                case "Integrated":
                    displayName = "Integrated Network Card 1";
                    break;
                case "Slot":
                    displayName = "Slot " + g1;
                    break;
            }

            if (displayName != null) {
            	List<Map> biosList = (List<Map>)bioStringMappingCache.get(biosString);
            	if(biosList == null)
                {
            		ObjectMapper mapper = new ObjectMapper();
            		biosList = mapper.readValue(biosString, List.class);
            		bioStringMappingCache.put(biosString, biosList);  // CACHE THE DATA
                }
                for (Map ba: biosList) {
                    String attributeDisplayName = (String) ba.get("attributeDisplayName");
                    if (StringUtils.isNotEmpty(attributeDisplayName) &&
                            attributeDisplayName.equals(displayName)) {
                        String curValue = (String) ba.get("currentValue");
                        boolean retVal = "[\"Enabled\"]".equals(curValue);
                        if (!retVal) {
                            logger.warn("Found disabled by BIOS NIC:" + nic.getFqdd() + ", attributeDisplayName: " + attributeDisplayName);
                        }
                        return retVal;
                    }
                }
            }
        }

        logger.warn("No rules found to check for disabled NIC: " + fqdd);
        return true;
    }


    public class NicComparator implements Comparator<LogicalNetworkInterface> {
        @Override
        public int compare(LogicalNetworkInterface x, LogicalNetworkInterface y) {
            return x.getFqdd().compareTo(y.getFqdd());
        }
    }

    /**
     * Does not care of partition value.
     */
    public class NicComparatorWithoutPartition implements Comparator<LogicalNetworkInterface> {
        @Override
        public int compare(LogicalNetworkInterface x, LogicalNetworkInterface y) {
            NicFQDD nfx = new NicFQDD(x.getFqdd());
            NicFQDD nfy = new NicFQDD(y.getFqdd());
            String valueX = nfx.getPrefix() + "."  + nfx.getLocator() + "." + nfx.getCard() + "-" + nfx.getPort();
            String valueY = nfy.getPrefix() + "."  + nfy.getLocator() + "." + nfy.getCard() + "-" + nfy.getPort();
            return valueX.compareTo(valueY);
        }
    }

    private boolean findServerNicCapabilities(FilterEnvironment filterEnvironment, SelectedServer server,
                                         List<String> vendors) {

        Boolean serverMatch = null;
        Boolean serverMatchWithoutOrder = null;

        HashMap<String, List<Map>> bioStringMappingCache = new HashMap<String, List<Map>>();
        com.dell.pg.asm.server.client.device.Server serverInv = getServer(server.getRefId());
        if (serverInv == null) {
            logger.warn("findServerNicCapabilities: Unable to find server inventory for " + server.getRefId());
            return false;
        }
        List<LogicalNetworkInterface> nicsOriginal = serverInv.getNetworkInterfaceList();
        if (nicsOriginal == null) {
            logger.warn("findServerNicCapabilities: skipped " + server.getIpAddress() + ": no nics");
            return false;
        }

        List<LogicalNetworkInterface> nics = new ArrayList<>();
        for (LogicalNetworkInterface nic: nicsOriginal) {
            try {
                if (!isNICEnabled(nic, serverInv.getBios(), bioStringMappingCache)) continue;

            } catch (IOException e) {
                logger.error("Check for server NIC capabilities failed for server " + serverInv.getServiceTag() +
                        ", NIC skipped:" + nic.getFqdd(), e);
                continue;
            }
            nics.add(nic);
        }

        if (filterEnvironment.getPortUIMap().isEmpty()) {
            logger.debug("findServerNicCapabilities: skipped " + server.getIpAddress() + ", no interfaces defined in template");
            return false;
        }

            List<String> filteredfqdd = new ArrayList<String>();
            Set<String> serverFqdd = new HashSet<String>();
            List<String> orderedInterfaces = new ArrayList<>();
            List<String> fcInterfaces = new ArrayList<>();
            List<SelectedNIC> selectedNICs = new ArrayList<>();

            for (LogicalNetworkInterface countNic : nics) {
                NicFQDD nFqdd = new NicFQDD(countNic.getFqdd());
                String fqddNoPartition;
                if (nFqdd.getPrefix().contains("FC")) {
                    fqddNoPartition = countNic.getFqdd().trim();
                    if (!fcInterfaces.contains(fqddNoPartition))
                        fcInterfaces.add(fqddNoPartition);

                } else {
                    fqddNoPartition = nFqdd.getPrefix() + "." + nFqdd.getLocator() + "." + nFqdd.getCard() + "-" + nFqdd.getPort();
                    String oIntf = nFqdd.getPrefix() + "." + nFqdd.getLocator() + "." + nFqdd.getCard();
                    // we want only one entry per interface in this list
                    // it will define the order of cards  which should match the template
                    if (!orderedInterfaces.contains(oIntf))
                        orderedInterfaces.add(oIntf);
                }

                serverFqdd.add(fqddNoPartition);
            }

            Collections.sort(nics, new NicComparator());
            Collections.sort(orderedInterfaces, new InterfaceComparator());
            Collections.reverse(orderedInterfaces);

            // have to sort template interfaces by card number or else we might skip some NICs below
            Set<Map.Entry<String, List<String>>> templateInterfaceSet = filterEnvironment.getPortUIMap().entrySet();
            List<Map.Entry<String, List<String>>> templateInterfaceList = new ArrayList<>(templateInterfaceSet);
            Collections.sort(templateInterfaceList, new TemplateInterfaceComparator());

            if (!filterEnvironment.getFCCards().isEmpty()) {
                if (!checkFCCards(filterEnvironment, filteredfqdd, fcInterfaces)) {
                    logger.warn("findServerNicCapabilities: skipped " + server.getIpAddress() + ": FC check failed");
                    return false;
                }

                serverMatchWithoutOrder = true;
            }

            // this is check for non-FC cards
        int fcPortsNeeded = filteredfqdd.size(); // adjust for ports needed by FC, we might have those fqdds selected already
        int totalPortsNeeded = fcPortsNeeded;
            // need to run this loop twice - one for exact card order match, second for match without order
        for (int idx = 1; idx<= 2; idx++) {

            totalPortsNeeded = fcPortsNeeded;
            int correctionNumber = 0; // adjust interface # by number of FC cards - those are out of orderedInterfaces
            for (Map.Entry<String, List<String>> entry : templateInterfaceList) {

                boolean twoByTwoInterface = filterEnvironment.getTwoByTwoInterfaces().contains(entry.getKey());
                boolean iscsiInterface = filterEnvironment.getIscsiInterfaces().contains(entry.getKey());
                boolean isPartitioned = filterEnvironment.getPartitionedInterfaces().contains(entry.getKey());
                boolean staticOSInstallInterface = filterEnvironment.getStaticOSInstallInterfaces().contains(entry.getKey());

                int interfaceNumber = Integer.parseInt(entry.getKey().split(":")[0]);

                if (filterEnvironment.getFCCards().contains(entry.getKey())) {
                    // skip the card, it is already checked earlier by checkFCCards
                    correctionNumber++;
                    continue;
                }
                interfaceNumber -= correctionNumber;

                List<String> values = entry.getValue();
                List<String> portsPerSlot = new ArrayList<String>();

                totalPortsNeeded += values.size();
                // check if the filteredfqdd has the same port.
                for (String port : values) {
                    boolean portMatchesOrdered = false;
                    boolean portMatches = false;
                    int numOfPorts = values.size();

                    for (LogicalNetworkInterface nic : nics) {
                        // special case: skip ISCSI interfaces for Intel cards and Hardware ISCSI in template OS settings
                        if (iscsiInterface && filterEnvironment.isHardwareISCSI() && nic.getVendorName() != null &&
                                nic.getVendorName().contains(Vendor.Intel.value())) {

                            logger.debug("findServerNicCapabilities: " + server.getIpAddress() + ", skipped NIC: " + nic.getFqdd() +
                                    " ISCSI interfaces for Intel cards and Hardware ISCSI in template OS settings");
                            continue;
                        }

                        // special case: skip partitioned interfaces for Intel cards
                        if (isPartitioned && nic.getVendorName() != null &&
                                nic.getVendorName().contains(Vendor.Intel.value())) {
                            logger.debug("findServerNicCapabilities: " + server.getIpAddress() + ", skipped NIC: " + nic.getFqdd() +
                                    " partitioned interfaces for Intel cards");
                            continue;
                        }

                        // Special case: skip staticOSInstall interfaces for non-Intel cards
                        if (staticOSInstallInterface && nic.getVendorName() != null &&
                                !nic.getVendorName().contains(Vendor.Intel.value())) {
                            logger.debug("findServerNicCapabilities: " + server.getIpAddress() + ", skipped NIC: " + nic.getFqdd() +
                                    " staticOSInstall interfaces for non-Intel cards");
                            continue;
                        }

                        // for Brocade 57800 NICs only consider 2x10GB,2x1GB port type
                        boolean isTwoByTwo = isTwoByTwoNic(nic, nics);
                        if (twoByTwoInterface) {
                            if (!isTwoByTwo) {
                                logger.debug("findServerNicCapabilities: " + server.getIpAddress() + ", skipped NIC: " + nic.getFqdd() +
                                        " need 2x2 card for this interface: " + interfaceNumber);
                                continue;
                            }
                        } else {
                            // reverse statement is true as well
                            if (isTwoByTwo) {
                                logger.debug("findServerNicCapabilities: " + server.getIpAddress() + ", skipped NIC: " + nic.getFqdd() +
                                        " do NOT need 2x2 card for this interface: " + interfaceNumber);
                                continue;
                            }
                        }

                        NicFQDD nFqdd = new NicFQDD(nic.getFqdd());

                        if (nFqdd.getPrefix().contains("FC")) {
                            logger.debug("findServerNicCapabilities: " + server.getIpAddress() + ", skipped NIC: " + nic.getFqdd() +
                                    " FC card");

                            continue;
                        }

                        String fqddNoPartition = nFqdd.getPrefix() + "." + nFqdd.getLocator() + "." + nFqdd.getCard() + "-" + nFqdd.getPort();
                        String fqddCard = nFqdd.getPrefix() + "." + nFqdd.getLocator() + "." + nFqdd.getCard();

                        if ((nFqdd.getPort().contentEquals(port))) {
                            if (filteredfqdd.isEmpty() || !filteredfqdd.contains(fqddNoPartition)) {
                                if (nFqdd.getPrefix().contains("NIC") && stringContains(nic.getVendorName(), vendors)) {
                                    boolean nicSelected = false;
                                    switch (numOfPorts) {
                                        case 1: // how is that possible???
                                            logger.error("Invalid filtering environment: the template has a card with a single port, interface #" + interfaceNumber);
                                            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
                                        case 2:
                                            if (checkNicQuad(nic, nics)) {
                                                logger.debug("findServerNicCapabilities: " + server.getIpAddress() + ", skipped NIC: " + nic.getFqdd() +
                                                        " Quad port card");

                                                continue;
                                            }

                                            if (portsPerSlot.isEmpty()) {
                                                LogicalNetworkInterface searchNicFqdd = new LogicalNetworkInterface();
                                                String m = nFqdd.getPrefix() + "." + nFqdd.getLocator() + "." + nFqdd.getCard() + "-" + 2;
                                                searchNicFqdd.setFqdd(m);
                                                int index = Collections.binarySearch(nics, searchNicFqdd, new NicComparatorWithoutPartition());
                                                if (index >= 0) {
                                                    nicSelected = true;
                                                    break;
                                                }
                                            } else {
                                                nicSelected = true;
                                                break;
                                            }
                                            break;
                                        case 4:
                                            if (!checkNicQuad(nic, nics)) {
                                                logger.debug("findServerNicCapabilities: " + server.getIpAddress() + ", skipped NIC: " + nic.getFqdd() +
                                                        " NOT quad port card");

                                                continue;
                                            }

                                            if (portsPerSlot.isEmpty()) {
                                                String m = nFqdd.getPrefix() + "." + nFqdd.getLocator() + "." + nFqdd.getCard() + "-" + 4;
                                                LogicalNetworkInterface searchNicFqdd = new LogicalNetworkInterface();
                                                searchNicFqdd.setFqdd(m);
                                                int index = Collections.binarySearch(nics, searchNicFqdd, new NicComparatorWithoutPartition());
                                                if (index >= 0) {
                                                    nicSelected = true;
                                                    break;
                                                }
                                            } else {
                                                nicSelected = true;
                                                break;
                                            }
                                            break;

                                    }

                                    if (nicSelected) {
                                        portsPerSlot.add(fqddNoPartition);
                                        if (orderedInterfaces.indexOf(fqddCard) == interfaceNumber - 1) {
                                            portMatchesOrdered = true;
                                        } else {
                                            portMatches = true;
                                        }

                                        if (portMatchesOrdered || (idx == 2 && portMatches)) {
                                            filteredfqdd.add(fqddNoPartition);
                                            selectedNICs.add(new SelectedNIC(entry.getKey(), port, nic.getFqdd()));
                                        }
                                    }
                                }
                            }
                        }

                        if (portMatchesOrdered || (idx == 2 && portMatches))
                            break;
                    }

                    if (!portMatchesOrdered && !portMatches) {
                        logger.debug("findServerNicCapabilities: skipped " + server.getIpAddress() + ": cannot find matched NIC for interface #  " + interfaceNumber + ", port # " + port);
                        logger.debug("Selected NICs: " + selectedNICs.toString());
                        return false;
                    }

                    serverMatch = (serverMatch != null)?serverMatch && portMatchesOrdered : portMatchesOrdered ;
                    if (idx == 2) {
                        serverMatchWithoutOrder = (serverMatchWithoutOrder != null) ? serverMatchWithoutOrder && portMatches : portMatches;
                    }
                }
            }
            // only run second check if we didn't find exact match for ordered interfaces
            if (serverMatch != null && serverMatch && filteredfqdd.size() == totalPortsNeeded)
                break;
        }

        serverMatch = (serverMatch != null) && (serverMatch && filteredfqdd.size() == totalPortsNeeded);
        serverMatchWithoutOrder = (serverMatchWithoutOrder != null) && (serverMatchWithoutOrder && filteredfqdd.size() == totalPortsNeeded);

        // server has exact number of ports & partitions and also order matches
        server.setExactMatch(serverMatch && serverFqdd.size() == filteredfqdd.size());
        server.setMatchUnordered(serverMatch || serverMatchWithoutOrder);
        server.setNics(selectedNICs);

        if (serverMatch || serverMatchWithoutOrder)
            return true;
        else{
            logger.debug("findServerNicCapabilities: skipped " + server.getIpAddress() + ": not enough card for required number of ports: " + totalPortsNeeded);
            logger.debug("Selected NICs: " + selectedNICs.toString());
            return false;
        }
    }

    private boolean stringContains(String vendorName, List<String> vendors) {
        if (vendorName==null || vendors == null)
            return false;
        for (String vendor: vendors) {
            if (vendorName.contains(vendor))
                return true;
        }
        return false;
    }

    private boolean isTwoByTwoNic(LogicalNetworkInterface nic, List<LogicalNetworkInterface> nics) {
        if (nic.getProductName() == null)
            return false;

        // some cards don't have enough data on Product Name on all interfaces
        // first port seems to always have that

        LogicalNetworkInterface firstNic = findFirstNIC(nic, nics);
        if (firstNic == null || firstNic.getProductName() == null)
            return false;


        return (firstNic.getProductName().contains(QLOGIC_57800) ||
                firstNic.getProductName().contains(INTEL_X520_I350)) ||
                firstNic.getProductName().contains(INTEL_X710_I350) ||
                firstNic.getProductName().contains(INTEL_X540_I350);
    }

    /**
     * Returns true is any NIC from the list belongs to Quad port card
     * @param nics
     * @return
     */
    public boolean checkNICsQuad(List<LogicalNetworkInterface> nics) {
        return CollectionUtils.isNotEmpty(nics) && checkNicQuad(nics.get(0), nics);
    }

    private boolean checkNicQuad(LogicalNetworkInterface nic, List<LogicalNetworkInterface> nics) {
        if (nic.getProductName() == null || nic.getVendorName() == null)
            return false;

        LogicalNetworkInterface firstNic = findFirstNIC(nic, nics);
        if (firstNic == null || firstNic.getProductName() == null)
            return false;

        return (firstNic.getProductName().contains(QLOGIC_57840) || firstNic.getProductName().contains(QLOGIC_57840)
                || (firstNic.getVendorName().contains(Vendor.Intel.value()) &&
                firstNic.getProductName().contains(INTEL_QUAD) &&
                !firstNic.getProductName().contains(INTEL_X710_I350) &&
                !firstNic.getProductName().contains(INTEL_X520_I350) &&
                !firstNic.getProductName().contains(INTEL_X540_I350)));
    }

    private LogicalNetworkInterface findFirstNIC(LogicalNetworkInterface nic, List<LogicalNetworkInterface> nics) {
        NicFQDD fqdd = new NicFQDD(nic.getFqdd());
        final String nicFqdd1stPort;
        if (fqdd.getPartition() == null) {
            nicFqdd1stPort = fqdd.getCardKey() + "-1";
        }else{
            nicFqdd1stPort = fqdd.getCardKey() + "-1-1";
        }
        return (LogicalNetworkInterface) CollectionUtils.find(nics, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((LogicalNetworkInterface) object).getFqdd().equals(nicFqdd1stPort);
            }
        });
    }

    /**
     * Check if server NICs have requested number of FC cards
     * @param filterEnvironment
     * @param filteredfqdd
     * @param fcnics
     * @return
     */
    private boolean checkFCCards(FilterEnvironment filterEnvironment, List<String> filteredfqdd,
                                  List<String> fcnics) {

        for (String card : filterEnvironment.getFCCards()) {
            // need to know number of port per cards
            List<String> portNumPerCard = filterEnvironment.getPortUIMap().get(card);
            if (portNumPerCard==null) {
                logger.error("Invalid filtering environment: no template card for position " + card);
                throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
            }
            int portNum = portNumPerCard.size(); // should be always 2 right now but it is possible to trick UI into making it 4
            if (portNum != 2) {
                logger.warn("UI provided incorrect template: number of ports per FC card is not 2 but " + portNum);
                portNum = 2;
            }

            boolean cardMatches = false;
            for (String nic : fcnics) {
                NicFQDD nfqdd = new NicFQDD(nic);

                if (!filteredfqdd.contains(nic)) {
                    filteredfqdd.add(nic);
                    portNum--;
                }

                if (portNum == 0) {
                    cardMatches = true;
                    break;
                }
            }

            if (!cardMatches)
                return false; // at least one card failed check - could not find any FC card at that slot
        }
        return true;
    }

    // No permissions checking is done by this internal method
    private void getAvailableServersInternal(List<DeviceInventoryEntity> serversInPool,
                                             DeploymentEnvironment deploymentEnvironment,
                                             DeploymentFilterResponse response,
                                             FilterEnvironment filterEnvironment,
                                             int numberOfInstances) {
        if (serversInPool == null || serversInPool.size() == 0) {
            return;
        }

        boolean filter = !filterEnvironment.isMinimalServer() && checkFilteringFlag();

        DeviceInventoryEntity server;

        List<SelectedServer> serverAlmostMatch = new ArrayList<>();

        serversInPool = sortServersByHealth(serversInPool, filterEnvironment.getClonedModelString());
        RAIDConfiguration raidConfiguration = null;
        HashMap<String, ManagedDevice> managedDevicesCache = new HashMap<String, ManagedDevice>();
        
        boolean isSDPresent;
        for (DeviceInventoryEntity sEntity : serversInPool) {

            /*
             * Reject server if linked to a deployment
             */
            if (!isServerAvailable(sEntity)) {
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(),sEntity.getIpAddress(), RejectedServer.Reason.ALREADY_DEPLOYED.toString()));
                continue;
            }

            /*
             * Reject servers based on the device managed state attribute
             */
            String rejection_reason = null;
            switch(sEntity.getManagedState()) {
                case UNMANAGED:
                    rejection_reason = RejectedServer.Reason.UNMANAGED_STATE.toString();
                    break;
                case RESERVED:
                    rejection_reason = RejectedServer.Reason.RESERVED_STATE.toString();
                    break;
                default:
                    break;
            }
            if (rejection_reason != null) {
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(), sEntity.getIpAddress(), rejection_reason));
                continue;
            }

            /*
             * Reject servers based on the device state attribute
             */
            rejection_reason = null;
            switch(sEntity.getState()) {
                case DEPLOYED:
                case DEPLOYING:
                    rejection_reason = RejectedServer.Reason.ALREADY_DEPLOYED.toString();
                    break;
                case UPDATING:
                    rejection_reason = RejectedServer.Reason.UPDATING_STATE.toString();
                    break;
                case PENDING:
                case PENDING_DELETE:
                case PENDING_CONFIGURATION_TEMPLATE:
                    rejection_reason = RejectedServer.Reason.PENDING_STATE.toString();
                    break;
                case DISCOVERY_FAILED:
                    rejection_reason = RejectedServer.Reason.BAD_STATE.toString();
                    break;
                default:
                    break;
            }
            if (rejection_reason != null) {
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(), sEntity.getIpAddress(), rejection_reason));
                continue;
            }


            if (sEntity.getModel() == null) {
                continue;
            }
            if (deploymentEnvironment.getSkipServers().contains(sEntity.getServiceTag())) {
                continue;
            }


            if (!isLocalStorageVSANConditionSatisified(sEntity.getRefId(), filterEnvironment)) {
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(),sEntity.getIpAddress(), RejectedServer.Reason.VSAN_NOT_SUPPORTED.toString()));
                continue;
            }
            isSDPresent = isSDConditionSatisified(sEntity.getRefId(), filterEnvironment);
            if (!isSDPresent) {
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(),sEntity.getIpAddress(), RejectedServer.Reason.SD_NOT_PRESENT.toString()));
                continue;
            }
            raidConfiguration = isRaidConditionSatisfied(sEntity.getRefId(), filterEnvironment);
            if (raidConfiguration == null) {
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(),sEntity.getIpAddress(), RejectedServer.Reason.RAID.toString()));
                continue;
            }

            SelectedServer selectedServer = new SelectedServer(sEntity.getRefId(),sEntity.getServiceTag(),sEntity.getIpAddress(), null, raidConfiguration);

            if (isServerAvailable(sEntity) && !filter) {
                deploymentEnvironment.markServerProcesed(sEntity.getServiceTag());
                response.getSelectedServers().add(selectedServer);
                numberOfInstances--;
                if (numberOfInstances == 0) {
                    return;
                } else {
                    continue;
                }
            }

            boolean isFirmwareSatisfactory = isFirmwareConditionSatisfied(sEntity,managedDevicesCache,filterEnvironment);
            if (!isFirmwareSatisfactory) {
                deploymentEnvironment.markServerProcesed(sEntity.getServiceTag());
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(), sEntity.getIpAddress(), RejectedServer.Reason.FIRMWARE_NOT_COMPLIANT.toString()));
                continue;
            }

            if (!modelChecks(sEntity)) {
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(), sEntity.getIpAddress(), RejectedServer.Reason.BAD_MODEL.toString()));
                continue;
            }

            String model = FilterEnvironment.extractModelNumberFromString(sEntity.getModel());
            if (model.contains("M5")) {
                deploymentEnvironment.markServerProcesed(sEntity.getServiceTag());
                response.getSelectedServers().add(selectedServer);
                numberOfInstances--;
                if (numberOfInstances == 0)
                    return;
                else
                    continue;
            }

            List<String> vendors = new ArrayList<>(Arrays.asList(new String[]{
                    Vendor.Broadcom.value(),
                    Vendor.QLogic.value(),
                    Vendor.Intel.value()}));

            // check networking capabilities. Ignore servers without NICs
            if (!DeviceType.isRAServer(sEntity.getDeviceType())) {
                // no RA DB -> no nics
                continue;
            }

            // it will set exactMatch or exactMatchOrered if number of server network cards matches the template
            // it will return false if server can't be used in any card combination
            // it still will return true if server has more cards than needed by template, but exactMatch would be false
            if (!findServerNicCapabilities(filterEnvironment, selectedServer, vendors)) {
                response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(),sEntity.getIpAddress(), RejectedServer.Reason.NIC_CAPS.toString()));
                continue;
            }

            // at this point we either have exact match at sEntity - server has exact number of requested interfaces
            // or we have a list of matching servers with few extra ports

            if (selectedServer.isExactMatch()) {
                logger.debug("Server " + sEntity.getServiceTag() + " is available for deployment");
                server = sEntity;
                deploymentEnvironment.markServerProcesed(server.getServiceTag());
                response.getSelectedServers().add(selectedServer);
                numberOfInstances--;
                if (numberOfInstances == 0)
                    return;
                else
                    continue;
            }else{
                serverAlmostMatch.add(selectedServer);
            }

            logger.debug("Server was not explicitly rejected nor selected:" + sEntity.getIpAddress());
            // add to rejected list for now, will remove if it found in almostMatch list
            response.getRejectedServers().add(new RejectedServer(sEntity.getRefId(),sEntity.getIpAddress(), RejectedServer.Reason.OTHER.toString()));

        } // end of servers in the pool


        if (!serverAlmostMatch.isEmpty()) {
            for (SelectedServer serv : serverAlmostMatch) {
                deploymentEnvironment.markServerProcesed(serv.getServiceTag());
                response.getSelectedServers().add(serv);
                Iterator<RejectedServer> rsIterator = response.getRejectedServers().iterator();
                while (rsIterator.hasNext()) {
                    RejectedServer rs = rsIterator.next();
                    if (rs.getRefId().equals(serv.getRefId())) {
                        rsIterator.remove();
                        break;
                    }
                }
                numberOfInstances--;
                if (numberOfInstances == 0) return;

            }
        }
    }

    /**
     * Whether the server model is supported for full server component deployment.
     *
     * Any Dell server for which iDrac inventory is available is currently supported.
     *
     * @param sEntity the server device inventory entity
     * @return true if the server model is supported, false otherwise.
     */
    private boolean modelChecks(DeviceInventoryEntity sEntity) {
        if (!sEntity.getVendor().contains("Dell")) {
            logger.debug("Server " + sEntity.getIpAddress() + " skipped by filtering, vendor is not Dell");
            return false;
        }
        String model = FilterEnvironment.extractModelNumberFromString(sEntity.getModel());
        if (model == null) {
            logger.debug("Server " + sEntity.getIpAddress() + " skipped by filtering, model is null");
            return false;
        }

        // Can't support the server if we don't have iDrac inventory
        if (getServer(sEntity.getRefId()) == null) {
            logger.debug("Server " + sEntity.getIpAddress() + " model " + model + " skipped by filtering, iDrac inventory not available");
            return false;
        }

        return true;
    }

    public void getAvailableServers(String serverRefId,
                                    DeploymentEnvironment deploymentEnvironment,
                                    DeploymentFilterResponse response,
                                    FilterEnvironment filterEnvironment,
                                    int numberOfInstances) {

        List<DeviceGroupDAO.BriefServerInfo> serverInfos = groupDao.getAccessibleServers(
                deploymentEnvironment.getUserID(), Arrays.asList(serverRefId));

        List<String> refIds = new ArrayList<>(serverInfos.size());
        for (DeviceGroupDAO.BriefServerInfo serverInfo : serverInfos) {
            refIds.add(serverInfo.getRefId());
        }
        List<DeviceInventoryEntity> servers = inventoryDao.getDevicesByIds(refIds);

        getAvailableServersInternal(servers, deploymentEnvironment, response,
                filterEnvironment, numberOfInstances);

        // sort selected servers - exact match goes first followed by unordered match followed by others
        Collections.sort(response.getSelectedServers(), new SelectedServerComparator());
    }

    public void getAvailableServerFromPool(String poolID,
                                           DeploymentEnvironment deploymentEnvironment,
                                           DeploymentFilterResponse response,
                                           FilterEnvironment filterEnvironment,
                                           int numberOfInstances) {
        DeploymentEnvironment.DeviceGroupInfo groupInfo = deploymentEnvironment.getCachedDeviceGroup(poolID);
        if (groupInfo == null) {

            List<DeviceGroupDAO.BriefServerInfo> serverInfos = groupDao.getAccessiblePoolServers(
                    deploymentEnvironment.getUserID(), poolID);

			// Check for ERRORS
			if (serverInfos == null || serverInfos.size() == 0) {
				if (groupDao.getNumberOfServersInPool(poolID) > 0) {
					/*
					Means Servers ARE Available but the user does to have
					access to them
					*/
                    logger.error("getAvailableServerFromPool: there are servers in the pool but no servers available for user ID=" +
                            deploymentEnvironment.getUserID() + ", pool ID=" + poolID);
                    IUserResource userResource = new UserResource();
                    String userName = userResource
					        .getUser(deploymentEnvironment.getUserID())
					        .getUserName();
					throw new LocalizedWebApplicationException(
					        Response.Status.NOT_FOUND,
					        AsmManagerMessages
					                .userDoesNotHaveAccessToPool(userName));
				} else { // Means there are NO Servers available throw a
						 // different error
                    logger.error("getAvailableServerFromPool: no servers available for user " + deploymentEnvironment.getUserID() + " in the pool " + poolID);
					throw new LocalizedWebApplicationException(
					        Response.Status.NOT_FOUND,
					        AsmManagerMessages.noServersInPool());
				}
			}

            List<String> refIds = new ArrayList<>(serverInfos.size());
            for (DeviceGroupDAO.BriefServerInfo serverInfo : serverInfos) {
                refIds.add(serverInfo.getRefId());
            }
            List<DeviceInventoryEntity> servers = inventoryDao.getDevicesByIds(refIds);

            deploymentEnvironment.addCachedDeviceGroup(poolID, servers);
            groupInfo = deploymentEnvironment.getCachedDeviceGroup(poolID);
        }

        if (numberOfInstances < 0) {
            // special case: need all available servers
            if (groupInfo.getServers() != null) {
                numberOfInstances = groupInfo.getServers().size();
            } else {
                numberOfInstances = 1;
            }
        }

        getAvailableServersInternal(groupInfo.getServers(), deploymentEnvironment, response,
                filterEnvironment, numberOfInstances);

        // sort selected servers - exact match goes first followed by unordered match followed by others
        Collections.sort(response.getSelectedServers(), new SelectedServerComparator());
    }

    private boolean isSDConditionSatisified(String refId,
			FilterEnvironment filterEnvironment) {
    	boolean isSDBoot = filterEnvironment.isSDBoot();
		if(isSDBoot)
		{
            com.dell.pg.asm.server.client.device.Server server = getServer(refId);
            if (server == null) {
                return false;
            }

	        SDCardState sdState = server.getSdState();
	        if (sdState.equals(SDCardState.SD_ABSENT) || sdState.equals(SDCardState.SD_PRESENT))
	        return true;
	  	}
		else
			//UI does not have SD boot option selected, just return true to move on to other filtering conditions
			return true;
		
		return false;
	}


    /**
     * There are requirements for VSAN storage as well as for boot drive.
     * @param refId Server to check
     * @param filterEnvironment
     * @return
     */
    private boolean isLocalStorageVSANConditionSatisified(String refId,
                                            FilterEnvironment filterEnvironment) {

        if(filterEnvironment.isLocalStorageForVSANEnabled()) {
            com.dell.pg.asm.server.client.device.Server server = getServer(refId);
            if (server == null) {
                return false;
            }

            boolean localBootCheckPassed = true;
            boolean hasSATADOM = isSATADOMAvailable(server);
            String fqdd = null;
            if (!hasSATADOM) {
                fqdd = getVSANSupportedController(server, filterEnvironment);
            }

            boolean storageSupported = hasSATADOM || fqdd != null;

            if (filterEnvironment.isLocalDiskVSANBoot()) {
                localBootCheckPassed = hasSATADOM || isSATAAHCIMode(server)
                        || isLocalDiskBootForVSANSupported(server, fqdd);
            }


            logger.debug("isLocalDiskVSANBootConditionSatisified: local boot check=" + localBootCheckPassed +
                    ", VSAN storage supported=" + storageSupported + " for server IP=" + server.getManagementIP());
            return (localBootCheckPassed && storageSupported);
        }
        else
            return true;
    }

    private boolean isFirmwareConditionSatisfied(DeviceInventoryEntity device,
                                                 HashMap<String, ManagedDevice> managedDevicesCache,
                                                 FilterEnvironment filterEnvironment) {
        // Demand compliance
        if (filterEnvironment.getCompliantState() == CompliantState.COMPLIANT) {
            return isServerFirmwareCompliant(device, managedDevicesCache);
        } else {
            return true;
        }
    }

	private RAIDConfiguration isRaidConditionSatisfied(String refId, FilterEnvironment filterEnvironment) {
        if (filterEnvironment.getRaidConfiguration()==null)
            return new RAIDConfiguration();

        com.dell.pg.asm.server.client.device.Server raServer = getServer(refId);
        if (raServer == null) {
            return null;
        }

        try {
            return hardwareUtil.prepareRAID(raServer.getControllers(), filterEnvironment);
        }catch(AsmManagerNotEnoughDisksException e) {
            logger.warn("No suitable RAID controllers found on server " + raServer.getManagementIP(), e);
            return null;
        }
    }

    //sort the list of servers by health by arranging in this order : GREEN, YELLOW, RED, UNKNOWN
    public static List<DeviceInventoryEntity> sortServersByHealth(List<DeviceInventoryEntity> servers, String cloneModel) {
    	if (servers != null)
        {
            Collections.sort(servers, new HealthComparator());
            if (cloneModel != null)
            {
            	List<DeviceInventoryEntity> cloneServers = sortCloneModelMatch(servers, cloneModel);
            	return cloneServers;
            }
           
        }
        
        return servers;

    }
    
    private static List<DeviceInventoryEntity> sortCloneModelMatch(List<DeviceInventoryEntity> servers, String cloneModel) {
        List<DeviceInventoryEntity> newList = new ArrayList<>();
        int max_count = 8;
        int count = 0;
        while (count <= max_count) {
            for (DeviceHealth h : DeviceHealth.values()) {
                for (DeviceInventoryEntity en : servers) {
                    String model = FilterEnvironment.extractModelNumberFromString(en.getModel());
                    if (model != null) {
                        if (en.getFailuresCount() == count && model.contains(cloneModel.substring(0, 1)) && en.getHealth().equals(h)) {
                            newList.add(en);
                        } else if (en.getFailuresCount() == count && model.equalsIgnoreCase(cloneModel) && en.getHealth().equals(h)) {
                            newList.add(en);
                        }
                    }
                }

            }
            // now check for mixed types result to add
            getServersOriginalHealthCheck(servers, newList, cloneModel, count);
            count++;

        }
        return newList;
    }
    
    private static void getServersOriginalHealthCheck(List<DeviceInventoryEntity> servers, List<DeviceInventoryEntity> newList, String cloneModel,
            int count) {
        for (DeviceHealth h : DeviceHealth.values()) {
            for (DeviceInventoryEntity en : servers) {
                String model = FilterEnvironment.extractModelNumberFromString(en.getModel());
                if (model != null) {
                    if (en.getFailuresCount() == count && en.getHealth().equals(h) && !model.contains(cloneModel.substring(0, 1))) {
                        newList.add(en);
                    }
                }
            }

        }

    }


    /**
     * Sort by n_failures (descending), health
     */
    public static class HealthComparator implements Comparator<DeviceInventoryEntity> {
        @Override
        public int compare(DeviceInventoryEntity x, DeviceInventoryEntity y) {
            if (x.getFailuresCount()> y.getFailuresCount())
                return 1;
            else if (x.getFailuresCount()< y.getFailuresCount())
                return -1;
            else
                return x.getHealth().compareTo(y.getHealth());
        }
    }

    private boolean checkFilteringFlag() {
        InputStream input = null;
        try {
            Properties prop = new Properties();
            input = DeploymentService.class.getClassLoader().getResourceAsStream(ASM_MANAGER_CONFIG_FILE);
            prop.load(input);
            boolean filter = Boolean.parseBoolean(prop.getProperty(FILTER_FLAG));
            return filter;
        } catch (IOException e) {
            logger.info("Exception while parsing asmmanager properties file", e);
            throw new AsmManagerRuntimeException(e);
        } finally {
            try {
                if (null != input) {
                    input.close();
                }
            } catch (IOException ioe) {
                logger.error("Error closing input stream for file " + ASM_MANAGER_CONFIG_FILE + ": " + ioe.getMessage());
            }
        }

    }

    private boolean isServerFirmwareCompliant(DeviceInventoryEntity device, HashMap<String, ManagedDevice> managedDevicesCache) {
	if (device != null) {
            CompliantState compliance = CompliantState.fromValue(device.getCompliant());
            if (compliance != null && (compliance == CompliantState.UPDATEREQUIRED)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method will get the port/nic cards from UI
     * @param component
     * @param servletRequest
     * @return
     */
    public FilterEnvironment initFilterEnvironment(ServiceTemplateComponent component,
                                                  final HttpServletRequest servletRequest) {

        FilterEnvironment filterEnvironment = new FilterEnvironment();
        filterEnvironment.initEnvironment(component);

        // Side effect of initializing the FilterEnvironment.
        firmwareRepositoryService.setServletRequest(servletRequest);
        return filterEnvironment;
    }

    /**
     * Check the deployment count for server availability
     *
     * @param server
     * @return
     */
    private boolean isServerAvailable(DeviceInventoryEntity server) {
        if (server.getDeploymentCount() > 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *  Loop over given deployments searching for inclusion of the given server ID
     *
     * @param serverID
     * @param deployments
     * @return
     */
    private boolean isServerAvailable(String serverID, List<DeploymentEntity> deployments) {
        if (deployments != null) {
            for (int i = 0; i < deployments.size(); i++) {
                Set<DeviceInventoryEntity> serversDeployed = deployments.get(i).getDeployedDevices();
                if (serversDeployed != null) {
                    for (DeviceInventoryEntity currDevice : serversDeployed) {
                        if (currDevice.getRefId().compareToIgnoreCase(serverID) == 0) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }



    /**
     * If the clondeModelNumber is null then we can't do a comparison so just say its alright.  This case can happen when we are deploying a non-cloned server,
     * or when the server is of a type that we can't parse.
     * @param server The server we want to compare against a model number
     * @param clonedModelNumber The extracted model number of a cloned server or null if not applicable.
     * @return true/false
     */
    public boolean modelIsAppropriate(DeviceInventoryEntity server, String clonedModelNumber)
    {
        if (server != null && clonedModelNumber != null)
        {
            String thisModelNumber = FilterEnvironment.extractModelNumberFromString(server.getModel());
            logger.debug("server model " + thisModelNumber + " clone modelnumber: " + clonedModelNumber + " are equal: " + clonedModelNumber.equals(thisModelNumber));
            return clonedModelNumber.equals(thisModelNumber);
        }

        logger.info("No model number comparison performed server was null: " + (server == null) + " clonedModelNumber was: " + clonedModelNumber);
        return true;
    }

    /**
     * Returns the cached server inventory, or looks it up if not already cached.
     *
     * Returns null if the specified server refId does not exist in server inventory.
     *
     * @param refId The server ref_id
     * @return The server inventory
     */
	private com.dell.pg.asm.server.client.device.Server getServer(String refId) {
        if (!serverMap.containsKey(refId)) {
            try {
                com.dell.pg.asm.server.client.device.Server server = serverDeviceService.getServer(refId);
                serverMap.put(refId, server);
            } catch (WebApplicationException e) {
                if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                    serverMap.put(refId, null);
                } else {
                    throw e;
                }
            }
        }
        return serverMap.get(refId);
	}

    /**
     * Ignore prefix, Integrated || Embedded goes first, next compare card number
     */
    private class InterfaceComparator implements Comparator<String> {
        @Override
        public int compare(String x, String y) {
            String[] xarr = x.split("\\.");
            String[] yarr = y.split("\\.");

            // Integrated or Embedded is higher order than anything else
            int rankX = (NicFQDD.LOCATOR_INTEGRATED.equals(xarr[1]) || NicFQDD.LOCATOR_EMBEDDED.equals(xarr[1]))?1:-1;
            int rankY = (NicFQDD.LOCATOR_INTEGRATED.equals(yarr[1]) || NicFQDD.LOCATOR_EMBEDDED.equals(yarr[1]))?1:-1;

            if (rankX > rankY) {
                return 1;
            }
            if (rankX < rankY) {
                return -1;
            }

            // same locator, compare card numbers
            int xc = parseCard(xarr[2]);
            if (xc<0)
                return -1;

            int yc = parseCard(yarr[2]);
            if (yc<0)
                return -1;

            if (xc == yc)
                return 0;
            else if (xc < yc) // 1 is higher order than 5
                return 1;
            else
                return -1;
        }
    }

    /**
     * Card number can be a digit (for racks) or alphanumeric, like 2B, 1C etc for blades.
     * For blades, this method translates letters into card number, either 2 (for B) or 3 (for C).
     *
     * @param card
     * @return
     */
    protected int parseCard(String card) {
        try {
            int x = 0;
            // can be alphanumeric for blades
            Pattern slotPattern = Pattern.compile("^(\\d)(B|C)$");
            Matcher slotMatcher = slotPattern.matcher(card);
            if (slotMatcher.matches()) {
                //String cardNumber = slotMatcher.group(1);
                String cardLetter = slotMatcher.group(2);
                if (cardLetter!=null)
                    x = cardLetter.equals("B")?2:3;
            }else {
                x = Integer.parseInt(card);
            }
            return x;
        }catch(NumberFormatException e) {
            logger.error("Cannot parse card number in FQDD: " + card,e );
            return -1;
        }
    }

    /**
     * Sort selected servers - exact match goes first followed by unordered match followed by others sorted by IP
     */
    private class SelectedServerComparator implements Comparator<SelectedServer> {
        @Override
        public int compare(SelectedServer x, SelectedServer y) {

            if (x.isExactMatch() && !y.isExactMatch())
                return 1;

            if (y.isExactMatch() && !x.isExactMatch())
                return -1;

            // both exact match
            if (x.isExactMatch()) {
                return x.getIpAddress().compareTo(y.getIpAddress());
            }

            if (x.isMatchUnordered() && !y.isMatchUnordered())
                return 1;

            if (y.isMatchUnordered() && !x.isMatchUnordered())
                return -1;

            return x.getIpAddress().compareTo(y.getIpAddress());
        }
    }

    private class TemplateInterfaceComparator implements Comparator<Map.Entry<String, List<String>>> {
        @Override
        public int compare(Map.Entry<String, List<String>> x, Map.Entry<String, List<String>> y) {
            if (x == null || y == null) return 0;

            try {
                String xkey = x.getKey();
                int xCardNum = Integer.parseInt(xkey.split(":")[0]);

                String ykey = y.getKey();
                int yCardNum = Integer.parseInt(ykey.split(":")[0]);

                return Integer.compare(xCardNum, yCardNum);
            }catch (NumberFormatException nfx) {
                logger.error("Invalid coding of interface key: " + x.getKey() + "," + y.getKey() );
                return 0;
            }
        }
    }

    /**
     * Returns true if server Embedded SATA BIOS attribute is set to AHCI mode.
     * @param server
     * @return
     */
    private boolean isSATAAHCIMode(com.dell.pg.asm.server.client.device.Server server) {
        String biosString = server.getBios();

        List<Map> biosList = findBios(server.getRefId(), biosString);
        boolean hasAhciMode = false;

        for (Map ba : biosList) {
            String attributeDisplayName = (String) ba.get("attributeName");
            if (StringUtils.isNotEmpty(attributeDisplayName)) {
                if (EMB_SATA.equals(attributeDisplayName)) {
                    String possibleValues = (String) ba.get("possibleValues");

                    hasAhciMode = possibleValues.contains("\"AhciMode\"");
                    if (!hasAhciMode) {
                        logger.debug("AhciMode not available for EmbSata attribute for " + server.getManagementIP());
                        return false;
                    }
                }
            }
        }

        if (hasAhciMode) {
            logger.debug("AhciMode supported for server " + server.getManagementIP());
            return true;
        } else {
            logger.debug("AhciMode not supported for server " + server.getManagementIP());
            return false;
        }
    }

    private List<Map> findBios(String refId, String biosString) {

        if (serverBiosMap.containsKey(refId)) {
            return serverBiosMap.get(refId);
        }else {
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<Map> biosList = mapper.readValue(biosString, List.class);
                serverBiosMap.put(refId, biosList);
                return biosList;
            } catch (IOException e) {
                logger.error("Invalid BIOS string for server " + refId + ":" + biosString, e);
                return null;
            }
        }
    }

    // Disk.SATAEmbedded
    /**
     * Returns true if attribute OneTimeHddSeqDev has SATADOM in possible values .
     * @param server
     * @return
     */
    private boolean isSATADOMAvailable(com.dell.pg.asm.server.client.device.Server server) {
        String biosString = server.getBios();

        List<Map> biosList = findBios(server.getRefId(), biosString);
        boolean hasSATADOM = false;

        for (Map ba: biosList) {
            String attributeName = (String) ba.get("attributeName");
            if (StringUtils.isNotEmpty(attributeName)) {
                if (ONE_TIME_HDD_SEQ.equals(attributeName)) {
                    String possibleValues = (String) ba.get("possibleValuesDescription");

                    hasSATADOM = possibleValues != null && possibleValues.contains("SATADOM");
                    if (!hasSATADOM) {
                        logger.debug("SATADOM not available for OneTimeHddSeqDev attribute for " + server.getManagementIP());
                        return false;
                    }
                }
            }
        }

        if (hasSATADOM)
            return true;
        else {
            logger.debug("No OneTimeHddSeqDev BIOS attribute found for server " + server.getManagementIP());
            return false;
        }
    }

    /**
     * Performs check for supported storage.
     * We support HBA330, H730, FD33x
     * For hybrid mode we need at least SSD and one HDD
     * For flash mode we need all SSDs on supported controller
     * @param server
     * @param filterEnvironment
     * @return
     */
    private String getVSANSupportedController(com.dell.pg.asm.server.client.device.Server server,
                                              FilterEnvironment filterEnvironment) {

        for (Controller controller : server.getControllers()) {
            if (controller.getProductName() == null ||
                    (!controller.getProductName().contains("HBA330") &&
                            !controller.getProductName().contains("H730") &&
                            !controller.getProductName().contains("FD33x")
                    )) {
                logger.debug("isVSANSupportedController: Ignore controller " + controller.getProductName() + "  for server " + server.getManagementIP());
                continue;
            }

            List<PhysicalDisk> physicalDisks = controller.getPhysicalDisks();
            boolean foundHDD = false;
            boolean foundSSD = false;
            for (PhysicalDisk pd : physicalDisks) {
                if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.HDD) {
                    foundHDD = true;
                }else if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.SSD) {
                    foundSSD = true;
                }
                if (foundHDD && foundSSD)
                    break; // quit early if found both
            }

            logger.debug("getVSANSupportedController: checking candidate controller " + controller.getProductName() +
                    "  for server " + server.getManagementIP() +
                    ", SSD found=" + foundSSD + ", HDD found = " + foundHDD +
                    ", all flash mode = " + filterEnvironment.isAllFlash());

            if (filterEnvironment.isAllFlash()) {
                if (foundSSD && !foundHDD) {
                    return controller.getFqdd();
                }
            } else {
                if (foundSSD && foundHDD) {
                    return controller.getFqdd();
                }
            }
        }
        logger.debug("getVSANSupportedController: Controller and physical disks condition not satisfied for server " + server.getManagementIP());
        return null;
    }

    /**
     * Performs check for supported local disk boot for VSAN.
     * For boot we need HDD on non-VSAN controller
     * @param server
     * @return
     */
    private boolean isLocalDiskBootForVSANSupported(com.dell.pg.asm.server.client.device.Server server, String fqdd) {
        for (Controller controller : server.getControllers()) {
            if (controller.getFqdd() == null ||
                    controller.getFqdd().equals(fqdd)) {
                // skip controller used for VSAN
                continue;
            }

            // any drive will work
            if (CollectionUtils.isNotEmpty(controller.getPhysicalDisks())) {
                return true;
            }
        }
        logger.debug("isLocalDiskBootForVSANSupported: Not found any controller for a server " + server.getManagementIP());
        return false;
    }
}

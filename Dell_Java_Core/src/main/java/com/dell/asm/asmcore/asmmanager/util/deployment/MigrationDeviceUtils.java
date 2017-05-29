package com.dell.asm.asmcore.asmmanager.util.deployment;

import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.app.rest.DeviceInventoryService;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.rest.common.util.FilterParamParser;

import com.dell.pg.asm.server.client.device.IServerDeviceService;
import com.dell.pg.asm.server.client.device.LogicalNetworkInterface;

public class MigrationDeviceUtils {

    public enum MigrateMatch {

        EXACT, ALMOST;

        public String value() {
            return name();
        }

        public static MigrateMatch fromValue(String v) {
            return valueOf(v);
        }

    }

    private static final Logger logger = Logger.getLogger(MigrationDeviceUtils.class);

    private final DeviceInventoryDAO deviceInventoryDAO;
    private final ServerFilteringUtil filteringUtil;
    private final IServerDeviceService serverDeviceProxy;

    /**
     * Default constructor.
     */
    public MigrationDeviceUtils(){
        deviceInventoryDAO = new DeviceInventoryDAO();
        filteringUtil = new ServerFilteringUtil();
        serverDeviceProxy = ProxyUtil.getDeviceServerProxyWithHeaderSet();
    }

    public Map<String, DeviceInventoryEntity> migrateFilterServer(DeviceInventoryEntity server, String poolID, List <String> attemptedServers) {

        Map<String, DeviceInventoryEntity> filteredServer = new HashMap<String, DeviceInventoryEntity>();
       
        if (server != null) {

            List<LogicalNetworkInterface> nicsOfRefServer = new ArrayList<LogicalNetworkInterface>();
            boolean isQuad = false;
            if (DeviceType.isRAServer(server.getDeviceType())) {
                isQuad = filteringUtil.checkNICsQuad(nicsOfRefServer);
            }
            getServerNics(server, nicsOfRefServer);
            
            // Get all pools
            List<DeviceGroupEntity> deviceGroups = null;
            DeviceGroupDAO deviceGroupDAO = DeviceGroupDAO.getInstance();
            try {
                deviceGroups = deviceGroupDAO.getAllDeviceGroup(null, null, null);
            } catch (AsmManagerCheckedException e) {
                logger.error("Unable to get device groups", e);
            }
            if (poolID.compareToIgnoreCase(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID) == 0) {

                // Get all servers
                List<String> filter = new ArrayList<String>();
                filter.add("eq,deviceType,RackServer, TowerServer, BladeServer, Server, FXServer");
                FilterParamParser filterParser = new FilterParamParser(filter, DeviceInventoryService.validFilterColumns);
                List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();
                List<DeviceInventoryEntity> servers = deviceInventoryDAO.getAllDeviceInventory(null, filterInfos, null);
                if (servers != null) {
                    logger.debug("Found " + servers.size() + " servers in the ASM DB");

                    filterOutAttemptedServers(servers, attemptedServers);

                    filteredServer = filterMigrateServerCombinations(servers, server, nicsOfRefServer, isQuad);
                    return filteredServer;
                    // now go through all the servers in the pool to look for a perfect match.

                }

            } else {

                if (deviceGroups != null) {
                    for (int i = 0; i < deviceGroups.size(); i++) {
                        String sID = "" + deviceGroups.get(i).getSeqId();
                        if (sID.compareToIgnoreCase(poolID) == 0) {
                            logger.debug("Found pool with ID " + poolID);
                            List<DeviceInventoryEntity> serversInPool = deviceGroups.get(i).getDeviceInventories();
                            if (serversInPool != null) {

                                filterOutAttemptedServers(serversInPool, attemptedServers);

                                filteredServer = filterMigrateServerCombinations(serversInPool, server, nicsOfRefServer, isQuad);
                                return filteredServer;
                            }
                        }

                    }
                }

            }

        }
        return filteredServer;

    }

    private void filterOutAttemptedServers(List<DeviceInventoryEntity> servers, List <String> attemptedServers) {
        Iterator<DeviceInventoryEntity> it = servers.iterator();
        while (it.hasNext()) {
            DeviceInventoryEntity aServer = it.next();
            if (attemptedServers.contains(aServer.getRefId())) {
                it.remove();
            }
        }
    }

    private Map<String, DeviceInventoryEntity> filterMigrateServerCombinations(List<DeviceInventoryEntity> serversInPool,
            DeviceInventoryEntity server, List<LogicalNetworkInterface> nicsOfRefServer, boolean isQuad) {

        ServerFilteringUtil.sortServersByHealth(serversInPool,null);

        DeviceInventoryEntity migrateServer = new DeviceInventoryEntity();
        Map<String, DeviceInventoryEntity> filteredServer = new HashMap<String, DeviceInventoryEntity>();
        migrateServer = matchExactMigrateServer(serversInPool, server, nicsOfRefServer, migrateServer);

        if (migrateServer != null) {
            filteredServer.put(MigrateMatch.EXACT.name(), migrateServer);
            return filteredServer;
        }

        migrateServer = matchAlmostMigrateServerOnSameModel(serversInPool, server, nicsOfRefServer, migrateServer, isQuad);
        if (migrateServer != null) {
            filteredServer.put(MigrateMatch.ALMOST.name(), migrateServer);
            return filteredServer;
        }

        migrateServer = matchAlmostMigrateBladeServer(serversInPool, server, nicsOfRefServer, migrateServer, isQuad);
        if (migrateServer != null) {
            filteredServer.put(MigrateMatch.ALMOST.name(), migrateServer);
            return filteredServer;
        }

        migrateServer = matchAlmostRackServer(serversInPool, server, nicsOfRefServer, migrateServer, isQuad);

        if (migrateServer != null) {
            filteredServer.put(MigrateMatch.ALMOST.name(), migrateServer);
            return filteredServer;
        }

        migrateServer = matchBladewithRackServer(serversInPool, server, nicsOfRefServer, migrateServer, isQuad);

        if (migrateServer != null) {
            filteredServer.put(MigrateMatch.ALMOST.name(), migrateServer);
            return filteredServer;

        }
        return filteredServer;

    }

    private DeviceInventoryEntity matchAlmostMigrateServerOnSameModel(List<DeviceInventoryEntity> serversInPool, DeviceInventoryEntity server,
            List<LogicalNetworkInterface> nicsOfRefServer, DeviceInventoryEntity migrateServer, boolean isQuad) {

        for (int j = 0; j < serversInPool.size(); j++) {
             if (serversInPool.get(j).getState() != DeviceState.READY ||
                     serversInPool.get(j).getManagedState() != ManagedState.MANAGED) {
                 continue;
             }

            if (serversInPool.get(j).getServiceTag().equalsIgnoreCase(server.getServiceTag())) {
                continue;
            }

            if (filteringUtil.modelIsAppropriate(serversInPool.get(j), FilterEnvironment.extractModelNumberFromString(server.getModel()))) {
                // now check their nic configration.
                List<LogicalNetworkInterface> nicsOfTargetServer = new ArrayList<LogicalNetworkInterface>();
                boolean isQuadTarget = false;
                if (DeviceType.isRAServer(server.getDeviceType())) {
                    //check for quad port
                    isQuadTarget = filteringUtil.checkNICsQuad(nicsOfTargetServer);
                }
                if ((isQuad && !isQuadTarget) || (!isQuad && isQuadTarget))
                {
                    continue;
                }
                getServerNics(serversInPool.get(j), nicsOfTargetServer);
                if (nicsOfRefServer != null && !nicsOfRefServer.isEmpty() && nicsOfTargetServer != null && !nicsOfTargetServer.isEmpty()) {

                    if (nicsOfTargetServer.size() >= nicsOfRefServer.size()) {
                        List<String> nics = new ArrayList<String>();
                        for (int indexOfRefServer = 0; indexOfRefServer < nicsOfRefServer.size(); indexOfRefServer++) {

                            for (int indexOfTargetServer = 0; indexOfTargetServer < nicsOfTargetServer.size(); indexOfTargetServer++) {
                                if (nicsOfTargetServer.get(indexOfTargetServer).getFqdd().contains(nicsOfRefServer.get(indexOfRefServer).getFqdd())) {
                                    nics.add(nicsOfRefServer.get(indexOfRefServer).getFqdd());
                                    break;
                                }
                            }

                        }
                        if (!nics.isEmpty())
                            Collections.sort(nics);
                        if (nics.size() == nicsOfRefServer.size()) {
                            return serversInPool.get(j);

                        }
                    }

                }
                //

            }

        }
        return null;
    }

    private DeviceInventoryEntity matchBladewithRackServer(List<DeviceInventoryEntity> serversInPool, DeviceInventoryEntity server,
            List<LogicalNetworkInterface> nicsOfRefServer, DeviceInventoryEntity migrateServer, boolean isQuad) {
        for (int j = 0; j < serversInPool.size(); j++) {
            if (serversInPool.get(j).getState() != DeviceState.READY ||
                    serversInPool.get(j).getManagedState() != ManagedState.MANAGED) {
                continue;
            }
            if (serversInPool.get(j).getServiceTag().equalsIgnoreCase(server.getServiceTag())) {
                continue;
            }
            if ((server.getModel().contains("R520")) || server.getModel().contains("M520")) {
                return serversInPool.get(j);
            }
            if ((server.getModel().contains("R") && serversInPool.get(j).getModel().contains("M")) || (server.getModel().contains("M") && serversInPool.get(j).getModel().contains("R"))) {
                migrateServer = rackBladeServerCheck(serversInPool.get(j), isQuad, nicsOfRefServer);
                if (migrateServer != null) {
                    return migrateServer;
                }
            }

        }
        return null;
    }

    private DeviceInventoryEntity matchAlmostRackServer(List<DeviceInventoryEntity> serversInPool, DeviceInventoryEntity server,
            List<LogicalNetworkInterface> nicsOfRefServer, DeviceInventoryEntity migrateServer, boolean isQuad) {
        for (int j = 0; j < serversInPool.size(); j++) {
            if (serversInPool.get(j).getState() != DeviceState.READY ||
                    serversInPool.get(j).getManagedState() != ManagedState.MANAGED) {
                continue;
            }
            if (serversInPool.get(j).getServiceTag().equalsIgnoreCase(server.getServiceTag())) {
                continue;
            }
            if (server.getModel().contains("R520")) {
                return serversInPool.get(j);
            }
            if (server.getModel().contains("R") && serversInPool.get(j).getModel().contains("R")) {
                migrateServer = rackBladeServerCheck(serversInPool.get(j), isQuad, nicsOfRefServer);
                if (migrateServer != null) {
                    return migrateServer;
                }
            }

        }
        return null;
    }

    private DeviceInventoryEntity rackBladeServerCheck(DeviceInventoryEntity serversInPool, boolean isQuad, List<LogicalNetworkInterface> nicsOfRefServer) {
        // nic.integrated 1-1, 1-2. make sure u have 1-1
        // now check their nic configration.
        boolean serverMatch = false;

        if (!DeviceType.isRAServer(serversInPool.getDeviceType()))
            return null;

        List<LogicalNetworkInterface> nicsOfTargetServer = new ArrayList<LogicalNetworkInterface>();
        //check for quad port
        boolean isQuadTarget =filteringUtil.checkNICsQuad(nicsOfTargetServer);
        if ((isQuad && !isQuadTarget) || (!isQuad && isQuadTarget))
        {
           return null;
        }
        getServerNics(serversInPool, nicsOfTargetServer);
        if (nicsOfRefServer != null && !nicsOfRefServer.isEmpty() && !nicsOfTargetServer.isEmpty()) {

            List<String> nicsFqdd = new ArrayList<String>();
            if (nicsOfTargetServer.size() >= nicsOfRefServer.size()) {
                List<String> nics = new ArrayList<String>();
                Map<Integer, List<String>> portUIMap = new HashMap<Integer, List<String>>();
                getPortMapForReferenceServer(portUIMap, nicsOfRefServer);
                if (!portUIMap.isEmpty()) {
                    List<String> filteredfqdd = new ArrayList<String>();
                    for (Map.Entry<Integer, List<String>> entry : portUIMap.entrySet()) {

                        List<String> values = entry.getValue();
                        int key = entry.getKey();

                        List<String> portsPerSlot = new ArrayList<String>();
                        // check if the filteredfqdd has the same port.
                        for (String val : values) {
                            serverMatch = false;
                            for (LogicalNetworkInterface nic : nicsOfTargetServer) {

                                String[] fqdd = nic.getFqdd().trim().split("\\.");
                                String checkFC = fqdd[0];
                                String cardPortPartition = "";
                                cardPortPartition = fqdd[2];
                                String card = cardPortPartition.split("-")[0];
                                String port = cardPortPartition.split("-")[1];

                                String fqddNoPartition = "";
                                fqddNoPartition = nic.getFqdd().trim();

                                if ((port.contentEquals(val))) {

                                    if (key < 0 && checkFC.contains("FC")) {
                                        if (filteredfqdd.isEmpty() || !filteredfqdd.contains(fqddNoPartition))

                                        {
                                            portsPerSlot.add(fqddNoPartition);
                                            filteredfqdd.add(fqddNoPartition);
                                            serverMatch = true;
                                            break;
                                        }

                                    } else if (key >= 0 && checkFC.contains("NIC")) {
                                        if (filteredfqdd.isEmpty() || !filteredfqdd.contains(fqddNoPartition))

                                        {
                                            switch (values.size()) {
                                            case 1:
                                                portsPerSlot.add(fqddNoPartition);
                                                serverMatch = true;
                                                break;
                                            case 2:
                                                if (portsPerSlot.isEmpty()) {
                                                    LogicalNetworkInterface searchNicFqdd = new LogicalNetworkInterface();
                                                    String m = checkFC + "." + fqdd[1] + "." + card + "-" + 2;
                                                    searchNicFqdd.setFqdd(m);
                                                    int index = Collections.binarySearch(nicsOfTargetServer, searchNicFqdd,
                                                            new NicComparator());
                                                    if (index >= 0) {
                                                        portsPerSlot.add(fqddNoPartition);
                                                        filteredfqdd.add(fqddNoPartition);
                                                        serverMatch = true;
                                                        break;
                                                    }
                                                } else {
                                                    portsPerSlot.add(fqddNoPartition);
                                                    filteredfqdd.add(fqddNoPartition);
                                                    serverMatch = true;
                                                    break;

                                                }
                                                break;
                                            case 4:
                                                if (portsPerSlot.isEmpty()) {
                                                    String m = checkFC + "." + fqdd[1] + "." + card + "-" + 4;
                                                    LogicalNetworkInterface searchNicFqdd = new LogicalNetworkInterface();
                                                    searchNicFqdd.setFqdd(m);
                                                    int index = Collections.binarySearch(nicsOfTargetServer, searchNicFqdd,
                                                            new NicComparator());
                                                    if (index >= 0) {
                                                        portsPerSlot.add(fqddNoPartition);
                                                        filteredfqdd.add(fqddNoPartition);
                                                        serverMatch = true;
                                                        break;
                                                    }
                                                } else {
                                                    portsPerSlot.add(fqddNoPartition);
                                                    filteredfqdd.add(fqddNoPartition);
                                                    serverMatch = true;
                                                    break;

                                                }
                                                break;

                                            }

                                        }

                                    }

                                }

                                if (serverMatch)
                                    break;

                            }

                            if (!serverMatch) {
                                return null;
                            }

                        }

                    }

                    if (!filteredfqdd.isEmpty())
                        Collections.sort(filteredfqdd);
                    if (filteredfqdd.size() >= nicsOfRefServer.size()) {
                        return serversInPool;

                    }
                }

            }

        }
        return null;
        
    }

    private void getPortMapForReferenceServer(Map<Integer, List<String>> portUIMap, List<LogicalNetworkInterface> nicsOfRefServer) {
        int temp = -1;
        for (int i = 0; i < nicsOfRefServer.size(); i++) {

            String[] fqdd = nicsOfRefServer.get(i).getFqdd().trim().split("\\.");
            String checkFC = fqdd[0];
            String cardPortPartition = "";
            cardPortPartition = fqdd[2];
            String card = cardPortPartition.split("-")[0];
            String port = cardPortPartition.split("-")[1];
            LogicalNetworkInterface searchNicFqdd = new LogicalNetworkInterface();
            if (port.equalsIgnoreCase("1")) {
                List<String> values = new ArrayList<String>();
                int quad_port = 4;
                String m = checkFC + "." + fqdd[1] + "." + card + "-" + quad_port;
                searchNicFqdd.setFqdd(m);
                int index = Collections.binarySearch(nicsOfRefServer, searchNicFqdd, new NicComparator());
                if (index >= 0) {

                    for (int indexCheck = 1; indexCheck <= quad_port; indexCheck++)
                        values.add(String.valueOf(indexCheck));
                } else {
                    int dual_port = 2;
                    // check for dual port
                    String mn = checkFC + "." + fqdd[1] + "." + card + "-" + dual_port;
                    searchNicFqdd.setFqdd(mn);
                    int indexmn = Collections.binarySearch(nicsOfRefServer, searchNicFqdd, new NicComparator());
                    if (indexmn >= 0) {

                        for (int indexCheck = 1; indexCheck <= dual_port; indexCheck++)
                            values.add(String.valueOf(indexCheck));
                    } else
                        values.add("1");
                }
                if (checkFC.contains("FC")) {
                    portUIMap.put(temp, values);
                    temp--;
                } else
                    portUIMap.put(i, values);

            }
        }

    }

    private DeviceInventoryEntity matchAlmostMigrateBladeServer(List<DeviceInventoryEntity> serversInPool, DeviceInventoryEntity server,
            List<LogicalNetworkInterface> nicsOfRefServer, DeviceInventoryEntity migrateServer, boolean isQuad) {

        for (int j = 0; j < serversInPool.size(); j++) {
            if (serversInPool.get(j).getState() != DeviceState.READY ||
                    serversInPool.get(j).getManagedState() != ManagedState.MANAGED) {
                continue;
            }
            if (serversInPool.get(j).getServiceTag().equalsIgnoreCase(server.getServiceTag())) {
                continue;
            }
            // now get the model with starting same number . M and M, R and R and compare.
            if ((server.getModel().contains("M") && serversInPool.get(j).getModel().contains("M"))) {
                if (server.getModel().contains("M520")) {
                    return serversInPool.get(j);
                }
                // now check their nic configration.
                boolean isQuadTarget = false;
                List<LogicalNetworkInterface> nicsOfTargetServer = new ArrayList<LogicalNetworkInterface>();

                if (DeviceType.isRAServer(server.getDeviceType())) {
                    //check for quad port
                    isQuadTarget = filteringUtil.checkNICsQuad(nicsOfTargetServer);
                }
                if ((isQuad && !isQuadTarget) || (!isQuad && isQuadTarget))
                {
                    continue;
                }
                getServerNics(serversInPool.get(j), nicsOfTargetServer);
                // go through fabric check first, if their is a fabric a1 and a2 in the ref server, check if their is a fabric
                // a1 and a2.
                if (nicsOfTargetServer.size() >= nicsOfRefServer.size()) {
                    List<String> nics = new ArrayList<String>();
                    for (int indexOfRefServer = 0; indexOfRefServer < nicsOfRefServer.size(); indexOfRefServer++) {

                        for (int indexOfTargetServer = 0; indexOfTargetServer < nicsOfTargetServer.size(); indexOfTargetServer++) {
                            if (nicsOfTargetServer.get(indexOfTargetServer).getFqdd().contains(nicsOfRefServer.get(indexOfRefServer).getFqdd())) {
                                nics.add(nicsOfRefServer.get(indexOfRefServer).getFqdd());
                                break;
                            } else {
                                String regex = "\\.+[0-9]";
                                String refServerFqddFilterCard = null;
                                String targetServerFqddFilterCard = null;
                                // need to check for fabric b and c as they have different numbering systems for cards
                                if ((nicsOfRefServer.get(indexOfRefServer).getFqdd().contains("Mezzanine"))
                                        && (nicsOfTargetServer.get(indexOfTargetServer).getFqdd().contains("Mezzanine"))) {

                                    refServerFqddFilterCard = nicsOfRefServer.get(indexOfRefServer).getFqdd().trim().replaceAll(regex, "\\.");
                                    targetServerFqddFilterCard = nicsOfTargetServer.get(indexOfTargetServer).getFqdd().trim()
                                            .replaceAll(regex, "\\.");

                                }
                                if (refServerFqddFilterCard != null && targetServerFqddFilterCard != null) {
                                    if (targetServerFqddFilterCard.contains(refServerFqddFilterCard)) {
                                        nics.add(nicsOfRefServer.get(indexOfRefServer).getFqdd());
                                        break;
                                    }

                                } else if (nicsOfTargetServer.get(indexOfTargetServer).getFqdd()
                                        .contains(nicsOfRefServer.get(indexOfRefServer).getFqdd())) {
                                    nics.add(nicsOfRefServer.get(indexOfRefServer).getFqdd());
                                    break;
                                }

                            }
                        }

                    }
                    if (!nics.isEmpty())
                        Collections.sort(nics);
                    if (nics.size() == nicsOfRefServer.size()) {
                        return serversInPool.get(j);

                    }
                }

            }

        }
        return null;
    }

    private DeviceInventoryEntity matchExactMigrateServer(List<DeviceInventoryEntity> serversInPool, DeviceInventoryEntity server,
            List<LogicalNetworkInterface> nicsOfRefServer, DeviceInventoryEntity migrateServer) {

        for (int j = 0; j < serversInPool.size(); j++) {
            if (serversInPool.get(j).getState() != DeviceState.READY ||
                    serversInPool.get(j).getManagedState() != ManagedState.MANAGED) {
                continue;
            }
            if (serversInPool.get(j).getServiceTag().equalsIgnoreCase(server.getServiceTag())) {
                continue;
            }
            if (filteringUtil.modelIsAppropriate(serversInPool.get(j), FilterEnvironment.extractModelNumberFromString(server.getModel()))) {
                // now check their nic configration.
                List<LogicalNetworkInterface> nicsOfTargetServer = new ArrayList<LogicalNetworkInterface>();
                getServerNics(serversInPool.get(j), nicsOfTargetServer);
                if (nicsOfTargetServer.size() == nicsOfRefServer.size()) {
                    List<String> nics = new ArrayList<String>();
                    for (int i = 0; i < nicsOfRefServer.size(); i++) {

                        for (int k = 0; k < nicsOfTargetServer.size(); k++) {
                            if (nicsOfTargetServer.get(k).getFqdd().contains(nicsOfRefServer.get(i).getFqdd())) {
                                nics.add(nicsOfRefServer.get(i).getFqdd());
                                break;
                            }
                        }
                    }
                    if (!nics.isEmpty()) {
                        Collections.sort(nics);
                    }
                    if (nics.size() == nicsOfRefServer.size()) {
                        return serversInPool.get(j);
                    }
                }
            }
        }
        return null;
    }

    private void getServerNics(DeviceInventoryEntity server, List<LogicalNetworkInterface> nicsOfServer) {

        if (server != null) {
            if (!DeviceType.isRAServer(server.getDeviceType())) {
                return;
            }
            com.dell.pg.asm.server.client.device.Server serverInv = serverDeviceProxy.getServer(server.getRefId());
            List<LogicalNetworkInterface> temp = new ArrayList<LogicalNetworkInterface>();
            Set<String> tempNoPartition = new HashSet<String>();
            temp = serverInv.getNetworkInterfaceList();

            for (LogicalNetworkInterface nic : temp) {
                LogicalNetworkInterface fqddNoPartition = new LogicalNetworkInterface();
                if (nic.getFqdd().contains("NIC")) {
                    fqddNoPartition.setFqdd(nic.getFqdd().trim().substring(0, nic.getFqdd().trim().length() - 2));
                } else {
                    fqddNoPartition.setFqdd(nic.getFqdd().trim());
                }
                tempNoPartition.add(fqddNoPartition.getFqdd());
            }
            for (String tempNoPartitionElement : tempNoPartition) {
                LogicalNetworkInterface n = new LogicalNetworkInterface();
                n.setFqdd(tempNoPartitionElement);
                nicsOfServer.add(n);
            }

            Collections.sort(nicsOfServer, new NicComparator());

        }

    }

    public class NicComparator implements Comparator<LogicalNetworkInterface> {
        @Override
        public int compare(LogicalNetworkInterface x, LogicalNetworkInterface y) {
            return x.getFqdd().compareTo(y.getFqdd());
        }
    }

}

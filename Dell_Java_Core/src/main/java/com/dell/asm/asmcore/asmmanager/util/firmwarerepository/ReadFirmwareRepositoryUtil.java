package com.dell.asm.asmcore.asmmanager.util.firmwarerepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SystemIDEntity;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;

public class ReadFirmwareRepositoryUtil {

    public static final String DEFAULT_CATALOG_NAME = "New Firmware Repository";
    private static Logger logger = Logger.getLogger(ReadFirmwareRepositoryUtil.class);

    /**
     * Builds a {code FirmwareRepositoryEntity} from an XML catalog file. Note that the {code sourceLocation}
     * {code sourceType} cannot be calculated and will need to be set by the caller.
     *
     * @param file The XML catalog file
     * @return A representative FirmwareRepositoryEntity
     */
    public static FirmwareRepositoryEntity loadFirmwareRepositoryFromFile(File file) {
        FirmwareRepositoryEntity ret = new FirmwareRepositoryEntity();
        HashMap<String, List<SoftwareComponentEntity>> pathToSCE = new HashMap<>();
        InputStream inputStream = null;
        try {
            ret.setDiskLocation(file.getParentFile().getAbsolutePath());
            ret.setFilename(file.getName());
            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            inputStream = new FileInputStream(file);
            Document doc = builder.parse(inputStream);

            ret.setName(getAttribute(xPath, doc, "/Manifest/@name", DEFAULT_CATALOG_NAME));
            ret.setBaseLocation(getAttribute(xPath, doc, "/Manifest/@baseLocation", null));

            String expression = "//SoftwareComponent/SupportedDevices/Device";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
            if (nodeList != null) {
                Set<String> paths = new HashSet<>();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node deviceNode = nodeList.item(i);
                    NamedNodeMap nnm = deviceNode.getAttributes();
                    String componentId = getNamedItem(nnm, "componentID");

                    Node softwareComponentNode = deviceNode.getParentNode().getParentNode();
                    nnm = softwareComponentNode.getAttributes();
                    String packageId = getNamedItem(nnm, "packageID", "missing");
                    String dellVersion = getNamedItem(nnm, "dellVersion", "missing");
                    String vendorVersion = getNamedItem(nnm, "vendorVersion", "missing");
                    String path = getNamedItem(nnm, "path", "missing");
                    String remoteProtocol = "";
                    
                    // The Remote Protocol is to support the Software Components 
                    if (path != null) {
                        if (path.startsWith("http://") && path.length() >= 7) {
                            remoteProtocol = "http://";
                            path = path.substring(7);
                        }
                        else if(path.startsWith("ftp://") && path.length() >=6) {
                            remoteProtocol = "ftp://";
                            path = path.substring(6);
                        }
                    }
                    
                    paths.add(path);
                    String hashMd5 = getNamedItem(nnm, "hashMD5", "missing");

                    String deviceId, subDeviceId, vendorId, subVendorId;
                    String name = null;
                    String category = null;
                    String componentType = null;
                    String operatingSystem = null;
                    boolean pciSeen = false;

                    //First extract values from the software component top level
                    NodeList nl = softwareComponentNode.getChildNodes();
                    if (nl != null) {
                        for (int j = 0; j < nl.getLength(); j++) {
                            Node node = nl.item(j);
                            if ("Name".equals(node.getNodeName())) {
                                Node display = null;
                                for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                                    if ("Display".equals(node.getChildNodes().item(k).getNodeName())) {
                                        display = node.getChildNodes().item(k);
                                        break;
                                    }
                                }

                                if (display != null) {
                                    name = getCdataOrTextContent(display);
                                }
                            } else if ("ComponentType".equals(node.getNodeName())) {
                                Node display = null;
                                for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                                    if ("Display".equals(node.getChildNodes().item(k).getNodeName())) {
                                        display = node.getChildNodes().item(k);
                                        break;
                                    }
                                }

                                if (display != null) {
                                    componentType = getCdataOrTextContent(display);
                                }
                            } else if ("Category".equals(node.getNodeName())) {
                                Node display = null;
                                for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                                    if ("Display".equals(node.getChildNodes().item(k).getNodeName())) {
                                        display = node.getChildNodes().item(k);
                                        break;
                                    }
                                }

                                if (display != null) {
                                    category = getCdataOrTextContent(display);
                                }
                            } else if ("SupportedOperatingSystems".equals(node.getNodeName())) {
                                Node operatingSystemNode = null;
                                for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                                    if ("OperatingSystem".equals(node.getChildNodes().item(k).getNodeName())) {
                                        operatingSystemNode = node.getChildNodes().item(k);
                                        String osCode = getNamedItem(operatingSystemNode.getAttributes(), "osCode");
                                        String majorVersion =  getNamedItem(operatingSystemNode.getAttributes(), "majorVersion");
                                        String minorVersion = getNamedItem(operatingSystemNode.getAttributes(), "minorVersion");
                                        
                                        if (majorVersion == null || majorVersion.trim().isEmpty()) {
                                            majorVersion = "0";
                                        }
                                        if (minorVersion == null || minorVersion.trim().isEmpty()) {
                                            minorVersion = "0";
                                        }
                                        
                                        operatingSystem = osCode + majorVersion + "." + minorVersion;
                                    }
                                }
                            }
                        }
                    }

                    //Now parse supported devices these are denoted by pciinfo elements
                    nl = deviceNode.getChildNodes();
                    if (nl != null) {
                        for (int j = 0; j < nl.getLength(); j++) {
                            Node node = nl.item(j);
                            if ("PCIInfo".equals(node.getNodeName())) {
                                //for each of these we will create an entry
                                //if nonoe just create one for top level
                                //deviceID="0702" subDeviceID="E642" subVendorID="10DF" vendorID="19A2"
                                nnm = node.getAttributes();
                                deviceId = getNamedItem(nnm, "deviceID");
                                subDeviceId = getNamedItem(nnm, "subDeviceID");
                                subVendorId = getNamedItem(nnm, "subVendorID");
                                vendorId = getNamedItem(nnm, "vendorID");
                                pciSeen = true;

                                //create one per pci line
                                SoftwareComponentEntity sce = new SoftwareComponentEntity();
                                sce.setPackageId(packageId);
                                sce.setComponentId(componentId);
                                sce.setDellVersion(dellVersion);
                                sce.setVendorVersion(vendorVersion);
                                sce.setFirmwareRepositoryEntity(ret);
                                sce.setDeviceId(deviceId);
                                sce.setSubDeviceId(subDeviceId);
                                sce.setVendorId(vendorId);
                                sce.setSubVendorId(subVendorId);
                                sce.setPath(path);
                                sce.setRemoteProtocol(remoteProtocol);
                                sce.setHashMd5(hashMd5);
                                sce.setName(name);
                                sce.setComponentType(componentType);
                                sce.setCategory(category);
                                sce.setOperatingSystem(operatingSystem);
                                ret.getSoftwareComponents().add(sce);
                                addToMap(pathToSCE, sce);
                            }
                        }
                    }

                    //just create one to hold the top level because we didnt see any pciinfo sub elements
                    if (!pciSeen) {
                        SoftwareComponentEntity sce = new SoftwareComponentEntity();
                        sce.setPackageId(packageId);
                        sce.setComponentId(componentId);
                        sce.setDellVersion(dellVersion);
                        sce.setPath(path);
                        sce.setHashMd5(hashMd5);
                        sce.setName(name);
                        sce.setComponentType(componentType);
                        sce.setCategory(category);
                        sce.setVendorVersion(vendorVersion);
                        sce.setFirmwareRepositoryEntity(ret);
                        ret.getSoftwareComponents().add(sce);
                        addToMap(pathToSCE, sce);
                    }
                }

                ret.setComponentCount(paths.size());
            }

            expression = "//SoftwareBundle";
            nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

            if (nodeList != null) {
                ret.setBundleCount(nodeList.getLength());
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node softwareBundle = nodeList.item(i);
                    NodeList childNodes = softwareBundle.getChildNodes();
                    String time, version, bundleType;
                    Date dateTime = null;
                    NamedNodeMap nnm = softwareBundle.getAttributes();
                    time = getNamedItem(nnm, "dateTime");
                    version = getNamedItem(nnm, "vendorVersion");
                    bundleType = getNamedItem(nnm, "bundleType");

                    if (time != null && !"".equals(time.trim())) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        try {
                            dateTime = sdf.parse(time);
                        } catch (ParseException p) {
                            logger.error("Could not parse time: " + time, p);
                        }
                    }

                    SoftwareBundleEntity sbe = new SoftwareBundleEntity();
                    sbe.setBundleDate(dateTime);
                    sbe.setVersion(version);
                    sbe.setFirmwareRepositoryEntity(ret);
                    sbe.setBundleType(bundleType);
                    ret.getSoftwareBundles().add(sbe);
                    

                    //We first need to get all fo the systemID's
                    Node targetSystemsNode = getNamedChildNode(softwareBundle, "TargetSystems");
                    List<Node> brandNodes = getAllNamedChildren(targetSystemsNode, "Brand");
                    Set<String> ids = new HashSet<>();
                    if (brandNodes != null) {
                        for (Node brandNode : brandNodes) {
                            Node modelNode = getNamedChildNode(brandNode, "Model");
                            ids.add(getNamedItem(modelNode.getAttributes(), "systemID"));
                        }
                    }
                    
                    // Lets Get Supported Operating Systems Next
                    Set<String> opSystemsCodes = new HashSet<>();
                    Node targetOsesNode = getNamedChildNode(softwareBundle, "TargetOSes");
                    List<Node> operatingSystemNodes = getAllNamedChildren(targetOsesNode, "OperatingSystem");
                    if (operatingSystemNodes != null) {
                        for (Node operatingSystemNode : operatingSystemNodes) {
                            String opSystemCode = getNamedItem(operatingSystemNode.getAttributes(), "osCode");
                            String majorVersion =  getNamedItem(operatingSystemNode.getAttributes(), "majorVersion");
                            String minorVersion = getNamedItem(operatingSystemNode.getAttributes(), "minorVersion");
                            
                            if (majorVersion == null || majorVersion.trim().isEmpty()) {
                                majorVersion = "0";
                            }
                            if (minorVersion == null || minorVersion.trim().isEmpty()) {
                                minorVersion = "0";
                            }
                            
                            String operatingSystem = opSystemCode + majorVersion + "." + minorVersion;
                            
                            opSystemsCodes.add(operatingSystem);
                        }
                    }

                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node child = childNodes.item(j);
                        if ("Name".equals(child.getNodeName())) {
                            Node display = null;
                            for (int k = 0; k < child.getChildNodes().getLength(); k++) {
                                if ("Display".equals(child.getChildNodes().item(k).getNodeName())) {
                                    display = child.getChildNodes().item(k);
                                    break;
                                }
                            }

                            if (display != null)
                                sbe.setName(display.getTextContent());
                        } else if ("Contents".equals(child.getNodeName())) {

                            NodeList contentsChildren = child.getChildNodes();
                            for (int k = 0; k < contentsChildren.getLength(); k++) {
                                if ("Package".equals(contentsChildren.item(k).getNodeName())) {
                                    nnm = contentsChildren.item(k).getAttributes();
                                    String path = getNamedItem(nnm, "path");
                                    List<SoftwareComponentEntity> sces = pathToSCE.get(path);
                                    if (sces != null) {
                                        for (SoftwareComponentEntity sce : sces) {
                                            if (ids != null)
                                                for (String id : ids) {
                                                    SystemIDEntity systemID = new SystemIDEntity(Integer.parseInt(id, 16) + "");
                                                    sce.getSystemIDs().add(systemID);
                                                    systemID.setSoftwareComponentEntity(sce);
                                                }
                                            if (opSystemsCodes != null) {
                                                for (String osCode : opSystemsCodes) {
                                                    SystemIDEntity systemID = new SystemIDEntity(osCode);
                                                    sce.getSystemIDs().add(systemID);
                                                    systemID.setSoftwareComponentEntity(sce);
                                                }
                                            }
                                            sbe.getSoftwareComponents().add(sce);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            logger.error("Exception seen while loading the Software/Firmware Catalog " + e.getMessage(), e);
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.unableToProcessCatalogXml());
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                logger.error("Exception seen closing inputstream", e);
            }
        }


        return ret;
    }

    /**
     * Updates the SoftwareBundleEntities from an XML catalog file. Note that the {code sourceLocation}
     * {code sourceType} cannot be calculated and will need to be set by the caller.
     *
     * @param softwareBundles Map of SoftwareBundleEntities based on the Name of the Bundle as the Key
     * @param softwareComponents Map of SoftwareComponentEntities based on the Path of the Component being the Key
     * @param catalogFile The XML catalog file
     */
    public static void updateSoftwareBundlesByCatalog(Map<String, SoftwareBundleEntity> softwareBundles, Map<String, List<SoftwareComponentEntity>> softwareComponents, File catalogFile) {
        InputStream inputStream = null;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            inputStream = new FileInputStream(catalogFile);
            Document doc = builder.parse(inputStream);
            String expression = "//SoftwareBundle";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
            if (nodeList != null) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node softwareBundle = nodeList.item(i);
                    NodeList childNodes = softwareBundle.getChildNodes();
                    SoftwareBundleEntity currentBundle = null;
                    Set<String> packages = new HashSet<String>();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node child = childNodes.item(j);
                        if ("Name".equals(child.getNodeName())) {
                            Node display = null;
                            for (int k = 0; k < child.getChildNodes().getLength(); k++) {
                                if ("Display".equals(child.getChildNodes().item(k).getNodeName())) {
                                    display = child.getChildNodes().item(k);
                                    break;
                                }
                            }

                            if (display != null) {
                                currentBundle = softwareBundles.get(display.getTextContent());
                            }
                        } else if ("Contents".equals(child.getNodeName())) {

                            NodeList contentsChildren = child.getChildNodes();
                            for (int k = 0; k < contentsChildren.getLength(); k++) {
                                if ("Package".equals(contentsChildren.item(k).getNodeName())) {
                                    NamedNodeMap nnm = contentsChildren.item(k).getAttributes();
                                    String path = getNamedItem(nnm, "path");
                                    packages.add(path);
                                }
                            }
                        }
                    }
                    if (currentBundle != null) {
                        for (String currentPackage : packages) {
                            List<SoftwareComponentEntity> softwareComponentEntities = softwareComponents.get(currentPackage);
                            if (softwareComponentEntities != null && softwareComponentEntities.size() > 0) {
                                currentBundle.getSoftwareComponents().addAll(softwareComponentEntities);
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            logger.error("Exception seen while loading SoftwareBundles " + e.getMessage(), e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(AsmManagerMessages.internalError()).build());
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                logger.error("Exception seen closing inputstream", e);
            }
        }
    }

    /**
     * If the children of this element contain a CDATA element, it is
     * assumed that any other children are extraneous, i.e. whitespace text nodes.
     */
    private static String getCdataOrTextContent(Node elem) {

        String cleanContent = null;
        boolean foundCDATA = false;
        NodeList nl = elem.getChildNodes();
        for (int j = 0; j < nl.getLength(); j++) {
            Node child = nl.item(j);
            if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                cleanContent = child.getTextContent();
                foundCDATA = true;
                break;
            }
        }
        if (!foundCDATA) {
            cleanContent = elem.getTextContent();
        }
        return cleanContent;
    }

    /**
     * Given an xpath expression which should evaluate to a single node, returns that node's value.
     * If the node is not found returns the {@code defaultValue}
     *
     * @param xPath The xpath object to use
     * @param doc The document to search
     * @param expression The xpath expression to find
     * @param defaultValue The default value to return if no matching node is found.
     * @return The node value, or the default value if none found
     * @throws XPathExpressionException If an error is contained in the xpath expression.
     */
    private static String getAttribute(XPath xPath, Document doc, String expression, String defaultValue) throws XPathExpressionException {
        String ret = null;
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        if (nodeList != null && nodeList.getLength() > 0) {
            ret = nodeList.item(0).getNodeValue();
        }

        if (StringUtils.isBlank(ret)) {
            // If the xpath expression is an attribute and the parent node is found, the attribute
            // value will be an empty string even if it doesn't exist at all. Set it to the
            // defaultValue in this case.
            ret = defaultValue;
        }

        return ret;
    }

    /**
     * Used when we are expecting multiple children to match the provided nodeName.
     *
     * @param parentNode The parent node
     * @param nodeName The child node name to find
     * @return Returns all children matching the nodeName.
     */
    private static List<Node> getAllNamedChildren(Node parentNode, String nodeName) {

        if (parentNode == null || nodeName == null)
            return null;

        ArrayList<Node> children = new ArrayList<>();
        NodeList childNodes = parentNode.getChildNodes();
        if (childNodes != null)
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node child = childNodes.item(j);
                if (nodeName.equals(child.getNodeName())) {
                    children.add(child);
                }
            }

        return children;

    }

    /**
     * Used when we expect a single child node to match the provided name.  If there are multiple matches only the first will be returned.
     *
     * @param parentNode The parent node
     * @param nodeName The child node name to find
     * @return Returns first match of nodeName.
     */
    private static Node getNamedChildNode(Node parentNode, String nodeName) {
        if (parentNode == null || nodeName == null)
            return null;

        NodeList childNodes = parentNode.getChildNodes();
        if (childNodes != null)
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node child = childNodes.item(j);
                if (nodeName.equals(child.getNodeName())) {
                    return child;
                }
            }

        return null;
    }

    private static String getNamedItem(NamedNodeMap nmm, String key, String defaultValue) {
        Node node = nmm.getNamedItem(key);
        if (node == null || StringUtils.isBlank(node.getNodeValue())) {
            return defaultValue;
        } else {
            return node.getNodeValue();
        }
    }

    private static String getNamedItem(NamedNodeMap nmm, String key) {
        return getNamedItem(nmm, key, null);
    }

    public static void addToMap(Map<String, List<SoftwareComponentEntity>> map, SoftwareComponentEntity sce) {
        if (sce != null) {
            String path = sce.getPath();
            if (path != null) {
                if (path.indexOf('/') != -1)
                    path = path.substring(path.lastIndexOf('/') + 1);

                List<SoftwareComponentEntity> current = map.get(path);
                if (current == null) {
                    current = new ArrayList<>();
                    map.put(path, current);
                }

                current.add(sce);
            }
        }

    }

}

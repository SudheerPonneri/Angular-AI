package com.fis.bom.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class AnnotationAppender {

  private static final String COMMENTS = "Comments:";
  private static final String BUSINESS_DESCRIPTION = "Business Description:";
  private static final String VALID_VALUES = "Valid Values:";
  private static final String BUSINESS_NAME = "Business Name:";
  public static final PropertiesFileReader propproperties = new PropertiesFileReader();
  public static final Properties abbrivatedToFullProperties = new Properties();
  static Logger log = Logger.getLogger(AnnotationAppender.class.getName());

  static {
    try (InputStream input =
        AnnotationAppender.class.getClassLoader().getResourceAsStream("abbrivations.properties")) {

      // load a properties file
      propproperties.load(input);

      // get the property value and print it out

      for (Map.Entry<Object, Object> entry : propproperties.entrySet()) {
        String key = (String) entry.getKey();
        String value = (String) entry.getValue();
        propproperties.put(decapitalizeString(key), decapitalizeString(value));
        if (abbrivatedToFullProperties.get(value) == null) {
          abbrivatedToFullProperties.put(value, key);
        }
      }

      for (Map.Entry<Object, Object> entry : propproperties.entrySet()) {
        // System.out.println(entry.getKey() + "=" + entry.getValue());
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static String decapitalizeString(String string) {
    return string == null || string.isEmpty()
        ? ""
        : Character.toLowerCase(string.charAt(0)) + string.substring(1);
  }

  public static String getBusinessNameFromJavaBusinessName(String javaBusinessName) {
    if (javaBusinessName.startsWith("/")) {
      return "";
    }
    if (javaBusinessName.contains("-")) {
      String[] businessNameSection = javaBusinessName.split("-");
      String busName = "";
      for (String section : businessNameSection) {
        if (!section.isEmpty()) {
          String secString = section.toLowerCase();
          secString =
              secString.substring(0, 1).toUpperCase() + secString.substring(1, secString.length());
          busName = busName + " " + secString;
        }
      }

      busName = busName.substring(0, 1).toUpperCase() + busName.substring(1);
      return busName;
    } else {
      String busName = javaBusinessName;
      busName = busName.substring(0, 1).toUpperCase() + busName.substring(1);
      busName = busName.replaceAll("([^_A-Z])([A-Z])", "$1 $2");

      String[] businessNameSection = busName.split(" ");
      String javaBusName = "";
      for (String section : businessNameSection) {
        if (!section.isEmpty()) {
          String fullName = abbrivatedToFullProperties.getProperty(section);
          if (fullName != null && !fullName.isEmpty()) {
            section = fullName;
          }
          javaBusName = javaBusName + " " + section;
        }
      }

      return javaBusName;
    }
  }

  public static String getJavaBusinessName(String businessName) {

    String javaBusinessName = businessName.replaceAll(" ", "");
    if (businessName.endsWith(".")) {
      businessName = businessName.substring(0, businessName.lastIndexOf('.'));
    }
    if (javaBusinessName.isEmpty()) {
      return businessName;
    }
    if (businessName.toUpperCase().equals(businessName)) {
      businessName = businessName.toLowerCase();
    }
    if (businessName.indexOf("-") > 0) {
      businessName = businessName.replaceAll("-", " ");
    }
    return removeSpcialChar(businessName, " ");

    //
    //
    //    String javaBusinessName = businessName.replaceAll(" ", "");
    //    if (javaBusinessName.isEmpty()) {
    //      return businessName;
    //    }
    //    javaBusinessName =
    //        javaBusinessName.substring(0, 1).toLowerCase() + javaBusinessName.substring(1);
    //
    //
    //    return getAbbreviatedName(javaBusinessName);
  }

  public static String removeSpcialChar(String businessName, String specialChar) {
    String[] businessNameSection = businessName.split(specialChar);
    String javaBusName = "";
    for (String section : businessNameSection) {
      if (!section.isEmpty()) {
        section = getAbbreviatedName(section);
        String secString = section.toLowerCase();
        secString =
            secString.substring(0, 1).toUpperCase() + secString.substring(1, secString.length());
        javaBusName = javaBusName + secString;
      }
    }

    javaBusName = javaBusName.substring(0, 1).toLowerCase() + javaBusName.substring(1);
    return getAbbreviatedName(javaBusName);
  }

  public static String getAbbreviatedName(String businessName) {

    String orgBusinessName = businessName;
    // System.out.println("old business name:" + businessName);
    for (Object entry : propproperties.keySet()) {
      String key = (String) entry;
      String value = (String) propproperties.get(key);
      // System.err.println("Key: " + entry.getKey() + " Value: " + entry.getValue());

      Pattern pattern = Pattern.compile(Pattern.quote((String) key));

      Matcher matcher = pattern.matcher(businessName);

      String stringToBeReplaced = (String) value;

      businessName = matcher.replaceAll(matcher.quoteReplacement(stringToBeReplaced));
    }
    // System.out.println("newBusiness Name:" + businessName);
    if (!businessName.equals(orgBusinessName) && businessName.length() > 40) {
      System.out.println("businessName:" + businessName + " orgBusinessName:" + orgBusinessName);
      return getAbbreviatedName(businessName);
    }
    return businessName;
  }

  public static void main(String[] args) {
    // String filePath =
    // "C:\\projects\\JaxbAdvancedDemo\\src\\main\\resources\\EC_WSDLs_v5_6\\messages\\ECRecordFeIpNameFormatRsp_v1_0.xsd";

    String inputFolder = "";
    String outputFolder = "";

    for (int i = 0; i < args.length; i++) {
      // log input arguments
      if (i == 0) {
        log.info("Message Schema folder location");
      }
      if (i == 1) {
        log.info("Output folder location");
      }
      log.info("Argument " + i + " = " + args[i]);
    }
    if (args.length >= 1) {
      inputFolder = args[0] + File.separator;
      outputFolder = args[1] + File.separator;
    } else {
      // Return with usage information
      log.info(
          "This utility generates JSON Schema compliant to Swagger Specification from xsd schema");
      log.info("This utility accepts two argument");
      log.info("args[0]: Folder location where all xsd files are");
      log.info(
          "args[1]: Output folder location for storing generated JSON Schema compliant to Swagger Specification");
      return;
    }

    // Validate output folder path
    if (outputFolder.equals("") == false) {
      File outFolder = new File(outputFolder);
      if (outFolder.exists() == false) {
        try {
          // Create output folder, if cannot create, then use current directory as output
          // folder
          if (outFolder.mkdir() == false) {
            log.info(
                "Cannot create folder: "
                    + outFolder.getAbsolutePath()
                    + " Defaulting to current directory");
            outputFolder = "";
          }
        } catch (SecurityException ex) {
          log.info(
              "Cannot create folder: "
                  + outFolder.getAbsolutePath()
                  + " Defaulting to current directory: "
                  + ex.toString());
          outputFolder = "";
        }
      }
    }
    if (inputFolder.endsWith("messages" + File.separator)) {

      Path path = Paths.get(outputFolder + "messages" + File.separator);

      // java.nio.file.Files;
      try {
        Files.createDirectories(path);
      } catch (IOException e) { // TODO Auto-generated catch block
        log.error(e);
      }
      String outputPath = outputFolder + "messages";
      log.info("Output messages path: " + outputPath);
      processFolder(inputFolder, outputPath + File.separator);
    } else {
      processFolder(inputFolder, outputFolder);
    }

    int index = inputFolder.lastIndexOf("messages");
    if (index > 0) {
      inputFolder = inputFolder.substring(0, index) + "ECBusinessObjects" + File.separator;

      Path path = Paths.get(outputFolder + "ECBusinessObjects" + File.separator);

      // java.nio.file.Files;
      try {
        Files.createDirectories(path);
      } catch (IOException e) { // TODO Auto-generated catch block
        log.error(e);
      }
      String outputPath = outputFolder + "ECBusinessObjects";
      log.info("Output BOM path: " + outputPath);
      processFolder(inputFolder, outputPath + File.separator);
    }
  }

  private static void processFolder(String inputFolder, String outputFolder) {
    File dir = new File(inputFolder);

    // Validate newVersionFolder folder path
    if (dir.exists() == false) {
      log.info("Path does not exist: " + dir.getAbsolutePath());
    }

    int counter = 0;
    for (File child : dir.listFiles()) {

      // if(counter > 1)return;
      counter++;
      String fileName = child.getName();
      // if (!fileName.endsWith("ECRetrieveIpPocRsp_v5_1.xsd")) {
      // continue;
      // }

      String filePathName = child.getAbsolutePath();
      // Only worry about files ending with .xsd extension
      if (fileName.endsWith(".xsd")) {
        processFile(filePathName, outputFolder + File.separator + fileName);
      }
    }
  }

  public static String getSimpleTypeName(Node node) {
    if (node != null) {
      if (node.getAttributes() != null) {
        Node attribNode = node.getAttributes().getNamedItem("name");
        if (node.getNodeName().endsWith("simpleType")
            && (attribNode != null)
            && !attribNode.getNodeValue().isEmpty()) {
          return attribNode.getNodeValue();
        }

        if (node.getNodeName().endsWith("complexType") || node.getNodeName().endsWith("element")) {
          return "";
        }
      }
      if (node.getParentNode() != null) {
        return getSimpleTypeName(node.getParentNode());
      } else {
        return "";
      }

    } else {
      return "";
    }
  }

  public static Node getElementNode(Node node) {
    if (node != null) {
      if (node.getAttributes() != null) {
        Node attribNode = node.getAttributes().getNamedItem("name");
        if (!node.getNodeName().endsWith("complexType")
            && !node.getNodeName().endsWith("simpleType")
            && (attribNode != null)
            && !attribNode.getNodeValue().isEmpty()) {
          return node;
        }
      }
      if (node.getParentNode() != null) {
        return getElementNode(node.getParentNode());
      } else {
        return null;
      }

    } else {
      return null;
    }
  }

  public static String getNodeName(Node node) {
    if (node != null) {
      if (node.getAttributes() != null) {
        Node attribNode = node.getAttributes().getNamedItem("name");
        if (attribNode != null && !attribNode.getNodeValue().isEmpty()) {
          return attribNode.getNodeValue();
        }
      }
    }
    return "";
  }

  public static String getComplexElementName(Node node) {
    if (node != null) {
      if (node.getAttributes() != null) {
        Node attribNode = node.getAttributes().getNamedItem("name");
        if ((attribNode != null) && !attribNode.getNodeValue().isEmpty()) {
          return attribNode.getNodeValue();
        }
      }
    }
    return "";
  }

  public static String getName(Node node) {
    if (node != null) {
      if (node.getAttributes() != null) {
        Node attribNode = node.getAttributes().getNamedItem("name");
        if (!node.getNodeName().endsWith("complexType")
            && !node.getNodeName().endsWith("simpleType")
            && (attribNode != null)
            && !attribNode.getNodeValue().isEmpty()) {
          return attribNode.getNodeValue();
        }
      }
      if (node.getParentNode() != null) {
        return getName(node.getParentNode());
      } else {
        return "";
      }

    } else {
      return "";
    }
  }

  public static String getTypeName(Node node) {
    if (node != null) {
      if (node.getAttributes() != null) {
        Node attribNode = node.getAttributes().getNamedItem("type");
        if (node.getNodeName().endsWith("element")
            && (attribNode != null)
            && !attribNode.getNodeValue().isEmpty()) {
          return attribNode.getNodeValue();
        }

        if (node.getNodeName().endsWith("complexType")
            || node.getNodeName().endsWith("simpleType")) {
          return "";
        }
      }
      if (node.getParentNode() != null) {
        return getTypeName(node.getParentNode());
      } else {
        return "";
      }

    } else {
      return "";
    }
  }

  public static void processFile(String fileInputPath, String fileOutputPath) {

    try {
      // parse file and convert it to a DOM
      Document doc =
          DocumentBuilderFactory.newInstance()
              .newDocumentBuilder()
              .parse(new InputSource(fileInputPath));

      // use xpath to find node to add to
      XPath xPath = XPathFactory.newInstance().newXPath();

      Element schema =
          (Element)
              xPath.evaluate(
                  "//*[local-name()='schema']", doc.getDocumentElement(), XPathConstants.NODE);
      schema.setAttribute("xmlns:jaxb", "http://java.sun.com/xml/ns/jaxb");
      schema.setAttribute("xmlns:annox", "http://annox.dev.java.net");
      schema.setAttribute("xmlns:jl", "http://annox.dev.java.net/java.lang");
      schema.setAttribute("jaxb:version", "2.1");
      schema.setAttribute("jaxb:extensionBindingPrefixes", "annox");

      processEnumerationElement(doc, xPath);
      processDocumentElement(doc, xPath);

      processOsoElement(doc, xPath);

      // output
      //	    TransformerFactory.newInstance().newTransformer().transform(new
      // DOMSource(doc.getDocumentElement()),
      //		    new StreamResult(System.out));

      TransformerFactory.newInstance()
          .newTransformer()
          .transform(
              new DOMSource(doc.getDocumentElement()),
              new StreamResult(new File(fileOutputPath).getPath()));
      log.info("Annotated File: " + fileOutputPath);

      // Result output = new StreamResult(new File(pomFilePath).getPath());
      // Source input = new DOMSource(doc);
      // transformer.transform(input, output);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static Node getAnnotationNode(Node node) {
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      String elemName = childNode.getNodeName();
      if (elemName.endsWith("annotation")) {
        return childNode;
      }
    }
    return null;
  }

  private static void processOsoElement(Document doc, XPath xPath) throws XPathExpressionException {
    NodeList nodes =
        (NodeList)
            xPath.evaluate(
                "//*[local-name()='complexType']",
                doc.getDocumentElement(),
                XPathConstants.NODESET);

    List<String> processedElemeNames = new ArrayList<>();

    String targetNamespace = doc.getDocumentElement().getAttribute("targetNamespace");
    int lastInd = targetNamespace.lastIndexOf(":");
    if (lastInd > 0) {
      targetNamespace = targetNamespace.substring(lastInd + 1);
    }

    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      String elemName = getComplexElementName(node);
      String type = getTypeName(node);
      String baseURI = doc.getBaseURI();
      String documentURI = doc.getDocumentURI();
      int lstInd = type.lastIndexOf(":");
      if (lstInd > 0) {
        type = type.substring(lstInd + 1);
      }
      int lastIndback = baseURI.lastIndexOf("/");
      if (lastIndback > 0) {
        baseURI =
            baseURI
                .substring(lastIndback + 1)
                .substring(0, baseURI.substring(lastIndback + 1).lastIndexOf("."));
      }

      // System.out.println("000-" + elemName + ":" + type + ":" + baseURI);
      if (elemName.endsWith("Oso") && targetNamespace.contains("Retrieve")) {
        NodeList seqNodes = node.getChildNodes();
        //  System.out.println(elemName);
        for (int j = 0; j < seqNodes.getLength(); j++) {
          Node seqNode = seqNodes.item(j);
          if (seqNode.getNodeName().endsWith("sequence")) {
            NodeList childNodes = seqNode.getChildNodes();
            for (int k = 0; k < childNodes.getLength(); k++) {
              Node childNode = childNodes.item(k);
              String childElemName = getComplexElementName(childNode);
              //  System.out.println(childElemName);
            }
          }
          ;
          String seqElemName = getName(seqNode);
        }
        continue;
      }
    }
  }

  private static void processEnumerationElement(Document doc, XPath xPath)
      throws XPathExpressionException {
    NodeList nodes =
        (NodeList)
            xPath.evaluate(
                "//*[local-name()='enumeration']",
                doc.getDocumentElement(),
                XPathConstants.NODESET);

    List<String> processedElemeNames = new ArrayList<>();

    String targetNamespace = doc.getDocumentElement().getAttribute("targetNamespace");
    int lastInd = targetNamespace.lastIndexOf(":");
    if (lastInd > 0) {
      targetNamespace = targetNamespace.substring(lastInd + 1);
    }

    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      String elemName = getSimpleTypeName(node);
      if (elemName.isEmpty()
          || processedElemeNames.contains(elemName)
          || elemName.equals("TimePeriod")) {
        continue;
      }
      processedElemeNames.add(elemName);
      // create element to add

      Node annotationElementNode = getAnnotationNode(node.getParentNode().getParentNode());
      org.w3c.dom.Element annotationElement;
      if (annotationElementNode != null) {
        annotationElement = (Element) annotationElementNode;
      } else {
        annotationElement = doc.createElement("xsd:annotation");
      }
      org.w3c.dom.Element appInfoElement = doc.createElement("xsd:appinfo");
      org.w3c.dom.Element annotateEnumValueMethodElement =
          doc.createElement("annox:annotateEnumValueMethod");
      annotateEnumValueMethodElement.setTextContent("@com.fasterxml.jackson.annotation.JsonValue");
      org.w3c.dom.Element annotateEnumFromValueMethodElement =
          doc.createElement("annox:annotateEnumFromValueMethod");
      annotateEnumFromValueMethodElement.setTextContent(
          "@com.fasterxml.jackson.annotation.JsonCreator");

      appInfoElement.appendChild(annotateEnumValueMethodElement);
      appInfoElement.appendChild(annotateEnumFromValueMethodElement);

      annotationElement.appendChild(appInfoElement);

      node.getParentNode().getParentNode().insertBefore(annotationElement, node.getParentNode());
    }
  }

  static String getXPath(Node node) {
    Node parent = node.getParentNode();
    if (parent == null) {
      return "";
    }
    return getXPath(parent) + "/" + node.getNodeName();
  }

  private static void processDocumentElement(Document doc, XPath xPath)
      throws XPathExpressionException {

    String targetNamespace = doc.getDocumentElement().getAttribute("targetNamespace");
    int lastInd = targetNamespace.lastIndexOf(":");
    if (lastInd > 0) {
      targetNamespace = targetNamespace.substring(lastInd + 1);
    }

    NodeList nodes =
        (NodeList)
            xPath.evaluate(
                "//*[local-name()='documentation']",
                doc.getDocumentElement(),
                XPathConstants.NODESET);

    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      String elemName = getName(node);
      String type = getTypeName(node);
      String baseURI = doc.getBaseURI();
      String documentURI = doc.getDocumentURI();
      int lstInd = type.lastIndexOf(":");
      if (lstInd > 0) {
        type = type.substring(lstInd + 1);
      }

      int lastIndback = baseURI.lastIndexOf("/");
      if (lastIndback > 0) {
        baseURI =
            baseURI
                .substring(lastIndback + 1)
                .substring(0, baseURI.substring(lastIndback + 1).lastIndexOf("."));
      }

      // System.out.println("000-" + elemName + ":" + type + ":" + baseURI);
      if (elemName.isEmpty() || elemName.endsWith("Osi") || elemName.endsWith("Oso")) {
        continue;
      }
      String documentation = node.getTextContent();

      documentation =
          documentation
              .replaceAll("&lt;p&gt;", "")
              .replaceAll("&lt;br&gt;", "\n")
              .replaceAll("&lt;/p&gt;", "");

      String businessName = "";

      int bmToken = documentation.indexOf(BUSINESS_NAME);
      int vvToken = documentation.indexOf(VALID_VALUES);
      int fdToken = documentation.indexOf(BUSINESS_DESCRIPTION);
      int cToken = documentation.indexOf(COMMENTS);
      if ((bmToken >= 0) && (vvToken >= 0)) {
        businessName = (documentation.substring(BUSINESS_NAME.length(), vvToken - 1)).trim();
        businessName = getJavaBusinessName(businessName);
      }

      String comment = "";
      String description = "";
      String validVal = "";
      if ((fdToken >= 0) && (cToken >= 0)) {
        description =
            documentation.substring(fdToken + BUSINESS_DESCRIPTION.length(), cToken - 1).trim();
      }
      if ((vvToken >= 0) && (fdToken >= 0)) {
        validVal = documentation.substring(vvToken + VALID_VALUES.length(), fdToken - 1).trim();
      }
      if (cToken >= 0) {
        comment = (documentation.substring(cToken + COMMENTS.length())).trim();
      } else {
        cToken = documentation.indexOf("Comments");
        if (cToken >= 0) {
          comment = documentation.substring(cToken + "Comments".length()).trim();
          description =
              documentation.substring(fdToken + BUSINESS_DESCRIPTION.length(), cToken - 1).trim();
        }
      }

      // create element to add
      org.w3c.dom.Element appInfoElement = doc.createElement("xsd:appinfo");
      String elemNameStr = getName(getElementNode(node));
      String parentElemName = getNodeName(getElementNode(node).getParentNode().getParentNode());

      //      if ((elemNameStr.equals("Actn")
      //          && (!parentElemName.contains("Assignment"))
      //          && (!parentElemName.contains("CitizenshipMaintenance"))
      //          && (!parentElemName.contains("AffiliateAssociationMaintenance"))
      //          && (!parentElemName.contains("RiskRatingMaintenance"))
      //          && (!parentElemName.contains("IpProfitabilityMaint"))
      //          && (!parentElemName.contains("TaxReportingCountryCodeMaint"))
      //          && (!parentElemName.contains("DoNotCombineStatusMaint"))
      //          && (parentElemName.endsWith("Maintenance") || parentElemName.endsWith("Maint"))))
      // {
      //        System.out.println("Missing Action: XSDSchemaObject:" + parentElemName);
      //      }

      if (comment.contains("Field not expose in swagger payload")
          //          || comment.contains("Field is expose in swagger as query")
          //          || comment.contains("Field is expose in swagger as header")
          //          || (elemNameStr.equals("Actn")
          //              && (!parentElemName.contains("Assignment"))
          //              && (!parentElemName.contains("CitizenshipMaintenance"))
          //              && (!parentElemName.contains("AffiliateAssociationMaintenance"))
          //              && (!parentElemName.contains("RiskRatingMaintenance"))
          //              && (!parentElemName.contains("IpProfitabilityMaint"))
          //              && (!parentElemName.contains("TaxReportingCountryCodeMaint"))
          //              && (!parentElemName.contains("DoNotCombineStatusMaint"))
          //              && (!parentElemName.contains("ArIpRelationshipMaintenance"))
          //              && (!parentElemName.contains("IpPointOfContactMaintenance"))
          //              && (parentElemName.endsWith("Maintenance") ||
          // parentElemName.endsWith("Maint")))
          //          || (elemNameStr.equals("ActnCde")
          //              && (parentElemName.endsWith("Maintenance") ||
          // parentElemName.endsWith("Maint")))
          //          || (elemNameStr.equals("MaintActn")
          //              && (parentElemName.endsWith("Maintenance") ||
          // parentElemName.endsWith("Maint")))
          || (elemNameStr.equals("FeRqstKey") && parentElemName.endsWith("Osi"))
          //          //          || (elemNameStr.equals("Actn")
          //          //              && targetNamespace.equals("ECDOMInvolvedParty")
          //          //              && (!parentElemName.contains("TaxReportingCountryCodeMaint")))
          //          //          || (elemNameStr.equals("LstCntl") &&
          // parentElemName.endsWith("Osi"))
          //          //          || (elemNameStr.equals("LstCntrl") &&
          // parentElemName.endsWith("Osi"))
          //          || (elemNameStr.equals("Actn") && parentElemName.endsWith("IpSorReference"))
          || (elemNameStr.equals("MaintRqst") && parentElemName.endsWith("Osi"))
          // || (elemNameStr.equals("MaintActn") && parentElemName.endsWith("Osi"))
          || (elemNameStr.equals("ArRqstKey") && parentElemName.endsWith("Osi"))
          || (elemNameStr.equals("ArKey")
              && parentElemName.endsWith("Osi")
              && !targetNamespace.equals("ECRetrieveArCurrentBalanceRqst"))
          || (elemNameStr.equals("IpRqstKey")
              && parentElemName.endsWith("Osi")
              && !parentElemName.equals("ECCreateIpOsi"))) {

        //        if (!comment.contains("Field not expose in swagger payload")
        //            && ((elemNameStr.equals("Actn")
        //                    && (parentElemName.endsWith("Maintenance") ||
        // parentElemName.endsWith("Maint")))
        //                || (elemNameStr.equals("FeRqstKey") && parentElemName.endsWith("Osi"))
        //            //   || (elemNameStr.equals("LstCntlOtpt") && parentElemName.endsWith("Oso"))
        //            //  || (elemNameStr.equals("IpRqstKey") && parentElemName.endsWith("Osi"))
        //
        //            )) {
        //
        //          node.setTextContent(documentation + " Field not expose in swagger payload");
        //        }

        //        if (!((elemNameStr.equals("Actn")
        //                && (!parentElemName.contains("Assignment"))
        //                && (parentElemName.endsWith("Maintenance") ||
        // parentElemName.endsWith("Maint")))
        //            || (elemNameStr.equals("ActnCde")
        //                && (parentElemName.endsWith("Maintenance") ||
        // parentElemName.endsWith("Maint")))
        //            || (elemNameStr.equals("MaintActn")
        //                && (parentElemName.endsWith("Maintenance") ||
        // parentElemName.endsWith("Maint")))
        //            || (elemNameStr.equals("MaintActn") && parentElemName.endsWith("Osi"))
        //            //            || (elemNameStr.equals("Actn")
        //            //                && targetNamespace.equals("ECDOMInvolvedParty")
        //            //                &&
        // (!parentElemName.contains("TaxReportingCountryCodeMaint")))
        //            || (elemNameStr.equals("FeRqstKey") && parentElemName.endsWith("Osi"))
        //            //            || (elemNameStr.equals("LstCntl") &&
        // parentElemName.endsWith("Osi"))
        //            //            || (elemNameStr.equals("LstCntrl") &&
        // parentElemName.endsWith("Osi"))
        //            || (elemNameStr.equals("MaintRqst") && parentElemName.endsWith("Osi"))
        //            || (elemNameStr.equals("IpRqstKey")
        //                && parentElemName.endsWith("Osi")
        //                && !parentElemName.equals("ECCreateIpOsi")))) {
        //          // System.out.println("Ignoring3:" + targetNamespace + ":" + elemNameStr);
        //        }

        org.w3c.dom.Element annoxElement = doc.createElement("annox:annotate");
        annoxElement.setAttribute("target", "field");
        appInfoElement.appendChild(annoxElement);
        //   System.out.println("Ignoring:" + targetNamespace + ":" + elemNameStr);
        annoxElement.setTextContent("@com.fasterxml.jackson.annotation.JsonIgnore");
      } else {

        if (getTypeName(node).endsWith(":gMonthDay")) {

          // @JsonIgnore

          org.w3c.dom.Element annoxElement = doc.createElement("annox:annotate");
          annoxElement.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement);

          annoxElement.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fis.ec.customer.core.json.util.MonthDaySerializer.class)");

          org.w3c.dom.Element annoxElement2 = doc.createElement("annox:annotate");
          annoxElement2.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement2);

          annoxElement2.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fis.ec.customer.core.json.util.MonthDayDeserializer.class)");
        }
        if (getTypeName(node).endsWith(":date")) {

          // @JsonIgnore

          org.w3c.dom.Element annoxElement = doc.createElement("annox:annotate");
          annoxElement.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement);

          annoxElement.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fis.ec.customer.core.json.util.DateSerializer.class)");

          org.w3c.dom.Element annoxElement2 = doc.createElement("annox:annotate");
          annoxElement2.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement2);

          annoxElement2.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fis.ec.customer.core.json.util.DateDeserializer.class)");
        }
        if (getTypeName(node).endsWith(":dateTime")) {

          // @JsonIgnore

          org.w3c.dom.Element annoxElement = doc.createElement("annox:annotate");
          annoxElement.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement);

          annoxElement.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fis.ec.customer.core.json.util.DateTimeSerializer.class)");

          org.w3c.dom.Element annoxElement2 = doc.createElement("annox:annotate");
          annoxElement2.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement2);

          annoxElement2.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fis.ec.customer.core.json.util.DateTimeDeserializer.class)");
        }
        if (getTypeName(node).endsWith(":time")) {

          // @JsonIgnore

          org.w3c.dom.Element annoxElement = doc.createElement("annox:annotate");
          annoxElement.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement);

          annoxElement.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fis.ec.customer.core.json.util.TimeSerializer.class)");

          org.w3c.dom.Element annoxElement2 = doc.createElement("annox:annotate");
          annoxElement2.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement2);

          annoxElement2.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fis.ec.customer.core.json.util.TimeDeserializer.class)");
        }
        if (getTypeName(node).endsWith(":gYear")) {
          org.w3c.dom.Element annoxElement = doc.createElement("annox:annotate");
          annoxElement.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement);
          annoxElement.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fis.ec.customer.core.json.util.YearSerializer.class)");

          org.w3c.dom.Element annoxElement2 = doc.createElement("annox:annotate");
          annoxElement2.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement2);

          annoxElement2.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fis.ec.customer.core.json.util.YearDeserializer.class)");
        }

        if (getTypeName(node).endsWith(":gYearMonth")) {
          org.w3c.dom.Element annoxElement = doc.createElement("annox:annotate");
          annoxElement.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement);
          annoxElement.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fis.ec.customer.core.json.util.YearMonthSerializer.class)");

          org.w3c.dom.Element annoxElement2 = doc.createElement("annox:annotate");
          annoxElement2.setAttribute("target", "field");
          appInfoElement.appendChild(annoxElement2);

          annoxElement2.setTextContent(
              "@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fis.ec.customer.core.json.util.YearMonthDeserializer.class)");
        }

        if (!businessName.isEmpty()) {
          org.w3c.dom.Element annoxElement = doc.createElement("annox:annotate");
          appInfoElement.appendChild(annoxElement);
          annoxElement.setTextContent(
              "@com.fasterxml.jackson.annotation.JsonProperty(\"" + businessName + "\")");

          //     System.out.println(
          //         "BusinessName:" + targetNamespace + ":" + elemNameStr + ":" + businessName);

        } else {
          log.error("BusinessName not found for:" + documentation);
        }
      }

      // set other attributes as appropriate

      node.getParentNode().insertBefore(appInfoElement, node);
    }
  }
}

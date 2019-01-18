package dependencyVis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import dependencyVis.apachePOI.ApachePOIExcelRead;
import dependencyVis.model.Interface;
import dependencyVis.model.Node;
import dependencyVis.model.Nodes;
import dependencyVis.neo4j.NEO4JDatabase;
import dependencyVis.neo4j.NEO4JDatabase.NodeType;
import dependencyVis.neo4j.NEO4JDatabase.RelationType;
import dependencyVis.utils.CommonUtils;
import dependencyVis.utils.ParsePOM;

/**
 * @author vhacimuf
 *
 */
public class Start {
  public static void main(String[] args) {

    Properties properties2 = new Properties();
    BufferedInputStream stream = null;

    try {
      stream = new BufferedInputStream(new FileInputStream("application.properties"));
    } catch (FileNotFoundException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    try {
      properties2.load(stream);
    } catch (IOException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    try {
      stream.close();
    } catch (IOException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }

    String inputExcelPath = properties2.getProperty("excel");

    String inputSheetNumber = properties2.getProperty("sheet");
    int inputSheetNumberValue = Integer.valueOf(inputSheetNumber);

    String inputColumnNumber = properties2.getProperty("column");
    int inputColumnNumberValue = Integer.valueOf(inputColumnNumber);

    String inputRowNumber = properties2.getProperty("row");
    int inputRowNumberValue = Integer.valueOf(inputRowNumber);

    String svnTempPath = properties2.getProperty("svnTemp");

    String neo4J = properties2.getProperty("neo4J");

    // System.out.println(sprache);

    // String pathToExcelFile = args[0];
    // String secondParam = args[1];
    // String thirdParam = args[2];
    // System.out.println(pathToExcelFile + " " + secondParam + " " + thirdParam);

    // 1. Step: Extrahiere Repository Links aus der Excel Tabelle
    List<String> column = ApachePOIExcelRead.getColumn(inputSheetNumberValue, inputExcelPath, inputColumnNumberValue);

    LinkedList<Node> nodesList = new LinkedList<Node>();
    Nodes nodes = new Nodes();
    nodes.setNodes(nodesList);

    for (int i = 0; i < column.size(); i++) {
      String currentColumn = column.get(i);
      System.out.println(currentColumn);
    }

    // 2. Step: Checkout
    // for (int i = 0; i < column.size(); i++) {

    String[] folder = new String[2];
    folder[0] = svnTempPath + "/Vorlage-Geschaeftsanwendung_bza_1.4.0_01";
    folder[1] = svnTempPath + "/Vorlage-Register_bza_1.3.0_01";

    Map<String, LinkedList<Interface>> mapPrefixToOfferedInterfaces = new HashMap<String, LinkedList<Interface>>();
    Map<String, LinkedList<Interface>> mapPrefixToUsedInterfaces = new HashMap<String, LinkedList<Interface>>();

    for (int i = 0; i < 2; i++) {
      // String url = column.get(i);
      String url = "https://svn.win.tue.nl/repos/prom/Packages/GuideTreeMiner/Trunk/";
      SVNClientManager ourClientManager = SVNClientManager.newInstance();
      SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
      updateClient.setIgnoreExternals(false);

      File f = new File(svnTempPath);

      SVNRepository repository = null;
      SVNURL svnUrl = null;

      try {
        svnUrl = SVNURL.parseURIEncoded(url);
      } catch (SVNException e1) {
        e1.printStackTrace();
      }

      try {
        repository = SVNRepositoryFactory.create(svnUrl);
      } catch (SVNException e) {
        e.printStackTrace();
      }

      try {
        updateClient.doCheckout(svnUrl, f, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
      } catch (SVNException e) {
        e.printStackTrace();
      }

      // TODO access to POM File and get path of it
      // 3. Step: Extrahiere POM Modelle aus den Repositories
      // POM des Parent
      // String pathParent = "svn/Vorlage-Geschaeftsanwendung_bza_1.4.0_01";

      String pathParent = folder[i];

      ParsePOM parsePOMinstance = new ParsePOM(pathParent + "/pom.xml");
      parsePOMinstance.extractInformationFromPOMModel();
      List<String> modules = parsePOMinstance.getPomModel().getModules();

      String[] stockArr = new String[modules.size()];
      stockArr = modules.toArray(stockArr);

      File file = new File(pathParent);
      String[] directories = file.list(new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {

          return new File(current, name).isDirectory();
        }
      });

      String nameMainApplication = CommonUtils.longestCommonPrefix(stockArr);
      LinkedList<Interface> interfaceNames = new LinkedList<Interface>();

      String pathToMainApplication = pathParent + "/" + nameMainApplication;
      String pathToMainApplicationPom = pathParent + "/" + nameMainApplication + "/pom.xml";

      ParsePOM parsePOMMainApplication = new ParsePOM(pathToMainApplicationPom);
      parsePOMMainApplication.extractInformationFromPOMModel(modules);

      List<Interface> offeredInterfacesMainApplication = parsePOMMainApplication.getOfferedInterfaces();
      List<Interface> usedInterfacesMainApplication = parsePOMMainApplication.getUsedInterfaces();

      mapPrefixToOfferedInterfaces.put(nameMainApplication, (LinkedList<Interface>) offeredInterfacesMainApplication);
      mapPrefixToUsedInterfaces.put(nameMainApplication, (LinkedList<Interface>) usedInterfacesMainApplication);

      // String pathMainApplication = "C:\\Users\\vhacimuf\\Desktop\\TWS-4\\Vorlage-Geschaeftsanwendung_bza_1.4.0_01";
      Node node = new Node();
      node.setName(nameMainApplication);
      node.setUsedInterfaces(usedInterfacesMainApplication);
      node.setOfferedInterfaces(offeredInterfacesMainApplication);
      nodesList.add(node);
    }

    // 4. Step: Relationen extrahieren

    // 5. Step: Informationen in die Neo4J Datenbank eintragen
    NEO4JDatabase db = new NEO4JDatabase(neo4J);

    for (Iterator iterator = nodesList.iterator(); iterator.hasNext();) {
      Node node = (Node) iterator.next();

      String name = node.getName();

      Map<String, String> properties = new HashMap<String, String>();
      // properties.put("PiD", "1");
      properties.put("Pid", name);
      properties.put("name", name);

      db.addNode(NodeType.Application, properties, name);

      // myNode.addLabel( DynamicLabel.label( "11" ) );
      // bobNode.setProperty("PiD", 5002);
      // bobNode.setProperty("Age", 23);

      // Relationship alice = aliceNode.createRelationshipTo(bobNode, RelationType.Know);
      // alice.setProperty("test", "test");

      List<Interface> used = node.getUsedInterfaces();
      for (Iterator iterator2 = used.iterator(); iterator2.hasNext();) {
        Interface dependency = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();

        String InterfaceName = dependency.getName();
        String Pid = dependency.getId();
        propertiesInterface.put("Pid", Pid);
        propertiesInterface.put("name", InterfaceName);
        propertiesInterface.put("version", dependency.getVersion());
        // propertiesInterface.put("applicaton", dependency.getVersion());

        db.addNode(NodeType.UsedInterface, propertiesInterface, Pid);

        Map<String, String> propertiesRelation = new HashMap<String, String>();
        // propertiesRelation.put("color", "green");

        db.addRelation(name, NodeType.Application, Pid, NodeType.UsedInterface, propertiesRelation, RelationType.uses);
      }

      List<Interface> offered = node.getOfferedInterfaces();

      for (Iterator iterator2 = offered.iterator(); iterator2.hasNext();) {
        Interface dependency = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        String InterfaceName = dependency.getName();
        String Pid = dependency.getId();
        propertiesInterface.put("Pid", Pid);
        propertiesInterface.put("name", InterfaceName);
        propertiesInterface.put("version", dependency.getVersion());

        db.addNode(NodeType.OfferedInterface, propertiesInterface, InterfaceName);

        Map<String, String> propertiesRelation = new HashMap<String, String>();

        // properties.put("PiD", "1");
        db.addRelation(name, NodeType.Application, Pid, NodeType.OfferedInterface, propertiesRelation,
            RelationType.offers);
      }
    }

    // Relationen herstellen

    for (Iterator iterator = nodesList.iterator(); iterator.hasNext();) {
      Node node = (Node) iterator.next();

      String name = node.getName();
      List<Interface> usedInterfaces = node.getUsedInterfaces();
      for (Iterator iterator2 = usedInterfaces.iterator(); iterator2.hasNext();) {
        Interface interface1 = (Interface) iterator2.next();

        String interface1Name = interface1.getName();
        String interface1Version = interface1.getVersion();
        String interface1Id = interface1.getId();
        // Set<String> set = mapPrefixToOfferedInterfaces.keySet();

        String asds = getKey(mapPrefixToOfferedInterfaces, interface1);

        Node asdsa = nodes.findNode(asds);
        if (asdsa != null) {
          List<Interface> sad = asdsa.getOfferedInterfaces();

          HashMap<String, List<Interface>> hashMap = new HashMap<String, List<Interface>>();

          for (Iterator iterator4 = sad.iterator(); iterator4.hasNext();) {
            Interface interface4 = (Interface) iterator4.next();
            String interfaceName = interface4.getName();

            if (!hashMap.containsKey(interfaceName)) {
              List<Interface> list = new ArrayList<Interface>();
              list.add(interface4);

              hashMap.put(interfaceName, list);
            } else {
              hashMap.get(interfaceName).add(interface4);
            }
          }

          Comparator<Interface> cmp = new Comparator<Interface>() {
            @Override
            public int compare(Interface o1, Interface o2) {

              return Double.valueOf(o1.getVersion()).compareTo(Double.valueOf(o2.getVersion()));
            }
          };

          // System.out.println("max : " + Collections.max(dirNo, cmp));

          Set<String> asdsadsads = hashMap.keySet();

          for (Iterator iterator3 = asdsadsads.iterator(); iterator3.hasNext();) {
            String interface2 = (String) iterator3.next();
            List<Interface> asdasdasd = hashMap.get(interface2);

            Interface asdsad = Collections.max(asdasdasd, cmp);
            double maxVersion = asdsad.getVersionDouble();

            String interface2Name = asdsad.getName();
            String interface2Version = asdsad.getVersion();
            String interface2Id = asdsad.getId();

            if (interface1Name.equals(interface2Name)) {
              Map<String, String> propertiesRelation = new HashMap<String, String>();

              if (Double.valueOf(interface1Version) < maxVersion) {

                db.addRelation(interface1Id, NodeType.UsedInterface, interface2Id, NodeType.OfferedInterface,
                    propertiesRelation, RelationType.Oldversion);
              } else {

                db.addRelation(interface1Id, NodeType.UsedInterface, interface2Id, NodeType.OfferedInterface,
                    propertiesRelation, RelationType.Connection);

              }
            }
          }
        }

        // falls version älter als die neuste -> kante rot

        // mapPrefixToOfferedInterfaces.
        // mapPrefixToUsedInterfaces

        // String abc = Start.getKey(mapPrefixToInterfaces, interfaceName);
        // List<Interface> asdsa = nodes.findNodeWithOfferedInterface(name, interfaceName);

      }
    }

    System.out.println("test");
    // Map<String, Integer> properties = new HashMap<String, Integer>();
    // properties.put("PiD", 1);

    // bobNode.setProperty("PiD", 5002);
    // bobNode.setProperty("Age", 23);
    // db.addNode(NodeType.Application, properties);

    // Map<String, String> properties1 = new HashMap<String, String>();
    // properties.put("PiD", 1);

    db.shutdown();

    // 6. Step: NEO4J Datenbank starten
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.directory(new File("/Users/vhacimuf/Desktop/neo4j-community-3.5.1/bin"));

    processBuilder.command("neo4j console");

    String[] test = new String[] { "/Users/vhacimuf/Desktop/neo4j-community-3.5.1/bin/console" };

    // Process proc = new ProcessBuilder(test).start();

    /*
     * try { Process process = processBuilder.start();
     *
     * StringBuilder output = new StringBuilder();
     *
     * BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
     *
     * String line; while ((line = reader.readLine()) != null) { output.append(line + "\n"); }
     *
     * int exitVal = process.waitFor(); if (exitVal == 0) { System.out.println("Success!"); System.out.println(output);
     * System.exit(0); } else { // abnormal... }
     *
     * } catch (IOException e) { e.printStackTrace(); } catch (InterruptedException e) { e.printStackTrace(); }
     */
  }

  public static List<String> getColumn(String path, int columnIndex) {

    LinkedList<String> output = new LinkedList<String>();

    try {
      FileInputStream file = new FileInputStream(new File(path));

      // Create Workbook instance holding reference to .xlsx file
      XSSFWorkbook workbook = new XSSFWorkbook(file);

      // Get first/desired sheet from the workbook
      XSSFSheet sheet = workbook.getSheetAt(1);

      for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
          Cell cell = row.getCell(8);
          if (cell != null) {
            // Found column and there is value in the cell.
            String cellValueMaybeNull = cell.getStringCellValue();
            // System.out.println(cellValueMaybeNull);

            output.add(cellValueMaybeNull);
            // Do something with the cellValueMaybeNull here ...
          }
        }
      }

      // Iterate through each rows one by one
      file.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return output;
  }

  public static String getKey(Map<String, LinkedList<Interface>> mapPrefixToInterfaces, Interface pInterface) {

    Set<String> keySetHashMap = mapPrefixToInterfaces.keySet();

    String result = null;
    for (Iterator iterator = keySetHashMap.iterator(); iterator.hasNext();) {
      String string = (String) iterator.next();
      LinkedList<Interface> list = mapPrefixToInterfaces.get(string);

      boolean asdsa = checkTest(list, pInterface);

      if (asdsa) {
        result = string;
        break;
      }
    }
    return result;
  }

  public static boolean checkTest(LinkedList<Interface> list, Interface pInterface) {

    boolean result = false;
    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      Interface interface1 = (Interface) iterator.next();

      String pInterfaceName = pInterface.getName();
      String pInterfaceVersion = pInterface.getVersion();

      String InterfaceName = interface1.getName();
      String InterfaceVersion = interface1.getVersion();

      if (pInterfaceName.equals(InterfaceName) && pInterfaceVersion.equals(InterfaceVersion)) {
        result = true;
        break;
      }
    }
    return result;
  }

}

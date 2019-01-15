package com.programmer.gate2.readData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
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

import com.programmer.gate2.readData.NEO4JDatabase.NodeType;
import com.programmer.gate2.readData.NEO4JDatabase.RelationType;

/**
 * @author vhacimuf
 *
 */
public class Start {
  public static void main(String[] args) {

    // String pathToExcelFile = args[0];
    // String secondParam = args[1];
    // String thirdParam = args[2];
    // System.out.println(pathToExcelFile + " " + secondParam + " " + thirdParam);

    // 1. Step: Extrahiere Repository Links aus der Excel Tabelle
    String path = "excel/2019-01-04_Gesamtreleaseletter_2019-1-R.xlsm";
    List<String> column = ApachePOIExcelRead.getColumn(path, 8);

    LinkedList<Node> nodes = new LinkedList<Node>();

    for (int i = 0; i < column.size(); i++) {
      String currentColumn = column.get(i);
      System.out.println(currentColumn);
    }

    // 2. Step: Checkout
    // for (int i = 0; i < column.size(); i++) {
    for (int i = 0; i < 1; i++) {
      // String url = column.get(i);
      String url = "https://svn.win.tue.nl/repos/prom/Packages/GuideTreeMiner/Trunk/";
      SVNClientManager ourClientManager = SVNClientManager.newInstance();
      SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
      updateClient.setIgnoreExternals(false);

      File f = new File("svn");

      SVNRepository repository = null;
      SVNURL svnUrl = null;

      try {
        svnUrl = SVNURL.parseURIEncoded(url);
      } catch (SVNException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      try {
        repository = SVNRepositoryFactory.create(svnUrl);
      } catch (SVNException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      try {
        updateClient.doCheckout(svnUrl, f, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
      } catch (SVNException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // TODO access to POM File and get path of it
      // 3. Step: Extrahiere POM Modelle aus den Repositories
      // POM des Parent
      String pathParent = "svn/Vorlage-Geschaeftsanwendung_bza_1.4.0_01";

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
      // Name der Hauptanwendung entspricht dem größten gemeinsamen Präfix
      String nameMainApplication = CommonUtils.longestCommonPrefix(stockArr);

      String pathToMainApplication = pathParent + "/" + nameMainApplication;
      String pathToMainApplicationPom = pathParent + "/" + nameMainApplication + "/pom.xml";

      ParsePOM parsePOMMainApplication = new ParsePOM(pathToMainApplicationPom);
      parsePOMMainApplication.extractInformationFromPOMModel(modules);

      List<Dependency> offeredInterfacesMainApplication = parsePOMMainApplication.getOfferedInterfaces();
      List<Dependency> usedInterfacesMainApplication = parsePOMMainApplication.getUsedInterfaces();

      // String pathMainApplication = "C:\\Users\\vhacimuf\\Desktop\\TWS-4\\Vorlage-Geschaeftsanwendung_bza_1.4.0_01";
      Node node = new Node();
      node.setName(nameMainApplication);
      node.setUsedInterfaces(usedInterfacesMainApplication);
      node.setOfferedInterfaces(offeredInterfacesMainApplication);
      nodes.add(node);
    }

    // Für jeden Knoten: offeredInstances && usedInstances
    // 4. Step: Relationen extrahieren

    // nodes

    // 5. Step: Informationen in die Neo4J Datenbank eintragen
    NEO4JDatabase db = new NEO4JDatabase("/Users/vhacimuf/Desktop/neo4j-community-3.5.1" + "/data/databases/graph.db");

    for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
      Node node = (Node) iterator.next();

      String name = node.getName();

      Map<String, String> properties = new HashMap<String, String>();
      // properties.put("PiD", "1");
      properties.put("id", name);
      db.addNode(NodeType.Application, properties, name);

      // myNode.addLabel( DynamicLabel.label( "11" ) );

      // bobNode.setProperty("PiD", 5002);
      // bobNode.setProperty("Age", 23);

      // Relationship alice = aliceNode.createRelationshipTo(bobNode, RelationType.Know);
      // alice.setProperty("test", "test");

      List<Dependency> used = node.getUsedInterfaces();
      for (Iterator iterator2 = used.iterator(); iterator2.hasNext();) {
        Dependency dependency = (Dependency) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();

        String artifactId = dependency.getArtifactId();
        propertiesInterface.put("id", artifactId);
        db.addNode(NodeType.UsedInterface, propertiesInterface, artifactId);
        // TODO add relation

        Map<String, String> propertiesRelation = new HashMap<String, String>();
        propertiesRelation.put("color", "green");

        // properties.put("PiD", "1");

        db.addRelation(name, NodeType.Application, artifactId, NodeType.UsedInterface, propertiesRelation,
            RelationType.uses);
      }

      List<Dependency> offered = node.getOfferedInterfaces();

      for (Iterator iterator2 = offered.iterator(); iterator2.hasNext();) {
        Dependency dependency = (Dependency) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        String artifactId = dependency.getArtifactId();
        propertiesInterface.put("id", artifactId);
        db.addNode(NodeType.OfferedInterface, propertiesInterface, artifactId);

        Map<String, String> propertiesRelation = new HashMap<String, String>();
        // properties.put("PiD", "1");

        db.addRelation(name, NodeType.Application, artifactId, NodeType.OfferedInterface, propertiesRelation,
            RelationType.offers);
      }
    }

    Map<String, Integer> properties = new HashMap<String, Integer>();
    properties.put("PiD", 1);

    // bobNode.setProperty("PiD", 5002);
    // bobNode.setProperty("Age", 23);
    // db.addNode(NodeType.Application, properties);

    Map<String, String> properties1 = new HashMap<String, String>();
    properties.put("PiD", 1);

    // db.addNode(NodeType.Application, properties1);

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
}

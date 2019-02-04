package updatedinterfacesvis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import updatedinterfacesvis.model.Interface;
import updatedinterfacesvis.model.Node;
import updatedinterfacesvis.model.Nodes;
import updatedinterfacesvis.neo4j.Neo4J;
import updatedinterfacesvis.neo4j.Neo4J.NodeType;
import updatedinterfacesvis.neo4j.Neo4J.RelationType;
import updatedinterfacesvis.poi.ParseExcel;
import updatedinterfacesvis.svn.config.Konfiguration;
import updatedinterfacesvis.svn.utils.SvnHelperNeu;
import updatedinterfacesvis.utils.CommonUtils;
import updatedinterfacesvis.utils.ParsePOM;

/**
 * @author vhacimuf
 *
 */
public class main {

  private static final Logger LOG = Logger.getLogger("Main");

  public static void main(String[] args) {

    try {
      SimpleLayout layout = new SimpleLayout();
      ConsoleAppender consoleAppender = new ConsoleAppender(layout);
      LOG.addAppender(consoleAppender);
      FileAppender fileAppender = new FileAppender(layout, "logs/default.log", false);
      LOG.addAppender(fileAppender);
      // ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
      // LOG.setLevel(Level.WARN);
    } catch (Exception ex) {
      LOG.debug(ex.getMessage());
    }

    // 1. Lese Parameter aus der application.properties Datei ein
    Properties applicationProperties = new Properties();
    BufferedInputStream stream = null;

    // Einlesen des Property-Files
    try {
      stream = new BufferedInputStream(new FileInputStream("application.properties"));
    } catch (FileNotFoundException e2) {
      LOG.error("File 'application.properties' not found");
    }
    try {
      applicationProperties.load(stream);
    } catch (IOException e2) {
      LOG.debug(e2.getMessage());
    }
    try {
      stream.close();
    } catch (IOException e2) {
      LOG.debug(e2.getMessage());
    }

    // Zugriff auf die Properties
    String inputSVNUrl = applicationProperties.getProperty("svnUrl");
    String inputSVNUsername = applicationProperties.getProperty("svnUsername");
    String inputSVNPassword = applicationProperties.getProperty("svnPassword");
    String inputSVNCertificate = applicationProperties.getProperty("svnCertificate");
    String inputSVNCertificatePassword = applicationProperties.getProperty("svnCertificatePassword");
    String inputSVNGesamtreleaseletterAblageort = applicationProperties.getProperty("svnGesamtreleaseletterAblageort");
    String inputSVNCheckPath = applicationProperties.getProperty("svnCheckPath");
    String inputSVNStartScreen = applicationProperties.getProperty("svnStartScreen");
    String inputExcelPath = applicationProperties.getProperty("excel");
    String inputSheetNumber = applicationProperties.getProperty("sheet");
    int inputSheetNumberValue = Integer.parseInt(inputSheetNumber);
    String inputColumnNumber = applicationProperties.getProperty("column");
    int inputColumnNumberValue = Integer.parseInt(inputColumnNumber);

    String inputRowNumber = applicationProperties.getProperty("row");
    int inputRowNumberValue = Integer.parseInt(inputRowNumber);
    String svnTempPath = applicationProperties.getProperty("svnTemp");

    String svnTempPathDelete = applicationProperties.getProperty("svnTempDelete");
    boolean svnTempPathDeleteValue = Boolean.parseBoolean(svnTempPathDelete);

    String neo4J = applicationProperties.getProperty("neo4J");
    String neo4JServer = applicationProperties.getProperty("neo4JServer");

    String startNeo4JServer = applicationProperties.getProperty("startNeo4JServer");
    boolean startNeo4JServerValue = Boolean.parseBoolean(startNeo4JServer);

    String regEx = applicationProperties.getProperty("regEx");

    // Create SVNTemp Folder, if it's not exist
    createFolderIfNotExist(svnTempPath);

    // 2. Step: Greife auf die Exceltabelle zu und extrahiere Spalte mit den Repositories
    List<String> column = ParseExcel.parse(inputSheetNumberValue, inputExcelPath, inputColumnNumberValue);
    LinkedList<Node> nodesList = new LinkedList<>();
    Nodes nodes = new Nodes();
    nodes.setNodes(nodesList);

    Konfiguration svnConfig = new Konfiguration();
    svnConfig.setSvnUsername(inputSVNUsername);
    svnConfig.setSvnPassword(inputSVNPassword);

    svnConfig.setSvnCertificate(inputSVNCertificate);
    svnConfig.setSvnCertificatePasswort(inputSVNCertificatePassword);

    svnConfig.setGesamtreleaseletterAblageort(inputSVNGesamtreleaseletterAblageort);
    svnConfig.setSvnCheckPath(inputSVNCheckPath);
    svnConfig.setStartScreen(inputSVNStartScreen);

    SvnHelperNeu svnHelper = new SvnHelperNeu();
    svnHelper.init(svnConfig);

    Map<String, LinkedList<Interface>> mapPrefixToOfferedInterfaces = new HashMap<String, LinkedList<Interface>>();
    Map<String, LinkedList<Interface>> mapPrefixToUsedInterfaces = new HashMap<String, LinkedList<Interface>>();
    List<Dependency> dsfddsfdsf = null;

    // 3. Step: Führe auf jedes Repository ein SVN-Checkout aus
    for (int i = inputRowNumberValue; i < column.size(); i++) {
      String[] folder = new String[2];

      String url = column.get(i);
      SVNClientManager ourClientManager = SVNClientManager.newInstance();
      SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
      updateClient.setIgnoreExternals(false);

      File f = new File(svnTempPath + "/" + i);

      SVNRepository repository = null;
      SVNURL svnUrl = null;

      try {
        svnUrl = SVNURL.parseURIEncoded(url);
      } catch (SVNException e1) {
        LOG.debug(e1.getMessage());

      }

      try {
        repository = SVNRepositoryFactory.create(svnUrl);
      } catch (SVNException e) {
        LOG.debug(e.getMessage());

      }

      System.out.println("SVN Checkout:" + svnUrl);

      // try {
      // updateClient.doCheckout(svnUrl, f, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
      // } catch (SVNException e) {
      // e.printStackTrace();
      // LOG.error("SVN Checkout error");
      // }

      // 4. Step: Ableitung von POM-Modellen aus POM.xml's in den Repositories
      //
      // -> 4.1 POM Modell des Parent POM.xml
      // -> 4.1 POM Modell der Hauptanwendung POM.xml
    }

    String[] subFolders = null;
    File file = new File(svnTempPath);
    subFolders = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {

        return new File(current, name).isDirectory();
      }
    });

    int sizeFolders = subFolders.length;

    for (int i = 0; i < sizeFolders; i++) {
      String folderName = svnTempPath + "/" + subFolders[i];

      ParsePOM parsePOMinstance = null;

      try {
        parsePOMinstance = new ParsePOM(folderName + "/pom.xml", regEx);
      } catch (IOException e1) {
        LOG.debug(e1.getMessage());
      } catch (XmlPullParserException e1) {
        LOG.debug(e1.getMessage());

      } catch (NullPointerException e1) {
        LOG.debug(e1.getMessage());
        continue;
      }

      parsePOMinstance.extractInformationFromPOMModel();
      List<String> modules = parsePOMinstance.getPomModel().getModules();

      Model parentModuleModel = parsePOMinstance.getPomModel();

      DependencyManagement dependencyManagment = parsePOMinstance.getPomModel().getDependencyManagement();
      dsfddsfdsf = dependencyManagment.getDependencies();

      String[] stockArr = new String[modules.size()];
      stockArr = modules.toArray(stockArr);

      // Identifizierung der Hauptanwendung durch Bestimmung der größten gemeinsamen Präfix

      HashMap<String, Integer> countPrefix = new HashMap<>();
      for (int j = 0; j < stockArr.length; j++) {
        String a = stockArr[j];
        for (int j2 = 0; j2 < stockArr.length; j2++) {
          if (j != j2) {
            String b = stockArr[j2];

            String[] stringArray = new String[2];
            stringArray[0] = a;
            stringArray[1] = b;

            String longestCmmPrefix = CommonUtils.longestCommonPrefix(stringArray);

            Integer sads = countPrefix.get(longestCmmPrefix);

            if (sads == null) {
              countPrefix.put(longestCmmPrefix, 1);
            } else {
              countPrefix.put(longestCmmPrefix, sads + 1);

            }
          }
        }
      }

      Set<String> countPrefixKeySet = countPrefix.keySet();
      int countMax = 0;
      String countMaxKey = "";

      for (Iterator iterator = countPrefixKeySet.iterator(); iterator.hasNext();) {
        String string = (String) iterator.next();

        Integer count = countPrefix.get(string);
        if (count > countMax) {
          countMax = count;
          countMaxKey = string;
        }
      }

      if (countMaxKey.charAt(countMaxKey.length() - 1) == '-') {
        countMaxKey = (String) countMaxKey.subSequence(0, countMaxKey.length() - 1);
      }

      String nameMainApplication = countMaxKey;

      String pathToMainApplicationPom = folderName + "/" + nameMainApplication + "/pom.xml";

      ParsePOM parsePOMMainApplication = null;
      try {
        parsePOMMainApplication = new ParsePOM(pathToMainApplicationPom, regEx);
      } catch (IOException | XmlPullParserException e) {

        LOG.debug(e.getMessage());

      }

      parsePOMMainApplication.extractInformationFromPOMModel(parentModuleModel);

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

    // Leere svn Ordner mit Checkouts

    if (svnTempPathDeleteValue) {
      try {
        deleteDirectoryRecursion(new File(svnTempPath));
      } catch (IOException e) {
        LOG.debug(e.getMessage());
      }
    }

    File theDir = new File(svnTempPath);
    // if the directory does not exist, create it

    // Lösche alte NEO4J Datenbank.
    try {
      deleteDirectoryRecursion(new File(neo4J));
    } catch (IOException e) {
      LOG.debug(e.getMessage());
    }

    // 5. Step: Erstelle Neo4J Datenbank und bilde das Model auf einen NEO4J Graphen ab.
    Neo4J db = new Neo4J(neo4J);

    for (Iterator iterator = nodesList.iterator(); iterator.hasNext();) {
      Node node1 = (Node) iterator.next();

      String name = node1.getName();

      Map<String, String> properties = new HashMap<String, String>();
      // properties.put("pid", "1");
      properties.put("pid", name);
      properties.put("name", name);

      db.addNode(NodeType.Application, properties, name);

      List<Interface> offered = node1.getOfferedInterfaces();

      for (Iterator iterator2 = offered.iterator(); iterator2.hasNext();) {
        Interface dependency = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        String InterfaceName = dependency.getName();
        String Pid = dependency.getId();
        propertiesInterface.put("pid", Pid);
        propertiesInterface.put("name", InterfaceName);
        propertiesInterface.put("businessVersion", dependency.getVersion());
        // get technical version
        String technical = dependency.getTechnicalVersion();
        propertiesInterface.put("technicalVersion", technical);

        db.addNode(NodeType.Interface, propertiesInterface);

        Map<String, String> propertiesRelation = new HashMap<String, String>();

        // properties.put("pid", "1");
        db.addRelation(name, NodeType.Application, Pid, NodeType.Interface, propertiesRelation, RelationType.offers);
      }

      List<Interface> used = node1.getUsedInterfaces();
      for (Iterator iterator2 = used.iterator(); iterator2.hasNext();) {
        Interface dependency = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();

        String InterfaceName = dependency.getName();
        String Pid = dependency.getId();
        propertiesInterface.put("pid", Pid);
        propertiesInterface.put("name", InterfaceName);
        propertiesInterface.put("businessVersion", dependency.getVersion());
        String technical = dependency.getTechnicalVersion();
        propertiesInterface.put("technicalVersion", technical);

        // Fallunterscheidug
        Map<String, String> propertiesRelation = new HashMap<String, String>();

      }

    }

    for (Iterator iterator = nodesList.iterator(); iterator.hasNext();) {
      Node node1 = (Node) iterator.next();

      String applicationName = node1.getName();

      List<Interface> used = node1.getUsedInterfaces();
      for (Iterator iterator2 = used.iterator(); iterator2.hasNext();) {
        Interface usedInterface = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        Map<String, String> propertiesRelation = new HashMap<String, String>();

        // Gibt es einen Knoten der das anbietet?
        // Prüfe ob es einen Knoten existiert, der usedInterface anbietet
        String checkNode = Nodes.getKey(mapPrefixToOfferedInterfaces, usedInterface);

        if (checkNode == null) {

          String interfaceName = usedInterface.getName();
          String pId = usedInterface.getId();
          propertiesInterface.put("pid", pId);
          propertiesInterface.put("name", interfaceName);
          propertiesInterface.put("businessVersion", usedInterface.getVersion());
          // get technical version
          String technical = usedInterface.getTechnicalVersion();
          propertiesInterface.put("technicalVersion", technical);

          db.addNode(NodeType.Interface, propertiesInterface);

          db.addRelation(applicationName, NodeType.Application, usedInterface.getId(), NodeType.Interface,
              propertiesRelation, RelationType.uses);

          // TODO: Relation used but not offered! Solved 04.02.2018

        } else {
          db.addRelation(applicationName, NodeType.Application, usedInterface.getId(), NodeType.Interface,
              propertiesRelation, RelationType.uses);

        }
      }
    }

    // nach nicht technischem Update wird version zurückgesetzt
    /*
     * // 6. Relationen herstellen for (Iterator iterator = nodesList.iterator(); iterator.hasNext();) { Node node =
     * (Node) iterator.next();
     *
     * String name = node.getName(); List<Interface> usedInterfaces = node.getUsedInterfaces(); for (Iterator iterator2
     * = usedInterfaces.iterator(); iterator2.hasNext();) { Interface interface1 = (Interface) iterator2.next();
     *
     * String interface1Name = interface1.getName(); String interface1Version = interface1.getVersion(); String
     * interface1Id = interface1.getId(); // Set<String> set = mapPrefixToOfferedInterfaces.keySet();
     *
     * String asds = getKey(mapPrefixToOfferedInterfaces, interface1);
     *
     * Node foundNode = nodes.findNode(asds); if (foundNode != null) { List<Interface> foundNodeOfferedInterfaces =
     * foundNode.getOfferedInterfaces();
     *
     * HashMap<String, List<Interface>> groupedByInterfaces = new HashMap<String, List<Interface>>();
     *
     * // Gruppiere alle Interfaces mit gleichem Namen aber unterschiedlicher Version in ein Cluster (Group by) for
     * (Iterator iterator4 = foundNodeOfferedInterfaces.iterator(); iterator4.hasNext();) { Interface interface4 =
     * (Interface) iterator4.next(); String interfaceName = interface4.getName();
     *
     * if (!groupedByInterfaces.containsKey(interfaceName)) { List<Interface> list = new ArrayList<Interface>();
     * list.add(interface4);
     *
     * groupedByInterfaces.put(interfaceName, list); } else { groupedByInterfaces.get(interfaceName).add(interface4); }
     * }
     *
     * Comparator<Interface> cmp = new Comparator<Interface>() {
     *
     * @Override public int compare(Interface o1, Interface o2) {
     *
     * return Double.valueOf(o1.getVersion()).compareTo(Double.valueOf(o2.getVersion())); } };
     *
     * Set<String> asdsadsads = groupedByInterfaces.keySet();
     *
     * for (Iterator iterator3 = asdsadsads.iterator(); iterator3.hasNext();) { String interface2 = (String)
     * iterator3.next(); List<Interface> asdasdasd = groupedByInterfaces.get(interface2);
     *
     * Interface asdsad = Collections.max(asdasdasd, cmp); double maxVersion = asdsad.getVersionDouble();
     *
     * String interface2Name = asdsad.getName(); String interface2Version = asdsad.getVersion(); String interface2Id =
     * asdsad.getId();
     *
     * List<Interface> asdsadsadasds = asdasdasd;
     *
     * for (Iterator iterator4 = asdsadsadasds.iterator(); iterator4.hasNext();) { Interface interface3 = (Interface)
     * iterator4.next();
     *
     * if (interface1Name.equals(interface3.getName()) && interface1Version.equals(interface3.getVersion())) {
     * Map<String, String> propertiesRelation = new HashMap<String, String>();
     *
     * // Falls eine veraltete Schnittstelle genutzt wird, verwende Oldversion als Relationslabel(zur visuellen //
     * Unterscheidung) if (Double.valueOf(interface1Version) < maxVersion) {
     *
     * db.addRelation(interface1Id, NodeType.UsedInterface, interface3.getId(), NodeType.OfferedInterface,
     * propertiesRelation, RelationType.Oldversion); } else {
     *
     * db.addRelation(interface1Id, NodeType.UsedInterface, interface3.getId(), NodeType.OfferedInterface,
     * propertiesRelation, RelationType.Connection);
     *
     * } }
     *
     * }
     *
     * } }
     *
     * } }
     *
     *
     */
    // Beende die Verbindung mit der Datenbank
    db.shutdown();

    // Prüfe, ob Neo4J Server gestartet werden soll.
    if (startNeo4JServerValue) {
      // Erstelle BAT File
      final File batFile = new File("run.bat");
      try {
        batFile.createNewFile();
      } catch (IOException e2) {
        LOG.debug(e2.getMessage());
      }
      PrintWriter writer = null;
      try {
        writer = new PrintWriter(batFile, "UTF-8");
      } catch (FileNotFoundException e1) {
        //
      } catch (UnsupportedEncodingException e1) {
      }
      writer.println("cd " + neo4JServer);
      writer.println("neo4j console");
      writer.close();

      // Führe BAT File aus
      Process p = null;
      try {
        p = Runtime.getRuntime().exec("cmd /c start run.bat");
      } catch (IOException e) {
        LOG.debug(e.getMessage());
      }
      try {
        p.waitFor();
      } catch (InterruptedException e) {
        LOG.debug(e.getMessage());
      }
    }
  }

  /**
   * @param theDir
   */
  private static void createFolderIfNotExist(String path) {

    File theDir = new File(path);

    if (!theDir.exists()) {
      System.out.println("creating directory: " + theDir.getName());
      boolean result = false;

      try {
        theDir.mkdir();
        result = true;
      } catch (SecurityException se) {
        // handle it
      }
      if (result) {
        // System.out.println("DIR created");
      }
    }
  }

  public static void deleteDirectoryRecursion(File file) throws IOException {

    if (file.isDirectory()) {
      File[] entries = file.listFiles();
      if (entries != null) {
        for (File entry : entries) {
          deleteDirectoryRecursion(entry);
        }
      }
    }
    if (!file.delete()) {
      throw new IOException("Failed to delete " + file);
    }
  }
}

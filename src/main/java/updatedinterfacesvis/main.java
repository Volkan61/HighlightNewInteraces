package updatedinterfacesvis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
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
      System.out.println(ex);
    }

    /*
     * logger.debug( "Meine Debug-Meldung" ); logger.info( "Meine Info-Meldung" ); logger.warn( "Meine Warn-Meldung" );
     * logger.error( "Meine Error-Meldung" ); logger.fatal( "Meine Fatal-Meldung" );
     */

    boolean append = true;

    // 1. Lese Parameter aus der application.properties Datei ein

    Properties applicationProperties = new Properties();
    BufferedInputStream stream = null;

    // Einlesen des Propertie-Files
    try {
      stream = new BufferedInputStream(new FileInputStream("application.properties"));
    } catch (FileNotFoundException e2) {
      LOG.error("File 'application.properties' not found");
    }
    try {
      applicationProperties.load(stream);
    } catch (IOException e2) {
      LOG.debug(e2);
    }
    try {
      stream.close();
    } catch (IOException e2) {
      LOG.debug(e2);
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
    // int inputRowNumberValue = Integer.parseInt(inputRowNumber);
    String svnTempPath = applicationProperties.getProperty("svnTemp");
    String neo4J = applicationProperties.getProperty("neo4J");
    String regEx = applicationProperties.getProperty("regEx");

    // 2. Step: Greife auf die Exceltabelle zu und extrahiere Spalte mit den Repositories
    List<String> column = ParseExcel.parse(inputSheetNumberValue, inputExcelPath, inputColumnNumberValue);

    LinkedList<Node> nodesList = new LinkedList<>();
    Nodes nodes = new Nodes();
    nodes.setNodes(nodesList);

    // Links alle hier gespeichert
    // row Min und row Max berücksichtigen
    for (int i = 0; i < column.size(); i++) {
      String currentColumn = column.get(i);
      // System.out.println(currentColumn);
    }

    // Preferences pdfds = Preferences.userNodeForPackage();

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

    // svnHelper.checkSVNLink("test");
    // write funciton for checkout

    // 3. Step: Führe auf jedes Repository ein SVN-Checkout aus
    // for (int i = 0; i < column.size(); i++) {
    String[] folder = new String[2];
    folder[0] = svnTempPath + "/Vorlage-Geschaeftsanwendung_bza_1.4.0_01";
    folder[1] = svnTempPath + "/Vorlage-Register_bza_1.3.0_01";

    Map<String, LinkedList<Interface>> mapPrefixToOfferedInterfaces = new HashMap<String, LinkedList<Interface>>();
    Map<String, LinkedList<Interface>> mapPrefixToUsedInterfaces = new HashMap<String, LinkedList<Interface>>();
    List<Dependency> dsfddsfdsf = null;

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
        LOG.error("SVN Checkout error");
      }

      // 4. Step: Ableitung von POM-Modellen aus POM.xml's in den Repositories
      //
      // -> 4.1 POM Modell des Parent POM.xml
      // -> 4.1 POM Modell der Hauptanwendung POM.xml

      String pathParent = folder[i];

      ParsePOM parsePOMinstance = null;

      try {
        parsePOMinstance = new ParsePOM(pathParent + "/pom.xml", regEx);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (XmlPullParserException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (NullPointerException e1) {

        // TODO Which project is not a maven project, print out!
        e1.printStackTrace();
        LOG.error("POM File not found");
        continue;
      }

      parsePOMinstance.extractInformationFromPOMModel();
      List<String> modules = parsePOMinstance.getPomModel().getModules();

      Model parentModuleModel = parsePOMinstance.getPomModel();

      DependencyManagement dependencyManagment = parsePOMinstance.getPomModel().getDependencyManagement();
      dsfddsfdsf = dependencyManagment.getDependencies();

      String[] stockArr = new String[modules.size()];
      stockArr = modules.toArray(stockArr);

      File file = new File(pathParent);

      String[] directories = file.list(new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {

          return new File(current, name).isDirectory();
        }
      });

      // Identifizierung der Hauptanwendung durch Bestimmung der größten gemeinsamen Präfix

      String nameMainApplication = CommonUtils.longestCommonPrefix(stockArr);
      LinkedList<Interface> interfaceNames = new LinkedList<Interface>();

      String pathToMainApplication = pathParent + "/" + nameMainApplication;
      String pathToMainApplicationPom = pathParent + "/" + nameMainApplication + "/pom.xml";

      ParsePOM parsePOMMainApplication = null;
      try {
        parsePOMMainApplication = new ParsePOM(pathToMainApplicationPom, regEx);
      } catch (IOException | XmlPullParserException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
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

    // 5. Step: Erstelle Neo4J Datenbank und bilde das Model auf einen NEO4J Graphen ab.

    Neo4J db = new Neo4J(neo4J);

    for (Iterator iterator = nodesList.iterator(); iterator.hasNext();) {
      Node node = (Node) iterator.next();

      String name = node.getName();

      Map<String, String> properties = new HashMap<String, String>();
      // properties.put("PiD", "1");
      properties.put("Pid", name);
      properties.put("name", name);

      db.addNode(NodeType.Application, properties, name);

      List<Interface> offered = node.getOfferedInterfaces();

      for (Iterator iterator2 = offered.iterator(); iterator2.hasNext();) {
        Interface dependency = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        String InterfaceName = dependency.getName();
        String Pid = dependency.getId();
        propertiesInterface.put("Pid", Pid);
        propertiesInterface.put("name", InterfaceName);
        propertiesInterface.put("version", dependency.getVersion());
        // get technical version
        String technical = dependency.getTechnicalVersion();
        propertiesInterface.put("technicalVersion", technical);

        db.addNode(NodeType.Interface, propertiesInterface, InterfaceName);

        Map<String, String> propertiesRelation = new HashMap<String, String>();

        // properties.put("PiD", "1");
        db.addRelation(name, NodeType.Application, Pid, NodeType.Interface, propertiesRelation, RelationType.offers);
      }

      List<Interface> used = node.getUsedInterfaces();
      for (Iterator iterator2 = used.iterator(); iterator2.hasNext();) {
        Interface dependency = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();

        String InterfaceName = dependency.getName();
        String Pid = dependency.getId();
        propertiesInterface.put("Pid", Pid);
        propertiesInterface.put("name", InterfaceName);
        propertiesInterface.put("version", dependency.getVersion());
        String technical = dependency.getTechnicalVersion();
        propertiesInterface.put("technicalVersion", technical);

        // get technical version

        // propertiesInterface.put("applicaton", dependency.getVersion());

        // db.addNode(NodeType.Interface, propertiesInterface, Pid);
        // Fallunterscheidug
        Map<String, String> propertiesRelation = new HashMap<String, String>();
        // propertiesRelation.put("color", "green");

        // db.addRelation(name, NodeType.Application, Pid, NodeType.Interface, propertiesRelation, RelationType.uses);
      }

    }

    for (Iterator iterator = nodesList.iterator(); iterator.hasNext();) {
      Node node = (Node) iterator.next();

      String sadsad = node.getName();

      List<Interface> used = node.getUsedInterfaces();
      for (Iterator iterator2 = used.iterator(); iterator2.hasNext();) {
        Interface usedInterface = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        Map<String, String> propertiesRelation = new HashMap<String, String>();

        // Gibt es einen Knoten der das anbietet?
        // Prüfe ob es einen Knoten existiert, der usedInterface anbietet
        String checkNode = Nodes.getKey(mapPrefixToOfferedInterfaces, usedInterface);

        if (checkNode == null) {

          String InterfaceName = usedInterface.getName();
          String Pid = usedInterface.getId();
          propertiesInterface.put("Pid", Pid);
          propertiesInterface.put("name", InterfaceName);
          propertiesInterface.put("version", usedInterface.getVersion());
          // get technical version
          String technical = usedInterface.getTechnicalVersion();
          propertiesInterface.put("technicalVersion", technical);

        } else {
          db.addRelation(sadsad, NodeType.Application, usedInterface.getId(), NodeType.Interface, propertiesRelation,
              RelationType.uses);

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
  }

}
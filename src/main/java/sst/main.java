package sst;

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
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import sst.model.Interface;
import sst.model.ModelUtils;
import sst.model.Node;
import sst.neo4j.Neo4j;
import sst.neo4j.Neo4j.NodeType;
import sst.neo4j.Neo4j.RelationType;
import sst.poi.ParseExcel;
import sst.svn.config.Konfiguration;
import sst.svn.utils.SvnHelperNeu;
import sst.utils.CommonUtils;
import sst.utils.ParsePOM;

/**
 * Extraktion von Relationen zwischen Anwendungen und Generierung einer NEO4J Datenbank zur Visualisierung des Graphes
 * und Ableitung von neuen Erkenntnissen.
 *
 * @author CapGemini, Volkan Hacimüftüoglu
 * @version 05.02.2018
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
    } catch (Exception ex) {
      LOG.debug(ex.getMessage());
    }

    // 1 Lese Parameter aus der application.properties Datei ein
    Properties applicationProperties = new Properties();
    BufferedInputStream stream = null;

    // 1.1 Einlesen des Property-Files
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

    /*
     * Zugriff auf die Properties
     */
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
    String inputColumnNumberRepositories = applicationProperties.getProperty("columnRepositoryLinks");
    int inputColumnNumberRepositoriesValue = Integer.parseInt(inputColumnNumberRepositories);
    String inputColumnNumberRepositoryNames = applicationProperties.getProperty("columnRepositoryNames");
    int inputColumnNumberRepositoryNamesValue = Integer.parseInt(inputColumnNumberRepositoryNames);
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

    /*
     * Der SVNTemp Ordner soll erstellt werden, falls dieser noch nicht existiert. In diesem Ordner werden die SVN
     * Checkouts hinterlegt.
     */
    createFolderIfNotExist(svnTempPath);

    /*
     * 2. Für jede Anwendung existiert ein SVN Repository. Die URL's zu den Repositories sind in einer Excel Tabelle
     * hinterlegt. In diesem Schritt erfolgt der Zugriff auf die Exceltabelle und die Extraktion der Repository-Links.
     */
    List<String> columnExcelRepositoryLinks = ParseExcel.parse(inputSheetNumberValue, inputExcelPath,
        inputColumnNumberRepositoriesValue);

    /*
     * Jedes Repository wird im SVNTemp in einem Ordner hinterlegt. Um den Ordner aussagekräftige Namen zu vergeben,
     * können die Namen der Repositories aus der Exceltabelle extrahiert werden. Die Spalte, in der sich die Namen
     * befinden, muss im Property File festgelegt werden.
     */
    List<String> columnExcelRepositoryNames = null;
    if (inputColumnNumberRepositoryNamesValue >= 0)
      columnExcelRepositoryNames = ParseExcel.parse(inputSheetNumberValue, inputExcelPath,
          inputColumnNumberRepositoryNamesValue);

    LinkedList<Node> nodes = new LinkedList<>();

    /*
     * Dieser Schritt ist nur nötig, falls für ein SVN Checkout eine Authentifizierung benötigt wird. In diesem Falle
     * muss das SVN Zertifikat und das SVN Zertifikatpasswort übergeben werden.
     */
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

    /*
     * 3. Hat man die Repository Links aus der Exceltabelle extrahiert, kann auf diese in einem LinkedList zugegriffen
     * werden. In diesem Schritt iteriert man über alle SVN Repositories und führt ein SVN Checkout durch.
     */
    boolean useRepositoryNames = inputColumnNumberRepositoryNamesValue >= 0;

    for (int i = inputRowNumberValue; i < columnExcelRepositoryLinks.size(); i++) {
      String[] folder = new String[2];

      String url = columnExcelRepositoryLinks.get(i);

      if (url.charAt(url.length() - 1) != '/') {
        url = url + '/';
      }

      /*
       * Prüfe, ob die URL zu einer Datei verweist
       */

      boolean urlContainsSlash = url.contains("/");

      String[] dasds = null;
      String sadsa = null;

      if (urlContainsSlash) {

        dasds = url.split("/");
        sadsa = dasds[dasds.length - 1];
      }

      SVNClientManager ourClientManager = SVNClientManager.newInstance();
      SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
      updateClient.setIgnoreExternals(false);

      File f = null;

      /*
       * Wenn der Nutzer im Property File eine Spalte kleiner als 0 festgelegt hat, werden Ordner Zahlen als Namen in
       * aufsteigender Reihenfolge vergeben.
       */
      if (useRepositoryNames) {
        String repositoryName = columnExcelRepositoryNames.get(i);
        f = new File(svnTempPath + "/" + repositoryName);
      } else {
        f = new File(svnTempPath + "/" + i);
      }

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

      System.out.println("SVN Checkout: " + svnUrl);

      /*
       * Beim Ausschecken beschränkt man sich lediglich auf alle Dateien, Ordner und Unterordner. Insbesondere werden
       * Dateien und Ordner ab einer Rekursionstiefe von 2 nicht berücksichtigt.
       */

      try {
        updateClient.doCheckout(svnUrl, f, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.IMMEDIATES, true);
      } catch (SVNException e) {

        String errorMessage = e.getMessage();
        boolean noFolderRepository = errorMessage.contains("E200007");

        if (noFolderRepository) {
          LOG.error("SVN Checkout error: Repository: " + url
              + " wird ignoriert. Nur Repositories mit einem Ordner werden berücksichtigt.");

        } else {
          e.printStackTrace();
          LOG.error("SVN Checkout error: " + e.getMessage());
        }
        continue;

      }

      String[] subFolders = null;
      subFolders = f.list(new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {

          return new File(current, name).isDirectory();
        }
      });

      LinkedList<String> subFoldersDynamicList = new LinkedList<>();

      for (int j = 0; j < subFolders.length; j++) {
        subFoldersDynamicList.add(subFolders[j]);
      }

      /*
       * Ordner, die mit einem . beginnen, werden ignoriert. Dabei handelt es sich um den .svn Ordner, der sich im
       * Hauptverzeichnis befindet.
       */

      for (Iterator iterator = subFoldersDynamicList.iterator(); iterator.hasNext();) {
        String string = (String) iterator.next();

        boolean isPoint = string.charAt(0) == '.';

        if (isPoint)
          iterator.remove();

      }

      for (Iterator iterator = subFoldersDynamicList.iterator(); iterator.hasNext();) {
        String string = (String) iterator.next();

        try {
          svnUrl = SVNURL.parseURIEncoded(url + string);
        } catch (SVNException e1) {
          LOG.debug(e1.getMessage());
        }
        try {
          repository = SVNRepositoryFactory.create(svnUrl);
        } catch (SVNException e) {
          LOG.debug(e.getMessage());
        }
        try {

          File g = new File(f.getPath() + "/" + string);
          updateClient.doCheckout(svnUrl, g, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.IMMEDIATES, true);
        } catch (SVNException e) {
          e.printStackTrace();
          LOG.error("SVN Checkout error");
        }
      }

    }

    /*
     * 4. Hat man alle SVN Repositories ausgecheckt, beginnt die nächste große for-Schleife, bei der wir über alle
     * SVN-Checkouts iterieren und in jedem Repository das Parent POM-File und das POM File der Hauptanwendung parsen.
     */

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

      /*
       * Nur Repositories, die eine POM.xml File enthalten werden berücksichtigt. Falls POM.xml nicht vorhanden, mache
       * mit dem nächsten Schleifendurchlauf weiter.
       */

      try {
        parsePOMinstance = new ParsePOM(folderName + "/pom.xml", regEx);
      } catch (IOException e1) {
        LOG.debug(e1.getMessage());
      } catch (XmlPullParserException e1) {
        LOG.debug(e1.getMessage());

      } catch (NullPointerException e1) {
        LOG.debug("Repository '" + folderName + "' hat keine POM Struktur und wird nicht berücksichtigt: " + e1);
        continue;
      }

      parsePOMinstance.extractInformationFromPOMModel();
      List<String> modules = parsePOMinstance.getPomModel().getModules();

      Model parentModuleModel = parsePOMinstance.getPomModel();

      DependencyManagement dependencyManagment = parsePOMinstance.getPomModel().getDependencyManagement();
      dsfddsfdsf = dependencyManagment.getDependencies();

      String[] stockArr = new String[modules.size()];
      stockArr = modules.toArray(stockArr);

      /*
       * In einem der Ordner im Hauptverzeichnis befindet sich die Hauptanwendung. Alle Schnittstellen der
       * Hauptanwendung sind durch Namen der Hauptanwenung als Präfix gekennzeichnet. Darüberhinaus kann es Ordner
       * geben, die die Hauptanwendung nicht als Präfix haben, wobei diese selten vorkommen und es sich um Ausnahmefälle
       * handelt. Zur Identifizierung der Hauptanwendung werden unter den Ordnern alle möglichen ungleiche Paare
       * gebildet. Im nächsten Schritt iteriert man über alle Paare. Für jedes Paar wird das größtmögliche gemeinsame
       * Präfix bestimmt. Für alle bestimmten Präfixe wird Buch darüber geführt, wie oft es vorgekommen ist. Das Präfix,
       * welches am häufigsten v vorgekommen ist, entspricht dem Ordernamen der Hauptanwendung.
       */

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

      Node node = new Node();
      node.setName(nameMainApplication);
      node.setUsedInterfaces(usedInterfacesMainApplication);
      node.setOfferedInterfaces(offeredInterfacesMainApplication);
      nodes.add(node);
    }

    /*
     * Der temporäre SVN Ordner ist nach dem Parsevorgang überflüssig und kann optional gelöscht werden.
     */
    if (svnTempPathDeleteValue) {
      try {
        deleteDirectoryRecursion(new File(svnTempPath));
      } catch (IOException e) {
        LOG.debug(e.getMessage());
      }
    }

    File theDir = new File(svnTempPath);

    /*
     * Bevor eine neue NEO4J Datenbank erstellt werden kann, muss die alte gelöscht werden.
     */

    try {
      deleteDirectoryRecursion(new File(neo4J));
    } catch (IOException e) {
      LOG.debug(e.getMessage());
    }

    /*
     * 5. In diesem Schritt wird eine neue NEO4 Datenbank erstellt und die zuvor abgeleiteten Relationen zwischen
     * Anwendungen und Schnittstellen in die Graphendatenbank gespeichert.
     */
    Neo4j db = new Neo4j(neo4J);

    for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
      Node node1 = (Node) iterator.next();

      String name = node1.getName();

      Map<String, String> properties = new HashMap<String, String>();
      // properties.put("pid", "1");
      properties.put("pid", name);
      properties.put("name", name);

      db.addNode(NodeType.APPLICATION, properties, name);

      List<Interface> offered = node1.getOfferedInterfaces();

      for (Iterator iterator2 = offered.iterator(); iterator2.hasNext();) {
        Interface dependency = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        String InterfaceName = dependency.getName();
        String Pid = dependency.getId();
        propertiesInterface.put("pid", Pid);
        propertiesInterface.put("name", InterfaceName);
        propertiesInterface.put("businessVersion", dependency.getVersion());
        String technical = dependency.getTechnicalVersion();
        propertiesInterface.put("technicalVersion", technical);

        db.addNode(NodeType.INTERFACE, propertiesInterface);

        Map<String, String> propertiesRelation = new HashMap<String, String>();
        db.addRelation(name, NodeType.APPLICATION, Pid, NodeType.INTERFACE, propertiesRelation, RelationType.OFFERS);
      }
    }

    for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
      Node node1 = (Node) iterator.next();

      String applicationName = node1.getName();

      List<Interface> used = node1.getUsedInterfaces();
      for (Iterator iterator2 = used.iterator(); iterator2.hasNext();) {
        Interface usedInterface = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        Map<String, String> propertiesRelation = new HashMap<String, String>();

        // Gibt es einen Knoten der das anbietet?
        // Prüfe ob es einen Knoten existiert, der usedInterface anbietet
        String checkNode = ModelUtils.getKey(mapPrefixToOfferedInterfaces, usedInterface);

        if (checkNode == null) {

          String interfaceName = usedInterface.getName();
          String pId = usedInterface.getId();
          propertiesInterface.put("pid", pId);
          propertiesInterface.put("name", interfaceName);
          propertiesInterface.put("businessVersion", usedInterface.getVersion());
          // get technical version
          String technical = usedInterface.getTechnicalVersion();
          propertiesInterface.put("technicalVersion", technical);

          if (!db.findNode(pId)) {
            db.addNode(NodeType.INTERFACE, propertiesInterface);
          }

          db.addRelation(applicationName, NodeType.APPLICATION, usedInterface.getId(), NodeType.INTERFACE,
              propertiesRelation, RelationType.USES);
          // TODO: Relation used but not offered! Solved 04.02.2018

        } else {
          db.addRelation(applicationName, NodeType.APPLICATION, usedInterface.getId(), NodeType.INTERFACE,
              propertiesRelation, RelationType.USES);

        }
      }
    }

    /*
     * Die Verbindung mit der Datenbank wird beendet.
     */
    db.shutdown();

    /*
     * Nachdem die Graphendatenbank erstellt wurde, kann der NEO4 Server gestartet werden.
     */

    if (startNeo4JServerValue) {

      /*
       * Generiere run.bat zur automatisierten Ausführung.
       */
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
   * Erstellt einen neuen Ordner im übergebenen Pfad, falls diseser nicht existiert.
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
        se.printStackTrace();
      }
      if (result) {
        // System.out.println("DIR created");
      }
    }
  }

  /**
   * Löscht rekursiv alle Ordner und Unterordner im übergebenen Pfad
   */

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

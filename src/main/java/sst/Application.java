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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.maven.model.Dependency;
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
import sst.utils.ParsePOM;
import sst.utils.Util;

public class Application {

  private static final Logger LOG = Logger.getLogger(Application.class);

  private String inputSVNUrl;

  private String inputSVNUsername;

  private String inputSVNPassword;

  private String inputSVNCertificate;

  private String inputSVNCertificatePassword;

  private String inputSVNGesamtreleaseletterAblageort;

  private String inputSVNCheckPath;

  private String inputSVNStartScreen;

  private String inputExcelPath;

  private String inputSheetNumber;

  private int inputSheetNumberValue;

  private String inputColumnNumberRepositories;

  private int inputColumnNumberRepositoriesValue;

  private String inputColumnNumberRepositoryNames;

  private int inputColumnNumberRepositoryNamesValue;

  private String inputRowNumber;

  private int inputRowNumberValue;

  private String svnTempPath;

  private String svnTempPathDelete;

  private boolean svnTempPathDeleteValue;

  private String neo4J;

  private String neo4JServer;

  private String startNeo4JServer;

  private boolean startNeo4JServerValue;

  private String regEx;

  private String ignoreFolderRegEx;

  List<String> columnExcelRepositoryLinks;

  List<String> columnExcelRepositoryNames;

  Map<String, LinkedList<Interface>> mapPrefixToOfferedInterfaces;

  Map<String, LinkedList<Interface>> mapPrefixToUsedInterfaces;

  LinkedList<Node> nodes;

  SVNClientManager ourClientManager;

  SVNUpdateClient updateClient;

  List<String> pathToPomFiles;

  String depth;

  String conditions;

  LinkedList<LinkedList<String>> conditionsList;

  public Application() {

    accessApplicationProperties();

    createFolderIfNotExist(this.svnTempPath);

    parseExcel();

    this.ourClientManager = SVNClientManager.newInstance();
    this.updateClient = this.ourClientManager.getUpdateClient();
    this.updateClient.setIgnoreExternals(false);

    executeSVNCheckouts();

    parseApplications();

    createNeo4JDatabase();

    startNeo4JServer();
  }

  /*
   * Nachdem die Graphendatenbank erstellt wurde, kann der NEO4 Server gestartet werden.
   */
  private void startNeo4JServer() {

    if (this.startNeo4JServerValue) {

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
      writer.println("cd " + this.neo4JServer);
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

  /*
   * 5. In diesem Schritt wird eine neue NEO4 Datenbank erstellt und die zuvor abgeleiteten Relationen zwischen
   * Anwendungen und Schnittstellen in die Graphendatenbank gespeichert.
   */
  private void createNeo4JDatabase() {

    Neo4j db = new Neo4j(this.neo4J);

    for (Iterator iterator = this.nodes.iterator(); iterator.hasNext();) {
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

        // db.addNode(NodeType.INTERFACE, propertiesInterface);

        if (!db.findNode(Pid)) {
          db.addNode(NodeType.INTERFACE, propertiesInterface);
        }

        Map<String, String> propertiesRelation = new HashMap<String, String>();
        db.addRelation(name, NodeType.APPLICATION, Pid, NodeType.INTERFACE, propertiesRelation, RelationType.OFFERS);
      }
    }

    for (Iterator iterator = this.nodes.iterator(); iterator.hasNext();) {
      Node node1 = (Node) iterator.next();

      String applicationName = node1.getName();

      List<Interface> used = node1.getUsedInterfaces();
      for (Iterator iterator2 = used.iterator(); iterator2.hasNext();) {
        Interface usedInterface = (Interface) iterator2.next();

        Map<String, String> propertiesInterface = new HashMap<String, String>();
        Map<String, String> propertiesRelation = new HashMap<String, String>();

        // Gibt es einen Knoten der das anbietet?
        // Prüfe ob es einen Knoten existiert, der usedInterface anbietet
        String checkNode = ModelUtils.getKey(this.mapPrefixToOfferedInterfaces, usedInterface);

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
  }

  /*
   * 4. Hat man alle SVN Repositories ausgecheckt, beginnt die nächste große for-Schleife, bei der wir über alle
   * SVN-Checkouts iterieren und in jedem Repository das Parent POM-File und das POM File der Hauptanwendung parsen.
   */

  private void parseApplications() {

    List<Dependency> dependencies = null;

    String[] subFolders = null;
    File file = new File(this.svnTempPath);
    subFolders = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {

        return new File(current, name).isDirectory();
      }
    });

    int sizeFolders = subFolders.length;

    this.mapPrefixToOfferedInterfaces = new HashMap<String, LinkedList<Interface>>();
    this.mapPrefixToUsedInterfaces = new HashMap<String, LinkedList<Interface>>();
    this.nodes = new LinkedList<>();

    for (int i = 0; i < sizeFolders; i++) {
      String folderName = this.svnTempPath + "/" + subFolders[i] + "/";

      // test

      this.pathToPomFiles = new LinkedList<>();
      traverseChcekouts(folderName);
      String parentPomFilePath = getParentPom(this.pathToPomFiles);

      LinkedList<String> pathToPomFilesWithoutParentPom = new LinkedList<>();

      for (Iterator iterator = this.pathToPomFiles.iterator(); iterator.hasNext();) {
        String string = (String) iterator.next();
        if (!string.equals(parentPomFilePath)) {
          pathToPomFilesWithoutParentPom.add(string);
        }
      }

      List<String> pathToPomFiles = null;
      try {
        pathToPomFiles = getApplicationPoms(pathToPomFilesWithoutParentPom);
      } catch (IOException | XmlPullParserException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      ParsePOM parsePOMinstance = null;

      /*
       * Nur Repositories, die eine POM.xml File enthalten werden berücksichtigt. Falls POM.xml nicht vorhanden, mache
       * mit dem nächsten Schleifendurchlauf weiter.
       */

      try {
        parsePOMinstance = new ParsePOM(parentPomFilePath, this.regEx);
      } catch (IOException e1) {
        LOG.debug(e1.getMessage());
      } catch (XmlPullParserException e1) {
        LOG.debug(e1.getMessage());
      }

      parsePOMinstance.extractInformationFromPOMModel();
      List<String> modules = parsePOMinstance.getPomModel().getModules();

      Model parentModuleModel = parsePOMinstance.getPomModel();

      // DependencyManagement dependencyManagment = parsePOMinstance.getPomModel().getDependencyManagement();
      // dependencies = dependencyManagment.getDependencies();

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

      // HashMap<String, Integer> countPrefix = new HashMap<>();
      // for (int j = 0; j < stockArr.length; j++) {
      // String a = stockArr[j];
      // for (int j2 = 0; j2 < stockArr.length; j2++) {
      // if (j != j2) {
      // String b = stockArr[j2];
      //
      // String[] stringArray = new String[2];
      // stringArray[0] = a;
      // stringArray[1] = b;
      //
      // String longestCmmPrefix = CommonUtils.longestCommonPrefix(stringArray);
      //
      // Integer sads = countPrefix.get(longestCmmPrefix);
      //
      // if (sads == null) {
      // countPrefix.put(longestCmmPrefix, 1);
      // } else {
      // countPrefix.put(longestCmmPrefix, sads + 1);
      //
      // }
      // }
      // }
      // }
      //
      // Set<String> countPrefixKeySet = countPrefix.keySet();
      // int countMax = 0;
      // String countMaxKey = "";
      //
      // for (Iterator iterator = countPrefixKeySet.iterator(); iterator.hasNext();) {
      // String string = (String) iterator.next();
      //
      // Integer count = countPrefix.get(string);
      // if (count > countMax) {
      // countMax = count;
      // countMaxKey = string;
      // }
      // }
      //
      // if (countMaxKey.charAt(countMaxKey.length() - 1) == '-') {
      // countMaxKey = (String) countMaxKey.subSequence(0, countMaxKey.length() - 1);
      // }

      // String nameMainApplication = countMaxKey;

      // String pathToMainApplicationPom = folderName + "/" + nameMainApplication + "/pom.xml";

      /*
       * Fallunterscheidung POMs die httpinvoker haben und nicht
       */

      for (Iterator iterator = pathToPomFiles.iterator(); iterator.hasNext();) {
        String string = (String) iterator.next();

        ParsePOM parsePOMMainApplication = null;
        try {
          parsePOMMainApplication = new ParsePOM(string, this.regEx);
        } catch (IOException | XmlPullParserException e) {

          LOG.debug(e.getMessage());

        }

        parsePOMMainApplication.extractInformationFromPOMModel(parentModuleModel);

        String applicationName = parsePOMMainApplication.getPomModel().getArtifactId();
        List<Interface> offeredInterfacesMainApplication = parsePOMMainApplication.getOfferedInterfaces();
        List<Interface> usedInterfacesMainApplication = parsePOMMainApplication.getUsedInterfaces();

        this.mapPrefixToOfferedInterfaces.put(applicationName,
            (LinkedList<Interface>) offeredInterfacesMainApplication);
        this.mapPrefixToUsedInterfaces.put(applicationName, (LinkedList<Interface>) usedInterfacesMainApplication);

        Node node = new Node();
        node.setName(applicationName);
        node.setUsedInterfaces(usedInterfacesMainApplication);
        node.setOfferedInterfaces(offeredInterfacesMainApplication);
        this.nodes.add(node);
      }

    }

    /*
     * Der temporäre SVN Ordner ist nach dem Parsevorgang überflüssig und kann optional gelöscht werden.
     */
    if (this.svnTempPathDeleteValue) {
      try {
        deleteDirectoryRecursion(new File(this.svnTempPath));
      } catch (IOException e) {
        LOG.debug(e.getMessage());
      }
    }

    File theDir = new File(this.svnTempPath);

    /*
     * Bevor eine neue NEO4J Datenbank erstellt werden kann, muss die alte gelöscht werden.
     */

    try {
      deleteDirectoryRecursion(new File(this.neo4J));
    } catch (IOException e) {
      LOG.debug(e.getMessage());
    }
  }

  /*
   * 3. Hat man die Repository Links aus der Exceltabelle extrahiert, kann auf diese in einem LinkedList zugegriffen
   * werden. In diesem Schritt iteriert man über alle SVN Repositories und führt ein SVN Checkout durch.
   */
  private void executeSVNCheckouts() {

    boolean useRepositoryNames = this.inputColumnNumberRepositoryNamesValue >= 0;

    for (int i = this.inputRowNumberValue; i < this.columnExcelRepositoryLinks.size(); i++) {
      String[] folder = new String[2];

      String url = this.columnExcelRepositoryLinks.get(i);

      if (url.charAt(url.length() - 1) != '/') {
        url = url + '/';
      }

      /*
       * Prüfe, ob die URL zu einer Datei verweist
       */

      boolean urlContainsSlash = url.contains("/");

      String[] splittedURL = null;
      String sadsa = null;

      if (urlContainsSlash) {

        splittedURL = url.split("/");
        sadsa = splittedURL[splittedURL.length - 1];
      }

      String f = null;

      /*
       * Wenn der Nutzer im Property File eine Spalte kleiner als 0 festgelegt hat, werden Ordner Zahlen als Namen in
       * aufsteigender Reihenfolge vergeben.
       */
      if (useRepositoryNames) {
        String repositoryName = this.columnExcelRepositoryNames.get(i);
        f = this.svnTempPath + "/" + repositoryName;
      } else {
        f = this.svnTempPath + "/" + i;
      }

      checkoutRecursive(url, f, 0);
      System.out.println("SVN Checkout: " + url);

      /*
       * Beim Ausschecken beschränkt man sich lediglich auf alle Dateien, Ordner und Unterordner. Insbesondere werden
       * Dateien und Ordner ab einer Rekursionstiefe von 2 nicht berücksichtigt.
       */

      /*
       * Ordner, die mit einem . beginnen, werden ignoriert. Dabei handelt es sich um den .svn Ordner, der sich im
       * Hauptverzeichnis befindet.
       */

    }
  }

  /*
   * 2. Für jede Anwendung existiert ein SVN Repository. Die URL's zu den Repositories sind in einer Excel Tabelle
   * hinterlegt. In diesem Schritt erfolgt der Zugriff auf die Exceltabelle und die Extraktion der Repository-Links.
   */
  private void parseExcel() {

    this.columnExcelRepositoryLinks = ParseExcel.parse(this.inputSheetNumberValue, this.inputExcelPath,
        this.inputColumnNumberRepositoriesValue);

    /*
     * Jedes Repository wird im SVNTemp in einem Ordner hinterlegt. Um den Ordner aussagekräftige Namen zu vergeben,
     * können die Namen der Repositories aus der Exceltabelle extrahiert werden. Die Spalte, in der sich die Namen
     * befinden, muss im Property File festgelegt werden.
     */
    if (this.inputColumnNumberRepositoryNamesValue >= 0)
      this.columnExcelRepositoryNames = ParseExcel.parse(this.inputSheetNumberValue, this.inputExcelPath,
          this.inputColumnNumberRepositoryNamesValue);

    LinkedList<Node> nodes = new LinkedList<>();

    /*
     * Dieser Schritt ist nur nötig, falls für ein SVN Checkout eine Authentifizierung benötigt wird. In diesem Falle
     * muss das SVN Zertifikat und das SVN Zertifikatpasswort übergeben werden.
     */
    Konfiguration svnConfig = new Konfiguration();
    svnConfig.setSvnUsername(this.inputSVNUsername);
    svnConfig.setSvnPassword(this.inputSVNPassword);

    svnConfig.setSvnCertificate(this.inputSVNCertificate);
    svnConfig.setSvnCertificatePasswort(this.inputSVNCertificatePassword);

    svnConfig.setGesamtreleaseletterAblageort(this.inputSVNGesamtreleaseletterAblageort);
    svnConfig.setSvnCheckPath(this.inputSVNCheckPath);
    svnConfig.setStartScreen(this.inputSVNStartScreen);

    SvnHelperNeu svnHelper = new SvnHelperNeu();
    svnHelper.init(svnConfig);

    Map<String, LinkedList<Interface>> mapPrefixToOfferedInterfaces = new HashMap<String, LinkedList<Interface>>();
    Map<String, LinkedList<Interface>> mapPrefixToUsedInterfaces = new HashMap<String, LinkedList<Interface>>();
    List<Dependency> dsfddsfdsf = null;
  }

  /**
   * @param LOG
   */
  private void accessApplicationProperties() {

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
    this.inputSVNUrl = applicationProperties.getProperty("svnUrl");
    this.inputSVNUsername = applicationProperties.getProperty("svnUsername");
    this.inputSVNPassword = applicationProperties.getProperty("svnPassword");
    this.inputSVNCertificate = applicationProperties.getProperty("svnCertificate");
    this.inputSVNCertificatePassword = applicationProperties.getProperty("svnCertificatePassword");
    this.inputSVNGesamtreleaseletterAblageort = applicationProperties.getProperty("svnGesamtreleaseletterAblageort");
    this.inputSVNCheckPath = applicationProperties.getProperty("svnCheckPath");
    this.inputSVNStartScreen = applicationProperties.getProperty("svnStartScreen");
    this.inputExcelPath = applicationProperties.getProperty("excel");
    this.inputSheetNumber = applicationProperties.getProperty("sheet");
    this.inputSheetNumberValue = Integer.parseInt(this.inputSheetNumber);
    this.inputColumnNumberRepositories = applicationProperties.getProperty("columnRepositoryLinks");
    this.inputColumnNumberRepositoriesValue = Integer.parseInt(this.inputColumnNumberRepositories);
    this.inputColumnNumberRepositoryNames = applicationProperties.getProperty("columnRepositoryNames");
    this.inputColumnNumberRepositoryNamesValue = Integer.parseInt(this.inputColumnNumberRepositoryNames);
    this.inputRowNumber = applicationProperties.getProperty("row");
    this.inputRowNumberValue = Integer.parseInt(this.inputRowNumber);
    this.svnTempPath = applicationProperties.getProperty("svnTemp");
    this.svnTempPathDelete = applicationProperties.getProperty("svnTempDelete");
    this.svnTempPathDeleteValue = Boolean.parseBoolean(this.svnTempPathDelete);
    this.neo4J = applicationProperties.getProperty("neo4J");
    this.neo4JServer = applicationProperties.getProperty("neo4JServer");
    this.startNeo4JServer = applicationProperties.getProperty("startNeo4JServer");
    this.startNeo4JServerValue = Boolean.parseBoolean(this.startNeo4JServer);
    this.regEx = applicationProperties.getProperty("regEx");
    this.ignoreFolderRegEx = applicationProperties.getProperty("ignoreFolderRegEx");
    this.depth = applicationProperties.getProperty("depth");
    this.conditions = applicationProperties.getProperty("conditions");

    String[] asdsa = this.conditions.split("\\|");

    LinkedList<LinkedList<String>> conditionsDynamicList = new LinkedList<LinkedList<String>>();

    for (int i = 0; i < asdsa.length; i++) {
      String sdf = asdsa[i];
      String[] asdsad = sdf.split("&");

      LinkedList<String> andConditions = new LinkedList<>();
      for (int j = 0; j < asdsad.length; j++) {
        andConditions.add(asdsad[j].replace("(", "").replace(")", ""));
      }

      conditionsDynamicList.add(andConditions);

    }

    this.conditionsList = conditionsDynamicList;

  }

  public List<String> getApplicationPoms(List<String> pathToPomFiles) throws IOException, XmlPullParserException {

    LinkedList<String> output = new LinkedList<>();

    for (Iterator iterator = pathToPomFiles.iterator(); iterator.hasNext();) {
      String string = (String) iterator.next();

      File f = new File(string);
      Model model = Util.getModelFromPOM(f);
      String artifactId = model.getArtifactId();

      Pattern p = Pattern.compile(this.regEx.toLowerCase());
      Matcher m = p.matcher(artifactId);
      boolean foundString = m.find();

      Pattern p2 = Pattern.compile(this.ignoreFolderRegEx);
      Matcher m2 = p2.matcher(artifactId);
      boolean ignoreApplication = m2.find();

      if (!foundString && !ignoreApplication) {
        output.add(string);
      }

    }
    return output;
  }

  public String getParentPom(List<String> pathToPomFiles) {

    for (Iterator iterator = pathToPomFiles.iterator(); iterator.hasNext();) {
      String string = (String) iterator.next();
      ParsePOM parsePOMinstance = null;
      try {
        parsePOMinstance = new ParsePOM(string, this.regEx);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (XmlPullParserException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      parsePOMinstance.extractInformationFromPOMModel();
      List<String> modules = parsePOMinstance.getPomModel().getModules();
      List<String> modules2 = parsePOMinstance.getPomModel().getModules();

      if (!modules.isEmpty())
        return string;
    }
    return null;
  }

  private void traverseChcekouts(String pathToProject) {

    File f = new File(pathToProject);

    String[] subFolders = null;
    File file = new File(pathToProject);
    subFolders = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {

        return new File(current, name).isDirectory();
      }
    });
    int sizeFolders = subFolders.length;

    String[] subFiles = null;
    subFiles = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {

        return new File(current, name).exists();
      }
    });
    int sizeFiles = subFiles.length;

    LinkedList<String> subFoldersDynamicList = new LinkedList<>();
    LinkedList<String> subFilesDynamicList = new LinkedList<>();

    for (int j = 0; j < subFolders.length; j++) {
      subFoldersDynamicList.add(subFolders[j]);
    }

    for (int j = 0; j < subFiles.length; j++) {
      subFilesDynamicList.add(subFiles[j]);
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

    for (Iterator iterator2 = subFilesDynamicList.iterator(); iterator2.hasNext();) {
      String string = (String) iterator2.next();

      boolean isPoint = string.charAt(0) == '.';

      if (isPoint)
        iterator2.remove();
    }

    for (Iterator iterator = subFilesDynamicList.iterator(); iterator.hasNext();) {
      String string = (String) iterator.next();

      boolean equalsPOMFile = string.equals("pom.xml");

      if (equalsPOMFile) {
        this.pathToPomFiles.add(pathToProject + "pom.xml");
      }
    }

    boolean empty = subFoldersDynamicList.isEmpty();
    // gehe rekursiv die Ordner nicht durch falls die Bedinungen im ordner nicht erfüllt

    if (empty) {
      return;
    }

    else

    {
      for (Iterator iterator = subFoldersDynamicList.iterator(); iterator.hasNext();) {
        String string = (String) iterator.next();

        String newPath = pathToProject + string + "/";
        traverseChcekouts(newPath);
      }
    }

  }

  private void checkoutRecursive(String svnPath, String path, int accumulator) {

    File f = new File(path);
    SVNRepository repository = null;
    SVNURL svnUrl = null;

    try {
      svnUrl = SVNURL.parseURIEncoded(svnPath);
    } catch (SVNException e1) {
      LOG.debug(e1.getMessage());
    }

    try {
      repository = SVNRepositoryFactory.create(svnUrl);
    } catch (SVNException e) {
      LOG.debug(e.getMessage());

    }
    try {
      this.updateClient.doCheckout(svnUrl, f, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.IMMEDIATES, true);
    } catch (SVNException e) {

      String errorMessage = e.getMessage();
      boolean noFolderRepository = errorMessage.contains("E200007");

      if (noFolderRepository) {
        LOG.error("SVN Checkout error: Repository: " + "url"
            + " wird ignoriert. Nur Repositories mit einem Ordner werden berücksichtigt.");

      } else {
        e.printStackTrace();
        LOG.error("SVN Checkout error: " + e.getMessage());
      }
    }

    String[] subFiles = null;
    File file = new File(path);
    subFiles = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {

        return new File(current, name).exists();
      }
    });

    LinkedList<String> subFilesDynamicList = new LinkedList<>();

    for (int j = 0; j < subFiles.length; j++) {
      subFilesDynamicList.add(subFiles[j]);
    }

    String[] subFolders = null;
    subFolders = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {

        return new File(current, name).isDirectory();
      }
    });
    int sizeFolders = subFolders.length;

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

    boolean continueRecursion = false;

    for (Iterator iterator = this.conditionsList.iterator(); iterator.hasNext();) {
      LinkedList<String> linkedList = (LinkedList<String>) iterator.next();

      boolean resultAndEvaluation = true;
      for (Iterator iterator2 = linkedList.iterator(); iterator2.hasNext();) {
        String string = (String) iterator2.next();
        String asds = createRegexFromGlob(string);

        boolean found = containRegEx(subFilesDynamicList, asds);
        resultAndEvaluation = resultAndEvaluation && found;
      }

      continueRecursion = continueRecursion || resultAndEvaluation;

    }

    // gehe rekursiv die Ordner nicht durch falls die Bedinungen im ordner nicht erfüllt

    boolean givenDepthExceeded = false;

    if (!this.depth.equals("*")) {
      int intDepth = Integer.parseInt(this.depth);
      givenDepthExceeded = accumulator > intDepth;
    }

    if (continueRecursion || givenDepthExceeded) {

      return;
    }

    else

    {
      for (Iterator iterator = subFoldersDynamicList.iterator(); iterator.hasNext();) {
        String string = (String) iterator.next();

        String newPath = svnPath + string + "/";
        checkoutRecursive(newPath, path + "/" + string, accumulator + 1);
      }
    }

  }

  /*
   * Prüft, ob sich ein übergebener regulärer Ausdruck sich im übergebenen Array befindet.
   *
   */

  private boolean containRegEx(LinkedList<String> subFoldersDynamicList, String regEx) {

    boolean result = false;
    for (Iterator iterator = subFoldersDynamicList.iterator(); iterator.hasNext();) {
      String string = (String) iterator.next();

      Pattern p2 = Pattern.compile(regEx);
      Matcher m2 = p2.matcher(string);
      boolean ignoreApplication = m2.find();

      if (ignoreApplication) {
        result = true;
      }

    }
    return result;

  }

  /**
   * Erstellt einen neuen Ordner im übergebenen Pfad, falls diseser nicht existiert.
   */

  private void createFolderIfNotExist(String path) {

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

  private void deleteDirectoryRecursion(File file) throws IOException {

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

  private String createRegexFromGlob(String glob) {

    StringBuilder out = new StringBuilder("^");
    for (int i = 0; i < glob.length(); ++i) {
      final char c = glob.charAt(i);
      switch (c) {
        case '*':
          out.append(".*");
          break;
        case '?':
          out.append('.');
          break;
        case '.':
          out.append("\\.");
          break;
        case '\\':
          out.append("\\\\");
          break;
        default:
          out.append(c);
      }
    }
    out.append('$');
    return out.toString();
  }

  public static void main(String[] args) {

    new Application();
  }

}

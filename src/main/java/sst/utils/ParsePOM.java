package sst.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import sst.model.Interface;

/**
 * Eine Klasse mit Funktionalitäten für das Parsen von pom.xml Dateien aus einem Maven Projekt. Für eine Anwendung
 * werden alle angebotenen und verwendeten Schnittstellen extrahiert.
 *
 * @author CapGemini, Volkan Hacimüftüoglu
 * @version 05.02.2018
 */
public class ParsePOM {

  private Model pomModel = null;

  private List<Interface> offeredInterfaces = new LinkedList<Interface>();

  private List<Interface> usedInterfaces = new LinkedList<Interface>();

  private List<Dependency> dependencies;

  private List<String> modules;

  private List<String> filteredModules;

  private List<Dependency> filteredDependencies;

  private String regEx;

  public ParsePOM(String pathToPOMFile, String regEx) throws IOException, XmlPullParserException {

    this.regEx = regEx;
    File f = new File(pathToPOMFile);
    this.pomModel = Util.getModelFromPOM(f);
  }

  /*
   * Bekommt eine Liste von Modulen entgegen und filtert diejenigen Module aus, die den regulären String regEx nicht
   * enthalten.
   */
  private List<String> filterStrings(List<String> modules) {

    List<String> result = new ArrayList<String>();

    for (Iterator iter = modules.iterator(); iter.hasNext();) {
      String string = (String) iter.next();

      boolean containsString = string.toLowerCase().matches(this.regEx);

      Pattern p = Pattern.compile(this.regEx.toLowerCase());
      Matcher m = p.matcher(string);

      if (m.find()) {
        result.add(string);
      }
    }
    return result;
  }

  /*
   * Analog zur vorigen Methode. Alle Dependency werden ausgefiltert, die den regulären String regEx nicht enthalten.
   */
  private List<Dependency> filterDependencies(List<Dependency> modules) {

    List<Dependency> result = new ArrayList<Dependency>();

    for (Iterator iter = modules.iterator(); iter.hasNext();) {
      Dependency dependency = (Dependency) iter.next();

      String artifactId = dependency.getArtifactId();

      Pattern p = Pattern.compile(this.regEx);
      Matcher m = p.matcher(artifactId.toLowerCase());

      boolean containsString = artifactId.toLowerCase().matches(this.regEx);
      if (m.find()) {
        result.add(dependency);
      }
    }
    return result;
  }

  /*
   * Bekommt als Paramenter das parent pom.xml übergeben. Aus dem modules Bereich des parent pom kann geschlussfolgert
   * werden, welche Interfaces die Anwendung nach außen bereitstellt, sowie Informationen extrahiert, welche
   * Business-Version und Technical-Version die Schnittstellen haben.
   */
  public void extractInformationFromPOMModel(Model parentModuleModel) {

    List<Dependency> offeredInterfaces = new LinkedList<Dependency>();
    List<Dependency> usedInterfaces = new LinkedList<Dependency>();

    this.dependencies = this.pomModel.getDependencies();

    // process Modules

    DependencyManagement dependencyManagement = parentModuleModel.getDependencyManagement();
    List<Dependency> dependencyManagementDependencies = dependencyManagement.getDependencies();

    this.modules = parentModuleModel.getModules();
    this.filteredModules = filterStrings(parentModuleModel.getModules());
    this.filteredDependencies = filterDependencies(this.dependencies);

    for (int i = 0; i < this.filteredDependencies.size(); i++) {
      Dependency dependency = this.filteredDependencies.get(i);
      String dependencyArtifactIdString = dependency.getArtifactId();

      if (this.filteredModules.contains(dependencyArtifactIdString)) {
        offeredInterfaces.add(dependency);
      } else
        usedInterfaces.add(dependency);
    }

    for (Iterator iterator = offeredInterfaces.iterator(); iterator.hasNext();) {
      Dependency dependency = (Dependency) iterator.next();

      String Id = dependency.getArtifactId();
      int index = Id.lastIndexOf('-');

      char checkChar = Id.charAt(index + 1);

      String technicalVersion = null;

      String resultTechnicalVersionFromPom = getInterfaceTechnicalVersion(this.dependencies, Id);
      String resultTechnicalVersionFromParentPom = getInterfaceTechnicalVersion(dependencyManagementDependencies, Id);

      if (resultTechnicalVersionFromPom != null) {
        technicalVersion = resultTechnicalVersionFromPom;
      } else if (resultTechnicalVersionFromParentPom != null) {
        technicalVersion = resultTechnicalVersionFromParentPom;
      }

      double versionDobule = 0;
      String version = "0";
      String name = Id;

      if (checkChar == 'v') {
        name = Id.substring(0, index);
        version = Id.substring(index + 2, Id.length());
        versionDobule = Double.parseDouble(version);
      }

      Interface interfaceInstance = new Interface(Id, name, version, versionDobule, technicalVersion);
      this.offeredInterfaces.add(interfaceInstance);
    }

    for (Iterator iterator = usedInterfaces.iterator(); iterator.hasNext();) {
      Dependency dependency = (Dependency) iterator.next();

      String Id = dependency.getArtifactId();
      int index = Id.lastIndexOf('-');

      char sadasd = Id.charAt(index + 1);

      String technicalVersion = null;

      String resultTechnicalVersionFromPom = getInterfaceTechnicalVersion(this.dependencies, Id);
      String resultTechnicalVersionFromParentPom = getInterfaceTechnicalVersion(dependencyManagementDependencies, Id);

      if (resultTechnicalVersionFromPom != null) {
        technicalVersion = resultTechnicalVersionFromPom;
      } else if (resultTechnicalVersionFromParentPom != null) {
        technicalVersion = resultTechnicalVersionFromParentPom;
      }
      double versionDobule = 0;
      String version = "0";
      String name = Id;

      if (sadasd == 'v') {
        name = Id.substring(0, index);
        version = Id.substring(index + 2, Id.length());
        versionDobule = Double.parseDouble(version);
      }

      Interface interfaceInstance = new Interface(Id, name, version, versionDobule, technicalVersion);
      this.usedInterfaces.add(interfaceInstance);
    }
  }

  public void extractInformationFromPOMModel() {

    List<Dependency> offeredInterfaces = new LinkedList<Dependency>();
    List<Dependency> usedInterfaces = new LinkedList<Dependency>();

    this.dependencies = this.pomModel.getDependencies();

    this.modules = this.pomModel.getModules();
    this.filteredModules = filterStrings(this.modules);
    this.filteredDependencies = filterDependencies(this.dependencies);

    for (int i = 0; i < this.filteredDependencies.size(); i++) {
      Dependency dependency = this.filteredDependencies.get(i);
      String dependencyArtifactIdString = dependency.getArtifactId();

      if (this.filteredModules.contains(dependencyArtifactIdString)) {
        offeredInterfaces.add(dependency);
      } else
        usedInterfaces.add(dependency);
    }
  }

  /*
   * Bekommt eine List von Dependencies übergeben und gibt die technische Version der Dependency aus, die als Artifact
   * Id dem übergebenen Artifact Id entspricht.
   */
  public String getInterfaceTechnicalVersion(List<Dependency> listOfDependencies, String artifactId) {

    Dependency dep = null;
    String technicalVersion = null;

    for (Iterator iterator3 = listOfDependencies.iterator(); iterator3.hasNext();) {
      Dependency dependency2 = (Dependency) iterator3.next();
      if (dependency2.getArtifactId().equals(artifactId)) {
        dep = dependency2;
        break;
      }
    }
    if (dep != null) {
      technicalVersion = dep.getVersion();
    }

    return technicalVersion;
  }

  /**
   * @return modules
   */
  public List<String> getModules() {

    return this.modules;
  }

  /**
   * @param modules new value of {@link #getmodules}.
   */
  public void setModules(List<String> modules) {

    this.modules = modules;
  }

  /**
   * @return pomModel
   */
  public Model getPomModel() {

    return this.pomModel;
  }

  /**
   * @param pomModel new value of {@link #getpomModel}.
   */
  public void setPomModel(Model pomModel) {

    this.pomModel = pomModel;
  }

  /**
   * @return offeredInterfaces
   */
  public List<Interface> getOfferedInterfaces() {

    return this.offeredInterfaces;
  }

  /**
   * @param offeredInterfaces new value of {@link #getofferedInterfaces}.
   */
  public void setOfferedInterfaces(List<Interface> offeredInterfaces) {

    this.offeredInterfaces = offeredInterfaces;
  }

  /**
   * @return usedInterfaces
   */
  public List<Interface> getUsedInterfaces() {

    return this.usedInterfaces;
  }

  /**
   * @param usedInterfaces new value of {@link #getusedInterfaces}.
   */
  public void setUsedInterfaces(List<Interface> usedInterfaces) {

    this.usedInterfaces = usedInterfaces;
  }

}

package com.programmer.gate2.readData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Hello world!
 *
 */
public class ParsePOM {
  /**
   * @param args
   */

  private Model pomModel = null;

  private List<Dependency> offeredInterfaces = new LinkedList<Dependency>();

  private List<Dependency> usedInterfaces = new LinkedList<Dependency>();

  private List<Dependency> dependencies;

  private List<String> modules;

  private List<String> filteredModules;

  private List<Dependency> filteredDependencies;

  /**
   * The constructor.
   */
  public ParsePOM(String pathToPOMFile) {

    try {
      File f = new File(pathToPOMFile);
      this.pomModel = Util.getModelFromPOM(f);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (XmlPullParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // extractInformationFromPOMModel();
  }

  private static List<String> filterStrings(List<String> modules, String str) {

    List<String> result = new ArrayList<String>();

    for (Iterator iter = modules.iterator(); iter.hasNext();) {
      String string = (String) iter.next();

      boolean containsString = string.toLowerCase().contains(str.toLowerCase());
      if (containsString) {
        result.add(string);
      }
    }
    return result;
  }

  private static List<Dependency> filterDependencies(List<Dependency> modules, String str) {

    List<Dependency> result = new ArrayList<Dependency>();

    for (Iterator iter = modules.iterator(); iter.hasNext();) {
      Dependency dependency = (Dependency) iter.next();

      String artifactId = dependency.getArtifactId();

      boolean containsString = artifactId.toLowerCase().contains(str.toLowerCase());
      if (containsString) {
        result.add(dependency);
      }
    }
    return result;
  }

  public List<Dependency> getUsedInterfaces() {

    return this.usedInterfaces;
  }

  public List<Dependency> getOfferedInterfaces() {

    return this.offeredInterfaces;
  }

  public void extractInformationFromPOMModel(List<String> modules) {

    this.offeredInterfaces = new LinkedList<Dependency>();
    this.usedInterfaces = new LinkedList<Dependency>();

    // Lese pom.xml ein und speichere POM-Model ab
    // DependencyManagement dependencyManagement = this.pomModel.getDependencies();
    this.dependencies = this.pomModel.getDependencies();

    this.modules = modules;
    this.filteredModules = filterStrings(modules, "httpinvoker");
    this.filteredDependencies = filterDependencies(this.dependencies, "httpinvoker");

    for (int i = 0; i < this.filteredDependencies.size(); i++) {
      Dependency dependency = this.filteredDependencies.get(i);
      String dependencyArtifactIdString = dependency.getArtifactId();

      if (this.filteredModules.contains(dependencyArtifactIdString)) {
        this.offeredInterfaces.add(dependency);
      } else
        this.usedInterfaces.add(dependency);
    }
  }

  public void extractInformationFromPOMModel() {

    this.offeredInterfaces = new LinkedList<Dependency>();
    this.usedInterfaces = new LinkedList<Dependency>();

    // Lese pom.xml ein und speichere POM-Model ab
    // DependencyManagement dependencyManagement = this.pomModel.getDependencies();
    this.dependencies = this.pomModel.getDependencies();

    this.modules = this.pomModel.getModules();
    this.filteredModules = filterStrings(this.modules, "httpinvoker");
    this.filteredDependencies = filterDependencies(this.dependencies, "httpinvoker");

    for (int i = 0; i < this.filteredDependencies.size(); i++) {
      Dependency dependency = this.filteredDependencies.get(i);
      String dependencyArtifactIdString = dependency.getArtifactId();

      if (this.filteredModules.contains(dependencyArtifactIdString)) {
        this.offeredInterfaces.add(dependency);
      } else
        this.usedInterfaces.add(dependency);
    }
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

}

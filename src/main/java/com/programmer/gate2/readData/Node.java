package com.programmer.gate2.readData;

import java.util.List;

import org.apache.maven.model.Dependency;

/**
 * @author vhacimuf
 *
 */
public class Node {

  private String name = "";

  private List<Dependency> offeredInterfaces;

  private List<Dependency> usedInterfaces;

  /**
   * @return name
   */
  public String getName() {

    return this.name;
  }

  /**
   * @param name new value of {@link #getname}.
   */
  public void setName(String name) {

    this.name = name;
  }

  /**
   * @return offeredInterfaces
   */
  public List<Dependency> getOfferedInterfaces() {

    return this.offeredInterfaces;
  }

  /**
   * @param offeredInterfaces new value of {@link #getofferedInterfaces}.
   */
  public void setOfferedInterfaces(List<Dependency> offeredInterfaces) {

    this.offeredInterfaces = offeredInterfaces;
  }

  /**
   * @return usedInterfaces
   */
  public List<Dependency> getUsedInterfaces() {

    return this.usedInterfaces;
  }

  /**
   * @param usedInterfaces new value of {@link #getusedInterfaces}.
   */
  public void setUsedInterfaces(List<Dependency> usedInterfaces) {

    this.usedInterfaces = usedInterfaces;
  }

}

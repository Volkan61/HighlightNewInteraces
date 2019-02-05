package sst.model;

import java.util.List;

/**
 * @author CapGemini, Volkan Hacimüftüoglu
 * @version 05.02.2018
 */
public class Node {

  private String name = "";

  // private List<Dependency> offeredInterfaces;
  private List<Interface> offeredInterfaces;

  // private List<Dependency> usedInterfaces;
  private List<Interface> usedInterfaces;

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

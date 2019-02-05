package sst.model;

/**
 * @author CapGemini, Volkan Hacimüftüoglu
 * @version 05.02.2018
 */
public class Interface {

  private String id;

  private String name;

  private String version;

  private String technicalVersion;

  private double versionDouble;

  /**
   * The constructor.
   *
   * @param name
   * @param version
   */
  public Interface(String id, String name, String version, double versionDouble, String technicalVersion) {

    this.id = id;
    this.name = name;
    this.version = version;
    this.versionDouble = versionDouble;
    this.technicalVersion = technicalVersion;

  }

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
   * @return version
   */
  public String getVersion() {

    return this.version;
  }

  /**
   * @param version new value of {@link #getversion}.
   */
  public void setVersion(String version) {

    this.version = version;
  }

  /**
   * @return id
   */
  public String getId() {

    return this.id;
  }

  /**
   * @param id new value of {@link #getid}.
   */
  public void setId(String id) {

    this.id = id;
  }

  /**
   * @return versionDouble
   */
  public double getVersionDouble() {

    return this.versionDouble;
  }

  /**
   * @param versionDouble new value of {@link #getversionDouble}.
   */
  public void setVersionDouble(double versionDouble) {

    this.versionDouble = versionDouble;
  }

  /**
   * @return technicalVersion
   */
  public String getTechnicalVersion() {

    return this.technicalVersion;
  }

  /**
   * @param technicalVersion new value of {@link #gettechnicalVersion}.
   */
  public void setTechnicalVersion(String technicalVersion) {

    this.technicalVersion = technicalVersion;
  }

  @Override
  public String toString() {

    return "Interface [name=" + this.name + ", version=" + this.version + ", versionDouble=" + this.versionDouble + "]";
  }

}

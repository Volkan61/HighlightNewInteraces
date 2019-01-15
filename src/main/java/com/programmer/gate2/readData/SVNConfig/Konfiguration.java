package com.programmer.gate2.readData.SVNConfig;

/**
 * Konfigurations.
 */
public class Konfiguration {

  // SVN Attribute
  private String svnUsername;

  private String svnPassword;

  private String svnCertificate;

  private String svnCertificatePasswort;

  // Gesamtreleaseletter Attribute
  private String gesamtreleaseletterAblageort;

  private String svnCheckPath;

  private String startScreen;

  // Excel/ Gesamtreleaseletter Konfiguration
  public String getGesamtreleaseletterAblageort() {

    return this.gesamtreleaseletterAblageort;
  }

  public String getStartScreen() {

    return this.startScreen;
  }

  public String getSvnCertificate() {

    return this.svnCertificate;
  }

  public String getSvnCertificatePasswort() {

    return this.svnCertificatePasswort;
  }

  public String getSvnCheckPath() {

    return this.svnCheckPath;
  }

  public String getSvnPassword() {

    return this.svnPassword;
  }

  public String getSvnUsername() {

    return this.svnUsername;
  }

  public boolean isComplete() {

    if (this.svnCertificate.trim().isEmpty()) {
      return false;
    }
    if (this.svnCertificatePasswort.trim().isEmpty()) {
      return false;
    }
    if (this.svnCheckPath.trim().isEmpty()) {
      return false;
    }
    if (this.svnPassword.trim().isEmpty()) {
      return false;
    }
    if (this.svnUsername.trim().isEmpty()) {
      return false;
    }
    return true;
  }

  public void setGesamtreleaseletterAblageort(String gesamtreleaseletterAblageort) {

    this.gesamtreleaseletterAblageort = gesamtreleaseletterAblageort;
  }

  public final void setStartScreen(String startScreen) {

    this.startScreen = startScreen;
  }

  public void setSvnCertificate(String svnCertificate) {

    this.svnCertificate = svnCertificate;
  }

  public void setSvnCertificatePasswort(String svnCertificatePasswort) {

    this.svnCertificatePasswort = svnCertificatePasswort;
  }

  public void setSvnCheckPath(String svnCheckPath) {

    this.svnCheckPath = svnCheckPath;
  }

  public void setSvnPassword(String svnPassword) {

    this.svnPassword = svnPassword;
  }

  public void setSvnUsername(String svnUsername) {

    this.svnUsername = svnUsername;
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();
    builder.append("Konfiguration [svnUsername=");
    builder.append(this.svnUsername);
    builder.append(", svnPassword=");
    builder.append(this.svnPassword);
    builder.append(", svnCertificate=");
    builder.append(this.svnCertificate);
    builder.append(", svnCertificatePasswort=");
    builder.append(this.svnCertificatePasswort);
    builder.append(", gesamtreleaseletterAblageort=");
    builder.append(this.gesamtreleaseletterAblageort);
    builder.append(", svnCheckPath=");
    builder.append(this.svnCheckPath);
    builder.append(", startScreen=");
    builder.append(this.startScreen);
    builder.append("]");
    return builder.toString();
  }

}

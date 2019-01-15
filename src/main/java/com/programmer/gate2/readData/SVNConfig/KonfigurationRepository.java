package com.programmer.gate2.readData.SVNConfig;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.ejb.Singleton;

import org.apache.log4j.Logger;

/**
 *
 * @author Capgemini, Thomas Timu
 * @version $Id: KonfigurationRepository.java 62830 2017-08-18 12:09:44Z thtimu $
 */

@Singleton
public class KonfigurationRepository {
  private static final Logger LOG = Logger.getLogger(KonfigurationRepository.class);

  public Konfiguration load() {

    Preferences prefs = getPreferences();
    String svnUsername = prefs.get("svnUsername", "");
    byte[] svnPassword = prefs.getByteArray("svnPassword", new byte[] {});
    String svnCertificate = prefs.get("svnCertificate", "");
    byte[] svnCertificatePasswort = prefs.getByteArray("svnCertificatePasswort", new byte[] {});
    String gesamtreleaseletterAblageort = prefs.get("gesamtreleaseletterAblageort", "");
    String svnCheckPath = prefs.get("svnCheckPath",
        "https://risprepository:8800/svns/doku/trunk/12_Releasemanagement/03_Gesamtreleaseletter");
    String startScreen = prefs.get("startScreen", "Settings");

    Konfiguration konfiguration = new Konfiguration();
    konfiguration.setSvnUsername(svnUsername);
    konfiguration.setSvnPassword(new String(svnPassword));
    konfiguration.setSvnCertificate(svnCertificate);
    konfiguration.setSvnCertificatePasswort(new String(svnCertificatePasswort));
    konfiguration.setGesamtreleaseletterAblageort(gesamtreleaseletterAblageort);
    konfiguration.setSvnCheckPath(svnCheckPath);
    konfiguration.setStartScreen(startScreen);

    return konfiguration;
  }

  public void save(Konfiguration konfiguration) throws KonfigurationPersistenceException {

    Preferences prefs = getPreferences();
    prefs.put("svnUsername", konfiguration.getSvnUsername());
    prefs.putByteArray("svnPassword", konfiguration.getSvnPassword().getBytes());
    prefs.put("svnCertificate", konfiguration.getSvnCertificate());
    prefs.putByteArray("svnCertificatePasswort", konfiguration.getSvnCertificatePasswort().getBytes());
    prefs.put("gesamtreleaseletterAblageort", konfiguration.getGesamtreleaseletterAblageort());
    prefs.put("startScreen", konfiguration.getStartScreen());
    try {
      prefs.flush();
    } catch (BackingStoreException e) {
      throw new KonfigurationPersistenceException(e);
    }
  }

  private Preferences getPreferences() {

    return Preferences.userNodeForPackage(getClass());
  }
}

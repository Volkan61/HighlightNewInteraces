package updatedinterfacesvis.svn.config;

import java.util.prefs.BackingStoreException;

/**
 *
 * @author Capgemini, Thomas Timu
 * @version $Id: KonfigurationPersistenceException.java 62830 2017-08-18 12:09:44Z thtimu $
 */
public class KonfigurationPersistenceException extends Exception {

  /**
   * @param e
   */
  public KonfigurationPersistenceException(BackingStoreException e) {

    super(e);
  }

}

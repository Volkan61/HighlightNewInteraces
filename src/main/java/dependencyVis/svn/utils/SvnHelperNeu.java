package dependencyVis.svn.utils;

/*
 * ====================================================================
 * Copyright (c) 2004-2011 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */

import java.io.File;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSLAuthentication;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import dependencyVis.svn.config.Konfiguration;

/**
 *
 * SVN Helper stellt eine Verbindung zu SVN Repository und
 *
 * @author Capgemini, Rickey Gladstone
 * @version $Id: SvnHelperNeu.java 62830 2017-08-18 12:09:44Z thtimu $
 */
public class SvnHelperNeu {
  private static final Logger LOG = Logger.getLogger(SvnHelperNeu.class);

  static {
    setupLibrary();
  }

  /*
   * Initializes the library to work with a repository via different protocols.
   */
  public static void setupLibrary() {

    /*
     * For using over http:// and https://
     */
    DAVRepositoryFactory.setup();
    /*
     * For using over svn:// and svn+xxx://
     */
    SVNRepositoryFactoryImpl.setup();

    /*
     * For using over file:///
     */
    FSRepositoryFactory.setup();
  }

  private ISVNAuthenticationManager authManager;

  /**
   * Prüft ob der funktionierende Link funktioniert. Dabei wird versucht den aktuellen Logeintrag zu lesen.
   *
   * @param url
   * @return true or false
   */
  public SvnCheckResult checkSVNLink(String url) {

    final StopWatch stopwatch = new StopWatch();
    SVNRepository repository = null;

    try {

      SVNURL svnUrl = SVNURL.parseURIEncoded(url);
      repository = SVNRepositoryFactory.create(svnUrl);
      repository.setAuthenticationManager(this.authManager);
      stopwatch.start();
      long latestRevision = repository.getLatestRevision();
      stopwatch.stop();
      LOG.debug("Duration for getLatestversion: " + stopwatch);
      stopwatch.reset();
      stopwatch.start();
      SVNURL repositoryRoot = repository.getRepositoryRoot(true);
      stopwatch.stop();
      LOG.debug("Duration for getRepositoryRoot: " + stopwatch);
      String path = url.substring(repositoryRoot.toString().length());
      stopwatch.reset();
      stopwatch.start();
      SVNNodeKind nodeKind = repository.checkPath(path, latestRevision);
      stopwatch.stop();
      LOG.debug("Duration for checkPath(" + path + "): " + stopwatch);
      SvnCheckResult result;
      if (nodeKind.equals(SVNNodeKind.FILE) || nodeKind.equals(SVNNodeKind.DIR)) {
        LOG.debug("ERFOLGREICH: '" + url);
        result = new SvnCheckResult(true, true);
      } else if (nodeKind.equals(SVNNodeKind.NONE)) {
        result = new SvnCheckResult(false, true);
      } else {
        result = new SvnCheckResult(false, true);
      }
      return result;
    } catch (SVNException svne) {
      LOG.warn("FEHLGESCHLAGEN: '" + url);
      LOG.warn(svne);
      SvnCheckResult result;
      if (svne.getErrorMessage().getErrorCode().isAuthentication()) {
        result = new SvnCheckResult(false, false);
      } else {
        result = new SvnCheckResult(false, true);
      }
      return result;
    }
  }

  public void init(Konfiguration konfiguration) {

    File certFile = new File(konfiguration.getSvnCertificate());
    char[] certPassword = konfiguration.getSvnCertificatePasswort().toCharArray();
    boolean storageAllowed = false;
    SVNURL url = null;
    boolean isPartial = false;
    SVNSSLAuthentication svnsslAuthentication = SVNSSLAuthentication.newInstance(certFile, certPassword, storageAllowed,
        url, isPartial);
    String svnUserName = konfiguration.getSvnUsername();
    char[] svnPassword = konfiguration.getSvnPassword().toCharArray();
    SVNPasswordAuthentication svnPasswordAuthentication = SVNPasswordAuthentication.newInstance(svnUserName,
        svnPassword, storageAllowed, url, isPartial);

    this.authManager = new BasicAuthenticationManager(
        new SVNAuthentication[] { svnsslAuthentication, svnPasswordAuthentication });
  }
  /*
   * public void checkout() {
   * 
   * SVNClientManager ourClientManager = SVNClientManager.newInstance(null, repository.getAuthenticationManager());
   * SVNUpdateClient updateClient = ourClientManager.getUpdateClient(); updateClient.setIgnoreExternals(false);
   * updateClient.doCheckout(url, destPath, revision, revision, isRecursive);
   * 
   * }
   */
}
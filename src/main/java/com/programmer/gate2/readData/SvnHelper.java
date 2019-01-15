package com.programmer.gate2.readData;

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
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSLAuthentication;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import com.programmer.gate2.readData.SVNConfig.Konfiguration;

/**
 *
 * SVN Helper stellt eine Verbindung zu SVN Repository und
 *
 * @author Capgemini, Rickey Gladstone
 * @version $Id: SvnHelper.java 62830 2017-08-18 12:09:44Z thtimu $
 */
// TODO: Can this be made more robust?
public class SvnHelper {
  private static final Logger LOG = Logger.getLogger(SvnHelper.class);

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

  private BasicAuthenticationManager authManager;

  /**
   * Prüft, ob eine allgemein zu SVN ohne Probleme verbunden werden kann, verwendet das SVN_Ziel URL aus der
   * release.checker.properties
   * 
   * @return true or false
   */
  public boolean checkSVNConnection(String url) {

    SVNRepository repository = null;

    try {
      repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
      repository.setAuthenticationManager(this.authManager);
      repository.testConnection();
      LOG.debug("ERFOLGREICH: '" + url + "'");
      return true;

    } catch (SVNException svne) {
      LOG.warn("FEHLGESCHLAGEN: '" + url + "'");
      LOG.warn(svne);
      return false;
    }

  }

  /**
   * Prüft ob der funktionierende Link funktioniert. Dabei wird versucht den aktuellen Logeintrag zu lesen.
   *
   * @param url
   * @return true or false
   */
  public boolean checkSVNLink(String url) {

    final StopWatch stopwatch = new StopWatch();

    SVNRepository repository = null;
    stopwatch.start();

    try {

      LOG.debug("Step 1: " + stopwatch);
      SVNURL svnUrl = SVNURL.parseURIEncoded(url);
      LOG.debug("Step 2: " + stopwatch);
      repository = SVNRepositoryFactory.create(svnUrl);

      LOG.debug("Step 3: " + stopwatch);
      repository.setAuthenticationManager(this.authManager);

      LOG.debug("Step 4: " + stopwatch);
      long latestRevision = repository.getLatestRevision();
      LOG.debug("Step 5: " + stopwatch);
      SVNURL repositoryRoot = repository.getRepositoryRoot(true);
      LOG.debug("Step 6: " + stopwatch);
      String path = url.substring(repositoryRoot.toString().length());
      LOG.debug("Step 7: " + stopwatch);
      SVNNodeKind nodeKind = repository.checkPath(path, latestRevision);
      LOG.debug("Step 8: " + stopwatch);
      boolean success = false;
      if (nodeKind.equals(SVNNodeKind.FILE) || nodeKind.equals(SVNNodeKind.DIR)) {
        LOG.debug("ERFOLGREICH: '" + url + "': " + stopwatch);
        success = true;
      }
      LOG.debug("Step 9: " + stopwatch);
      return success;

    } catch (SVNException svne) {
      LOG.warn("FEHLGESCHLAGEN: '" + url + "'");
      LOG.warn(svne);
      return false;
    }
  }

  public void init(Konfiguration konfiguration) {

    konfiguration = konfiguration;
    this.authManager = new BasicAuthenticationManager(
        new SVNAuthentication[] {
        new SVNSSLAuthentication(new File(konfiguration.getSvnCertificate()), konfiguration.getSvnCertificatePasswort(),
            false),
        new SVNPasswordAuthentication(konfiguration.getSvnUsername(), konfiguration.getSvnPassword(), false) });
  }

}
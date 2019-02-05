package sst.svn.utils;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import sst.svn.config.Konfiguration;

/**
 * @author vhacimuf
 *
 */
public class TestSvn {

  public static void main(String[] args) {

    String url = "https://svn.win.tue.nl/repos/prom/Packages/GuideTreeMiner/Trunk/";
    Konfiguration conf = new Konfiguration();

    SvnHelperNeu svnHelper = new SvnHelperNeu();
    SvnCheckResult svnCheckResult = svnHelper
        .checkSVNLink("https://svn.win.tue.nl/repos/prom/Packages/GuideTreeMiner/Trunk/");
    System.out.println(svnCheckResult);

    // svnHelper.init(conf);

    SVNClientManager ourClientManager = SVNClientManager.newInstance();
    SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
    updateClient.setIgnoreExternals(false);

    String targetPathBase = "svn/";
    String targetPath = targetPathBase + 1;
    File targetPathFile = new File(targetPath);
    boolean success = targetPathFile.mkdir();
    File f = new File(targetPath);

    if (!success) {
      // Directory creation failed
    }

    SVNRepository repository = null;
    SVNURL svnUrl = null;

    try {
      svnUrl = SVNURL.parseURIEncoded(url);
    } catch (SVNException e1) {
      e1.printStackTrace();
    }

    try {
      repository = SVNRepositoryFactory.create(svnUrl);
    } catch (SVNException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      updateClient.doCheckout(svnUrl, f, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
    } catch (SVNException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}

package dependencyVis.svn.utils;

/**
 * @author Capgemini, Thomas Timu
 * @version $Id: SvnCheckResult.java 62830 2017-08-18 12:09:44Z thtimu $
 */
public class SvnCheckResult {

  private boolean reachable;

  private boolean authorized;

  public SvnCheckResult(boolean reachable, boolean authorized) {

    this.reachable = reachable;
    this.authorized = authorized;
  }

  public boolean isAuthorized() {

    return this.authorized;
  }

  public boolean isReachable() {

    return this.reachable;
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();
    builder.append("SvnCheckResult [reachable=");
    builder.append(this.reachable);
    builder.append(", authorized=");
    builder.append(this.authorized);
    builder.append("]");
    return builder.toString();
  }

}

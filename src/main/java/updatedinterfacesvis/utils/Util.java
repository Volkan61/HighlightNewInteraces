package updatedinterfacesvis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author vhacimuf
 *
 */
public class Util {

  /**
   * Load a Maven {@link Model} object from a POM file.
   *
   * @param pom
   * @param logger
   * @return the model parsed from the POM file
   * @throws XmlPullParserException
   * @throws IOException
   */
  public static Model getModelFromPOM(File pom) throws IOException, XmlPullParserException {

    Model model = null;
    FileInputStream fis = null;
    InputStreamReader isr = null;
    try {
      fis = new FileInputStream(pom);
      isr = new InputStreamReader(fis, "utf-8"); // FIXME
      MavenXpp3Reader reader = new MavenXpp3Reader();
      model = reader.read(isr);
    } finally {
      try {
        isr.close();
        fis.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return model;
  }

}

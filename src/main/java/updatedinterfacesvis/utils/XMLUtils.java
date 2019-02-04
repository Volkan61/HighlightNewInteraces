package updatedinterfacesvis.utils;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author vhacimuf
 *
 */
public class XMLUtils {

  public static void main(String[] args) {

    String personXMLStringValue = null;
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;

    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }

    Document doc = docBuilder.newDocument();

    Element personRootElement = doc.createElement("Person");
    doc.appendChild(personRootElement);

    Element firstNameElement = doc.createElement("FirstName");

    firstNameElement.appendChild(doc.createTextNode("Sergey"));
    personRootElement.appendChild(firstNameElement);
    // Create Last Name Element
    Element lastNameElement = doc.createElement("LastName");
    lastNameElement.appendChild(doc.createTextNode("Kargopolov"));
    personRootElement.appendChild(lastNameElement);

    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = null;

    try {
      transformer = tf.newTransformer();
    } catch (TransformerConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    StringWriter writer = new StringWriter();

    try {
      transformer.transform(new DOMSource(doc), new StreamResult(writer));
    } catch (TransformerException e) {
      e.printStackTrace();
    }

    // Get the String value of final xml document
    personXMLStringValue = writer.getBuffer().toString();

  }
}

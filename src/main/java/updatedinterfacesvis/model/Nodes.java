package updatedinterfacesvis.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author vhacimuf
 *
 */
public class Nodes {

  LinkedList<Node> nodes = new LinkedList<Node>();

  /**
   * @return nodes
   */
  public LinkedList<Node> getNodes() {

    return this.nodes;
  }

  /**
   * @param nodes new value of {@link #getnodes}.
   */
  public void setNodes(LinkedList<Node> nodes) {

    this.nodes = nodes;
  }

  /**
   * @param nodes new value of {@link #getnodes}.
   */

  public List<Interface> findNodeWithOfferedInterface(String name, String interfaceName) {

    Node node = findNode(name);

    List<Interface> offeredInterfacesFound = null;
    // for (Iterator iterator = this.nodes.iterator(); iterator.hasNext();) {
    // Node node = (Node) iterator.next();
    List<Interface> offeredInterfaces = node.getOfferedInterfaces();
    offeredInterfacesFound = new LinkedList<Interface>();

    for (Iterator iterator2 = offeredInterfaces.iterator(); iterator2.hasNext();) {
      Interface interface1 = (Interface) iterator2.next();
      String s = interface1.getName();

      if (s.equals(interfaceName)) {
        offeredInterfacesFound.add(interface1);
      }
    }

    // }

    return offeredInterfacesFound;

  }

  public List<Interface> findNodeWithUsedInterface(String name, String interfaceName) {

    Node node = findNode(name);

    List<Interface> usedInterfacesFound = null;
    // for (Iterator iterator = this.nodes.iterator(); iterator.hasNext();) {
    // Node node = (Node) iterator.next();
    List<Interface> usedInterfaces = node.getUsedInterfaces();
    usedInterfacesFound = new LinkedList<Interface>();

    for (Iterator iterator2 = usedInterfaces.iterator(); iterator2.hasNext();) {
      Interface interface1 = (Interface) iterator2.next();
      String s = interface1.getName();

      if (s.equals(interfaceName)) {
        usedInterfacesFound.add(interface1);
      }
    }

    // }

    return usedInterfacesFound;

  }

  public Node findNode(String name) {

    Node foundNode = null;
    for (Iterator iterator = this.nodes.iterator(); iterator.hasNext();) {
      Node node = (Node) iterator.next();
      String nodeName = node.getName();

      if (nodeName.equals(name)) {

        foundNode = node;
        return foundNode;
      }
    }

    return foundNode;

  }

  /**
   * @param list
   * @param pInterface
   * @return
   */
  public static boolean checkTest(LinkedList<Interface> list, Interface pInterface) {

    boolean result = false;
    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      Interface interface1 = (Interface) iterator.next();

      String pInterfaceName = pInterface.getName();
      String pInterfaceVersion = pInterface.getVersion();

      String interfaceName = interface1.getName();
      String interfaceVersion = interface1.getVersion();

      if (pInterfaceName.equals(interfaceName) && pInterfaceVersion.equals(interfaceVersion)) {
        result = true;
        break;
      }
    }
    return result;
  }

  public static String getKey(Map<String, LinkedList<Interface>> mapPrefixToInterfaces, Interface pInterface) {

    Set<String> keySetHashMap = mapPrefixToInterfaces.keySet();

    String result = null;
    for (Iterator iterator = keySetHashMap.iterator(); iterator.hasNext();) {
      String string = (String) iterator.next();
      LinkedList<Interface> list = mapPrefixToInterfaces.get(string);

      boolean asdsa = checkTest(list, pInterface);

      if (asdsa) {
        result = string;
        break;
      }
    }
    return result;
  }

}

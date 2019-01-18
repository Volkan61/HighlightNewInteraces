package dependencyVis.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

}

package updatedinterfacesvis.neo4j;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * @author vhacimuf
 *
 */
public class Neo4J {

  GraphDatabaseService graphDb;

  /**
   * The constructor.
   */

  public Neo4J(String path) {

    // File a = new File("/Users/vhacimuf/Desktop/neo4j-community-3.5.1" + "/data/databases/graph.db");
    File a = new File(path);
    this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(a);
  }

  public void addNode(NodeType nodeType, Map<String, Integer> properties) {

    try (Transaction tx = this.graphDb.beginTx()) {
      // Database operations go here

      Node bobNode = this.graphDb.createNode(NodeType.Application);

      Set<String> keySet = properties.keySet();

      for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();
        bobNode.setProperty(key, properties.get(key));
      }
      tx.success();
    } finally {
    }
  }

  public void addNode(NodeType nodeType, Map<String, String> properties, String label) {

    try (Transaction tx = this.graphDb.beginTx()) {

      Node bobNode = this.graphDb.createNode(nodeType);

      Set<String> keySet = properties.keySet();

      for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();
        bobNode.setProperty(key, properties.get(key));
      }

      String id = "";
      for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();
        id = id + properties.get(key);
      }
      tx.success();
    } finally {
    }
  }

  public void shutdown() {

    this.graphDb.shutdown();
  }

  public void addRelation(String nodeOneId, NodeType nodeTypeOne, String nodeTwoId, NodeType nodeTypeTwo,
      Map<String, String> properties, RelationType type) {

    try (Transaction tx = this.graphDb.beginTx()) {

      Node nodeOne = this.graphDb.findNode(nodeTypeOne, "Pid", nodeOneId);
      Node nodeTwo = this.graphDb.findNode(nodeTypeTwo, "Pid", nodeTwoId);

      Relationship relation = nodeOne.createRelationshipTo(nodeTwo, type);
      // relation.setProperty("test", "test");

      tx.success();
    } finally {
    }
  }

  public enum NodeType implements Label {
    Application, Interface
  }

  public enum RelationType implements RelationshipType {
    uses, offers, Interface, Connection, Oldversion
  }
}

package com.programmer.gate2.readData;

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
public class NEO4JDatabase {

  GraphDatabaseService graphDb;

  /**
   * The constructor.
   */
  // File a = new File("/Users/vhacimuf/Desktop/neo4j-community-3.5.1" + "/data/databases/graph.db");

  public NEO4JDatabase(String pathToDatabase) {

    File a = new File("/Users/vhacimuf/Desktop/neo4j-community-3.5.1" + "/data/databases/graph.db");
    this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(a);
  }

  // Map<String, String> map = new HashMap<String, String>();

  public void addNode(NodeType nodeType, Map<String, Integer> properties) {

    try (Transaction tx = this.graphDb.beginTx()) {
      // Database operations go here

      Node bobNode = this.graphDb.createNode(NodeType.Application);

      Set<String> keySet = properties.keySet();

      for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();
        bobNode.setProperty(key, properties.get(key));
      }
      // Node aliceNode = this.graphDb.createNode(NodeType.Person);
      // bobNode.setProperty("PiD", 5002);
      // bobNode.setProperty("Age", 23);

      // Relationship alice = aliceNode.createRelationshipTo(bobNode, RelationType.Know);
      // alice.setProperty("test", "test");
      tx.success();
    } finally {
    }
  }

  public void addNode(NodeType nodeType, Map<String, String> properties, String label) {

    try (Transaction tx = this.graphDb.beginTx()) {
      // Database operations go here

      Node bobNode = this.graphDb.createNode(nodeType);

      Set<String> keySet = properties.keySet();

      for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();

        bobNode.setProperty(key, properties.get(key));
      }

      // bobNode.addLabel(DynamicLabel.label(label));
      // Node aliceNode = this.graphDb.createNode(NodeType.Person);
      // bobNode.setProperty("PiD", 5002);
      // bobNode.setProperty("Age", 23);

      // Relationship alice = aliceNode.createRelationshipTo(bobNode, RelationType.Know);
      // alice.setProperty("test", "test");
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
      // Database operations go here

      // Iterable<Node> lNodes =this.graphDb.find

      Node nodeOne = this.graphDb.findNode(nodeTypeOne, "id", nodeOneId);
      Node nodeTwo = this.graphDb.findNode(nodeTypeTwo, "id", nodeTwoId);

      // Node nodeTwo = this.graphDb.findNode(DynamicLabel.label("Entity"), "id", nodeTwoId);

      // Get Node 1
      // Node bobNode = this.graphDb.createNode(NodeType.Person);
      // Get Node 2
      // Node aliceNode = this.graphDb.createNode(NodeType.Person);

      Relationship relation = nodeOne.createRelationshipTo(nodeTwo, type);
      relation.setProperty("test", "test");
      tx.success();
    } finally {
    }
  }

  public enum NodeType implements Label {
    Application, UsedInterface, OfferedInterface
  }

  public enum RelationType implements RelationshipType {
    uses, offers, Interface
  }

  public static void main(String[] args) {

    GraphDatabaseService graphDb;

    File a = new File("/Users/vhacimuf/Desktop/neo4j-community-3.5.1" + "/data/databases/graph.db");

    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(a);
    try (Transaction tx = graphDb.beginTx()) {
      // Database operations go here

      Node bobNode = graphDb.createNode(NodeType.Application);
      bobNode.setProperty("PiD", 5001);
      bobNode.setProperty("Age", 23);

      Node aliceNode = graphDb.createNode(NodeType.Application);
      bobNode.setProperty("PiD", 5002);
      bobNode.setProperty("Age", 23);

      Relationship alice = aliceNode.createRelationshipTo(bobNode, RelationType.Interface);
      alice.setProperty("test", "test");
      tx.success();
    } finally {

    }

    graphDb.shutdown();

  }
}

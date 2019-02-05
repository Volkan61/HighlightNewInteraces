package sst.neo4j;

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
 * @author CapGemini, Volkan Hacimüftüoglu
 * @version 05.02.2018
 */
public class Neo4j {

  GraphDatabaseService graphDb;

  public Neo4j(String path) {

    File a = new File(path);
    this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(a);
  }

  /*
   * Fügt einen Knoten in die Graphendatenbank hinzu.
   */

  public void addNode(NodeType nodeType, Map<String, String> properties) {

    try (Transaction tx = this.graphDb.beginTx()) {

      Node bobNode = this.graphDb.createNode(nodeType);

      Set<String> keySet = properties.keySet();

      for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();
        bobNode.setProperty(key, properties.get(key));
      }
      tx.success();
    } finally {
    }
  }

  /*
   * Fügt einen Knoten in die Graphendatenbank hinzu.
   */

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

  /*
   * Fügt eine Relation in die Graphendatenbank hinzu.
   */

  public void addRelation(String nodeOneId, NodeType nodeTypeOne, String nodeTwoId, NodeType nodeTypeTwo,
      Map<String, String> properties, RelationType type) {

    try (Transaction tx = this.graphDb.beginTx()) {

      Node nodeOne = this.graphDb.findNode(nodeTypeOne, "pid", nodeOneId);
      Node nodeTwo = this.graphDb.findNode(nodeTypeTwo, "pid", nodeTwoId);

      Relationship relation = nodeOne.createRelationshipTo(nodeTwo, type);

      tx.success();
    } finally {
    }
  }

  /*
   * Überprüft ob ein Knoten mit dem übergebenen Id in der Graphendatenbank existiert.
   */

  public boolean findNode(String nodeId) {

    try (Transaction tx = this.graphDb.beginTx()) {
      Node node = this.graphDb.findNode(NodeType.INTERFACE, "pid", nodeId);
      tx.success();
      return node != null;
    } finally {
    }
  }

  public enum NodeType implements Label {
    APPLICATION, INTERFACE
  }

  public enum RelationType implements RelationshipType {
    USES, OFFERS
  }
}

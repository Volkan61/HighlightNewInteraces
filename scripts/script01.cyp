MATCH  (node1)-[:OFFERS]->(node2)
WHERE NOT (node2)<-[:USES]-(:APPLICATION)
RETURN (node2)
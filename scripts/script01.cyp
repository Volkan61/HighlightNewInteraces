MATCH  (node1)-[:offers]->(node2)
WHERE NOT (node2)<-[:uses]-(:Application)
RETURN (node2)
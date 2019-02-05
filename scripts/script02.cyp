MATCH (node1:APPLICATION)-[:USES]->(i1:INTERFACE), (node2:APPLICATION)-[:OFFERS]->(i1:INTERFACE),(node2:APPLICATION)-[:offers]->(i2:Interface) 
WHERE i1.name=i2.name AND ((i1.technicalVersion<i2.technicalVersion) OR (i1.businessVersion<i2.businessVersion))
CREATE (node1)-[:USES_OLDVERSION]->(i1)

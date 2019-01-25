MATCH (node1:Application)-[:uses]->(i1:Interface), (node2:Application)-[:offers]->(i1:Interface),(node2:Application)-[:offers]->(i2:Interface) 
WHERE i1.name=i2.name AND ((i1.technicalVersion<i2.technicalVersion) OR (i1.version<i2.version))
CREATE (node1)-[:USES_OLDVERSION]->(i1)

MATCH (node1:APPLICATION)-[:USES]->(i1:INTERFACE), (node2:APPLICATION)-[:OFFERS]->(i1:INTERFACE),(node2:APPLICATION)-[:OFFERS]->(i2:INTERFACE) 
WHERE i1.name=i2.name AND ((i1.businessVersion<i2.businessVersion) XOR ((i1.businessVersion=i2.businessVersion) AND (i1.technicalVersion<i2.technicalVersion)))
SET node1 :TOBEUPDATED
CREATE (i1)-[:OLDER]->(i2)
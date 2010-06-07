package org.zfin.ontology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RelationshipType implements Serializable{

    private static final RelationshipType DEVELOPS_FROM = new RelationshipType("develops_from", "develops from");
    private static final RelationshipType DEVELOPS_INTO = new RelationshipType("develops_into", "develops into");
    private static final RelationshipType IS_A = new RelationshipType("is_a", "is a type of");
    private static final RelationshipType HAS_SUBTYPE = new RelationshipType("has_subtype", "has subtype");
    private static final RelationshipType PART_OF = new RelationshipType("part_of", "is part of");
    private static final RelationshipType HAS_PARTS = new RelationshipType("has_parts", "has parts");

    private static List<RelationshipType> predefinedTypes = new ArrayList<RelationshipType>(8);

    static {
        predefinedTypes.add(DEVELOPS_FROM);
        predefinedTypes.add(DEVELOPS_INTO);
        predefinedTypes.add(IS_A);
        predefinedTypes.add(HAS_SUBTYPE);
        predefinedTypes.add(PART_OF);
        predefinedTypes.add(HAS_PARTS);
    }

    private static Map<RelationshipType,RelationshipType> inverseTypes = new HashMap<RelationshipType, RelationshipType>(8);
    static{
        inverseTypes.put(DEVELOPS_FROM, DEVELOPS_INTO);
        inverseTypes.put(DEVELOPS_INTO,DEVELOPS_FROM);
        inverseTypes.put(IS_A,HAS_SUBTYPE);
        inverseTypes.put(HAS_SUBTYPE,IS_A);
        inverseTypes.put(PART_OF,HAS_PARTS);
        inverseTypes.put(HAS_PARTS,PART_OF);
    }

    private String name;
    private String relType;

    public RelationshipType(String name, String relType) {
        this.name = name;
        this.relType = relType;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public static RelationshipType getRelationshipTypeByRelName(String relName) {
        if (relName == null)
            return null;

        for (RelationshipType type : predefinedTypes) {
            if (type.getName().equals(relName))
                return type;
        }
        return new RelationshipType(relName, relName);
    }

    public static RelationshipType getInverseRelationshipByName(String relName) {
        RelationshipType type = getRelationshipTypeByRelName(relName);
        return getInverseRelationship(type);
    }

    public static RelationshipType getInverseRelationship(RelationshipType type) {
        if (type == null)
            return null;
        RelationshipType inverseRelationshipType = inverseTypes.get(type);
        if(inverseRelationshipType != null)
            return inverseRelationshipType;
        return new RelationshipType("inverse "+type.getName(), "inverse "+type.getTypeName());
    }

    public String getTypeName() {
        return relType;
    }
}

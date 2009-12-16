package org.zfin.ontology;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public enum RelationshipType {
    DEVELOPS_FROM("develops_from", "develops from"),
    DEVELOPS_INTO("develops_into", "develops into"),
    IS_A("is_a", "is a type of"),
    HAS_SUBTYPE("has_subtype", "has subtype"),
    PART_OF("part_of", "is part of"),
    HAS_PARTS("has_parts", "has parts");

    private String name;
    private String relType;

    RelationshipType(String name, String relType) {
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
        for (RelationshipType t : values()) {
            if (t.toString().equals(relName))
                return t;
        }
        throw new RuntimeException("No relationship type of string " + relName + " found.");

    }

    public static RelationshipType getInverseRelationshipByName(String relName) {
        RelationshipType type = getRelationshipTypeByRelName(relName);
        return getInverseRelationship(type);
    }

    public static RelationshipType getInverseRelationship(RelationshipType type) {
        switch (type) {
            case DEVELOPS_FROM:
                return DEVELOPS_INTO;
            case DEVELOPS_INTO:
                return DEVELOPS_FROM;
            case IS_A:
                return HAS_SUBTYPE;
            case HAS_SUBTYPE:
                return IS_A;
            case PART_OF:
                return HAS_PARTS;
            case HAS_PARTS:
                return PART_OF;
        }
        throw new IllegalStateException("This RelationshipType has not been mapped to an inverse type: " + type.getName());
    }

    public String getTypeName() {
        return relType;
    }
}

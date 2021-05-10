package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RelationshipType implements Serializable, IsSerializable {

    public RelationshipType(){}

    public static final RelationshipType DEVELOPS_FROM = new RelationshipType("develops_from", "develops from");
    // for some reason there are 2 develops from relationships here
    public static final RelationshipType DEVELOPS_FROM2 = new RelationshipType("develops from", "develops from");
    public static final RelationshipType DEVELOPS_INTO = new RelationshipType("develops_into", "develops into");
    public static final RelationshipType IS_A = new RelationshipType("is_a", "is a type of");
    public static final RelationshipType HAS_SUBTYPE = new RelationshipType("has_subtype", "has subtype");
    public static final RelationshipType PART_OF = new RelationshipType("part_of", "is part of");
    public static final RelationshipType PART_OF2 = new RelationshipType("part of", "is part of");
    // madeup for inverse.  HAS_PARTS is not correctly inverse for PART_OF, though HAS_PARTS does imply PART_OF inverse
    public static final RelationshipType MAY_HAVE_PART = new RelationshipType("may have part", "may have part");
    public static final RelationshipType HAS_PARTS = new RelationshipType("has_parts", "has parts");

    public static final RelationshipType REGULATES = new RelationshipType("regulates", "regulates");
    // madeup for invser
    public static final RelationshipType REGULATED_BY = new RelationshipType("regulated_by", "regulated by");

    public static final RelationshipType POSITIVELY_REGULATES = new RelationshipType("positively_regulates", "positively regulates");
    // madeup
    public static final RelationshipType POSITIVELY_REGULATED_BY = new RelationshipType("positively regulated by", "positively regulated by");

    public static final RelationshipType NEGATIVELY_REGULATES = new RelationshipType("negatively_regulates", "negatively regulates");
    // madeup
    public static final RelationshipType NEGATIVELY_REGULATED_BY = new RelationshipType("negatively regulated by", "negatively regulated by");

    public static final RelationshipType START_STAGE = new RelationshipType("start stage","start stage") ;
    public static final RelationshipType END_STAGE = new RelationshipType("end stage","end stage") ;

    private static List<RelationshipType> predefinedTypes = new ArrayList<RelationshipType>();

    static {
        predefinedTypes.add(DEVELOPS_FROM);
        predefinedTypes.add(DEVELOPS_FROM2);
        predefinedTypes.add(DEVELOPS_INTO);
        predefinedTypes.add(IS_A);
        predefinedTypes.add(HAS_SUBTYPE);
        predefinedTypes.add(PART_OF);
        predefinedTypes.add(PART_OF2);
        predefinedTypes.add(HAS_PARTS);
        predefinedTypes.add(REGULATES);
        predefinedTypes.add(POSITIVELY_REGULATES);
        predefinedTypes.add(NEGATIVELY_REGULATES);
    }

    private static Map<RelationshipType, RelationshipType> inverseTypes = new HashMap<RelationshipType, RelationshipType>();

    static {
        inverseTypes.put(DEVELOPS_FROM2, DEVELOPS_INTO);
        inverseTypes.put(DEVELOPS_FROM, DEVELOPS_INTO);
        inverseTypes.put(DEVELOPS_INTO, DEVELOPS_FROM);
        inverseTypes.put(IS_A, HAS_SUBTYPE);
        inverseTypes.put(HAS_SUBTYPE, IS_A);
//        inverseTypes.put(PART_OF, MAY_HAVE_PART);
//        inverseTypes.put(PART_OF2, MAY_HAVE_PART);
        inverseTypes.put(PART_OF, HAS_PARTS);
        inverseTypes.put(PART_OF2, HAS_PARTS);
        inverseTypes.put(HAS_PARTS, PART_OF);
        inverseTypes.put(REGULATES, REGULATED_BY);
        inverseTypes.put(POSITIVELY_REGULATES, POSITIVELY_REGULATED_BY);
        inverseTypes.put(NEGATIVELY_REGULATES, NEGATIVELY_REGULATED_BY);
    }

    private String dbMappedName;
    private String display;

    public RelationshipType(String dbMappedName, String display) {
        this.dbMappedName = dbMappedName;
        this.display = display;
    }

    public String toString() {
        return dbMappedName;
    }

    public String getDbMappedName() {
        return dbMappedName;
    }

    public String getDisplay() {
        return display;
    }

    public static RelationshipType getRelationshipTypeByDbName(String relName) {
        if (relName == null)
            return null;

        for (RelationshipType type : predefinedTypes) {
            if (type.getDbMappedName().equals(relName)) {
                return type;
            }
        }
        return new RelationshipType(relName, relName);
    }

    public static RelationshipType getInverseRelationshipByName(String relName) {
        RelationshipType type = getRelationshipTypeByDbName(relName);
        return getInverseRelationship(type);
    }

    public static RelationshipType getInverseRelationship(RelationshipType type) {
        if (type == null) {
            return null;
        }
        RelationshipType inverseRelationshipType = inverseTypes.get(type);
        if (inverseRelationshipType != null) {
            return inverseRelationshipType;
        }
        return new RelationshipType("inverse " + type.getDbMappedName(), "inverse " + type.getDisplay());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelationshipType that = (RelationshipType) o;

        if (dbMappedName != null ? !dbMappedName.equals(that.dbMappedName) : that.dbMappedName != null) return false;
        if (display != null ? !display.equals(that.display) : that.display != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dbMappedName != null ? dbMappedName.hashCode() : 0;
        result = 31 * result + (display != null ? display.hashCode() : 0);
        return result;
    }

    public static boolean isStage(String type) {
        return type.equalsIgnoreCase(RelationshipType.START_STAGE.getDisplay())
                ||
                type.equalsIgnoreCase(RelationshipType.END_STAGE.getDisplay()) ;
    }
}

package org.zfin.infrastructure;

/**
 */
public class ActiveSource implements ZdbID {
    private String zdbID;
    public static final String ZDB = "ZDB-";

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        validateID(zdbID);
        this.zdbID = zdbID;
    }

    public Type validateID(String zdbID) {
        if (zdbID == null) {
            throw new InvalidZdbID();
        }

        if (!zdbID.startsWith(ActiveSource.ZDB))
            throw new InvalidZdbID(zdbID);

        Type type = null;
        for (Type zdbType : Type.values()) {
            if (zdbID.contains(zdbType.name())) {
                type = zdbType;
            }
        }

        if (type == null) {
            throw new InvalidZdbID(zdbID, Type.getValues());
        }
        return type;
    }

    public enum Type {
        COMPANY,
        JRNL,
        LAB,
        PERS,
        PUB;

        private static String allValues;

        public static String getValues() {
            if (allValues != null)
                return allValues;
            StringBuilder sb = new StringBuilder("[");
            int size = values().length;
            int index = 0;
            for (Type type : values()) {
                sb.append(type.name());
                index++;
                if (index < size)
                    sb.append(",");
            }
            sb.append("]");
            allValues = sb.toString();
            return allValues;
        }
    }
}

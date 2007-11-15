package org.zfin.infrastructure;

/**
 * Base class for alias records. You need to extend this class
 * to make use of the real function, e.g. MarkerAlias or AnatomySynonym.
 */
public abstract class DataAlias {

    public enum Group {
        ALIAS("alias"),
        PLURAL("plural"),
        SECONDARY_ID("secondary id"),
        SEQUENCE_SIMILARITY("sequence similarity");

        private String value;

        private Group(String value) {
            this.value = value;
        }

        public String toString(){
            return value;
        }
    }

    private String zdbID;
    protected String alias;
    private Group group;


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}

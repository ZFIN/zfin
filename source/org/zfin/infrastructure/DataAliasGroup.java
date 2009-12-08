package org.zfin.infrastructure;

/**
 * This class is only used for validation of the correct
 * group enumeration items.
 * Use DataAlias.Group enum if needed.
 */
public class DataAliasGroup {

    private int id;
    private String name;
    private int significance;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSignificance() {
        return significance;
    }

    public void setSignificance(int significance) {
        this.significance = significance;
    }

    public enum Group {
        ALIAS("alias"),
        EXACT_ALIAS("exact alias"),
        EXACT_PLURAL("exact plural"),
        PLURAL("plural"),
        RELATED_ALIAS("related alias"),
        RELATED_PLURAL("related plural"),
        SECONDARY_ID("secondary id"),
        SEQUENCE_SIMILARITY("sequence similarity"),
        NARROW_ALIAS("narrow alias"),
        BROAD_ALIAS("broad alias"),
        SYSTEMATIC_SYNONYM("systematic_synonym");

        private String value;

        private Group(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }


}

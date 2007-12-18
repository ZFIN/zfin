package org.zfin.infrastructure;

import java.util.Date;

/**
 * This class maps to the flag table that is used to
 * indicate if a process is currently running to avoid
 * concurrent execution of the same process or processes
 * that should not be run concurrently.
 */
public class ZdbFlag {

    private Type type;
    private boolean systemUpdateDisabled;
    private Date dateLastModified;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isSystemUpdateDisabled() {
        return systemUpdateDisabled;
    }

    public void setSystemUpdateDisabled(boolean systemUpdateDisabled) {
        this.systemUpdateDisabled = systemUpdateDisabled;
    }

    public Date getDateLastModified() {
        return dateLastModified;
    }

    public void setDateLastModified(Date dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    public enum Type{
        DISABLE_UPDATES("disable updates"),
        ALIAS_TOKEN("regen_alias_tokens"),
        ANATOMY("regen_anatomy"),
        ANATOMY_TOKENS("regen_anatomy_tokens"),
        FISHSEARCH("regen_fishsearch"),
        GENOTYPE_DISPLAY("regen_genotype_display"),
        MAPS("regen_maps"),
        NAMES("regne_names"),
        OEVDISP("regen_oevdisp");

        private final String value;

        private Type(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No type of string " + type + " found.");
        }
    }

}

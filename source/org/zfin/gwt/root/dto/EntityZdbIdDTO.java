package org.zfin.gwt.root.dto;

import java.util.Objects;

public class EntityZdbIdDTO implements Comparable<EntityZdbIdDTO> {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String type;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityZdbIdDTO that = (EntityZdbIdDTO) o;
        return Objects.equals(zdbID, that.zdbID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zdbID);
    }

    @Override
    public int compareTo(EntityZdbIdDTO o) {
        int nameCompare = this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
        if (nameCompare == 0) {
            return this.getZdbID().compareTo(o.getZdbID());
        }
        return nameCompare;
    }
}

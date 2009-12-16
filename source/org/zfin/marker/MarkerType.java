package org.zfin.marker;

import java.util.HashSet;
import java.util.Set;

public class MarkerType implements Comparable {

    private String name;
    private Marker.Type type;
    private Set<String> typeGroupStrings;
    private Set<Marker.TypeGroup> typeGroups;
    private String displayName;

    public String toString() {
        String returnString = "";
        returnString += "name[" + name + "]";
        if (type != null) returnString += "type[" + type.name() + "]";
        if (typeGroupStrings != null) {
            returnString += "typeGroupStrings[";
            for (String typeGroupString : typeGroupStrings) {
                returnString += typeGroupString + " ";
            }
            returnString += "]";
        }
        if (typeGroups != null) {
            returnString += "typeGroups[";
            for (Marker.TypeGroup markerTypeGroup : typeGroups) {
                returnString += markerTypeGroup.name() + " ";
            }
            returnString += "]";
        }

        return returnString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        type = Marker.Type.getType(name);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Marker.Type getType() {
        return type;
    }

    public void setType(Marker.Type type) {
        this.type = type;
    }

    public Set<String> getTypeGroupStrings() {
        return typeGroupStrings;
    }

    public void setTypeGroupStrings(Set<String> typeGroupStrings) {
        this.typeGroupStrings = typeGroupStrings;
        //now populate the enumeration objects
        typeGroups = new HashSet<Marker.TypeGroup>();
        for (String s : typeGroupStrings) {
            typeGroups.add(Marker.TypeGroup.getType(s));
        }
    }


    public Set<Marker.TypeGroup> getTypeGroups() {
        return typeGroups;
    }

    public void setTypeGroups(Set<Marker.TypeGroup> typeGroups) {
        this.typeGroups = typeGroups;
    }

    //todo: this comparison should be something a little more interesting than type name
    public int compareTo(Object o) {
        MarkerType mt = (MarkerType) o;
        if (mt == null)
            return +1;
        else return displayName.compareTo(mt.getDisplayName());
    }
}

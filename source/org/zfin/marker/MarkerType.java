package org.zfin.marker;

import java.util.Set;
import java.util.HashSet;

public class MarkerType {

    private String name;
    private Marker.Type type;
    private Set<String> typeGroupStrings;
    private Set<Marker.TypeGroup> typeGroups;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        type = Marker.Type.getType(name);
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
}

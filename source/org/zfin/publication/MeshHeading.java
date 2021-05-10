package org.zfin.publication;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;

public class MeshHeading implements Comparable<MeshHeading> {

    private Long id;
    private MeshHeadingTerm descriptor;
    private SortedSet<MeshHeadingTerm> qualifiers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MeshHeadingTerm getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(MeshHeadingTerm descriptor) {
        this.descriptor = descriptor;
    }

    public SortedSet<MeshHeadingTerm> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(SortedSet<MeshHeadingTerm> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public List<String> getDisplayList() {
        String base = descriptor.toString();
        List<String> displayList = new ArrayList<>();
        if (CollectionUtils.isEmpty(qualifiers)) {
            displayList.add(base);
        } else {
            for (MeshHeadingTerm qualifier : qualifiers) {
                displayList.add(base + "/" + qualifier.toString());
            }
        }
        return displayList;
    }

    @Override
    public int compareTo(MeshHeading o) {
        return this.descriptor.compareTo(o.getDescriptor());
    }

}

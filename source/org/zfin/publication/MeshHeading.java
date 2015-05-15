package org.zfin.publication;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MeshHeading {

    private Long id;
    private MeshHeadingTerm descriptor;
    private Set<MeshHeadingTerm> qualifiers;

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

    public Set<MeshHeadingTerm> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(Set<MeshHeadingTerm> qualifiers) {
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

}

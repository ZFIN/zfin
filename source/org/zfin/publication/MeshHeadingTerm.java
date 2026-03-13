package org.zfin.publication;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class MeshHeadingTerm implements Comparable<MeshHeadingTerm> {

    @ManyToOne
    @JoinColumn(name = "term_id")
    private MeshTerm term;

    @Column(name = "is_major_topic")
    private Boolean majorTopic;

    @Override
    public String toString() {
        String str = term.getName();
        if (majorTopic) {
            str += "*";
        }
        return str;
    }

    @Override
    public int compareTo(MeshHeadingTerm o) {
        return term.compareTo(o.getTerm());
    }

}

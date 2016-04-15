package org.zfin.feature;

import org.apache.commons.lang3.ObjectUtils;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "mdcv_used_in", discriminatorType = DiscriminatorType.STRING)
@Table(name = "mutation_detail_controlled_vocabulary")
public abstract class MutationDetailControlledVocabularyTerm implements Comparable<MutationDetailControlledVocabularyTerm> {

    @Id
    @Column(name = "mdcv_term_zdb_id")
    private String zdbID;

    @OneToOne
    @PrimaryKeyJoinColumn
    private GenericTerm term;

    @Column(name = "mdcv_term_display_name")
    private String displayName;

    @Column(name = "mdcv_term_abbreviation")
    private String abbreviation;

    @Column(name = "mdcv_term_order")
    private Integer order;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public int compareTo(MutationDetailControlledVocabularyTerm o) {
        int orderCompare = ObjectUtils.compare(order, o.order);
        if (orderCompare != 0) {
            return orderCompare;
        }
        return ObjectUtils.compare(displayName, o.displayName);
    }
}

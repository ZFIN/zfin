package org.zfin.ontology;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.gwt.root.dto.RelationshipType;

import javax.persistence.*;

/**
 * Domain object for a term-term relationship entity.
 */
@Entity
@Table(name = "term_relationship")
public class GenericTermRelationship implements TermRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "TERMREL")
            })
    @Column(name = "termrel_zdb_id")
    private String zdbId;
    @Column(name = "termrel_type")
    private String type;
    @ManyToOne
    @JoinColumn(name = "termrel_term_1_zdb_id")
    private GenericTerm termOne;
    @ManyToOne
    @JoinColumn(name = "termrel_term_2_zdb_id")
    private GenericTerm termTwo;


    public GenericTerm getTermOne() {
        return termOne;
    }

    public void setTermOne(GenericTerm termOne) {
        this.termOne = termOne;
    }

    public GenericTerm getTermTwo() {
        return termTwo;
    }

    public void setTermTwo(GenericTerm termTwo) {
        this.termTwo = termTwo;
    }

    public String getZdbId() {
        return zdbId;
    }

    public void setZdbId(String zdbId) {
        this.zdbId = zdbId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RelationshipType getRelationshipType() {
        return RelationshipType.getRelationshipTypeByDbName(type);
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        type = relationshipType.getDbMappedName();
    }

    /**
     * Retrieve the term that is related to the term provided.
     * Return null if the term passed in is null or if the term passed in
     * is not one of the two intrinsic terms.
     *
     * @param term Generic Term
     * @return GenericTerm
     */
    public GenericTerm getRelatedTerm(GenericTerm term) {
        if (term == null)
            return null;
        if (!(term.equals(getTermOne()) || term.equals(getTermTwo())))
            return null;

        if (term.equals(getTermOne()))
            return getTermTwo();
        else
            return getTermOne();
    }

    @Override
    public int compareTo(TermRelationship o) {
        if (!type.equalsIgnoreCase(o.getType()))
            return type.compareTo(o.getType());
        if (!termOne.equals(o.getTermOne()))
            return termOne.compareTo(o.getTermOne());
        return termTwo.compareTo(o.getTermTwo());
    }

}

package org.zfin.ontology.repository;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Ontology-related actions: mostly lookup.
 */
public class HibernateOntologyRepository implements OntologyRepository {


    /**
     * Retrieve a list of terms from the Quality
     * ontology that match a given
     * string.
     *
     * @param queryString query string
     * @return list of quality terms
     */
    public List<Term> getQualityTermsByQuery(String queryString) {
        return null;
    }

    /**
     * Retrieve all terms from the quality ontology that are not obsoleted.
     *
     * @return list of quality terms
     */
    public List<Term> getAllQualityTerms() {
        return getAllTermsFromOntology(Ontology.QUALITY);

    }

    /**
     * Retrieve all terms from a given ontology that are not obsoleted
     * and are not marked as secondary (which means the term has been merged with
     * another term and can be found in the alias table now. 
     *
     * @return list of terms
     */
    @SuppressWarnings("unchecked")
    public List<Term> getAllTermsFromOntology(Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        //criteria.add(Restrictions.ne("obsolete", true));
        criteria.add(Restrictions.eq("ontology", ontology));
        criteria.add(Restrictions.eq("secondary", false));
        criteria.addOrder(Order.asc("termName"));
        criteria.setFetchMode("aliases", FetchMode.JOIN);
        return (List<Term>) criteria.list();
    }

    /**
     * Retrieve a map of aliases that match a term of a given ontology.
     * An alias has to be unique within an ontology.
     *
     * @param ontology Ontology
     * @return list of TermAlias objects
     */
    @SuppressWarnings("unchecked")
    public List<TermAlias> getAllAliases(Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        String hql = " from TermAlias as alias where " +
                "          alias.term.obsolete <> :obsolete AND " +
                "          alias.term.ontology = :ontology ";

        Query query = session.createQuery(hql);
        query.setParameter("obsolete", true);
        query.setParameter("ontology", ontology);
        return (List<TermAlias>) query.list();
    }

    @SuppressWarnings({"unchecked"})
    public List<TermRelationship> getAllRelationships(Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        String hql = " from TermRelationship where " +
                "          termOne.ontology = :ontology";
        Query query = session.createQuery(hql);
        query.setParameter("ontology", ontology);
        return (List<TermRelationship>) query.list();
    }

    /**
     * Retrieve Term by OBO ID.
     *
     * @param termID term id
     * @return Generic Term
     */
    @SuppressWarnings("unchecked")
    public GenericTerm getTermByOboID(String termID) {

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        criteria.add(Restrictions.eq("oboID", termID));
        GenericTerm term = (GenericTerm) criteria.uniqueResult();
        if (term == null)
            return null;
        return term;
    }

    /**
     * Retrieve all related Terms and populate the correct relationship types.
     *
     * @param genericTerm term
     * @return list of relationships
     */
    @SuppressWarnings("unchecked")
    public List<TermRelationship> getTermRelationships(Term genericTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TermRelationship.class);
        criteria.add(Restrictions.eq("termOne.ID", genericTerm.getID()));
        criteria.setFetchMode("termTwo", FetchMode.JOIN);
        criteria.setFetchMode("termTwo.definition", FetchMode.JOIN);
        List<TermRelationship> rels = (List<TermRelationship>) criteria.list();
        if (rels != null) {
            for (TermRelationship relationship : rels) {
                RelationshipType type = RelationshipType.getInverseRelationshipByName(relationship.getType());
                relationship.setRelationshipType(type);
            }
        }

        Criteria criteriaTwo = session.createCriteria(TermRelationship.class);
        criteriaTwo.add(Restrictions.eq("termTwo.ID", genericTerm.getID()));
        criteriaTwo.setFetchMode("termOne", FetchMode.JOIN);
        List<TermRelationship> relationshipListTwo = (List<TermRelationship>) criteriaTwo.list();
        if (relationshipListTwo != null) {
            for (TermRelationship relationship : relationshipListTwo) {
                RelationshipType type = RelationshipType.getRelationshipTypeByRelName(relationship.getType());
                relationship.setRelationshipType(type);
                if (rels == null)
                    rels = new ArrayList<TermRelationship>();
                rels.add(relationship);
            }
        }
        return rels;
    }

    /**
     * Retrieve all Children terms from a given term.
     * Does not include obsolete terms.
     * @param termID ID
     * @return list of terms
     */
    @SuppressWarnings({"unchecked"})
    public List<? extends Term> getChildren(String termID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TermRelationship.class);
        criteria.add(Restrictions.eq("termOne.ID", termID));
        Criteria genericTerm = criteria.createCriteria("termTwo");
        genericTerm.add(Restrictions.eq("obsolete", false));
        List<TermRelationship> relationshipListTwo = (List<TermRelationship>) criteria.list();
        List<Term> terms = new ArrayList<Term>(5);
        if (relationshipListTwo != null) {
            for (TermRelationship relationship : relationshipListTwo) {
                RelationshipType type = RelationshipType.getRelationshipTypeByRelName(relationship.getType());
                relationship.setRelationshipType(type);
                terms.add(relationship.getTermTwo());
            }
        }
        return terms;
    }

    /**
     * Retrieve a term by name and ontology.
     * @param termName term name
     * @param ontology Ontology
     * @return term
     */
    @Override
    public Term getTermByName(String termName, Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        criteria.add(Restrictions.eq("termName", termName));
        criteria.add(Restrictions.eq("ontology", ontology));
        return (Term) criteria.uniqueResult();
    }

    /**
     * Retrieve all parent/child relationships.
     * @return List of transitive closure
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public List<TransitiveClosure> getTransitiveClosure() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TransitiveClosure.class);
        return (List<TransitiveClosure>) criteria.list();
    }


}

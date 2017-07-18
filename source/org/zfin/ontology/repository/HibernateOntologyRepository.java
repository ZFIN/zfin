package org.zfin.ontology.repository;

import org.hibernate.*;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.RelationshipSorting;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionResult2;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.marker.Marker;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.*;

import java.util.*;

/**
 * Repository for Ontology-related actions: mostly lookup.
 */
@Repository
public class HibernateOntologyRepository implements OntologyRepository {


    /**
     * Retrieve all terms from a given ontology that are not obsoleted
     * and are not marked as secondary (which means the term has been merged with
     * another term and can be found in the alias table now.
     *
     * @return list of terms
     */
    @SuppressWarnings("unchecked")
    public List<GenericTerm> getAllTermsFromOntology(Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        criteria.add(Restrictions.eq("ontology", ontology));
        criteria.add(Restrictions.eq("secondary", false));
        criteria.setFetchMode("aliases", FetchMode.JOIN);
        // no function as far as I can tell
//        criteria.addOrder(Order.asc("termName"));
//        criteria.setFetchMode("aliases.aliasGroup", FetchMode.JOIN);
//        criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        return (List<GenericTerm>) criteria.list();
    }

    @Override
    public Map<String, TermDTO> getTermDTOsFromOntology(Ontology ontology) {
        String sql = " " +
                "select" +
                "        this_.term_zdb_id as term1_43_2_," + // 0
                "        this_.term_name as term2_43_2_," + // 1
                "        this_.term_ont_id as term3_43_2_," +  // 2
                "        this_.term_ontology as term4_43_2_," +  // 3
                "        this_.term_is_obsolete as term5_43_2_," +  // 4
                "        this_.term_comment as term8_43_2_," +      // 5
                "        this_.term_definition as term9_43_2_," +   // 6
                "        aliases2_.dalias_alias as dalias2_39_0_," + // 7
                "        tr.termrel_term_2_zdb_id as child," +      //  8
                "        tr.termrel_type as childType," +  /// 9
                "        tr2.termrel_term_1_zdb_id as parent," + // 10
                "        tr2.termrel_type as parentType," +  // 11
                "        ontsubset.osubset_subset_name as subsetName" +  // 12
                "    from" +
                "        TERM this_ " +
                "    left outer join" +
                "        DATA_ALIAS aliases2_ " +
                "            on this_.term_zdb_id=aliases2_.dalias_data_zdb_id " +
                "    left outer join term_relationship tr on tr.termrel_term_1_zdb_id= this_.term_zdb_id  " +
                "    left outer join term_relationship tr2 on tr2.termrel_term_2_zdb_id=this_.term_zdb_id" +
                "    left outer join term_subset termSubset on termSubset.termsub_term_zdb_id=this_.term_zdb_id" +
                "    left outer join ontology_subset ontsubset on ontsubset.osubset_pk_id =termSubset.termsub_subset_id" +
                "    where" +
                "        this_.term_ontology= :ontology " +
                "        and this_.term_is_secondary= :secondary  " +
                "  ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setBoolean("secondary", false);
        query.setString("ontology", ontology.getOntologyName());
//        query.setMaxResults(5);

        List<Object[]> queryList = query.list();

        Map<String, TermDTO> termDTOMap = new HashMap<String, TermDTO>();
        // pass one get the results into the map
        for (Object[] result : queryList) {
            // denormalized over child, parent, and alias
            TermDTO termDTO = termDTOMap.get(result[0].toString());
            if (termDTO == null) {
                termDTO = new TermDTO();
                termDTO.setZdbID(result[0].toString());
                termDTO.setName(result[1].toString());
                termDTO.setOboID(result[2].toString());
                termDTO.setOntology(DTOConversionService.convertToOntologyDTO(Ontology.getOntology(result[3].toString())));
                termDTO.setObsolete(Boolean.valueOf(result[4].toString()));
                if (result[5] != null) {
                    termDTO.setComment(result[5].toString());
                }
                if (result[6] != null) {
                    termDTO.setDefinition(result[6].toString());
                }
            }

            // handle alias
            if (result[7] != null) {
                Set<String> aliases = termDTO.getAliases();
                if (aliases == null) {
                    aliases = new TreeSet<String>();
                }
                aliases.add(result[7].toString());
                termDTO.setAliases(aliases);
            }

            // handle subset info
            if (result[12] != null) {
                Set<String> subsets = termDTO.getSubsets();
                if (subsets == null) {
                    subsets = new TreeSet<String>();
                }

                subsets.add(result[12].toString().intern());
                termDTO.setSubsets(subsets);
                if (subsets.contains(Subset.GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS) ||
                        subsets.contains(Subset.GO_CHECK_DO_NOT_USE_FOR_MANUAL_ANNOTATIONS))
                    termDTO.setDoNotAnnotateWith(true);
                else
                    termDTO.setDoNotAnnotateWith(false);
            }


            // if a child term .. . . .
            if (result[8] != null) {
                TermDTO childDTO = new TermDTO();
                childDTO.setZdbID(result[8].toString());
                childDTO.setRelationshipType(result[9].toString());
                Set<TermDTO> childTerms = termDTO.getChildrenTerms();
                if (childTerms == null) {
                    childTerms = new HashSet<TermDTO>();
                }
                childTerms.add(childDTO);
                termDTO.setChildrenTerms(childTerms);
            }

            // if a parent term .. . . .
            if (result[10] != null) {
                TermDTO parentDTO = new TermDTO();
                parentDTO.setZdbID(result[10].toString());
                parentDTO.setRelationshipType(result[11].toString());
                Set<TermDTO> parentTerms = termDTO.getParentTerms();
                if (parentTerms == null) {
                    parentTerms = new HashSet<TermDTO>();
                }
                parentTerms.add(parentDTO);
                termDTO.setParentTerms(parentTerms);
            }

            termDTOMap.put(result[0].toString(), termDTO);
        }

        return termDTOMap;
    }

    @Override
    public int getNumberTermsForOntology(Ontology ontology) {
        String hql = " " +
                " select count(*) from GenericTerm t  " +
                " where t.ontology = :ontology  " +
                " and t.secondary = :secondary " +
                "";
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(hql);
        query.setParameter("ontology", ontology);
        query.setBoolean("secondary", false);
        return Integer.valueOf(query.uniqueResult().toString()).intValue();
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
    public List<String> getAllRelationships(Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        String hql = " select distinct relationship.type from GenericTermRelationship as relationship";
        if (ontology != null) {
            hql += " where   relationship.termOne.ontology = :ontology OR relationship.termTwo.ontology = :ontology";
        }
        Query query = session.createQuery(hql);
        if (ontology != null) {
            query.setParameter("ontology", ontology);
        }
        return (List<String>) query.list();
    }

    /**
     * Retrieve all Relationships.
     *
     * @return list of relationships
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public List<String> getAllRelationships() {
        Session session = HibernateUtil.currentSession();
        String hql = " select distinct relationships.type from GenericTermRelationship as relationships";
        Query query = session.createQuery(hql);
        return (List<String>) query.list();
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
        criteria.setCacheable(true);
        GenericTerm term = (GenericTerm) criteria.uniqueResult();
        if (term == null)
            return null;
        return term;
    }

    public List<GenericTermRelationship> getTermRelationshipsForTerms(List<Term> terms) {
        Set<String> termIds = new HashSet<String>();
        for (Term t : terms) {
            termIds.add(t.getZdbID());
        }

        List<GenericTermRelationship> relatedTerms = new ArrayList<GenericTermRelationship>();

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTermRelationship.class);
        criteria.add(Restrictions.in("termOne.zdbID", termIds));
        criteria.setFetchMode("termTwo", FetchMode.JOIN);
        criteria.setFetchMode("termTwo.definition", FetchMode.JOIN);
        List<GenericTermRelationship> rels = (List<GenericTermRelationship>) criteria.list();
        if (rels != null) {
            for (GenericTermRelationship relationship : rels) {
                RelationshipType type = RelationshipType.getInverseRelationshipByName(relationship.getType());
                if (type != null) {
                    relationship.setRelationshipType(type);
                    relatedTerms.add(relationship);
                }
            }
        }

        Criteria criteriaTwo = session.createCriteria(GenericTermRelationship.class);
        criteriaTwo.add(Restrictions.in("termTwo.zdbID", termIds));
        criteriaTwo.setFetchMode("termOne", FetchMode.JOIN);
        List<GenericTermRelationship> relationshipListTwo = (List<GenericTermRelationship>) criteriaTwo.list();
        if (relationshipListTwo != null) {
            for (GenericTermRelationship relationship : relationshipListTwo) {
                RelationshipType type = RelationshipType.getRelationshipTypeByDbName(relationship.getType());
                relationship.setRelationshipType(type);
                relatedTerms.add(relationship);
            }
        }


        return relatedTerms;

    }

    /**
     * Retrieve all Children terms from a given term.
     * Does not include obsolete terms.
     *
     * @param termID ID
     * @return list of terms
     */
    @SuppressWarnings({"unchecked"})
    public List<? extends Term> getChildren(String termID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTermRelationship.class);
        criteria.add(Restrictions.eq("termOne.zdbID", termID));
        Criteria genericTerm = criteria.createCriteria("termTwo");
        genericTerm.add(Restrictions.eq("obsolete", false));
        List<GenericTermRelationship> relationshipListTwo = (List<GenericTermRelationship>) criteria.list();
        List<Term> terms = new ArrayList<Term>(5);
        if (relationshipListTwo != null) {
            for (GenericTermRelationship relationship : relationshipListTwo) {
                RelationshipType type = RelationshipType.getRelationshipTypeByDbName(relationship.getType());
                relationship.setRelationshipType(type);
                terms.add(relationship.getTermTwo());
            }
        }
        return terms;
    }

    /**
     * Retrieve a term by name and ontology.
     *
     * @param termName term name
     * @param ontology Ontology
     * @return term
     */
    @Override
    public GenericTerm getTermByNameActive(String termName, Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        criteria.add(Restrictions.eq("termName", termName));
        criteria.add(Restrictions.eq("ontology", ontology));
        criteria.add(Restrictions.eq("obsolete", false));
        criteria.add(Restrictions.eq("secondary", false));
        return (GenericTerm) criteria.uniqueResult();
    }

    /**
     * Retrieve a term by name and ontology.
     *
     * @param termName term name
     * @param ontology Ontology
     * @return term
     */
    @Override
    public GenericTerm getTermByName(String termName, Ontology ontology) {
        return getTermByName(termName, ontology.getIndividualOntologies());
    }

    @Override
    public GenericTerm getTermByName(String termName, Collection<Ontology> ontology) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        criteria.add(Restrictions.eq("termName", termName));
        criteria.add(Restrictions.in("ontology", ontology));
        criteria.add(Restrictions.eq("secondary", false));
        criteria.add(Restrictions.eq("obsolete", false));
        return (GenericTerm) criteria.uniqueResult();
    }


    /**
     * Retrieve Term by term zdb ID.
     *
     * @param termZdbID term id
     * @return Generic Term
     */
    public GenericTerm getTermByZdbID(String termZdbID) {
        return (GenericTerm) HibernateUtil.currentSession().get(GenericTerm.class, termZdbID);
    }

    /**
     * Retrieve all subset definition from all ontologies in the database.
     *
     * @return set of subsets
     */
    @Override
    public List<Subset> getAllSubsets() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Subset.class);
        return (List<Subset>) criteria.list();
    }

    /**
     * Retrieve header info for all ontologies.
     *
     * @return list of headers
     */
    @Override
    public List<OntologyMetadata> getAllOntologyMetadata() {
        String hql = "from OntologyMetadata order by order";
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(hql);
        return query.list();
    }

    /**
     * Retrieve meta data for a given ontology identified by name
     *
     * @param name ontology name
     * @return ontology meta data
     */
    @Override
    public OntologyMetadata getOntologyMetadata(String name) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(OntologyMetadata.class);
        criteria.add(Restrictions.eq("name", name));
        return (OntologyMetadata) criteria.uniqueResult();
    }

    @Override
    public int getMaxOntologyOrderNumber() {
        Session session = HibernateUtil.currentSession();
        String hql = "select max(ontology.order) from OntologyMetadata ontology";
        return (Integer) session.createQuery(hql).uniqueResult();
    }

    /**
     * Retrieve a list of phenotypes that have annotations with secondary terms listed.
     *
     * @return list of phenotypes
     */
    @Override
    public List<PhenotypeStatement> getPhenotypesWithSecondaryTerms() {
        Session session = HibernateUtil.currentSession();
        String hql = "select phenotype from PhenotypeStatement phenotype " +
                "where " +
                "((phenotype.entity.superterm.secondary = :isSecondary) or" +
                "(phenotype.entity.subterm.secondary = :isSecondary) or " +
                "(phenotype.relatedEntity.superterm.secondary = :isSecondary) or" +
                "(phenotype.relatedEntity.subterm.secondary = :isSecondary) or " +
                "(phenotype.quality.secondary = :isSecondary )) ";
        Query query = session.createQuery(hql);
        query.setBoolean("isSecondary", true);
        List<PhenotypeStatement> phenotypesOnSuperterms = (List<PhenotypeStatement>) query.list();
        return phenotypesOnSuperterms;
    }

    /**
     * Save a new record in the ONTOLOGY database which keeps track of versions and namespaces.
     * It also auto-generates the order field.
     *
     * @param metaData meta data
     */
    @Override
    public void saveNewDbMetaData(OntologyMetadata metaData) {
        Session session = HibernateUtil.currentSession();
        metaData.setOrder(getMaxOntologyOrderNumber() + 1);
        session.save(metaData);
    }

    @Override
    public boolean isParentChildRelationshipExist(Term parentTerm, Term childTerm) {
        return null != HibernateUtil.currentSession().createCriteria(TransitiveClosure.class)
                .add(Restrictions.eq("root", parentTerm))
                .add(Restrictions.eq("child", childTerm))
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public List<GenericTerm> getParentDirectTerms(GenericTerm goTerm) {
        return getParentTerms(goTerm, 1);
    }

    @Override
    public List<GenericTerm> getParentTerms(GenericTerm goTerm, int distance) {
        String hql = " select t from TransitiveClosure tc join tc.root as t " +
                " where tc.child.id = :goTermId " +
                " and tc.distance = :distance " +
                " ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("goTermId", goTerm.getZdbID());
        if (distance >= 0) {
            query.setInteger("distance", distance);
        }
        return query.list();
    }

    @Override
    public List<GenericTerm> getChildDirectTerms(GenericTerm goTerm) {
        return getChildTerms(goTerm, 1);
    }


    @Override
    public List<GenericTerm> getChildTerms(GenericTerm goTerm, int distance) {
        String hql = " select t from TransitiveClosure tc join tc.child as t " +
                " where tc.root.id = :goTermId ";
        if (distance >= 0) {
            hql += " and tc.distance = :distance ";
        }

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("goTermId", goTerm.getZdbID());
        if (distance >= 0) {
            query.setInteger("distance", distance);
        }
        return query.list();
    }

    public List<GenericTerm> getAllChildTerms(GenericTerm goTerm) {
        return getChildTerms(goTerm, -1);
    }

    @Override
    public List<TransitiveClosure> getChildrenTransitiveClosures(GenericTerm term) {
        String hql = " select tc from TransitiveClosure tc  " +
                " where tc.root.id = :goTermId " +
                " ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("goTermId", term.getZdbID());
        return query.list();
    }

    /**
     * Get the sub ontology for this term.
     * If ROOT is PATO:0001236 then QUALITY_PROCESS   // ZDB-TERM-070117-1237
     * If ROOT is PATO:0001241 then QUALITY_QUALITIES //ZDB-TERM-070117-1242
     * // else return the term's ontology
     *
     * @param term
     * @return
     */
    @Override
    public Ontology getProcessOrPhysicalObjectQualitySubOntologyForTerm(Term term) {

        int value;

        String sqlQualityProcesses = "select count(*) from all_term_contains atc  " +
                "where " +
                "atc.alltermcon_container_zdb_id=:rootQualityTerm " +
                "and  " +
                "atc.alltermcon_contained_zdb_id=:termZdbID ";

        Query query = HibernateUtil.currentSession().createSQLQuery(sqlQualityProcesses)
                .setString("termZdbID", term.getZdbID());


        query.setString("rootQualityTerm", "ZDB-TERM-070117-1237");
        value = Integer.valueOf(query.uniqueResult().toString());
        if (value > 0) {
            return Ontology.QUALITY_PROCESSES;
        }

        query.setString("rootQualityTerm", "ZDB-TERM-070117-1242");
        value = Integer.valueOf(query.uniqueResult().toString());
        if (value > 0) {
            return Ontology.QUALITY_QUALITIES;
        }


        return term.getOntology();
    }

    //    @Override
//    public Map<String, List<TermRelationship>> getTermRelationshipsForOntology(Ontology ontology) {
//
//        Map<String, List<TermRelationship>> map = new HashMap<String, List<TermRelationship>>();
//        List<TermRelationship> tr = new ArrayList<TermRelationship>( );
//        tr.add(new CachedTermRelationship());
//        map.put("asdf", tr);
//
//        HibernateUtil.currentSession().createQuery( " select tr from GenericTermRelationship tr " +
//                "where tr.termOne =   " +
//                "t.term_ontology = 'biological_process' ")
//
//
//        return map;
//    }


    /**
     * Get obo id from relationship
     *
     * @param term
     * @return
     */
    @Override
    public DevelopmentStage getDevelopmentStageFromTerm(Term term) {
        return (DevelopmentStage) HibernateUtil.currentSession().createCriteria(DevelopmentStage.class)
                .add(Restrictions.eq("oboID", term.getOboID()))
                .uniqueResult();
    }

    @Override
    public Collection<TermDTO> getTermDTOsFromOntologyNoRelation(Ontology stage) {
        String sql = " " +
                "select" +
                "        this_.term_zdb_id as term1_43_2_," + // 0
                "        this_.term_name as term2_43_2_," + // 1
                "        this_.term_ont_id as term3_43_2_," +  // 2
                "        this_.term_ontology as term4_43_2_," +  // 3
                "        this_.term_is_obsolete as term5_43_2_," +  // 4
                "        this_.term_comment as term8_43_2_," +      // 5
                "        this_.term_definition as term9_43_2_" +   // 6
                "    from" +
                "        TERM this_ " +
                "    where" +
                "        this_.term_ontology= :ontology " +
                "        and this_.term_is_secondary= :secondary  " +
                "  ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setBoolean("secondary", false);
        query.setString("ontology", stage.getOntologyName());
//        query.setMaxResults(5);

        List<Object[]> queryList = query.list();

        Set<TermDTO> termDTOMap = new TreeSet<TermDTO>();
        // pass one get the results into the map
        for (Object[] result : queryList) {
            // denormalized over child, parent, and alias
            TermDTO termDTO = new TermDTO();
            termDTO.setZdbID(result[0].toString());
            termDTO.setName(result[1].toString());
            termDTO.setOboID(result[2].toString());
            termDTO.setOntology(DTOConversionService.convertToOntologyDTO(Ontology.getOntology(result[3].toString())));
            termDTO.setObsolete(Boolean.valueOf(result[4].toString()));
            if (result[5] != null) {
                termDTO.setComment(result[5].toString());
            }
            if (result[6] != null) {
                termDTO.setDefinition(result[6].toString());
            }

            termDTOMap.add(termDTO);
        }

        return termDTOMap;
    }

    @Override
    public Set<String> getAllChildZdbIDs(String zdbID) {

        String sql = " select atc.alltermcon_contained_zdb_id from all_term_contains atc " +
                "join term t on atc.alltermcon_contained_zdb_id=t.term_zdb_id " +
                "where " +
                "atc.alltermcon_container_zdb_id=:rootZdbID " +
                "and " +
                "t.term_is_secondary='f' ";

        List<Object> results = HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("rootZdbID", zdbID)
                .list();

        Set<String> returnZdbIDs = new HashSet<String>();

        for (Object result : results) {
            returnZdbIDs.add(result.toString());
        }


        return returnZdbIDs;

    }

    /**
     * Retrieve all term ids.
     * If firstNIds > 0 return only the first N.
     * If firstNIds < 0 return null
     *
     * @param firstNIds number of records
     * @return list of ids
     */
    @Override
    public List<String> getAllTerms(int firstNIds) {
        if (firstNIds < 0)
            return null;
        Session session = HibernateUtil.currentSession();
        String hql = "select id from GenericTerm " +
                " where secondary = :secondary order by id";
        Query query = session.createQuery(hql);
        if (firstNIds > 0)
            query.setMaxResults(firstNIds);
        query.setBoolean("secondary", false);
        return query.list();
    }

    /**
     * Retrieves firstN terms of each ontology.
     * If firstN = 0 retrieve all terms
     * If firstN < 0 return null
     * <p/>
     * Note: No terms marked as secondary are retrieved (those terms are removed
     * from the obo file and only function as an alias and a place holder for a
     * previously used oboID so it does not get re-used.
     *
     * @param firstNIds number of markers to be returned
     * @return list of terms
     */
    @Override
    public List<String> getFirstNTermsPerOntology(int firstNIds) {
        if (firstNIds < 0)
            return null;
        Session session = HibernateUtil.currentSession();
        List<Ontology> ontologies = getDistinctOntologies();
        if (ontologies == null)
            return null;

        // currently 6 different ontologies used.
        List<String> allTerms = new ArrayList<String>(6 * firstNIds);
        for (Ontology ontology : ontologies) {
            if (ontology.shouldNotBeIndexed())
                continue;
            String hql = "select oboID from GenericTerm where ontology = :ontology " +
                    " AND secondary = :secondary " +
                    "order by oboID";
            Query query = session.createQuery(hql);
            query.setParameter("ontology", ontology);
            query.setBoolean("secondary", false);
            if (firstNIds > 0)
                query.setMaxResults(firstNIds);
            allTerms.addAll(query.list());
        }
        return allTerms;
    }

    /**
     * Retrieve all terms that replace an obsoleted term.
     *
     * @param obsoletedTerm obsoleted Term
     * @return list of terms
     */
    @Override
    public List<ReplacementTerm> getReplacedByTerms(GenericTerm obsoletedTerm) {
        if (!obsoletedTerm.isObsolete())
            return null;
        Session session = HibernateUtil.currentSession();
        String hql = " from ReplacementTerm " +
                " where obsoletedTerm = :obsoletedTerm";
        Query query = session.createQuery(hql);
        query.setParameter("obsoletedTerm", obsoletedTerm);
        return query.list();
    }

    /**
     * Retrieve all terms that can be considered for a given obsoleted term.
     *
     * @param obsoletedTerm obsoleted Term
     * @return list of terms
     */
    @Override
    public List<ConsiderTerm> getConsiderTerms(GenericTerm obsoletedTerm) {
        if (!obsoletedTerm.isObsolete())
            return null;
        Session session = HibernateUtil.currentSession();
        String hql = " from ConsiderTerm " +
                " where obsoletedTerm = :obsoletedTerm";
        Query query = session.createQuery(hql);
        query.setParameter("obsoletedTerm", obsoletedTerm);
        return query.list();
    }

    /**
     * Retrieve phenotypes with secondary terms annotated.
     *
     * @return phenotypes
     */
    @Override
    public List<PhenotypeStatement> getPhenotypesOnSecondaryTerms() {
        Session session = HibernateUtil.currentSession();
        List<PhenotypeStatement> allPhenotypes = new ArrayList<PhenotypeStatement>();

        String hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.quality is not null AND phenotype.quality.secondary = :secondary";
        Query query = session.createQuery(hql);
        query.setBoolean("secondary", true);

        allPhenotypes.addAll((List<PhenotypeStatement>) query.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.entity.superterm is not null AND phenotype.entity.superterm.secondary = :secondary";
        Query queryEntitySuper = session.createQuery(hql);
        queryEntitySuper.setBoolean("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.entity.subterm is not null AND phenotype.entity.subterm.secondary = :secondary";
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setBoolean("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySub.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.relatedEntity.superterm is not null AND phenotype.relatedEntity.superterm.secondary = :secondary";
        Query queryRelatedEntitySuper = session.createQuery(hql);
        queryRelatedEntitySuper.setBoolean("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.relatedEntity.subterm is not null AND phenotype.relatedEntity.subterm.secondary = :secondary";
        Query queryRelatedEntitySub = session.createQuery(hql);
        queryRelatedEntitySub.setBoolean("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySub.list());

        return allPhenotypes;
    }

    /**
     * Retrieve expressions with secondary terms annotated.
     *
     * @return expressions
     */
    @Override
    public List<ExpressionResult2> getExpressionsOnSecondaryTerms() {
        Session session = HibernateUtil.currentSession();
        List<ExpressionResult2> allExpressions = new ArrayList<>();

        String hql = "from ExpressionResult2 " +
                "     where superTerm is not null AND superTerm.secondary = :secondary";
        Query query = session.createQuery(hql);
        query.setBoolean("secondary", true);

        allExpressions.addAll((List<ExpressionResult2>) query.list());

        hql = "from ExpressionResult2 " +
                "     where subTerm is not null AND subTerm.secondary = :secondary";
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setBoolean("secondary", true);
        allExpressions.addAll((List<ExpressionResult2>) queryEntitySub.list());

        return allExpressions;
    }

    /**
     * Retrieve go evidences with secondary terms annotated.
     *
     * @return expressions
     */
    @Override
    public List<MarkerGoTermEvidence> getGoEvidenceOnSecondaryTerms() {
        Session session = HibernateUtil.currentSession();
        String hql = "select goEvidence from MarkerGoTermEvidence goEvidence " +
                "     where goEvidence.goTerm.secondary = :secondary";
        Query query = session.createQuery(hql);
        query.setBoolean("secondary", true);

        return (List<MarkerGoTermEvidence>) query.list();
    }

    /**
     * Retrieves a list of term relationships for which the child's start stage is not compliant
     * with the terms parent term start stage.
     * (a child's stage needs to be within the stage range of the parent term).
     *
     * @return list of terms
     */
    @Override
    public List<GenericTermRelationship> getTermsWithInvalidStartStageRange() {
        Session session = HibernateUtil.currentSession();
        String hql = "select relationship from GenericTermRelationship relationship " +
                " where relationship.termOne.termStage.start.hoursStart > relationship.termTwo.termStage.start.hoursStart AND " +
                "       relationship.termTwo.termStage.start.name != :unknown AND" +
                "       relationship.type in (:typeList)";
        Query query = session.createQuery(hql);
        query.setString("unknown", DevelopmentStage.UNKNOWN);
        query.setParameterList("typeList", new String[]{"is_a", "part_of", "is a", "part of", "develops_from", "develops from"});
        return query.list();
    }

    /**
     * Retrieves a list of term relationships for which the child's end stage is not compliant
     * with the terms parent term end stage, i.e. child's end stage is after the parent's end stage
     * (a child's stage needs to be within the stage range of the parent term).
     * The termOne is the parent term while termTwo is the child term on the relationshipTerm object.
     * The child's end stage needs to be at or before the parent's end stage.
     *
     * @return list of term Relationships
     */
    @Override
    public List<GenericTermRelationship> getTermsWithInvalidEndStageRange() {
        Session session = HibernateUtil.currentSession();
        String hql = "select relationship from GenericTermRelationship relationship " +
                " where relationship.termOne.termStage.end.hoursEnd < relationship.termTwo.termStage.end.hoursEnd AND " +
                "       relationship.termTwo.termStage.end.name != :unknown AND " +
                "       relationship.type in (:typeList)";
        Query query = session.createQuery(hql);
        query.setString("unknown", DevelopmentStage.UNKNOWN);
        query.setParameterList("typeList", new String[]{"is_a", "part_of", "is a", "part of"});
        return query.list();
    }

    /**
     * Retrieves a list of term relationships of type develops_from
     * for which the start stage of the child is after the end stage of the parent term, i.e. there is no
     * stage overlap between the two terms (develops into requires a stage overlap).
     * The termOne is the parent term while termTwo is the child term on the relationshipTerm object.
     *
     * @return list of term Relationships
     */
    @Override
    public List<GenericTermRelationship> getTermsWithInvalidStartEndStageRangeForDevelopsFrom() {
        Session session = HibernateUtil.currentSession();
        String hql = "select relationship from GenericTermRelationship relationship " +
                " where relationship.termOne.termStage.end.hoursEnd < relationship.termTwo.termStage.start.hoursStart AND " +
                "       relationship.termTwo.termStage.end.name != :unknown AND " +
                " relationship.type = :developsFrom";
        Query query = session.createQuery(hql);
        query.setString("unknown", DevelopmentStage.UNKNOWN);
        query.setString("developsFrom", RelationshipSorting.DEVELOPS_FROM);
        return query.list();
    }

    /**
     * Retrieves all expression result objects that define stage ranges in violation of the stage ranges given
     * by the used term stages. Each term has a stage range in which it is defined, thus, the expression result
     * stage range needs to fit into the smallest window of the used terms.
     *
     * @return list of expression results records.
     */
    @Override
    public List<ExpressionResult> getExpressionResultsViolateStageRanges() {
        Session session = HibernateUtil.currentSession();
        String hql = "select result from ExpressionResult result " +
                " where ( result.startStage.hoursStart > result.entity.superterm.end.hoursStart  " +
                " AND result.entity.superterm.end.name != :excludedStageName " +
                " AND result.startStage.name != :excludedStageName ) OR " +
                " (result.endStage.hoursStart < result.entity.superterm.start.hoursStart " +
                " AND result.entity.superterm.start.name != :excludedStageName " +
                " AND result.endStage.name != :excludedStageName )  ";
        Query query = session.createQuery(hql);
        query.setParameter("excludedStageName", DevelopmentStage.UNKNOWN);
        return query.list();
    }

    /**
     * Retrieve a generic term by one or more of its values.
     *
     * @param superTerm
     * @return
     */
    @Override
    public GenericTerm getTermByExample(GenericTerm superTerm) {
        Session session = HibernateUtil.currentSession();
        return (GenericTerm) session.createCriteria(GenericTerm.class).add(Example.create(superTerm)).uniqueResult();
    }

    /**
     * Retrieve a stage by one or more of its values.
     *
     * @param stage stage
     * @return
     */
    @Override
    public DevelopmentStage getStageByExample(DevelopmentStage stage) {
        Session session = HibernateUtil.currentSession();
        return (DevelopmentStage) session.createCriteria(DevelopmentStage.class).add(Example.create(stage).excludeZeroes()).uniqueResult();
    }

    /**
     * Retrieve all new relationships that were generated on a given day.
     *
     * @param date     date
     * @param ontology Ontology
     */
    @Override
    public List<GenericTermRelationship> getNewRelationships(Calendar date, Ontology ontology) {
        String zdbDate = InfrastructureService.getZdbDate(date);
        Session session = HibernateUtil.currentSession();
        String hql = "select relationship from GenericTermRelationship relationship " +
                " where relationship.zdbId like :termRelationshipLike";
        Query query = session.createQuery(hql);
        query.setParameter("termRelationshipLike", "ZDB-TERMREL-" + zdbDate + "%");
        return query.list();

    }

    /**
     * Retrieve all new relationships that were generated today.
     *
     * @param ontology Ontology
     */
    @Override
    public List<GenericTermRelationship> getNewRelationships(Ontology ontology) {
        return getNewRelationships(Calendar.getInstance(), ontology);
    }

    /**
     * Retrieve a Term Relationship by ID
     *
     * @param id relationship id
     * @return term relationship
     */
    @Override
    public GenericTermRelationship getRelationshipById(String id) {
        return (GenericTermRelationship) HibernateUtil.currentSession().get(GenericTermRelationship.class, id);
    }

    /**
     * Retrieve Term Relationships that use merged terms, i.e. terms that are not used any longer.
     *
     * @return list of term relationships
     */
    @Override
    public List<GenericTermRelationship> getTermRelationshipsWithMergedTerms() {
        Session session = HibernateUtil.currentSession();
        String hql = "select relationship from GenericTermRelationship relationship" +
                " where relationship.termOne.secondary = :secondary OR relationship.termTwo.secondary = :secondary ";
        Query query = session.createQuery(hql);
        query.setBoolean("secondary", true);
        return (List<GenericTermRelationship>) query.list();
    }

    /**
     * Retrieve list of distinct merged terms used in relationships.
     *
     * @return list fo terms
     */
    @Override
    public List<GenericTerm> getMergedTermsInTermRelationships() {
        Session session = HibernateUtil.currentSession();
        String hql = "select term from GenericTerm term" +
                " where term.secondary = :secondary " +
                "       AND (term.childTermRelationships is not empty " +
                "         OR term.childTermRelationships is not empty)";
        Query query = session.createQuery(hql);
        query.setBoolean("secondary", true);
        return (List<GenericTerm>) query.list();
    }

    /**
     * Retrieve list of terms that are not obsoleted and not merged that do not have a defined relationship.
     *
     * @return list of terms
     */
    @Override
    public List<GenericTerm> getActiveTermsWithoutRelationships() {
        Session session = HibernateUtil.currentSession();
        String hql = "select term from GenericTerm term" +
                " where term.secondary = :secondary " +
                "       AND term.childTermRelationships is empty " +
                "       AND term.parentTermRelationships is empty " +
                "       AND term.secondary = :secondary " +
                "       AND term.obsolete = :obsolete ";
        Query query = session.createQuery(hql);
        query.setBoolean("secondary", false);
        query.setBoolean("obsolete", false);
        return (List<GenericTerm>) query.list();
    }

    private List<Ontology> getDistinctOntologies() {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct ontology from GenericTerm " +
                " where termName != :partOf order by ontology";
        Query query = session.createQuery(hql);
        query.setParameter("partOf", "part_of");
        return query.list();
    }
    public List<GenericTerm> getTermsInSubset(String subsetName){
        Session session = HibernateUtil.currentSession();
        String sql = "select distinct term_ont_id " +
                            "from term, term_subset, ontology_subset" +
                            "where term_zdb_id = termsub_term_zdb_id" +
                               "and osubset_pk_id = termsub_subset_id" +
                               "and osubset_subset_name in ( :subsetN )";

        Query query = session.createSQLQuery(sql);
        query.setString("subsetN", subsetName);
        return query.list();
    }
}

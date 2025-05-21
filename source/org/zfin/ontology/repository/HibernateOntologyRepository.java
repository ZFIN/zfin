package org.zfin.ontology.repository;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.RelationshipSorting;
import org.zfin.datatransfer.ctd.MeshChebiMapping;
import org.zfin.datatransfer.go.EcoGoEvidenceCodeMapping;
import org.zfin.expression.ExpressionResult2;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.*;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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
    public List<GenericTerm> getAllTermsFromOntology(Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        Query<GenericTerm> query = session.createQuery("""
            select term from GenericTerm as term
            where term.ontology = :ontology
            and term.secondary = false
            """, GenericTerm.class);
        query.setParameter("ontology", ontology);
        return query.list();
    }

    /**
     * With a given term, return all the direct relationships (just the types of relationships--eg. "is_a", etc.).
     * Each type of relationship is the key to the returned map, the value is the number of relationships of that type.
     * The forward argument is used to specify whether to return the relationships where the term is termOne vs termTwo
     *
     * @param term The term that has relationships
     * @param forward true if the term should be termOne of the relationship, false if termTwo
     * @return Map of relationship types with counts per type
     */
    @Override
    public Map<String, Long> getDirectlyRelatedRelationshipTypes(GenericTerm term, boolean forward) {
        Session session = HibernateUtil.currentSession();
        String hql = String.format("""
            SELECT r.type, COUNT(r.id)
            FROM GenericTermRelationship r
            WHERE r.%s = :term
            GROUP BY r.type
            ORDER BY r.type
            """, forward ? "termOne" : "termTwo" );
        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        query.setParameter("term", term);
        List<Tuple> results = query.list();

        //convert to Map
        Map<String, Long> typeCountMap = new HashMap<>();
        for (Tuple result : results) {
            String type = (String) result.get(0);
            Long count = (Long) result.get(1);
            typeCountMap.put(type, count);
        }

        return typeCountMap;
    }

    /**
     * Get all the direct relationships for a term
     * @param term              the term
     * @param relationshipType  the type of relationship (eg "is_a", etc.)
     * @param forward           if true, the term is the subject of the relationship, otherwise it's the object
     * @param offset            if we are paging into the results, how many results deep? (can be null to omit)
     * @param limit             the number of results to return
     * @return  All the relationships that fit the criteria
     */
    @Override
    public List<GenericTermRelationship> getDirectlyRelatedRelationshipsByType(GenericTerm term, String relationshipType, boolean forward, Integer offset, Integer limit) {
        Session session = HibernateUtil.currentSession();
        String hql = String.format("""
                                    SELECT r FROM GenericTermRelationship r
                                    WHERE r.type = :relType
                                    AND r.%s = :term
                                    ORDER BY r.type
                                    """, forward ? "termOne" : "termTwo" );

        Query<GenericTermRelationship> query = session.createQuery(hql, GenericTermRelationship.class);
        query.setParameter("relType", relationshipType);
        query.setParameter("term", term);
        if (offset != null) {
            query.setFirstResult(offset);
        }
        if (limit != null) {
            query.setMaxResults(limit);
        }
        return query.list();
    }

    /**
     * Get the counts for all the direct relationships for a term
     * @param term              the term
     * @param relationshipType  the type of relationship (eg "is_a", etc.)
     * @param forward           if true, the term is the subject of the relationship, otherwise it's the object
     * @return  The number of relationships that fit the criteria
     */
    @Override
    public Integer getDirectlyRelatedRelationshipsCountByType(GenericTerm term, String relationshipType, boolean forward) {
        Session session = HibernateUtil.currentSession();
        String hql = String.format("""
                                    SELECT count(r) FROM GenericTermRelationship r
                                    WHERE r.type = :relType
                                    AND r.%s = :term
                                    """, forward ? "termOne" : "termTwo" );

        Query<Number> query = session.createQuery(hql, Number.class);
        query.setParameter("relType", relationshipType);
        query.setParameter("term", term);
        return query.getSingleResult().intValue();
    }

    @Override
    public EcoGoEvidenceCodeMapping getEcoEvidenceCode(GenericTerm term) {
        Session session = HibernateUtil.currentSession();
        Query<EcoGoEvidenceCodeMapping> criteria = session.createQuery("from EcoGoEvidenceCodeMapping where ecoTerm = :ecoTerm ", EcoGoEvidenceCodeMapping.class);
        criteria.setParameter("ecoTerm", term);
        return criteria.uniqueResult();
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
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("secondary", false);
        query.setParameter("ontology", ontology.getOntologyName());
//        query.setMaxResults(5);

        List<Object[]> queryList = query.list();

        Map<String, TermDTO> termDTOMap = new HashMap<>();
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
                    subsets.contains(Subset.GO_CHECK_DO_NOT_USE_FOR_MANUAL_ANNOTATIONS)) {
                    termDTO.setDoNotAnnotateWith(true);
                } else {
                    termDTO.setDoNotAnnotateWith(false);
                }
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
        query.setParameter("secondary", false);
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
    public GenericTerm getTermByOboID(String termID) {

        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GenericTerm> cr = cb.createQuery(GenericTerm.class);
        Root<GenericTerm> root = cr.from(GenericTerm.class);
        cr.select(root).where(cb.equal(root.get("oboID"), termID));
        return session.createQuery(cr).uniqueResult();
    }

    private static List<GenericTerm> getTermsInOboIDList(List<String> oboIDs, boolean preserveOrder) {
        Session session = HibernateUtil.currentSession();
        Query<GenericTerm> query = session.createQuery("from GenericTerm where oboID in (:oboID)", GenericTerm.class);
        query.setParameterList("oboID", oboIDs);
        query.setCacheable(true);
        List<GenericTerm> terms = query.list();
        if (preserveOrder) {
            terms.sort(Comparator.comparing(term -> oboIDs.indexOf(term.getOboID())));
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
        String hql = """
            from GenericTerm
            where termName = :termName
            and ontology = :ontology
            and obsolete = false
            and secondary = false
            """;
        Query<GenericTerm> query = session.createQuery(hql, GenericTerm.class);
        query.setParameter("termName", termName);
        query.setParameter("ontology", ontology);
        return query.uniqueResult();
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
        String hql = """
            from GenericTerm where
                         termName = :termName and
                         secondary = false and
                         obsolete = false and
                         cast (ontology as string) in (:ontoList)
            """;
        org.hibernate.query.Query<GenericTerm> query = session.createQuery(hql, GenericTerm.class);
        query.setParameter("termName", termName);

        //compare based on the db ontology name if it exists, otherwise use the ontology name
        query.setParameterList("ontoList", ontology.stream().map(
            o -> o.getDbOntologyName() != null ? o.getDbOntologyName() : o.getOntologyName()
        ).toList());
        return query.uniqueResult();
    }

    /**
     * Retrieve Term by term zdb ID.
     *
     * @param termZdbID term id
     * @return Generic Term
     */
    public GenericTerm getTermByZdbID(String termZdbID) {
        return HibernateUtil.currentSession().get(GenericTerm.class, termZdbID);
    }

    /**
     * Retrieve all subset definition from all ontologies in the database.
     */
    @Override
    public List<Subset> getAllSubsets() {
        Session session = HibernateUtil.currentSession();
        return session.createQuery("from Subset", Subset.class).list();
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
        Query<OntologyMetadata> query = session.createQuery("from OntologyMetadata where name = :name", OntologyMetadata.class);
        query.setParameter("name", name);
        return query.uniqueResult();
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
        Query<PhenotypeStatement> query = session.createQuery(hql, PhenotypeStatement.class);
        query.setParameter("isSecondary", true);
        List<PhenotypeStatement> phenotypesOnSuperterms = query.list();
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
        return null != HibernateUtil.currentSession().createQuery("""
                from TransitiveClosure where root = :root and child = :child
                """, TransitiveClosure.class)
            .setParameter("root", parentTerm)
            .setParameter("child", childTerm)
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
        query.setParameter("goTermId", goTerm.getZdbID());
        if (distance >= 0) {
            query.setParameter("distance", distance);
        }
        return query.list();
    }

    @Override
    public List<GenericTerm> getChildDirectTerms(GenericTerm goTerm) {
        return getChildTerms(goTerm, 1);
    }


    @Override
    public List<GenericTerm> getChildTerms(GenericTerm goTerm, int distance) {
        if (goTerm == null) {
            return null;
        }
        String hql = " select t from TransitiveClosure tc join tc.child as t " +
                     " where tc.root.id = :goTermId ";
        if (distance >= 0) {
            hql += " and tc.distance = :distance ";
        }

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("goTermId", goTerm.getZdbID());
        if (distance >= 0) {
            query.setParameter("distance", distance);
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
        query.setParameter("goTermId", term.getZdbID());
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

        Query query = HibernateUtil.currentSession().createNativeQuery(sqlQualityProcesses)
            .setParameter("termZdbID", term.getZdbID());


        query.setParameter("rootQualityTerm", "ZDB-TERM-070117-1237");
        value = Integer.valueOf(query.uniqueResult().toString());
        if (value > 0) {
            return Ontology.QUALITY_PROCESSES;
        }

        query.setParameter("rootQualityTerm", "ZDB-TERM-070117-1242");
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
        return HibernateUtil.currentSession().createQuery("from DevelopmentStage where oboID = :oboID", DevelopmentStage.class)
            .setParameter("oboID", term.getOboID())
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
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("secondary", false);
        query.setParameter("ontology", stage.getOntologyName());
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

        List<Object> results = HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("rootZdbID", zdbID)
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
        if (firstNIds < 0) {
            return null;
        }
        Session session = HibernateUtil.currentSession();
        String hql = "select id from GenericTerm " +
                     " where secondary = :secondary order by id";
        Query query = session.createQuery(hql);
        if (firstNIds > 0) {
            query.setMaxResults(firstNIds);
        }
        query.setParameter("secondary", false);
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
        if (firstNIds < 0) {
            return null;
        }
        Session session = HibernateUtil.currentSession();
        List<Ontology> ontologies = getDistinctOntologies();
        if (ontologies == null) {
            return null;
        }

        // currently 6 different ontologies used.
        List<String> allTerms = new ArrayList<String>(6 * firstNIds);
        for (Ontology ontology : ontologies) {
            if (ontology.shouldNotBeIndexed()) {
                continue;
            }
            String hql = "select oboID from GenericTerm where ontology = :ontology " +
                         " AND secondary = :secondary " +
                         "order by oboID";
            Query query = session.createQuery(hql);
            query.setParameter("ontology", ontology);
            query.setParameter("secondary", false);
            if (firstNIds > 0) {
                query.setMaxResults(firstNIds);
            }
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
        if (!obsoletedTerm.isObsolete()) {
            return null;
        }
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
        if (!obsoletedTerm.isObsolete()) {
            return null;
        }
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
        query.setParameter("secondary", true);

        allPhenotypes.addAll((List<PhenotypeStatement>) query.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
              "     where phenotype.entity.superterm is not null AND phenotype.entity.superterm.secondary = :secondary";
        Query queryEntitySuper = session.createQuery(hql);
        queryEntitySuper.setParameter("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
              "     where phenotype.entity.subterm is not null AND phenotype.entity.subterm.secondary = :secondary";
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setParameter("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySub.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
              "     where phenotype.relatedEntity.superterm is not null AND phenotype.relatedEntity.superterm.secondary = :secondary";
        Query queryRelatedEntitySuper = session.createQuery(hql);
        queryRelatedEntitySuper.setParameter("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
              "     where phenotype.relatedEntity.subterm is not null AND phenotype.relatedEntity.subterm.secondary = :secondary";
        Query queryRelatedEntitySub = session.createQuery(hql);
        queryRelatedEntitySub.setParameter("secondary", true);
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
        query.setParameter("secondary", true);

        allExpressions.addAll((List<ExpressionResult2>) query.list());

        hql = "from ExpressionResult2 " +
              "     where subTerm is not null AND subTerm.secondary = :secondary";
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setParameter("secondary", true);
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
        query.setParameter("secondary", true);

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
                     "     JOIN relationship.termOne.termStage termStageOne " +
                     "     JOIN relationship.termTwo.termStage termStageTwo " +
                     " where termStageOne.start.hoursStart > termStageTwo.start.hoursStart AND " +
                     "       termStageTwo.start.name != :unknown AND" +
                     "       relationship.type in (:typeList)";
        Query query = session.createQuery(hql);
        query.setParameter("unknown", DevelopmentStage.UNKNOWN);
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
                     "     JOIN relationship.termOne.termStage termStageOne " +
                     "     JOIN relationship.termTwo.termStage termStageTwo " +
                     " where termStageOne.end.hoursEnd < termStageTwo.end.hoursEnd AND " +
                     "       termStageTwo.end.name != :unknown AND " +
                     "       relationship.type in (:typeList)";
        Query query = session.createQuery(hql);
        query.setParameter("unknown", DevelopmentStage.UNKNOWN);
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
                     "     JOIN relationship.termOne.termStage termStageOne " +
                     "     JOIN relationship.termTwo.termStage termStageTwo " +
                     " where termStageOne.end.hoursEnd < termStageTwo.start.hoursStart AND " +
                     "       termStageTwo.end.name != :unknown AND " +
                     " relationship.type = :developsFrom";
        Query query = session.createQuery(hql);
        query.setParameter("unknown", DevelopmentStage.UNKNOWN);
        query.setParameter("developsFrom", RelationshipSorting.DEVELOPS_FROM);
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
    public List<ExpressionResult2> getExpressionResultsViolateStageRanges() {
        Session session = HibernateUtil.currentSession();
        String sql = "SELECT xpatres_pk_id " +
                     "FROM   expression_result2 " +
                     "   LEFT OUTER JOIN term sub" +
                     "      ON xpatres_subterm_zdb_id = sub.term_zdb_id" +
                     "   JOIN expression_figure_stage" +
                     "      ON xpatres_efs_id = efs_pk_id" +
                     "   JOIN figure " +
                     "      ON efs_fig_zdb_id = fig_zdb_id" +
                     "   JOIN term super" +
                     "      ON xpatres_superterm_zdb_id = super.term_zdb_id" +
                     "   JOIN stage s1" +
                     "      ON efs_start_stg_zdb_id = s1.stg_zdb_id" +
                     "   JOIN stage s2" +
                     "      ON efs_end_stg_zdb_id = s2.stg_zdb_id" +
                     "   JOIN term_stage" +
                     "      ON super.term_zdb_id = ts_term_zdb_id" +
                     "   JOIN stage s3" +
                     "      ON ts_start_stg_zdb_id = s3.stg_zdb_id" +
                     "   JOIN stage s4" +
                     "      ON ts_end_stg_zdb_id = s4.stg_zdb_id " +
                     " WHERE  Aoterm_overlaps_stg_window(xpatres_superterm_zdb_id, efs_start_stg_zdb_id, efs_end_stg_zdb_id) = 'f' ";
        Query query = session.createNativeQuery(sql);
        //query.setParameter("excludedStageName", DevelopmentStage.UNKNOWN);
        final List<BigInteger> list = query.list();
        if (CollectionUtils.isEmpty(list))
            return new ArrayList<>();
        String hql = "from ExpressionResult2 where ID in (:IDs) ";
        query = session.createQuery(hql);
        query.setParameterList("IDs", list.stream().map(BigInteger::longValue).collect(Collectors.toSet()));
        List<ExpressionResult2> results = query.list();
        return results;
    }

    /**
     * Retrieve a stage by one or more of its values.
     *
     * @param stage stage
     */
    @Override
    public DevelopmentStage getStageByExample(DevelopmentStage stage) {
        Session session = HibernateUtil.currentSession();
        return session.createQuery("from DevelopmentStage where abbreviation = :abbreviation", DevelopmentStage.class)
            .setParameter("abbreviation", stage.getAbbreviation()).uniqueResult();
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
        query.setParameter("secondary", true);
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
        query.setParameter("secondary", true);
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
        query.setParameter("secondary", false);
        query.setParameter("obsolete", false);
        return (List<GenericTerm>) query.list();
    }

    @Override
    public List<GenericTerm> getObsoleteAndSecondaryTerms() {
        Session session = HibernateUtil.currentSession();
        String hql = "from GenericTerm where secondary = true OR obsolete = true";
        Query query = session.createQuery(hql);
        return (List<GenericTerm>) query.list();
    }

    @Override
    public List<GenericTerm> getObsoleteAndSecondaryTerms(Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        String hql = "from GenericTerm where (secondary = true OR obsolete = true) and ontology = :ontology";
        Query query = session.createQuery(hql);
        query.setParameter("ontology", ontology);
        return (List<GenericTerm>) query.list();
    }

    @Override
    public List<GenericTerm> getObsoleteAndSecondaryTermsByOntologies(Ontology... ontologies) {
        //TODO: We should be able to do the filtering in the query, but it fails for some reason
        //      it would be good to figure out why and uncomment the lines below.
        //      The same bug could be affecting other Repository methods.
        Session session = HibernateUtil.currentSession();
        String hql = "from GenericTerm where (secondary = true OR obsolete = true)";
//        hql += " and ontology in (:ontologies)";
        Query query = session.createQuery(hql);
//        query.setParameter("ontologies", ontologies);
        List<GenericTerm> results = (List<GenericTerm>) query.list();
        return results.stream().filter(term -> Arrays.asList(ontologies).contains(term.getOntology())).toList();
    }


    private List<Ontology> getDistinctOntologies() {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct ontology from GenericTerm " +
                     " where termName != :partOf order by ontology";
        Query query = session.createQuery(hql);
        query.setParameter("partOf", "part_of");
        return query.list();
    }

    public List<GenericTerm> getTermsInSubset(String subsetName) {
        Session session = HibernateUtil.currentSession();
        String hql = "select subset.terms " +
                     "from Subset as subset " +
                     "where subset.internalName = :subsetName ";

        Query query = session.createQuery(hql);
        query.setParameter("subsetName", subsetName);
        return query.list();
    }

    @Override
    public HumanGeneDetail getHumanGeneDetailById(String id) {
        Session session = HibernateUtil.currentSession();
        return (HumanGeneDetail) session.get(HumanGeneDetail.class, id);
    }

    @Override
    public List<GenericTerm> getZfaRibbonTerms() {
        return getTermsInOboIDList(getZfaRibbonTermIDs(), true);
    }

    @Override
    public List<String> getZfaRibbonTermIDs() {
        return List.of(
            "ZFA:0000010", //cardiovascular system
            "ZFA:0000339", //digestive system
            "ZFA:0001158", //endocrine system
            "ZFA:0001159", //immune system
            "ZFA:0000036", //liver and biliary system
            "ZFA:0000548", //musculature system
            "ZFA:0000396", //nervous system
            "ZFA:0000163", //renal system
            "ZFA:0000632", //reproductive system
            "ZFA:0000272", //respiratory system
            "ZFA:0000282", //sensory system
            "ZFA:0001127", //visual system
            "ZFA:0000108", //fin
            "ZFA:0000368", //integument
            "ZFA:0001135", //neural tube
            "ZFA:0001122", //primary germ layer
            "ZFA:0000155"  //somite
        );
    }

    @Override
    public List<GenericTerm> getExpressionRibbonCellularComponentTerms() {
        return getTermsInOboIDList(List.of(
            "GO:0030054", // cell junction
            "GO:0042995", // cell projection
            "GO:0005737", // cytoplasm
            "GO:0016020", // membrane
            "GO:0005739", // mitochondrion
            "GO:0005634", // nucleus
            "GO:0043226", // organelle
            "GO:0032991", // protein-containing complex
            "GO:0045202"  // synapse
        ), true);
    }

    @Override
    public List<GenericTerm> getPhenotypeRibbonMolecularFunctionTerms() {
        return getTermsInOboIDList(List.of(
            "GO:0005488", // binding
            "GO:0003824", // catalytic activity
            "GO:0038023", // signaling receptor activity
            "GO:0005215", // transporter activity
            "GO:0005198", // structural molecule activity"
            "GO:0140110"  // transcription regulator activity
        ), true);
    }

    @Override
    public List<GenericTerm> getPhenotypeRibbonBiologicalProcessTerms() {
        return getTermsInOboIDList(List.of(
            "GO:0048856", // anatomical structure development
            "GO:0030154", // cell differentiation
            "GO:0007399", // nervous system development
            "GO:0072359", // circulatory system development
            "GO:0048880", // sensory system development
            "GO:0023052", // signaling
            "GO:0007389"  // pattern specification process
        ), true);
    }

    @Override
    public List<GenericTerm> getPhenotypeRibbonCellularComponentTerms() {
        return getTermsInOboIDList(List.of(
            "GO:0016020", // membrane
            "GO:0045202", // synapse
            "GO:0030054", // cell junction
            "GO:0042995", // cell projection
            "GO:0031982", // vesicle
            "GO:0005829", // cytosol
            "GO:0005739", // mitochondrion
            "GO:0005634", // nucleus
            "GO:0005694", // chromosome
            "GO:0032991"  // protein-containing complex
        ), true);
    }

    @Override
    public List<GenericTerm> getGORibbonMolecularFunctionTerms() {
        return filterTermsByOntology(getTermsInSubset("goslim_agr"), Ontology.GO_MF);
    }

    @Override
    public List<GenericTerm> getGORibbonBiologicalProcessTerms() {
        return filterTermsByOntology(getTermsInSubset("goslim_agr"), Ontology.GO_BP);
    }

    @Override
    public List<GenericTerm> getGORibbonCellularComponentTerms() {
        return filterTermsByOntology(getTermsInSubset("goslim_agr"), Ontology.GO_CC);
    }

    @Override
    public boolean termExists(String oboID) {
        return getTermByOboID(oboID) != null;
    }

    @Override
    public GenericTerm getTermByZdbIDOrOboId(String termID) {
        GenericTerm term = getTermByZdbID(termID);
        if (term != null)
            return term;
        term = getTermByOboID(termID);
        if (term != null)
            return term;
        return null;
    }

    @Override
    public Set<GenericTerm> getDiseaseTermsOmimPhenotype() {
        String hql = """
            	select ref.term from TermExternalReference as ref
            	where ref.omimPhenotypes is not empty
            """;
        org.hibernate.query.Query<GenericTerm> query = HibernateUtil.currentSession().createQuery(hql, GenericTerm.class);
//        query.setMaxResults(20);
        return new HashSet<>(query.getResultList());
    }

    private Map<String, List<TermExternalReference>> casMap = null;

    public Map<String, List<TermExternalReference>> getAllTermExternalReference() {
        if (casMap != null)
            return casMap;
        String hql = """
            from TermExternalReference where prefix in (:prefix)
            """;
        org.hibernate.query.Query<TermExternalReference> query = HibernateUtil.currentSession().createQuery(hql, TermExternalReference.class);
        query.setParameterList("prefix", List.of("CAS", "MESH"));
        List<TermExternalReference> list = query.list();
        casMap = list.stream().collect(groupingBy(TermExternalReference::getPrefix));
        return casMap;
    }

    public TermExternalReference getTermExternalReference(String accession, String prefix) {
/*
        String hql = """
            from TermExternalReference where
            prefix = :prefix and accessionNumber = :accession
            """;

        org.hibernate.query.Query<TermExternalReference> query = HibernateUtil.currentSession().createQuery(hql, TermExternalReference.class);
        query.setParameter("prefix", prefix);
        query.setParameter("accession", accession);
*/
        List<TermExternalReference> list = getAllTermExternalReference().get(accession);

        if (list == null) {
            System.out.println("No Chebi Terms found for " + accession);
            return null;
        }
        if (list.size() > 1) {
            System.out.println("Multiple Chebi Terms found for " + accession);
            list.forEach(reference -> System.out.println(reference.getTerm().getOboID()));
            return null;
        }

        return list.get(0);
    }

    @Override
    public List<TermExternalReference> getAllCasReferences() {
        return getAllTermExternalReference().get("CAS");
    }

    @Override
    public void saveMeshChebi(MeshChebiMapping mapping) {
        HibernateUtil.currentStatelessSession().insert(mapping);
    }

    @Override
    public Map<String, GenericTerm> getGoTermsToZdbID() {
        String hql = " from GenericTerm where oboID like 'GO:%' ";
        org.hibernate.query.Query<GenericTerm> query = HibernateUtil.currentSession().createQuery(hql, GenericTerm.class);
        return query.getResultList().stream().collect(Collectors.toMap(GenericTerm::getOboID, term -> term));
    }

    private List<GenericTerm> filterTermsByOntology(List<GenericTerm> terms, Ontology ontology) {
        return terms.stream().filter(term -> term.getOntology() == ontology).collect(toList());
    }
}

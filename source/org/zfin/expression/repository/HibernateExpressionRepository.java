package org.zfin.expression.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Repository;
import org.zfin.antibody.Antibody;
import org.zfin.datatransfer.daniocell.DanioCellMapping;
import org.zfin.expression.*;
import org.zfin.expression.presentation.PublicationExpressionBean;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.agr.*;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.publication.presentation.FigurePresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.TermFigureStageRange;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.util.ZfinPropertyUtils.getStringOrNull;

/**
 * Repository that is used for curation actions, such as dealing with expression experiments.
 */
@Repository
public class HibernateExpressionRepository implements ExpressionRepository {

    private Logger logger = LogManager.getLogger(HibernateExpressionRepository.class);

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();


    public int getWtExpressionFigureCountForGene(Marker marker) {
        String sql = """
               select distinct efs_xpatex_zdb_id, xpatex_source_zdb_id
                       from expression_figure_stage
                            join expression_result2
                                   on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                            join expression_experiment2
                                   on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                            join fish_experiment
                       on genox_zdb_id = xpatex_genox_zdb_id
                            join fish
                       on fish_zdb_id = genox_fish_zdb_id
                      where xpatex_gene_zdb_id = :markerZdbID
                       and genox_is_std_or_generic_control = true
                       and fish_is_wildtype = true

                     and not exists
                     (
                       select 'x' from marker
                         where mrkr_zdb_id = xpatex_probe_feature_zdb_id
                         and substring(mrkr_abbrev from 1 for 10) = 'WITHDRAWN:'
                     )
                     and not exists
                     (
                      select 'x' from clone
                         where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
                         and clone_problem_type = 'Chimeric'
                     )
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }


    @Override
    public Publication getExpressionSinglePub(Marker marker) {
        String sql = "  select p from Publication p join p.expressionExperiments ee where ee.gene = :gene ";
        Query<Publication> query = HibernateUtil.currentSession().createQuery(sql, Publication.class);
        query.setParameter("gene", marker);
        List<Publication> pubs = query.list();

        if (CollectionUtils.isEmpty(pubs)) {
            logger.debug("a single pub not returned for marker expression: " + marker);
            return null;
        } else if (pubs.size() > 1) {
            logger.debug("multiple pubs [" + pubs.size() + "] returned for marker expression: " + marker);
        }

        return pubs.get(0);
    }

    @Override
    public List<Publication> getExpressionPub(Marker marker) {
        String sql = """
            select distinct p from Publication p
            join p.expressionExperiments ee
            where ee.gene = :gene
            and not exists (from Clone as clone
            where ee.probe = clone and clone.problem = :chimeric)
            AND size(ee.figureStageSet) != 0
            """;
        Query<Publication> query = HibernateUtil.currentSession().createQuery(sql, Publication.class);
        query.setParameter("gene", marker);
        query.setParameter("chimeric", Clone.ProblemType.CHIMERIC);
        return query.list();
    }

    @Override
    public List<Publication> getExpressionPubInSitu(Marker marker) {
        String sql = "  select distinct p from Publication p " +
                     "join p.expressionExperiments ee " +
                     "where ee.gene = :gene " +
                     "and ee.assay.name = 'mRNA in situ hybridization' " +
                     "and not exists (from Clone as clone " +
                     "where ee.probe = clone and clone.problem = :chimeric)";
        Query query = HibernateUtil.currentSession().createQuery(sql);
        query.setParameter("gene", marker.getZdbID());
        query.setParameter("chimeric", Clone.ProblemType.CHIMERIC);
        return query.list();
    }

    public int getExpressionPubCountForEfg(Marker marker) {
        String sql = """
              select count(distinct xpatex_source_zdb_id)
                  from expression_experiment2
                  join expression_figure_stage efs on expression_experiment2.xpatex_zdb_id = efs.efs_xpatex_zdb_id
                  join expression_result2 on efs.efs_pk_id = expression_result2.xpatres_efs_id
                 where xpatex_gene_zdb_id = :markerZdbID
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }

    public int getExpressionPubCountForClone(Clone clone) {
        String sql = """
              select count(distinct xpatex_source_zdb_id)
                  from expression_experiment2
                  join expression_figure_stage efs on expression_experiment2.xpatex_zdb_id = efs.efs_xpatex_zdb_id
                  join expression_result2 on efs.efs_pk_id = expression_result2.xpatres_efs_id
                 where xpatex_probe_feature_zdb_id = :markerZdbID
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", clone.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }


    public int getExpressionFigureCountForEfg(Marker marker) {
        String sql = """
               select count(distinct efs_fig_zdb_id)
                       from expression_figure_stage
                            join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                            join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                      where xpatex_gene_zdb_id = :markerZdbID
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }


    public Map<String, List<UberonSlimTermDTO>> getAllZfaUberonMap() {
        final String zumQueryString = """
            select distinct contained.term_ont_id, zum_uberon_id 
             from term as contained 
                 join all_term_contains on alltermcon_contained_zdb_id = contained.term_zdb_id
                 join zfa_uberon_mapping on zum_zfa_term_zdb_id = alltermcon_container_zdb_id
            """;

        final Query zumQuery = HibernateUtil.currentSession().createNativeQuery(zumQueryString);

        List<Object[]> zfaUberonRelationships = zumQuery.list();

        Map<String, List<UberonSlimTermDTO>> zfaUberonMap = new HashMap<>();

        for (Object[] zum : zfaUberonRelationships) {
            String zfaId = zum[0].toString();
            UberonSlimTermDTO uberonSlimTermDTO = new UberonSlimTermDTO(zum[1].toString());

            if (zfaUberonMap.get(zfaId) == null) {
                List<UberonSlimTermDTO> uberonIds = new ArrayList<>();
                uberonIds.add(uberonSlimTermDTO);
                zfaUberonMap.put(zfaId, uberonIds);
            } else {
                zfaUberonMap.get(zfaId).add(uberonSlimTermDTO);
            }
        }

        return zfaUberonMap;
    }

    public List<BasicExpressionDTO> getBasicExpressionDTOObjects() {

        final String expressionQueryString = """
            select distinct xpatres_efs_id,
                   xpatex_source_zdb_id,
                   accession_no,
                   xpatex_gene_zdb_id,
                   container.stg_obo_id,
                   xpatassay_mmo_id,
                   superterm.term_ont_id as superterm_id,
                   subterm.term_ont_id as subterm_id,
                   efs_fig_zdb_id,
                   superterm.term_name as superterm_name,
                   subterm.term_name as subterm_name,
                   case when container.stg_hours_end <= 48.00 then 'UBERON:0000068'
                        when container.stg_name_long like 'Hatching%' then 'post embryonic, pre-adult'
                        when container.stg_name_long like 'Larval%' then 'post embryonic, pre-adult'
                        when container.stg_name_long like 'Juvenile%' then 'post embryonic, pre-adult'
                        when container.stg_name = 'Adult' then 'UBERON:0000113'
                   end as uberon_stage,
                   container.stg_name as stage_name
              from expression_experiment2
              join expression_figure_stage on efs_xpatex_zdb_id = xpatex_zdb_id
              join expression_result2 on xpatres_efs_id = efs_pk_id
              join fish_experiment on genox_zdb_id = xpatex_genox_zdb_id
              join fish on genox_fish_zdb_id = fish_zdb_id
              join publication on xpatex_source_zdb_id = zdb_id
              join term as superterm on superterm.term_zdb_id = xpatres_superterm_zdb_id
              join stage as starts on starts.stg_zdb_id = efs_start_stg_zdb_id
              join stage as ends on ends.stg_zdb_id = efs_end_stg_zdb_id
              join stage as container on container.stg_hours_start >= starts.stg_hours_start
                   and container.stg_hours_end <= ends.stg_hours_end
              join expression_pattern_assay on xpatassay_name = xpatex_assay_name
              left outer join term as subterm on subterm.term_zdb_id = xpatres_subterm_zdb_id
              left outer join all_term_contains as super_parents on superterm.term_zdb_id = super_parents.alltermcon_contained_zdb_id
              left outer join zfa_uberon_mapping as super_uberon on super_parents.alltermcon_container_zdb_id = super_uberon.zum_zfa_term_zdb_id
              left outer join all_term_contains as sub_parents on subterm.term_zdb_id = sub_parents.alltermcon_contained_zdb_id
              left outer join zfa_uberon_mapping as sub_uberon on sub_parents.alltermcon_container_zdb_id = sub_uberon.zum_zfa_term_zdb_id
              where fish_is_wildtype = 't'
              and genox_is_std_or_generic_control = 't'
              and xpatres_expression_found = 't'
              and xpatex_gene_zdb_id is not null
              and container.stg_zdb_id != 'ZDB-STAGE-050211-1'
              and efs_start_stg_zdb_id != 'ZDB-STAGE-050211-1'
              and efs_end_stg_zdb_id != 'ZDB-STAGE-050211-1'
              """;

        final Query expressionQuery = HibernateUtil.currentSession().createNativeQuery(expressionQueryString);

        List<Object[]> expressions = expressionQuery.list();

        List<BasicExpressionDTO> basicExpressions = new ArrayList<>();

        Map<String, List<UberonSlimTermDTO>> zfaUberonMap = getAllZfaUberonMap();

        for (Object[] basicExpressionObjects : expressions) {

            //get the array objects converted to named vars & handle basic null stuff
            String expressionResultId = basicExpressionObjects[0].toString();
            String pubZdbId = basicExpressionObjects[1].toString();
            Integer pubMedId = basicExpressionObjects[2] != null ? (Integer) basicExpressionObjects[2] : null;
            String geneZdbId = basicExpressionObjects[3].toString();
            String stageZfaId = basicExpressionObjects[4].toString();
            String assay = basicExpressionObjects[5].toString();
            String anatomicalStructureTermId = basicExpressionObjects[6].toString();
            String subTermId = basicExpressionObjects[7] != null ? basicExpressionObjects[7].toString() : null;
            String superTermName = basicExpressionObjects[9].toString();
            String subTermName = basicExpressionObjects[10] != null ? basicExpressionObjects[10].toString() : null;
            UberonSlimTermDTO stageUberonDTO = basicExpressionObjects[11] != null ? new UberonSlimTermDTO(basicExpressionObjects[11].toString()) : null;
            String stageName = basicExpressionObjects[12] != null ? basicExpressionObjects[12].toString() : null;


            BasicExpressionDTO basicXpat = new BasicExpressionDTO();
            basicXpat.setExpressionResultId(expressionResultId);
            basicXpat.setGeneId("ZFIN:" + geneZdbId);

            basicXpat.setWhenExpressed(
                new ExpressionStageIdentifiersDTO(
                    stageName,
                    stageZfaId,
                    stageUberonDTO));

            basicXpat.setAssay(assay);
            PublicationAgrDTO pubDto = new PublicationAgrDTO();
            if (pubMedId != null) {
                pubDto.setPublicationId("PMID:" + pubMedId);
                List<String> pubPages = new ArrayList<>();
                pubPages.add("reference");
                CrossReferenceDTO zfinPubXref = new CrossReferenceDTO("ZFIN", pubZdbId, pubPages);
                pubDto.setCrossReference(zfinPubXref);
            } else {
                if (pubZdbId != null) {
                    pubDto.setPublicationId("ZFIN:" + pubZdbId);
                }
            }
            basicXpat.setEvidence(pubDto);

            List<String> annotationPages = new ArrayList<>();
            annotationPages.add("gene/expression/annotation/detail");
            basicXpat.setCrossReference(new CrossReferenceDTO("ZFIN", basicExpressionObjects[8].toString(), annotationPages));

            String whereExpressedStatement = null;
            String cellularComponentTermId = null;
            String anatomicalSubStructureTermId = null;
            String anatomicalStructureQualifierTermId = null;
            Set<UberonSlimTermDTO> anatomicalStructureUberonSlimTermIds = new HashSet<>();

            whereExpressedStatement = superTermName;

            if (subTermId != null) {
                if (subTermId.startsWith("GO:")) {
                    cellularComponentTermId = subTermId;
                }
                if (subTermId.startsWith("MPATH:")) {
                    anatomicalSubStructureTermId = subTermId;
                }
                if (subTermId.startsWith("BSPO:")) {
                    anatomicalStructureQualifierTermId = subTermId;
                }
                if (subTermId.startsWith("ZFA:")) {
                    anatomicalSubStructureTermId = subTermId;
                }

                whereExpressedStatement += " " + subTermName;

                if (zfaUberonMap.get(subTermId) != null) {
                    anatomicalStructureUberonSlimTermIds.addAll(zfaUberonMap.get(subTermId));
                }
            }

            if (zfaUberonMap.get(anatomicalStructureTermId) != null) {
                anatomicalStructureUberonSlimTermIds.addAll(zfaUberonMap.get(anatomicalStructureTermId));
            }

            if (CollectionUtils.isEmpty(anatomicalStructureUberonSlimTermIds)) {
                anatomicalStructureUberonSlimTermIds.add(new UberonSlimTermDTO("Other"));
            }

            ExpressionTermIdentifiersDTO wildtypeExpressionTermIdentifiers = new ExpressionTermIdentifiersDTO(whereExpressedStatement, cellularComponentTermId,
                anatomicalStructureTermId, anatomicalSubStructureTermId, anatomicalStructureQualifierTermId, anatomicalStructureUberonSlimTermIds);

            basicXpat.setWhereExpressed(wildtypeExpressionTermIdentifiers);

            basicExpressions.add(basicXpat);
        }
        return basicExpressions;
    }

    public Map<String, List<ImageDTO>> getDirectSubmissionImageDTOMap() {

        String baseUrl = "https://zfin.org/";

        Map<String, List<ImageDTO>> map = new HashMap<>();

        String sql = """
            select xpatres_pk_id, img_zdb_id, img_image 
            from expression_result2
                 join expression_figure_stage on xpatres_efs_id = efs_pk_id 
                 join figure on xpatres_pk_id = fig_source_zdb_id
                 join image on img_fig_zdb_id = fig_zdb_id 
                 join publication on publication.zdb_id = fig_source_zdb_id 
            where pub_can_show_images = true 
                  and jtype = 'Unpublished'
            """;

        Query query = HibernateUtil.currentSession().createNativeQuery(sql);

        List<Object[]> rows = query.list();
        for (Object[] row : rows) {
            String key = row[0].toString();
            String imgZdbId = row[1].toString();
            String imageFilename = row[2].toString();

            ImageDTO dto = new ImageDTO();

            dto.setImageId(imgZdbId);
            dto.setImageFileUrl(baseUrl + "imageLoadUp/" + imageFilename);
            dto.setImagePageUrl(baseUrl + imgZdbId);

            if (map.get(key) == null) {
                List<ImageDTO> dtoList = new ArrayList<>();
                map.put(key, dtoList);
            }
            map.get(key).add(dto);
        }

        return map;
    }

    public int getExpressionFigureCountForClone(Clone clone) {
        String sql = """
               select count(distinct efs_start_stg_zdb_id)
                       from expression_figure_stage
                        join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                        join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                      where xpatex_probe_feature_zdb_id = :markerZdbID
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", clone.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }

    @Override
    public FigureLink getExpressionSingleFigure(Marker marker) {
        String sql = """
            select distinct efs_fig_zdb_id , f.fig_label
                 from expression_figure_stage
                 join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                 join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                 join figure f on f.fig_zdb_id = efs_fig_zdb_id
                 where xpatex_gene_zdb_id = :markerZdbID
                 and not exists
                 (
                 select 'x' from marker
                 where mrkr_zdb_id = xpatex_probe_feature_zdb_id
                 and substring(mrkr_abbrev from 1 for 10) = 'WITHDRAWN:'
                 )
                 and not exists
                 (
                 select 'x' from clone
                 where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
                 and clone_problem_type = 'Chimeric'
                 )
                 """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());
        query.setMaxResults(1);
        query.setResultTransformer(

            (Object[] tuple, String[] aliases) -> {
                FigureLink figureLink = new FigureLink();
                figureLink.setFigureZdbId(tuple[0].toString());
                figureLink.setLinkContent(tuple[1].toString());
                figureLink.setLinkValue(
                    FigurePresentation.getLink(figureLink.getFigureZdbId(), figureLink.getLinkContent())
                );
                return figureLink;
            });
        return (FigureLink) query.uniqueResult();
    }

    public int getExpressionFigureCountForGene(Marker marker) {
        String sql = """
               select count(distinct efs_fig_zdb_id)
                     from expression_figure_stage
                     join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                     join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                      where xpatex_gene_zdb_id = :markerZdbID
                     and not exists
                     (
                       select 'x' from marker
                         where mrkr_zdb_id = xpatex_probe_feature_zdb_id
                         and substring(mrkr_abbrev from 1 for 10) = 'WITHDRAWN:'
                     )
                     and not exists
                     (
                      select 'x' from clone 
                         where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id 
                         and clone_problem_type = 'Chimeric' 
                     ) 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }


    public int getExpressionFigureCountForGeneInSitu(Marker marker) {
        String sql = """
               select count(distinct efs_fig_zdb_id) 
                     from expression_figure_stage
                     join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                     join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                      where xpatex_gene_zdb_id = :markerZdbID 
                      and xpatex_assay_name = 'mRNA in situ hybridization' 
                     and not exists 
                     ( 
                       select 'x' from marker 
                         where mrkr_zdb_id = xpatex_probe_feature_zdb_id 
                         and substring(mrkr_abbrev from 1 for 10) = 'WITHDRAWN:' 
                     ) 
                     and not exists 
                     ( 
                      select 'x' from clone 
                         where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id 
                         and clone_problem_type = 'Chimeric' 
                     ) 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }


    public int getExpressionFigureCountForFish(Fish fish) {
        String sql = """
            select count(distinct efs_fig_zdb_id)
            	           from expression_figure_stage
            	                join expression_result2 on efs_pk_id = xpatres_efs_id
            	                join expression_experiment on efs_xpatex_zdb_id = xpatex_zdb_id
            	                join fish_experiment on xpatex_genox_zdb_id = genox_zdb_id
            	          where genox_fish_zdb_id = :fishID
            	         and xpatex_atb_zdb_id is null
            	         """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("fishID", fish.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }

    public List<PublicationExpressionBean> getDirectlySubmittedExpressionForClone(Clone clone) {

        String sql = """
              select count(distinct efs_fig_zdb_id),
                   pub.zdb_id, pub.pub_mini_ref,m.mrkr_abbrev, m.mrkr_Zdb_id
                             from expression_figure_stage
                             join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                             join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                            join publication pub on pub.zdb_id = xpatex_source_zdb_id 
                            join marker m on m.mrkr_zdb_id=xpatex_probe_feature_zdb_id 
                       where xpatex_probe_feature_zdb_id = :markerZdbID 
                       and pub.jtype = 'Unpublished' 
                       group by m.mrkr_zdb_id,m.mrkr_abbrev, pub.zdb_id, pub.pub_mini_ref; 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", clone.getZdbID());

        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
                publicationExpressionBean.setNumFigures(Integer.parseInt(tuple[0].toString()));
                publicationExpressionBean.setPublicationZdbID(tuple[1].toString());
                publicationExpressionBean.setMiniAuth(tuple[2].toString());
                if (tuple[3] != null) {
                    publicationExpressionBean.setProbeFeatureAbbrev(tuple[3].toString());
                }
                if (tuple[4] != null) {
                    publicationExpressionBean.setProbeFeatureZdbId(tuple[4].toString());
                }
                return publicationExpressionBean;
            }

            @Override
            public List transformList(List collection) {
                List<PublicationExpressionBean> list = new ArrayList<>();
                for (Object o : collection) {
                    list.add((PublicationExpressionBean) o);
                }
                return list;
            }
        });
        List<PublicationExpressionBean> pubList = query.list();
        return pubList;
    }

    public List<PublicationExpressionBean> getDirectlySubmittedExpressionForGene(Marker marker) {

        String sql = """
              select count(distinct efs_fig_zdb_id),
            		pub.zdb_id, pub.pub_mini_ref,m.mrkr_abbrev, m.mrkr_zdb_id
                       from expression_figure_stage
                            join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                            join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                            join publication pub on pub.zdb_id = xpatex_source_zdb_id
                            join marker m on m.mrkr_zdb_id=xpatex_probe_feature_zdb_id
                      where xpatex_gene_zdb_id = :markerZdbID 
                      and pub.jtype = 'Unpublished' 
                      and not exists( 
                         select 'x' from clone 
                         where clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id 
                         and clone_problem_type = 'Chimeric' 
                      ) 
                      and not exists ( 
                         select 'x' from marker 
                         where mrkr_zdb_id = xpatex_probe_feature_zdb_id 
                         and substring(mrkr_abbrev from 1 for 10) = 'WITHDRAWN:' 
                     ) 
                     group by m.mrkr_zdb_id, m.mrkr_abbrev, pub.zdb_id, pub.pub_mini_ref 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());

        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
                publicationExpressionBean.setNumFigures(Integer.parseInt(tuple[0].toString()));
                publicationExpressionBean.setPublicationZdbID(tuple[1].toString());
                publicationExpressionBean.setMiniAuth(tuple[2].toString());
                if (tuple[3] != null) {
                    publicationExpressionBean.setProbeFeatureAbbrev(tuple[3].toString());
                }
                if (tuple[4] != null) {
                    publicationExpressionBean.setProbeFeatureZdbId(tuple[4].toString());
                }
                return publicationExpressionBean;
            }

            @Override
            public List transformList(List collection) {
                List<PublicationExpressionBean> list = new ArrayList<PublicationExpressionBean>();
                for (Object o : collection) {
                    list.add((PublicationExpressionBean) o);
                }
                return list;
            }
        });
        List<PublicationExpressionBean> pubList = query.list();
        return pubList;
    }

    @Override
    public List<PublicationExpressionBean> getDirectlySubmittedExpressionForEfg(Marker marker) {

        String sql = """
              select count(distinct efs_fig_zdb_id),
                       pub.zdb_id, pub.pub_mini_ref ,m.mrkr_abbrev, m.mrkr_zdb_id  
                         from expression_figure_stage
                         join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                         join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                         join publication pub on pub.zdb_id = xpatex_source_zdb_id 
                         join marker m on m.mrkr_zdb_id=xpatex_gene_zdb_id
                       where xpatex_gene_zdb_id = :markerZdbID 
                       and pub.jtype = 'Unpublished' 
                     group by m.mrkr_zdb_id, m.mrkr_abbrev, pub.zdb_id, pub.pub_mini_ref 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());

        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
                publicationExpressionBean.setNumFigures(Integer.parseInt(tuple[0].toString()));
                publicationExpressionBean.setPublicationZdbID(tuple[1].toString());
                publicationExpressionBean.setMiniAuth(tuple[2].toString());
                if (tuple[3] != null) {
                    publicationExpressionBean.setProbeFeatureAbbrev(tuple[3].toString());
                }
                if (tuple[4] != null) {
                    publicationExpressionBean.setProbeFeatureZdbId(tuple[4].toString());
                }
                return publicationExpressionBean;
            }

            @Override
            public List transformList(List collection) {
                List<PublicationExpressionBean> list = new ArrayList<PublicationExpressionBean>();
                for (Object o : collection) {
                    list.add((PublicationExpressionBean) o);
                }
                return list;
            }
        });
        List<PublicationExpressionBean> pubList = query.list();
        return pubList;
    }

    public List<PublicationExpressionBean> getThisseExpressionForGene(Marker marker, Set<String> pubList) {

        String sql = """
              select count(distinct efs_fig_zdb_id), 
            		pub.zdb_id, pub.pub_mini_ref,m.mrkr_abbrev, m.mrkr_zdb_id 
                             from expression_figure_stage
                             join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                             join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                            join publication pub on pub.zdb_id = xpatex_source_zdb_id 
                            join marker m on m.mrkr_zdb_id=xpatex_probe_feature_zdb_id 
                      where xpatex_gene_zdb_id = :markerZdbID 
                      and pub.jtype = 'Unpublished' 
                      and pub.zdb_id in (:pubList) 
                      and not exists( 
                         select 'x' from clone 
                         where clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id 
                         and clone_problem_type = 'Chimeric' 
                      ) 
                      and not exists ( 
                         select 'x' from marker 
                         where mrkr_zdb_id = xpatex_probe_feature_zdb_id 
                         and substring(mrkr_abbrev from 1 for 10) = 'WITHDRAWN:' 
                     ) 
                     group by m.mrkr_zdb_id, m.mrkr_abbrev, pub.zdb_id, pub.pub_mini_ref 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("markerZdbID", marker.getZdbID());
        query.setParameterList("pubList", pubList);

        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
                publicationExpressionBean.setNumFigures(Integer.parseInt(tuple[0].toString()));
                publicationExpressionBean.setPublicationZdbID(tuple[1].toString());
                publicationExpressionBean.setMiniAuth(tuple[2].toString());
                if (tuple[3] != null) {
                    publicationExpressionBean.setProbeFeatureAbbrev(tuple[3].toString());
                }
                if (tuple[4] != null) {
                    publicationExpressionBean.setProbeFeatureZdbId(tuple[4].toString());
                }
                return publicationExpressionBean;
            }

            @Override
            public List transformList(List collection) {
                List<PublicationExpressionBean> list = new ArrayList<PublicationExpressionBean>();
                for (Object o : collection) {
                    list.add((PublicationExpressionBean) o);
                }
                return list;
            }
        });
        List<PublicationExpressionBean> pubExpList = query.list();
        return pubExpList;
    }


    public ExpressionExperiment2 getExpressionExperiment(String experimentID) {
        Session session = HibernateUtil.currentSession();
        return session.get(ExpressionExperiment2.class, experimentID);
    }

    public List<ExpressionExperiment2> getExpressionExperiment2ByPub(String pubID, String geneID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from ExpressionExperiment2 where publication.zdbID = :pubID " +
                     "AND gene.zdbID = :geneID ";
        Query<ExpressionExperiment2> query = session.createQuery(hql, ExpressionExperiment2.class);
        query.setParameter("pubID", pubID);
        query.setParameter("geneID", geneID);

        return query.list();
    }

    public ExpressionDetailsGenerated getExpressionExperiment2(long id) {
        return currentSession().get(ExpressionDetailsGenerated.class, id);
    }

    /**
     * Retrieve an assay by name.
     *
     * @param assay assay name
     * @return expression Assay
     */
    public ExpressionAssay getAssayByName(String assay) {
        Session session = HibernateUtil.currentSession();
        return session.get(ExpressionAssay.class, assay);
    }

    /**
     * Retrieve db link by id.
     *
     * @param genbankID genbank id
     * @return MarkerDBLink
     */
    public MarkerDBLink getMarkDBLink(String genbankID) {
        Session session = HibernateUtil.currentSession();
        return session.get(MarkerDBLink.class, genbankID);
    }

    public FishExperiment getFishExperimentByID(String fishExpID) {
        Session session = HibernateUtil.currentSession();
        return session.get(FishExperiment.class, fishExpID);
    }

    /**
     * Retrieve FishExperiment by Experiment ID
     *
     * @param experimentID id
     * @return FishExperiment
     */
    public FishExperiment getFishExperimentByExperimentIDAndFishID(String experimentID, String fishID) {
        String hql = "from FishExperiment where experiment.zdbID = :experimentID and fish.zdbID = :fishID";
        Query<FishExperiment> query = HibernateUtil.currentSession().createQuery(hql, FishExperiment.class);
        query.setParameter("experimentID", experimentID);
        query.setParameter("fishID", fishID);
        return query.uniqueResult();
    }

    /**
     * Create a new genotype experiment for given experiment and genotype.
     *
     * @param experiment genotype experiment
     */
    public void createFishExperiment(FishExperiment experiment) {
        HibernateUtil.currentSession().save(experiment);
    }

    /**
     * Retrieve experiment by id.
     *
     * @param experimentID id
     * @return Experiment
     */
    public Experiment getExperimentByID(String experimentID) {
        Session session = HibernateUtil.currentSession();
        return session.get(Experiment.class, experimentID);
    }

    /**
     * Retrieve Genotype by PK.
     *
     * @param genotypeID id
     * @return genotype
     */
    public Genotype getGenotypeByID(String genotypeID) {
        Session session = HibernateUtil.currentSession();
        return session.get(Genotype.class, genotypeID);
    }

    /**
     * Convenience method to create a genotype experiment from
     * experiment ID and genotype ID
     *
     * @param experimentID id
     * @param fishID       id
     */
    public FishExperiment createFishExperiment(String experimentID, String fishID) {
        Experiment experiment = getExperimentByID(experimentID);
        Fish fish = getMutantRepository().getFish(fishID);
        FishExperiment fishox = new FishExperiment();
        fishox.setExperiment(experiment);
        fishox.setFish(fish);
        createFishExperiment(fishox);
        return fishox;
    }

    /**
     * Create a new expression Experiment.
     *
     * @param expressionExperiment expression experiment
     */
    public void createExpressionExperiment(ExpressionExperiment2 expressionExperiment) {
        HibernateUtil.currentSession().save(expressionExperiment);
    }

    /**
     * Remove an existing expression experiment and all objects that it is composed of.
     * Note: It delegates the call to removing the ActiveData record (OCD).
     *
     * @param experiment expression experiment
     */
    public void deleteExpressionExperiment(ExpressionExperiment2 experiment) {
        InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();
        infraRep.deleteActiveDataByZdbID(experiment.getZdbID());
    }

    public List<ExpressionExperiment2> getExperimentsByGeneAndFish(String publicationID, String geneZdbID, String fishID) {
        String hql = """
            SELECT DISTINCT
                experiment
            FROM
                ExpressionExperiment2 experiment
                LEFT JOIN experiment.gene AS gene
                LEFT JOIN experiment.fishExperiment AS fishox
            WHERE
                experiment.publication.zdbID = :pubID
                AND (:geneID IS NULL OR gene.zdbID = :geneID)
                AND (:fishID IS NULL OR fishox.fish.zdbID = :fishID)
            """;

        Query<ExpressionExperiment2> query = HibernateUtil.currentSession().createQuery(hql, ExpressionExperiment2.class);
        query.setParameter("pubID", publicationID);
        query.setParameter("geneID", geneZdbID);
        query.setParameter("fishID", fishID);

        List<ExpressionExperiment2> orderedList = query.list().stream().sorted(
            Comparator.comparing((ExpressionExperiment2 xp) -> getStringOrNull(xp, "gene.abbreviationOrder"), Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(xp -> getStringOrNull(xp, "fishExperiment.fish.name"), Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(xp -> getStringOrNull(xp, "fishExperiment.experiment.name"), Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(xp -> getStringOrNull(xp, "assay.displayOrder"), Comparator.nullsFirst(Comparator.naturalOrder()))
        ).toList();

        // Use LinkedHashSet to distinctify and preserve order
        return new ArrayList<>(new LinkedHashSet<>(orderedList));
    }

    public List<ExpressionExperiment2> getExpressionByExperiment(String experimentID) {
        String hql = "select experiment from ExpressionExperiment2 experiment "
                     + "     where experiment.fishExperiment.experiment.zdbID = :experimentID ";

        Query<ExpressionExperiment2> query = HibernateUtil.currentSession().createQuery(hql, ExpressionExperiment2.class);
        query.setParameter("experimentID", experimentID);
        return query.list();
    }

    public List<ExpressionExperiment2> getExperiments(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select experiment from ExpressionExperiment2 experiment
                   left join experiment.gene as gene
                 where experiment.publication.zdbID = :pubID
                order by gene.abbreviationOrder,
                         experiment.fishExperiment.fish.name,
                         experiment.assay.displayOrder
                         """;
        Query<ExpressionExperiment2> query = session.createQuery(hql, ExpressionExperiment2.class);
        query.setParameter("pubID", publicationID);
        return query.list();

    }


    /**
     * Retrieve an experiment figure stage for given pub, gene and fish.
     *
     * @param publicationID Publication
     * @param geneZdbID     gene
     * @param fishZdbID     fish
     * @return list of experiment figure stages.
     */
    public List<ExpressionFigureStage> getExperimentFigureStagesByGeneAndFish(String publicationID,
                                                                              String geneZdbID,
                                                                              String fishZdbID,
                                                                              String figureZdbID) {
        geneZdbID = StringUtils.isEmpty(geneZdbID) ? null : geneZdbID.trim();
        fishZdbID = StringUtils.isEmpty(fishZdbID) ? null : fishZdbID.trim();
        figureZdbID = StringUtils.isEmpty(figureZdbID) ? null : figureZdbID.trim();

        Session session = HibernateUtil.currentSession();
        String hql = """
            SELECT DISTINCT efs FROM ExpressionFigureStage AS efs
            LEFT JOIN efs.expressionExperiment.gene AS gene
            LEFT JOIN FETCH efs.startStage
            LEFT JOIN FETCH efs.endStage
            LEFT JOIN FETCH efs.expressionExperiment
            LEFT JOIN FETCH efs.expressionResultSet
            JOIN FETCH efs.figure
            JOIN efs.expressionExperiment.fishExperiment.fish AS fish
            WHERE efs.expressionExperiment.publication.zdbID = :pubID
            AND (:geneZdbID IS NULL OR gene.zdbID = :geneZdbID)
            AND (:figureZdbID IS NULL OR efs.figure.zdbID = :figureZdbID)
            AND (:fishZdbID IS NULL OR fish.zdbID = :fishZdbID)
            ORDER BY efs.figure.orderingLabel
            """;
        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("pubID", publicationID);
        query.setParameter("geneZdbID", geneZdbID);
        query.setParameter("figureZdbID", figureZdbID);
        query.setParameter("fishZdbID", fishZdbID);

//        It would be nice if this ordering was in the HQL, but I could not figure out how to do it in the latest Hibernate.
//        ORDER BY:
//            efs.figure.orderingLabel,
//            efs.expressionExperiment.gene.abbreviationOrder,
//            efs.expressionExperiment.fishExperiment.fish.name,
//            efs.expressionExperiment.assay.displayOrder
//            efs.startStage.abbreviation
        return query.list().stream()
            .sorted(
                Comparator.comparing((ExpressionFigureStage efs) -> getStringOrNull(efs, "figure.orderingLabel"), Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(efs -> getStringOrNull(efs, "expressionExperiment.gene.abbreviationOrder"), Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(efs -> getStringOrNull(efs, "expressionExperiment.fishExperiment.fish.name"), Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(efs -> getStringOrNull(efs, "expressionExperiment.assay.displayOrder"), Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(efs -> getStringOrNull(efs, "startStage.abbreviation"), Comparator.nullsFirst(Comparator.naturalOrder()))
            )
            .toList();
    }

    public ExpressionFigureStage getExpressionFigureStage(Long id) {
        return HibernateUtil.currentSession().get(ExpressionFigureStage.class, id);
    }

    public List<ExpressionExperiment2> getExperimentsByGeneAndFish2(String publicationID, String geneZdbID, String fishID) {

        String hql = """
                 SELECT DISTINCT
                     experiment
                 FROM
                     ExpressionExperiment2 experiment
                     LEFT JOIN experiment.gene AS gene
                     LEFT JOIN experiment.fishExperiment AS fishox
                 WHERE
                     experiment.publication.zdbID = :pubID
                     AND (:geneID IS NULL OR gene.zdbID = :geneID)
                     AND (:fishID IS NULL OR fishox.fish.zdbID = :fishID)
            """;
        Query<ExpressionExperiment2> query = HibernateUtil.currentSession().createQuery(hql, ExpressionExperiment2.class);
        query.setParameter("pubID", publicationID);
        query.setParameter("geneID", geneZdbID);
        query.setParameter("fishID", fishID);

        //                   order by gene.abbreviationOrder,
        //                            fishox.fish.name,
        //                            fishox.experiment.name,
        //                            experiment.assay.displayOrder
        List<ExpressionExperiment2> orderedList = query.list().stream()
            .sorted(
                Comparator.comparing((ExpressionExperiment2 xp) -> getStringOrNull(xp, "gene.abbreviationOrder"), Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(xp -> getStringOrNull(xp, "fishExperiment.fish.name"), Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(xp -> getStringOrNull(xp, "fishExperiment.experiment.name"), Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(xp -> getStringOrNull(xp, "assay.displayOrder"), Comparator.nullsFirst(Comparator.naturalOrder()))
            )
            .toList();

        // Use LinkedHashSet to distinctify and preserve order
        return new ArrayList<>(new LinkedHashSet<>(orderedList));
    }

    private ExpressionResult2 getUnspecifiedExpressResult(ExpressionResult2 result) {
        GenericTerm unspecified = ontologyRepository.getTermByNameActive(Term.UNSPECIFIED, Ontology.ANATOMY);
        Session session = HibernateUtil.currentSession();
        Query<ExpressionResult2> query = session.createQuery("""
            from ExpressionResult2 where
            expressionFigureStage.expressionExperiment = :expressionExperiment
            AND expressionFigureStage.startStage = :start
            AND expressionFigureStage.endStage = :end
            AND superTerm = :superTerm
            AND expressionFound = :expFound
            """, ExpressionResult2.class);
        query.setParameter("expressionExperiment", result.getExpressionFigureStage().getExpressionExperiment());
        query.setParameter("start", result.getExpressionFigureStage().getStartStage());
        query.setParameter("end", result.getExpressionFigureStage().getEndStage());
        query.setParameter("superTerm", unspecified);
        query.setParameter("expFound", result.isExpressionFound());
        return query.uniqueResult();
    }

    // run the script to update the fast search table for antibodies-anatomy

    /**
     * Delete a expressionFigureStage record including all expression results on it
     *
     * @param figureAnnotation experiment figure stage.
     */
    public void deleteFigureAnnotation(ExpressionFigureStage figureAnnotation) {
        if (figureAnnotation == null) {
            throw new NullPointerException("No Figure Annotation provided");
        }
        HibernateUtil.currentSession().delete(figureAnnotation);
    }

    /**
     * Retrieve an efs by experiment, figure, start and end stage id.
     *
     * @param experimentZdbID experiment
     * @param figureID        figure
     * @param startStageID    start
     * @param endStageID      end
     * @return efs object
     */
    public ExpressionFigureStage getExperimentFigureStage(String experimentZdbID, String figureID, String startStageID, String endStageID) {
        if (StringUtils.isEmpty(experimentZdbID)) {
            return null;
        }
        if (StringUtils.isEmpty(figureID)) {
            return null;
        }
        if (StringUtils.isEmpty(startStageID)) {
            return null;
        }
        if (StringUtils.isEmpty(endStageID)) {
            return null;
        }
        validateFigureAnnotationKey(experimentZdbID, figureID, startStageID, endStageID);

        Session session = HibernateUtil.currentSession();

        String hql = """
            select efs from ExpressionFigureStage as efs 
                 where efs.expressionExperiment.zdbID = :experimentID 
             AND efs.startStage.zdbID = :startID 
             AND efs.endStage.zdbID = :endID 
             AND efs.figure.zdbID = :figureID 
            """;
        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("experimentID", experimentZdbID);
        query.setParameter("startID", startStageID);
        query.setParameter("endID", endStageID);
        query.setParameter("figureID", figureID);

        return query.uniqueResult();
    }

    /**
     * Retrieve all expression structures for a given publication, which is the same as the
     * structure pile.
     *
     * @param publicationID publication ID
     * @return list of expression structures.
     */
    public List<ExpressionStructure> retrieveExpressionStructures(String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            from ExpressionStructure
            where publication.zdbID = :publicationID
            order by superterm.termName
            """;
        Query<ExpressionStructure> query = session.createQuery(hql, ExpressionStructure.class);
        query.setParameter("publicationID", publicationID);
        return query.list();
    }

    /**
     * Retrieve a single expression structure by ID.
     *
     * @param zdbID structure ID
     * @return expression structure
     */
    public ExpressionStructure getExpressionStructure(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(ExpressionStructure.class, zdbID);
    }

    /**
     * Delete a structure from the pile.
     *
     * @param structure expression structure
     */
    public void deleteExpressionStructure(ExpressionStructure structure) {
        Session session = HibernateUtil.currentSession();
        session.delete(structure);
    }

    public void deleteExpressionStructuresForPub(Publication publication) {
        List<ExpressionStructure> structures = HibernateUtil.currentSession()
            .createQuery("from ExpressionStructure where publication = :publication", ExpressionStructure.class)
            .setParameter("publication", publication)
            .list();
        for (ExpressionStructure structure : structures) {
            getInfrastructureRepository().deleteActiveDataByZdbID(structure.getZdbID());
        }
    }

    /**
     * Check if a pile structure already exists.
     * check for:
     * suberterm
     * subterm
     * publication ID
     *
     * @param expressedTerm term
     * @param publicationID publication
     * @return boolean
     */
    public boolean pileStructureExists(ExpressedTermDTO expressedTerm, String publicationID) {
        if (publicationID == null) {
            throw new NullPointerException("No Publication provided.");
        }
        String supertermName = expressedTerm.getEntity().getSuperTerm().getTermName();
        if (supertermName == null) {
            throw new NullPointerException("No superterm provided.");
        }

        Session session = HibernateUtil.currentSession();
        String hql = """
            from ExpressionStructure
            """;

        List<String> hqlClauses = new ArrayList<>();
        HashMap<String, Object> parameterMap = new HashMap<>();

        hqlClauses.add("superterm.termName = :superterm");
        parameterMap.put("superterm", supertermName);
        hqlClauses.add("expressionFound = :expressionFound");
        parameterMap.put("expressionFound", expressedTerm.isExpressionFound());
        hqlClauses.add("publication.zdbID = :publicationZdbID");
        parameterMap.put("publicationZdbID", publicationID);
        if (expressedTerm.getEntity().getSubTerm() != null) {
            OntologyDTO subtermOntology = expressedTerm.getEntity().getSubTerm().getOntology();
            if (subtermOntology == null) {
                throw new NullPointerException("No subterm ontology provided.");
            }
            hqlClauses.add("subterm.termName = :subtermName");
            parameterMap.put("subtermName", expressedTerm.getEntity().getSubTerm().getTermName());
        } else {
            hqlClauses.add("subterm is null");
        }
        if (expressedTerm.getQualityTerm() != null) {
            if (expressedTerm.getQualityTerm().getTerm() != null) {
                hqlClauses.add("eapQualityTerm.zdbID = :zdbID");
                parameterMap.put("zdbID", expressedTerm.getQualityTerm().getTerm().getZdbID());
            }
            hqlClauses.add("tag = :tag");
            parameterMap.put("tag", expressedTerm.getQualityTerm().getTag());
        } else {
            hqlClauses.add("eapQualityTerm is null");
        }
        hql += " where " + String.join(" and ", hqlClauses);
        Query<ExpressionStructure> query = session.createQuery(hql, ExpressionStructure.class);
        parameterMap.forEach(query::setParameter);
        List<ExpressionStructure> list = query.list();
        return list != null && !list.isEmpty();
    }

    /**
     * Retrieve a genotype experiment for a given genotype ID.
     *
     * @param genotypeID genotype id
     * @return FishExperiment
     */
    public FishExperiment getGenotypeExperimentByGenotypeID(String genotypeID) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            from FishExperiment as fishox where 
             fishox.fish.genotype.zdbID = :genotypeID
            """;
        Query<FishExperiment> query = session.createQuery(hql, FishExperiment.class);
        query.setParameter("genotypeID", genotypeID);
        return query.uniqueResult();
    }

    /**
     * Create all expression structures being used in a given publication.
     *
     * @param publicationID publication id
     */
    @Override
    public void createExpressionPile(String publicationID) {
        List<ExpressionResult2> expressionResults = getAllExpressionResults(publicationID);
        if (expressionResults == null || expressionResults.isEmpty()) {
            return;
        }
        Publication publication = getPublicationRepository().getPublication(publicationID);
        Set<ExpressionStructure> distinctStructures = new HashSet<>();
        for (ExpressionResult2 expressionResult : expressionResults) {
            Set<ExpressionStructure> expressionStructureSet = instantiateExpressionStructure(expressionResult, publication);
            for (ExpressionStructure structure : expressionStructureSet) {
                // only create structures that are not already on the pile.
                if (distinctStructures.add(structure))
                    createPileStructure(structure);
            }
        }

    }

    private Set<ExpressionStructure> instantiateExpressionStructure(ExpressionResult2 expressionResult, Publication publication) {
        Set<ExpressionStructure> structureList = new HashSet<>();
        if (expressionResult.isEap()) {
            for (ExpressionPhenotypeTerm quality : expressionResult.getPhenotypeTermSet()) {
                ExpressionStructure structure = getExpressionStructure(expressionResult, publication);
                structure.setEapQualityTerm(quality.getQualityTerm());
                structure.setTag(quality.getTag());
                structureList.add(structure);
            }
        } else {
            structureList.add(getExpressionStructure(expressionResult, publication));
        }
        return structureList;
    }

    private ExpressionStructure getExpressionStructure(ExpressionResult2 expressionResult, Publication publication) {
        ExpressionStructure structure = new ExpressionStructure();
        structure.setDate(new Date());
        structure.setPerson(ProfileService.getCurrentSecurityUser());
        structure.setPublication(publication);
        structure.setSuperterm(expressionResult.getSuperTerm());
        structure.setExpressionFound(expressionResult.isExpressionFound());
        GenericTerm subTerm = expressionResult.getSubTerm();
        if (subTerm != null) {
            structure.setSubterm(subTerm);
        }
        return structure;
    }

    private List<ExpressionResult2> getAllExpressionResults(String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct expression from ExpressionResult2 expression where" +
                     "          expression.expressionFigureStage.expressionExperiment.publication.id= :publicationID";
        Query<ExpressionResult2> query = session.createQuery(hql, ExpressionResult2.class);
        query.setParameter("publicationID", publicationID);
        return query.list();

    }

    /**
     * Create a new structure - post-composed - for the structure pile.
     *
     * @param structure structure
     */
    public void createPileStructure(ExpressionStructure structure) {
        Session session = HibernateUtil.currentSession();
        session.save(structure);
    }

    private void validateFigureAnnotationKey(String experimentZdbID, String figureID, String startStageID, String endStageID) {
        ActiveData data = new ActiveData();
        // these calls validate the keys according to zdb id syntax.
        // ToDo: Change the validate method static to be able to call it directly.
        data.setZdbID(experimentZdbID);
        data.setZdbID(figureID);
        data.setZdbID(startStageID);
        data.setZdbID(endStageID);
    }

    public List<ExpressionFigureStage> getExpressionResultsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select xpRslt from ExpressionFigureStage xpRslt, ExpressionExperiment2 xpExp, FishExperiment fishox 
                           where fishox.fish = :fish 
                             and fishox = xpExp.fishExperiment 
                             and xpRslt.expressionExperiment = xpExp 
                             and xpExp.gene IS NOT null
                     """;
        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("fish", fish);

        return query.list();
    }

    public List<ExpressionFigureStage> getExpressionFigureStagesByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        /*
        getExpressionFigureStagesByFish throws exception:
        org.hibernate.query.SemanticException: Cannot compare left expression of type 'org.zfin.expression.ExpressionExperiment2' with right expression of type 'org.zfin.expression.ExpressionExperiment'
        java.lang.IllegalArgumentException: org.hibernate.query.SemanticException: Cannot compare left expression of type 'org.zfin.expression.ExpressionExperiment2' with right expression of type 'org.zfin.expression.ExpressionExperiment'
        ...
         */
        String hql = """
            SELECT DISTINCT efs
            FROM ExpressionFigureStage efs
            JOIN ExpressionExperiment2 expressionExperiment
              ON efs.expressionExperiment = expressionExperiment
            JOIN FishExperiment fishExperiment
              ON fishExperiment = expressionExperiment.fishExperiment
            WHERE fishExperiment.fish = :fish
              AND expressionExperiment.gene IS NOT NULL
            """;

        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("fish", fish);
        return query.list();
    }

    public long getExpressionResultsByFishAndPublication(Fish fish, String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select count(result) from ExpressionResult2 result
            where  result.expressionFigureStage.expressionExperiment.publication.zdbID = :publicationID
            and result.expressionFigureStage.expressionExperiment.fishExperiment.fish = :fish 
            """;

        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);
        query.setParameter("publicationID", publicationID);

        return (long) query.uniqueResult();
    }

    public List<String> getExpressionFigureIDsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String sql = """
            select distinct efs_fig_zdb_id 
                 from expression_figure_stage
                 join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                 join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                 join fish_experiment on expression_experiment2.xpatex_genox_zdb_id = fish_experiment.genox_zdb_id 
             where genox_fish_zdb_id = :fishID 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("fishID", fish.getZdbID());

        return (List<String>) query.list();
    }

    public List<String> getExpressionPublicationIDsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String sql = """
            select distinct efs_fig_zdb_id  
                 from expression_figure_stage
                 join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                 join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                 join fish_experiment fe on expression_experiment2.xpatex_genox_zdb_id = fe.genox_zdb_id
                 join figure on efs_fig_zdb_id = fig_zdb_id
             where genox_fish_zdb_id = :fishID 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("fishID", fish.getZdbID());

        return (List<String>) query.list();
    }

    public List<ExpressionFigureStage> getNonEfgExpressionResultsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select xpRslt from ExpressionFigureStage xpRslt, ExpressionExperiment2 xpExp, FishExperiment fishox
                  where fishox.fish = :fish
                    and fishox = xpExp.fishExperiment
                    and xpRslt.expressionExperiment = xpExp
                    and xpExp.gene is not null
                    and xpExp.gene.zdbID not like :markerId
            """;
        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("fish", fish);
        query.setParameter("markerId", "%" + "ZDB-EFG" + "%");

        return query.list();
    }

    public List<ExpressionFigureStage> getEfgExpressionResultsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select xpRslt from ExpressionFigureStage xpRslt, ExpressionExperiment2 xpExp, FishExperiment fishox
                  where fishox.fish = :fish
                    and fishox = xpExp.fishExperiment
                    and xpRslt.expressionExperiment = xpExp
                    and xpExp.gene IS NOT null
                    and xpExp.gene.zdbID like :markerId
            """;
        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("fish", fish);
        query.setParameter("markerId", "%" + "ZDB-EFG" + "%");

        return query.list();
    }

    public List<ExpressionFigureStage> getProteinExpressionResultsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select xpRslt from ExpressionFigureStage xpRslt, ExpressionExperiment2 xpExp, FishExperiment fishox 
                           where fishox.fish = :fish 
                             and fishox = xpExp.fishExperiment 
                             and xpRslt.expressionExperiment = xpExp 
                             and xpExp.antibody IS NOT null
                     """;
        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("fish", fish);

        return query.list();
    }

    public List<ExpressionFigureStage> getExpressionResultsBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select xpRslt from ExpressionFigureStage xpRslt, ExpressionExperiment2 xpExp, FishExperiment fishox, Fish fish, Genotype geno, CleanExpFastSrch cefs 
                    where fishox = xpExp.fishExperiment 
                    and fish = fishox.fish 
                    and geno = fish.genotype 

                    and xpRslt.expressionExperiment = xpExp 
             and (xpExp.gene.zdbID like 'ZDB-GENE%' or xpExp.gene.zdbID like '%RNAG%')
             and cefs.fishExperiment=fishox
             and cefs.gene=:str
            """;

        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("str", sequenceTargetingReagent);

        return query.list();
    }

    public List<String> getExpressionFigureIDsBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        String sql = """
             select distinct efs_fig_zdb_id  
                 from expression_figure_stage
                 join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                 join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                    join fish_experiment on expression_experiment2.xpatex_genox_zdb_id = fish_experiment.genox_zdb_id
                    join fish on fish_experiment.genox_fish_zdb_id = fish_zdb_id
                    join genotype on fish_genotype_zdb_id = geno_zdb_id
                    join clean_expression_fast_search cefs on fish_experiment.genox_zdb_id = cefs.cefs_genox_zdb_id
              where (xpatex_gene_zdb_id like 'ZDB-GENE%' or xpatex_gene_zdb_id like '%RNAG%') 
                and cefs_mrkr_zdb_id = :strID 
            """;

        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("strID", sequenceTargetingReagent.getZdbID());

        return (List<String>) query.list();
    }

    public List<String> getExpressionFigureIDsBySequenceTargetingReagentAndExpressedGene(SequenceTargetingReagent sequenceTargetingReagent, Marker expressedGene) {
        String sql = """
             select distinct efs_fig_zdb_id  
                 from expression_figure_stage
                 join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                 join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                    join fish_experiment on expression_experiment2.xpatex_genox_zdb_id = fish_experiment.genox_zdb_id
                    join fish on fish_experiment.genox_fish_zdb_id = fish_zdb_id
                    join genotype on fish_genotype_zdb_id = geno_zdb_id
                    join clean_expression_fast_search cefs on fish_experiment.genox_zdb_id = cefs.cefs_genox_zdb_id
             where (xpatex_gene_zdb_id like 'ZDB-GENE%' or xpatex_gene_zdb_id like '%RNAG%') 
               and xpatex_gene_zdb_id = :expressedGeneID 
               and cefs_mrkr_zdb_id = :strID 
            """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("strID", sequenceTargetingReagent.getZdbID());
        query.setParameter("expressedGeneID", expressedGene.getZdbID());
        return (List<String>) query.list();
    }

    public List<String> getExpressionPublicationIDsBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        String sql = """
             select distinct efs_fig_zdb_id  
                 from expression_figure_stage
                 join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                 join expression_experiment2 on xpatex_zdb_id = expression_figure_stage.efs_xpatex_zdb_id
                    join fish_experiment on expression_experiment2.xpatex_genox_zdb_id = fish_experiment.genox_zdb_id
                    join fish on fish_experiment.genox_fish_zdb_id = fish_zdb_id
                    join genotype on fish_genotype_zdb_id = geno_zdb_id
                    join clean_expression_fast_search cefs on fish_experiment.genox_zdb_id = cefs.cefs_genox_zdb_id
             where (xpatex_gene_zdb_id like 'ZDB-GENE%' or xpatex_gene_zdb_id like '%RNAG%') 
               and cefs_mrkr_zdb_id = :strID 
            """;

        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("strID", sequenceTargetingReagent.getZdbID());
        return (List<String>) query.list();
    }

    /**
     * Retrieve all terms that are used in an expression statement.
     *
     * @return set of expressed Terms.
     */
    @Override
    public Set<String> getAllDistinctExpressionTermIDs() {
        String hql = "select distinct result.superTerm.id from ExpressionResult2 as result";
        List<String> results = HibernateUtil.currentSession().createQuery(hql).list();
        Set<String> expressedTerms = new HashSet<>(2000);
        expressedTerms.addAll(results);
        // sub terms
        hql = "select distinct result.subTerm.id from ExpressionResult2 as result";
        results = HibernateUtil.currentSession().createQuery(hql).list();
        expressedTerms.addAll(results);
        return expressedTerms;
    }

    /**
     * Retrieve all terms that are used in a phenotype statement except pato terms.
     *
     * @return set of expressed Terms.
     */
    @Override
    public Set<String> getAllDistinctPhenotypeTermIDs() {
        String hql = "select distinct pheno.entity.superterm.id from PhenotypeStatement as pheno";
        List<String> results = HibernateUtil.currentSession().createQuery(hql).list();
        Set<String> expressedTerms = new HashSet<>(2000);
        expressedTerms.addAll(results);
        // sub terms
        hql = "select distinct pheno.entity.subterm.id from PhenotypeStatement as pheno";
        results = HibernateUtil.currentSession().createQuery(hql).list();
        expressedTerms.addAll(results);
        // super terms related
        hql = "select distinct pheno.relatedEntity.superterm.id from PhenotypeStatement as pheno";
        results = HibernateUtil.currentSession().createQuery(hql).list();
        expressedTerms.addAll(results);
        hql = "select distinct pheno.relatedEntity.subterm.id from PhenotypeStatement as pheno";
        results = HibernateUtil.currentSession().createQuery(hql).list();
        expressedTerms.addAll(results);
        return expressedTerms;
    }

    /**
     * Retrieve expression results for given super term and stage range.
     *
     * @param range
     * @return
     */
    @Override
    public List<ExpressionResult2> getExpressionResultsByTermAndStage(TermFigureStageRange range) {
        String hql = """
            SELECT distinct result 
            FROM 
            ExpressionResult2 result 
             WHERE 
            result.superTerm = :superTerm 
            AND result.expressionFigureStage.startStage = :start 
            AND result.expressionFigureStage.endStage = :end 
            """;
        Query<ExpressionResult2> query = HibernateUtil.currentSession().createQuery(hql, ExpressionResult2.class);
        query.setParameter("superTerm", range.getSuperTerm());
        query.setParameter("start", range.getStart());
        query.setParameter("end", range.getEnd());
        return query.list();
    }

    @Override
    public ExpressionResult2 getExpressionResult(Long expressionResultID) {
        return HibernateUtil.currentSession().get(ExpressionResult2.class, expressionResultID);
    }

    @Override
    public void deleteExpressionResult(ExpressionResult2 expressionResult) {
        HibernateUtil.currentSession().delete(expressionResult);
    }

    public List<GenericTerm> getWildTypeAnatomyExpressionForMarker(String zdbID) {
        String hql = """
            SELECT distinct ai 
            FROM 
            ExpressionResult2 er, GenericTerm ai 
             join er.expressionFigureStage efs
             join efs.expressionExperiment ee
             join ee.fishExperiment ge
             join ge.fish fish
             join fish.genotype g
             WHERE
            ee.gene.zdbID = :zdbID
            AND er.superTerm.oboID = ai.oboID
            AND er.expressionFound = true
            AND ge.standard = true
            AND g.wildtype= true
            ORDER BY ai.termName asc
            """;
        return currentSession().createQuery(hql, GenericTerm.class)
            .setParameter("zdbID", zdbID)
            .list();
    }

    public List<Figure> getFigures(ExpressionSummaryCriteria expressionCriteria) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select figure from Figure as figure
            left outer join figure.expressionFigureStage as figureStage
            left outer join figureStage.expressionResultSet as expressionResult
            inner join figureStage.expressionExperiment as expressionExperiment
            inner join expressionExperiment.fishExperiment as fishExperiment
            """;

        List<String> hqlClauses = new ArrayList<>();
        HashMap<String, Object> parameterMap = new HashMap<>();
        if (expressionCriteria.getGene() != null) {
            hqlClauses.add("expressionExperiment.gene = :gene");
            parameterMap.put("gene", expressionCriteria.getGene());
        }

        if (expressionCriteria.getFishExperiment() != null) {
            hqlClauses.add("fishExperiment = :fishExperiment");
            parameterMap.put("fishExperiment", expressionCriteria.getFishExperiment());
        }

        if (expressionCriteria.getFish() != null) {
            hqlClauses.add("fishExperiment.fish = :fish");
            parameterMap.put("fish", expressionCriteria.getFish());
        }

        if (expressionCriteria.getAntibody() != null) {
            hqlClauses.add("expressionExperiment.antibody = :antibody");
            parameterMap.put("antibody", expressionCriteria.getAntibody());
        }

        if (expressionCriteria.getEntity() != null) {

            if (expressionCriteria.getEntity().getSuperterm() != null
                && expressionCriteria.getEntity().getSubterm() != null) {
                hqlClauses.add("expressionResult.superTerm = :superTerm AND expressionResult.subTerm = :subTerm ");
                parameterMap.put("superTerm", expressionCriteria.getEntity().getSuperterm());
                parameterMap.put("subTerm", expressionCriteria.getEntity().getSubterm());
            }

            if (expressionCriteria.getEntity().getSuperterm() != null
                && expressionCriteria.getEntity().getSubterm() == null) {
                hqlClauses.add("expressionResult.superTerm = :superTerm AND expressionResult.subTerm is null");
                parameterMap.put("superTerm", expressionCriteria.getEntity().getSuperterm());
            }

        }


        if (expressionCriteria.getSingleTermEitherPosition() != null) {
            hqlClauses.add("(expressionResult.superTerm = :superTerm OR expressionResult.subTerm = :subTerm )");
            parameterMap.put("superTerm", expressionCriteria.getSingleTermEitherPosition());
            parameterMap.put("subTerm", expressionCriteria.getSingleTermEitherPosition());
        }

        if (expressionCriteria.getStart() != null) {
            hqlClauses.add("figureStage.startStage = :start");
            parameterMap.put("start", expressionCriteria.getStart());
        }

        if (expressionCriteria.getEnd() != null) {
            hqlClauses.add("figureStage.endStage = :end");
            parameterMap.put("end", expressionCriteria.getEnd());
        }

        if (expressionCriteria.isWithImagesOnly()) {
            hqlClauses.add("size(figure.images) > 0");
        }

        if (expressionCriteria.isWildtypeOnly()) {
            hqlClauses.add("fishExperiment.fish.wildtype = true");
            hqlClauses.add("fishExperiment.fish.genotype.wildtype = true");
        }

        if (expressionCriteria.isStandardEnvironment()) {
            hqlClauses.add("fishExperiment.standardOrGenericControl = true");
        }

        hql += hqlClauses.isEmpty() ? "" : " where " + String.join(" and ", hqlClauses);
        Query<Figure> query = session.createQuery(hql, Figure.class);
        parameterMap.forEach(query::setParameter);
        return new ArrayList<>(query.list());
    }


    @Override
    public Set<ExpressionStatement> getExpressionStatements(ExpressionSummaryCriteria expressionCriteria) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            from ExpressionResult2
            """;

        List<String> hqlClauses = new ArrayList<>();
        HashMap<String, Object> parameterMap = new HashMap<>();

        if (expressionCriteria.getFigure() != null) {
            hqlClauses.add("expressionFigureStage.figure.zdbID = :figureZdbID");
            parameterMap.put("figureZdbID", expressionCriteria.getFigure().getZdbID());
        }
        if (expressionCriteria.getGene() != null) {
            hqlClauses.add("expressionFigureStage.expressionExperiment.gene = :gene");
            parameterMap.put("gene", expressionCriteria.getGene());
        }
        if (expressionCriteria.getFishExperiment() != null) {
            hqlClauses.add("expressionFigureStage.expressionExperiment.fishExperiment = :genox");
            parameterMap.put("genox", expressionCriteria.getFishExperiment());
        }
        if (expressionCriteria.getFish() != null) {
            hqlClauses.add("expressionFigureStage.expressionExperiment.fishExperiment.fish = :fish");
            parameterMap.put("fish", expressionCriteria.getFish());
        }
        if (expressionCriteria.getAntibody() != null) {
            hqlClauses.add("expressionFigureStage.expressionExperiment.antibody = :antibody");
            parameterMap.put("antibody", expressionCriteria.getAntibody());
        }
        if (expressionCriteria.getStart() != null) {
            hqlClauses.add("expressionFigureStage.startStage = :start");
            parameterMap.put("start", expressionCriteria.getStart());
        }
        if (expressionCriteria.getEnd() != null) {
            hqlClauses.add("expressionFigureStage.endStage = :end");
            parameterMap.put("end", expressionCriteria.getEnd());
        }

        if (expressionCriteria.isWithImagesOnly()) {
            hqlClauses.add("size(expressionFigureStage.figure.images) > 0");
        }
        if (expressionCriteria.isWildtypeOnly()) {
            hqlClauses.add("expressionFigureStage.expressionExperiment.fishExperiment.fish.wildtype = true");
            hqlClauses.add("expressionFigureStage.expressionExperiment.fishExperiment.fish.genotype.wildtype = true");
        }
        if (expressionCriteria.isStandardEnvironment()) {
            hqlClauses.add("expressionFigureStage.expressionExperiment.fishExperiment.standardOrGenericControl = true");
        }

        hql += " where " + String.join(" and ", hqlClauses);
        Query<ExpressionResult2> query = session.createQuery(hql, ExpressionResult2.class);
        parameterMap.forEach(query::setParameter);

        Set<ExpressionStatement> expressionStatements = new TreeSet<>();
        Set<ExpressionResult2> results = new HashSet<>(query.list());
        for (ExpressionResult2 result : results) {
            ExpressionStatement statement = new ExpressionStatement();
            statement.setEntity(result.getEntity());
            statement.setExpressionFound(result.isExpressionFound());
            expressionStatements.add(statement);
        }
        return expressionStatements;
    }

    public List<ExpressionResult2> getExpressionOnSecondaryTerms() {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select result from ExpressionResult2 result 
                 where result.superTerm is not null AND result.superTerm.secondary = true
            """;
        Query<ExpressionResult2> query = session.createQuery(hql, ExpressionResult2.class);

        List<ExpressionResult2> allExpressions = new ArrayList<>(query.list());

        hql = "select result from ExpressionResult2 result " +
              "     where result is not null AND result.subTerm is not null AND result.subTerm.secondary = true";
        Query<ExpressionResult2> queryEntitySub = session.createQuery(hql, ExpressionResult2.class);
        allExpressions.addAll(queryEntitySub.list());

        return allExpressions;
    }

    /**
     * Retrieve list of expression result records that use obsoleted terms in the annotation.
     *
     * @return
     */
    @Override
    public List<ExpressionResult2> getExpressionOnObsoletedTerms() {
        Session session = HibernateUtil.currentSession();

        String hql = """
            from ExpressionResult2 
                 where superTerm is not null AND superTerm.obsolete = true
            """;
        Query<ExpressionResult2> query = session.createQuery(hql, ExpressionResult2.class);

        List<ExpressionResult2> allExpressions = new ArrayList<>(query.list());

        hql = "from ExpressionResult2 " +
              "     where subTerm is not null AND subTerm.obsolete = true";
        Query<ExpressionResult2> queryEntitySub = session.createQuery(hql, ExpressionResult2.class);
        allExpressions.addAll(queryEntitySub.list());

        return allExpressions;
    }

    @Override
    public int getImagesFromPubAndClone(PublicationExpressionBean publicationExpressionBean) {
        String sql = """
            select count(distinct img_zdb_id) 
                         from image
                          join expression_figure_stage on img_fig_zdb_id=efs_fig_zdb_id
                          join expression_experiment2 on expression_figure_stage.efs_xpatex_zdb_id = expression_experiment2.xpatex_zdb_id
                          join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
                          join clone on expression_experiment2.xpatex_probe_feature_zdb_id=clone_mrkr_zdb_id
            	     where xpatex_source_zdb_id= :pubZdbId 
                         and  xpatex_probe_feature_zdb_id=:probeZdbId
            """;
        return Integer.parseInt(
            HibernateUtil.currentSession().createNativeQuery(sql)
                .setParameter("pubZdbId", publicationExpressionBean.getPublicationZdbID())
                .setParameter("probeZdbId", publicationExpressionBean.getProbeFeatureZdbId())
                .uniqueResult().toString()
        );
    }

    @Override
    public int getImagesForEfg(PublicationExpressionBean publicationExpressionBean) {
        String sql = """
            select count(distinct img_zdb_id)
                         from image
                          join expression_figure_stage on img_fig_zdb_id=efs_fig_zdb_id
                          join expression_experiment2 on expression_figure_stage.efs_xpatex_zdb_id = expression_experiment2.xpatex_zdb_id
                          join expression_result2 on expression_figure_stage.efs_pk_id = expression_result2.xpatres_efs_id
            	     where xpatex_source_zdb_id= :pubZdbId 
            """;
        return Integer.parseInt(
            HibernateUtil.currentSession().createNativeQuery(sql)
                .setParameter("pubZdbId", publicationExpressionBean.getPublicationZdbID())
                .uniqueResult().toString()
        );
    }


    @Override
    public long getExpressionExperimentByFishAndPublication(Fish fish, String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(*) from ExpressionExperiment2 " +
                     "      where  publication.zdbID = :publicationID" +
                     "        and fishExperiment.fish = :fish ";

        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);
        query.setParameter("publicationID", publicationID);

        return (long) query.uniqueResult();
    }

    @Override
    public List<ExpressionExperiment2> getExperiments2(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select experiment from ExpressionExperiment2 experiment
                   left join experiment.gene as gene 
                 where experiment.publication.zdbID = :pubID 
                order by gene.abbreviationOrder, 
                         experiment.fishExperiment.fish.name, 
                         experiment.assay.displayOrder 
            """;
        Query<ExpressionExperiment2> query = session.createQuery(hql, ExpressionExperiment2.class);
        query.setParameter("pubID", publicationID);

        return query.list();
    }

    @Override
    public void createExpressionFigureStage(ExpressionFigureStage expressionFigureStage) {
        Session session = HibernateUtil.currentSession();
        session.save(expressionFigureStage);
    }

    @Override
    public List<ExpressionResult2> getPhenotypeFromExpressionsByFigureFish(String publicationID, String figureID, String fishID, String featureID) {
        String hql = """
            select result from ExpressionResult2 result 
                 where result.expressionFigureStage.expressionExperiment.publication.zdbID = :pubID 
                 and result.phenotypeTermSet IS NOT EMPTY 
            """;
        if (StringUtils.isNotEmpty(figureID))
            hql += "     and result.expressionFigureStage.figure.zdbID = :figID ";
        if (StringUtils.isNotEmpty(fishID))
            hql += "     and result.expressionFigureStage.expressionExperiment.fishExperiment.fish.zdbID = :fishID ";
        hql += """
                order by result.expressionFigureStage.figure.orderingLabel, 
                         result.expressionFigureStage.expressionExperiment.fishExperiment.fish.nameOrder, 
                         result.expressionFigureStage.startStage.abbreviation 
            """;
        Query<ExpressionResult2> query = HibernateUtil.currentSession().createQuery(hql, ExpressionResult2.class);
        query.setParameter("pubID", publicationID);
        if (StringUtils.isNotEmpty(figureID))
            query.setParameter("figID", figureID);
        if (StringUtils.isNotEmpty(fishID))
            query.setParameter("fishID", fishID);

        return query.list();
    }

    @Override
    public List<ExpressionResult2> getPhenotypeFromExpressionsByFeatureSlowPerformance(String featureID) {
        String hql = """
            select result from ExpressionResult2 result, GenotypeFeature genoFeature 
                 where result.phenotypeTermSet IS NOT EMPTY 
             AND genoFeature.feature.zdbID = :zdbID 
             AND genoFeature in elements(result.expressionFigureStage.expressionExperiment.fishExperiment.fish.genotype.genotypeFeatures) 
            """;
        Query<ExpressionResult2> query = HibernateUtil.currentSession().createQuery(hql, ExpressionResult2.class);
        query.setParameter("zdbID", featureID);

        return query.list();
    }

    @Override
    public List<ExpressionResult2> getPhenotypeFromExpressionsByFeature(String featureID) {
        String hql = """
                select result
                from ExpressionResult2 result
                join result.expressionFigureStage efs
                join efs.expressionExperiment ee
                join ee.fishExperiment fe
                join fe.fish f
                join f.genotype g
                join g.genotypeFeatures genoFeature
                where result.phenotypeTermSet IS NOT EMPTY
                  and genoFeature.feature.zdbID = :zdbID
            """;
        Query<ExpressionResult2> query = HibernateUtil.currentSession().createQuery(hql, ExpressionResult2.class);
        query.setParameter("zdbID", featureID);

        return query.list();
    }

    @Override
    public List<ExpressionExperiment2> getExperiment2sByAntibody(Antibody antibody) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            from ExpressionExperiment2 where antibody = :antibody
            """;
        Query<ExpressionExperiment2> query = session.createQuery(hql, ExpressionExperiment2.class);
        query.setParameter("antibody", antibody);
        return query.list();
    }

    @Override
    public List<ExpressionExperiment2> getExpressionExperiment2sByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select xpExp from ExpressionExperiment2 xpExp, FishExperiment fishox
                  where fishox.fish = :fish
                    and fishox = xpExp.fishExperiment
                    """;
        Query<ExpressionExperiment2> query = session.createQuery(hql, ExpressionExperiment2.class);
        query.setParameter("fish", fish);
        return query.list();
    }

    @Override
    public List<ExpressionResult2> getExpressionResultList(Marker gene) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select result from ExpressionResult2 as result 
                  where expressionFigureStage.expressionExperiment.gene = :gene 
            AND result.phenotypeTermSet is not empty 
            """;
        Query<ExpressionResult2> query = session.createQuery(hql, ExpressionResult2.class);
        query.setParameter("gene", gene);

        return query.list();
    }

    @Override
    public List<ExpressionResult2> getNonEapExpressionResultList(Marker gene) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select result from ExpressionResult2 as result 
                  where expressionFigureStage.expressionExperiment.gene = :gene 
            AND result.phenotypeTermSet is empty 
            """;
        Query<ExpressionResult2> query = session.createQuery(hql, ExpressionResult2.class);
        query.setParameter("gene", gene);

        return query.list();
    }

    @Override
    public List<Experiment> geExperimentByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            from Experiment  
                  where publication.zdbID = :pubID 
                  order by name 
            """;
        Query<Experiment> query = session.createQuery(hql, Experiment.class);
        query.setParameter("pubID", publicationID);

        return query.list();
    }

    @Override
    public void deleteExperimentCondition(ExperimentCondition condition) {
        HibernateUtil.currentSession().delete(condition);
    }

    @Override
    public ExperimentCondition getExperimentCondition(String conditionID) {
        return HibernateUtil.currentSession().get(ExperimentCondition.class, conditionID);
    }

    @Override
    public void saveExperimentCondition(ExperimentCondition condition) {
        HibernateUtil.currentSession().save(condition);
        HibernateUtil.currentSession().saveOrUpdate(condition.getExperiment());
    }

    @Override
    public void saveExperiment(Experiment experiment) {
        HibernateUtil.currentSession().save(experiment);
    }

    @Override
    public ExpressionFigureStage getExperimentFigureStage(long id) {
        return HibernateUtil.currentSession().get(ExpressionFigureStage.class, id);
    }

    @Override
    public List<ExpressionFigureStage> getExperimentFigureStageByFigure(Figure fig) {
        Session session = HibernateUtil.currentSession();

        String hql = """
             from ExpressionFigureStage  
                  where figure.zdbID = :figID 
            """;

        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameter("figID", fig.getZdbID());

        return query.list();
    }

    @Override
    public List<MarkerDBLink> getAllDbLinks(ForeignDB.AvailableName database) {
        Session session = HibernateUtil.currentSession();
        Query<MarkerDBLink> query = session.createQuery("""
            from MarkerDBLink 
            where referenceDatabase.foreignDB.dbName = :database
            """, MarkerDBLink.class);
        query.setParameter("database", database);
        return query.list();
    }

    @Override
    public ExpressionResult2 getExpressionResult2(long id) {
        ExpressionResult2 expressionResult2 = currentSession().get(ExpressionResult2.class, id);
        return expressionResult2;
    }

    public List<ExpressionFigureStage> getExperimentFigureStagesByIds(List<Integer> expressionIDs) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct efs from ExpressionFigureStage as efs
                   left join efs.expressionExperiment.gene as gene
                   left join fetch efs.startStage
                   left join fetch efs.endStage
                   left join fetch efs.expressionExperiment
                   left join fetch efs.expressionResultSet
                   join fetch efs.figure
                   join efs.expressionExperiment.fishExperiment.fish as fish
                   where efs.id in :ids
                   """;

        Query<ExpressionFigureStage> query = session.createQuery(hql, ExpressionFigureStage.class);
        query.setParameterList("ids", expressionIDs.stream().map(Long::valueOf).collect(Collectors.toList()));
        List<ExpressionFigureStage> resultList = query.list();

        // Use LinkedHashSet to distinctify and preserve order
        Set<ExpressionFigureStage> distinctResults = new LinkedHashSet<>(resultList);
        return new ArrayList<>(distinctResults);
    }


    @Override
    public List<HTPDataset> getAllHTPDatasets() {
        Session session = HibernateUtil.currentSession();
        String hql = "select htpdset from HTPDataset htpdset";
        Query<HTPDataset> query = session.createQuery(hql, HTPDataset.class);
        return query.list();
    }

    @Override
    public ArrayList<String> getHTPSecondaryIds(String datasetId) {

        Session session = HibernateUtil.currentSession();
        String hql = """
            select htpaid.accessionNumber from HTPDatasetAlternateIdentifier htpaid
                 where htpaid.htpDataset.zdbID = :datasetId
            """;
        Query query = session.createQuery(hql);
        query.setParameter("datasetId", datasetId);
        return (ArrayList<String>) query.list();
    }

    @Override
    public ArrayList<Publication> getHTPPubs(String datasetId) {

        Session session = HibernateUtil.currentSession();
        String hql = """
            select htppid.publication from HTPDatasetPublication htppid
                 where htppid.htpDataset.zdbID = :datasetId
            """;
        Query query = session.createQuery(hql);
        query.setParameter("datasetId", datasetId);
        return (ArrayList<Publication>) query.list();
    }


    @Override
    public ArrayList<String> getCategoryTags(String datasetId) {

        Session session = HibernateUtil.currentSession();

        String hql = """
            select cvtag.categoryTag from HTPDatasetCategoryTag xtag, HTPCategoryTag cvtag
                 where xtag.htpDataset.zdbID = :datasetId
                 and  cvtag.zdbID = xtag.categoryTag 
            """;
        Query query = session.createQuery(hql);
        query.setParameter("datasetId", datasetId);
        return (ArrayList<String>) query.list();
    }

    @Override
    public ArrayList<HTPDatasetSample> getAllHTPDatasetSamples() {
        Session session = HibernateUtil.currentSession();
        String hql = "select htpdsetsample from HTPDatasetSample htpdsetsample";
        Query query = session.createQuery(hql);
        return (ArrayList<HTPDatasetSample>) query.list();
    }

    @Override
    public ArrayList<HTPDatasetSampleDetail> getAllHTPDatasetSampleDetails() {
        Session session = HibernateUtil.currentSession();
        String hql = "select htpdsetsampledetail from HTPDatasetSampleDetail htpdsetsampledetail";
        Query query = session.createQuery(hql);
        return (ArrayList<HTPDatasetSampleDetail>) query.list();
    }

    @Override
    public List<HTPDatasetSampleDetail> getSampleDetail(HTPDatasetSample sample) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select htpdsetsampledetail from HTPDatasetSampleDetail htpdsetsampledetail 
            where htpdsetsampledetail.htpDatasetSample = :sample
            """;
        Query<HTPDatasetSampleDetail> query = session.createQuery(hql, HTPDatasetSampleDetail.class);
        query.setParameter("sample", sample);
        return query.list();
    }

    @Override
    public List<ExpressionResult2> getAllExpressionResults() {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select result from ExpressionResult2 result
            left join fetch result.expressionFigureStage as figureStage
            left join fetch figureStage.expressionExperiment as experiment
            left join fetch experiment.gene as gene
            left join fetch figureStage.startStage as start
            left join fetch figureStage.endStage as end
            left join fetch experiment.fishExperiment as fishExperiment
            left join fetch fishExperiment.fish as fish
            left join fetch figureStage.figure
            where experiment.gene is not null
            """;
        Query<ExpressionResult2> query = session.createQuery(hql, ExpressionResult2.class);
        return query.list();
    }

    @Override
    public DanioCellMapping getDanioCellMappingForMarkerID(String markerID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from DanioCellMapping where markerZdbID = :markerID";
        Query<DanioCellMapping> query = session.createQuery(hql, DanioCellMapping.class);
        query.setParameter("markerID", markerID);
        return query.uniqueResult();
    }
}

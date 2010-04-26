package org.zfin.marker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.ReplacementZdbID;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.MappedMarker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.AntibodyWikiWebService;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class MergeService {

    private static Logger logger = Logger.getLogger(MarkerService.class);
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    /**
     * Validate that markers are the same type, and then do the merge.  We can assume that the
     * validation is done somewhere else (ie, the controller).
     *
     * 1. move the components (probably some common methods).
     * 2. add the alias from the deleted one into the markerToMergeInto
     * 3. to specific merging for the marker we are merging into.
     * 4. delete the old one
     *
     * I tried to follow fairly closely to process_delete here.
     *
     * @param markerToDelete Marker to strip attributes from and eventually delete
     * @param markerToMergeInto Marker to add attributes to
     * @return Process status
     */
    public static boolean mergeMarker(Marker markerToDelete,Marker markerToMergeInto) {
        // 1. move the components (probably some common methods).
        mergeRelatedMarkers(markerToDelete,markerToMergeInto) ;
        mergeSuppliers(markerToDelete,markerToMergeInto) ;
        mergeDirectAttributions(markerToDelete,markerToMergeInto) ;
        mergeLinkageData(markerToDelete,markerToMergeInto) ;
        mergeDataNotes(markerToDelete,markerToMergeInto) ;

        MarkerAlias newMarkerAlias = mergeAliases(markerToDelete,markerToMergeInto) ;
        createMarkerHistory(markerToDelete,markerToMergeInto,newMarkerAlias) ;
        mergeReplacedData(markerToDelete,markerToMergeInto) ;
        mergePublicNote(markerToDelete,markerToMergeInto) ;


        // 3. handle specific stuff for this marker type
        if(markerToDelete.isInTypeGroup(Marker.TypeGroup.ATB)
                &&
                markerToMergeInto.isInTypeGroup(Marker.TypeGroup.ATB)){
            Antibody antibodyToDelete = (Antibody) markerToDelete ;
            Antibody antibodyToMergeInto = (Antibody) markerToMergeInto;
            mergeAntibody(antibodyToDelete,antibodyToMergeInto) ;
        }
//        else
//        if(markerToDelete.isInTypeGroup(Marker.TypeGroup.GENEDOM)
//                &&
//                markerToMergeInto.isInTypeGroup(Marker.TypeGroup.GENEDOM)){
//            // expression experiments, marker go term evidence, feature relationships, etc.
////            mergeGene(antibodyToDelete,antibodyToMergeInto) ;
//        }
//        else
//        if(markerToDelete.isInTypeGroup(Marker.TypeGroup.CLONEDOM)
//                &&
//                markerToMergeInto.isInTypeGroup(Marker.TypeGroup.CLONEDOM)){
//            // expression experiments, clone data, etc.
////            mergeClone(antibodyToDelete,antibodyToMergeInto) ;
//        }


        infrastructureRepository.insertUpdatesTable(markerToMergeInto,"all"
                ,"Merged in marker: "+markerToDelete.getAbbreviation()+"("+markerToDelete.getZdbID()+")"
                , Person.getCurrentSecurityUser() );


        // regen
        markerRepository.runMarkerNameFastSearchUpdate(markerToMergeInto);

        // this may be unnecessary
        HibernateUtil.currentSession().update(markerToMergeInto) ;


        // 4. finally delete the old marker
        return deleteMarker(markerToDelete) ;
//        throw new RuntimeException("Marker merging not supported:\n"+ markerToDelete+"\n"+markerToMergeInto);
    }

    private static void createMarkerHistory(Marker markerToDelete, Marker markerToMergeInto, MarkerAlias newMarkerAlias) {
        MarkerHistory markerHistory = markerRepository.createMarkerHistory(
                markerToMergeInto
                ,markerToDelete
                ,MarkerHistory.Event.MERGED
                ,MarkerHistory.Reason.SAME_MARKER
                , newMarkerAlias
        );


        HibernateUtil.currentSession().save(markerHistory);
        markerToMergeInto.getMarkerHistory().add(markerHistory);

        if(CollectionUtils.isNotEmpty(markerToDelete.getMarkerHistory()))
            for(MarkerHistory markerHistoryLoop: markerToDelete.getMarkerHistory())
                markerHistoryLoop.setMarker(markerToMergeInto);

    }

    private static void mergeAntibody(Antibody antibodyToDelete, Antibody antibodyToMergeInto) {
        if(CollectionUtils.isNotEmpty(antibodyToDelete.getExternalNotes()))
            for(AntibodyExternalNote antibodyExternalNote: antibodyToDelete.getExternalNotes()){
                antibodyExternalNote.setAntibody(antibodyToMergeInto);
            }

        // merge assays/labeling
        if(CollectionUtils.isNotEmpty(antibodyToDelete.getAntibodyLabelings())){
            mergeAntibodyLabeling(antibodyToDelete,antibodyToMergeInto) ;
        }

        // merge actual antibody data
        // if the antibody to merge into is null then copy over the data
        // there can not be differences in this data if not null, but validation should
        // have already caught this
        if(antibodyToMergeInto.getHostSpecies()==null && antibodyToDelete.getHostSpecies()!=null){
            antibodyToMergeInto.setHostSpecies(antibodyToDelete.getHostSpecies());
        }
        if(antibodyToMergeInto.getImmunogenSpecies()==null && antibodyToDelete.getImmunogenSpecies()!=null){
            antibodyToMergeInto.setImmunogenSpecies(antibodyToDelete.getImmunogenSpecies());
        }
        if(antibodyToMergeInto.getClonalType()==null && antibodyToDelete.getClonalType()!=null){
            antibodyToMergeInto.setClonalType(antibodyToDelete.getClonalType());
        }
        if(antibodyToMergeInto.getHeavyChainIsotype()==null && antibodyToDelete.getHeavyChainIsotype()!=null){
            antibodyToMergeInto.setHeavyChainIsotype(antibodyToDelete.getHeavyChainIsotype());
        }
        if(antibodyToMergeInto.getLightChainIsotype()==null && antibodyToDelete.getLightChainIsotype()!=null){
            antibodyToMergeInto.setLightChainIsotype(antibodyToDelete.getLightChainIsotype());
        }


        try {
            AntibodyWikiWebService.getInstance().updatePageForAntibody(antibodyToMergeInto,antibodyToDelete.getAbbreviation()) ;
        } catch (Exception e) {
            logger.error("Failed to update antibody: "+antibodyToMergeInto.getAbbreviation(),e);
        }

    }

    /**
     * Given antibodies A and B, where A will be merged into B and then deleted

     for all expression experiments on A:  EEa
     if EEa not contained in antibody B: then
     update the antibody on EEa to point to antibody B
     else
     for all expression_results in EEa: ERa
     if ERa not contained in expresion_results on EEb (ERb): then
     update ERa to point to EEb
     else
     for all expresion_result_figures in ERa: ERFa
     if ERFa not contained in expression_results_figure on ERb (ERFb): then
     update ERFa to point to ERb
     else
     nada

     delete A
     * @param antibodyA, Antibody antibodyB) { Antibody to merge expression away from (and then delete).
     * @param antibodyB Antibody to merge expression on to.
     */
    @SuppressWarnings("unchecked")
    protected static void mergeAntibodyLabeling(Antibody antibodyA, Antibody antibodyB) {

        if(CollectionUtils.isEmpty(antibodyA.getAntibodyLabelings())) return ;

        Set<ExpressionExperiment> antibodyLabelingsARemoveSet = new HashSet<ExpressionExperiment>() ;

//        for all expression experiments on A:  EEa
        for(ExpressionExperiment expressionExperimentA : antibodyA.getAntibodyLabelings()){
//          if EEa not contained in antibody B: then
//            update the antibody on EEa to point to antibody B
            ExpressionExperiment expressionExperimentB = antibodyB.getMatchingAntibodyLabeling(expressionExperimentA) ;
            if(expressionExperimentB==null){
                // move out of the way both ways
                expressionExperimentA.setAntibody(antibodyB);
                antibodyB.getAntibodyLabelings().add(expressionExperimentA);
                antibodyLabelingsARemoveSet.add(expressionExperimentA) ;
            }
            //else
            // there is a match, then we move the expression results
            else
            if(CollectionUtils.isNotEmpty(expressionExperimentA.getExpressionResults())) {
                Set<ExpressionResult> expressionResultRemoveSet = moveExpressionResults(expressionExperimentA,expressionExperimentB) ;
                expressionExperimentA.getExpressionResults().removeAll(expressionResultRemoveSet) ;
            }
        }

        // cleanup things to remove:
        antibodyA.getAntibodyLabelings().removeAll(antibodyLabelingsARemoveSet) ;

    }

    private static Set<ExpressionResult> moveExpressionResults(ExpressionExperiment expressionExperimentA, ExpressionExperiment expressionExperimentB) {
        Set<ExpressionResult> expressionResultRemoveSet= new HashSet<ExpressionResult>() ;
        for(ExpressionResult expressionResultA : expressionExperimentA.getExpressionResults()){
            ExpressionResult expressionResultB = expressionExperimentB.getMatchingExpressionResult(expressionResultA) ;
            if(expressionResultB==null){
                expressionResultA.setExpressionExperiment(expressionExperimentB);
                expressionExperimentB.getExpressionResults().add(expressionResultA) ;
                expressionResultRemoveSet.add(expressionResultA) ;
//                        expressionExperimentA.getExpressionResults().remove(expressionResultA) ;
            }
            // if there is a match then we move the expression result figures
            else{
                HibernateUtil.currentSession().evict(expressionResultA);
                expressionResultA = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class,expressionResultA.getZdbID()) ;
                HibernateUtil.currentSession().evict(expressionResultB);
                expressionResultB = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class,expressionResultB.getZdbID()) ;

                moveFigures(expressionResultA,expressionResultB) ;
            }
        }
        return expressionResultRemoveSet ;
    }

    private static void moveFigures(ExpressionResult expressionResultA, ExpressionResult expressionResultB) {
        if(CollectionUtils.isNotEmpty(expressionResultA.getFigures())){
            for(Figure figureA: expressionResultA.getFigures()){
                Figure figureB = expressionResultB.getMatchingFigure(figureA) ;
                if(figureB==null){
                    // have to do this via SQL
//                                figureA.setExpressionResult(expressionResultB) ;
                    // move figure A to to expression result B if not already there

                    Object[] existingXpatfig = (Object[]) HibernateUtil.currentSession().createSQLQuery(
                            "select * " +
                                    " from expression_pattern_figure epf " +
                                    " where xpatfig_fig_zdb_id= :figureZdbID " +
                                    " and xpatfig_xpatres_zdb_id= :resultZdbID "
                    )
                            .setString("resultZdbID",expressionResultB.getZdbID())
                            .setString("figureZdbID",figureA.getZdbID())
                            .uniqueResult();

                    if(existingXpatfig==null){
                        HibernateUtil.currentSession().createSQLQuery(
                                " update expression_pattern_figure  " +
                                        " set xpatfig_xpatres_zdb_id= :resultBZdbID " +
                                        " where xpatfig_fig_zdb_id= :figureZdbID" +
                                        " and xpatfig_xpatres_zdb_id= :resultAZdbID "
                        )
                                .setString("resultBZdbID",expressionResultB.getZdbID())
                                .setString("figureZdbID",figureA.getZdbID())
                                .setString("resultAZdbID",expressionResultA.getZdbID())
                                .executeUpdate();
                        HibernateUtil.currentSession().refresh(figureA) ;
                        HibernateUtil.currentSession().refresh(expressionResultB);
                        HibernateUtil.currentSession().refresh(expressionResultA);
                        HibernateUtil.currentSession().flush();
                    }

                }
            }
        }
    }

    private static void mergePublicNote(Marker markerToDelete, Marker markerToMergeInto) {
        // transfer public note
        if(StringUtils.isNotEmpty(markerToDelete.getPublicComments())){
            String publicComment = markerToMergeInto.getPublicComments();
            publicComment = publicComment + "\n Comment from merged marker ["+markerToDelete.getAbbreviation()+"]:\n"+ markerToDelete.getPublicComments() ;
            markerToMergeInto.setPublicComments(publicComment);
        }
    }

    private static void mergeReplacedData(Marker markerToDelete, Marker markerToMergeInto) {
        // add data replacement
        HibernateUtil.currentSession().createSQLQuery("update zdb_replaced_data \n" +
                "                    set zrepld_new_zdb_id = :markerToMergeIntoZdbID \n" +
                "                    , zrepld_old_name = :oldName \n" +
                "                  where zrepld_new_zdb_id = :markerToDeleteZdbID ;")
                .setString("markerToMergeIntoZdbID",markerToMergeInto.getZdbID())
                .setString("oldName",markerToDelete.getAbbreviation())
                .setString("markerToDeleteZdbID",markerToDelete.getZdbID())
                .executeUpdate() ;

        ReplacementZdbID replacementZdbID = new ReplacementZdbID();
        replacementZdbID.setOldName(markerToDelete.getAbbreviation());
        replacementZdbID.setOldZdbID(markerToDelete.getZdbID());
        replacementZdbID.setReplacementZdbID(markerToMergeInto.getZdbID());
        HibernateUtil.currentSession().save(replacementZdbID) ;

    }

    protected static MarkerAlias mergeAliases(Marker markerToDelete, Marker markerToMergeInto) {
        // 2. need to add an alias to this markerToMergeInto
        // A - no overlap, create new alias from markertodelete and move all aliases over, add marker history for new alias
        // B - overlap in aliases only, create new alias from markertodelete and and combine attributions of matching alias, use old alias for marker history
        // C - markertomergeinto already has alias of markertodelete name, do not create new alias (no attribution to add), but move rest over
        // D - markertodelete has alias that is name of markertomergeinto, I think that this is case A
        String newAlias = markerToDelete.getAbbreviation();

        MarkerAlias newMarkerAlias = markerToMergeInto.getAlias(newAlias) ;
        // create new alias if not contained in marker to merge into
        if(null==newMarkerAlias){
            newMarkerAlias = markerRepository.addMarkerAlias(markerToMergeInto,markerToDelete.getAbbreviation(),null);
            Set<MarkerAlias> markerAliases = markerToMergeInto.getAliases();
            markerAliases.add(newMarkerAlias) ;
            markerToMergeInto.setAliases(markerAliases);
        }

        // data alias
        if(CollectionUtils.isNotEmpty(markerToDelete.getAliases()))
            for(MarkerAlias markerAlias : markerToDelete.getAliases()){
                MarkerAlias existingMarkerAlias = markerToMergeInto.getAlias(markerAlias.getAlias());
                // if there is no alias overlap, then just move the alias to this marker
                if(existingMarkerAlias==null){
                    markerAlias.setMarker(markerToMergeInto);
                }
                // if there IS an alias overlap, then combine attributions onto the existing one
                else{
                    for(PublicationAttribution publicationAttribution: markerAlias.getPublications()){
                        if(false==existingMarkerAlias.hasPublication(publicationAttribution)){
                            existingMarkerAlias.addPublication(publicationAttribution) ;
                        }
                    }
                }
            }

        return newMarkerAlias ;
    }


    /**
     * TODO: check for potiential key conflicts
     * @param markerToDelete
     * @param markerToMergeInto
     */
    private static void mergeMarkerGoTermEvidence(Marker markerToDelete, Marker markerToMergeInto) {
        // marker go term evidence
        HibernateUtil.currentSession().createSQLQuery("update marker_go_term_evidence \n" +
                "                    set mrkrgoev_mrkr_zdb_id = :markerToMergeIntoZdbID \n" +
                "                  where mrkrgoev_mrkr_zdb_id = :markerToDeleteZdbID ;")
                .setString("markerToMergeIntoZdbID",markerToMergeInto.getZdbID())
                .setString("markerToDeleteZdbID",markerToDelete.getZdbID())
                .executeUpdate() ;

    }

    private static void mergeDataNotes(Marker markerToDelete, Marker markerToMergeInto) {
        // handle data notes
        if(CollectionUtils.isNotEmpty(markerToDelete.getDataNotes()))
            for(DataNote dataNote : markerToDelete.getDataNotes())
                dataNote.setDataZdbID(markerToMergeInto.getZdbID());
    }

    private static void mergeLinkageData(Marker markerToDelete, Marker markerToMergeInto) {
        // linkage groups, etc.
        // mapped marker
        if(CollectionUtils.isNotEmpty(markerToDelete.getDirectPanelMappings()))
            for(MappedMarker mappedMarker: markerToDelete.getDirectPanelMappings())
                mappedMarker.setMarker(markerToMergeInto);

        HibernateUtil.currentSession().createSQLQuery("update linkage_member \n" +
                "                    set lnkgmem_member_zdb_id = :markerToMergeIntoZdbID \n" +
                "                  where lnkgmem_member_zdb_id = :markerToDeleteZdbID ;")
                .setString("markerToMergeIntoZdbID",markerToMergeInto.getZdbID())
                .setString("markerToDeleteZdbID",markerToDelete.getZdbID())
                .executeUpdate() ;

        HibernateUtil.currentSession().createSQLQuery("update linkage_pair_member \n" +
                "                    set lpmem_member_zdb_id = :markerToMergeIntoZdbID \n" +
                "                  where lpmem_member_zdb_id = :markerToDeleteZdbID ;")
                .setString("markerToMergeIntoZdbID",markerToMergeInto.getZdbID())
                .setString("markerToDeleteZdbID",markerToDelete.getZdbID())
                .executeUpdate() ;
        // update primer set??
        HibernateUtil.currentSession().createSQLQuery("update primer_set \n" +
                "                    set marker_id = :markerToMergeIntoZdbID \n" +
                "                  where marker_id = :markerToDeleteZdbID ;")
                .setString("markerToMergeIntoZdbID",markerToMergeInto.getZdbID())
                .setString("markerToDeleteZdbID",markerToDelete.getZdbID())
                .executeUpdate() ;
    }

    private static void mergeDirectAttributions(Marker markerToDelete, Marker markerToMergeInto) {
        // move pub attributions
        if(CollectionUtils.isNotEmpty(markerToDelete.getPublications()))
            for(PublicationAttribution publicationAttribution: markerToDelete.getPublications()){
                if(false==markerToMergeInto.hasPublicationAttribution(publicationAttribution)){
                    HibernateUtil.currentSession().createSQLQuery("update record_attribution \n" +
                            "                    set recattrib_data_zdb_id = :markerToMergeIntoZdbID \n" +
                            "                  where recattrib_data_zdb_id = :markerToDeleteZdbID " +
                            "                  and recattrib_source_zdb_id = :pubZdbID " +
                            ";")
                            .setString("markerToMergeIntoZdbID",markerToMergeInto.getZdbID())
                            .setString("markerToDeleteZdbID",markerToDelete.getZdbID())
                            .setString("pubZdbID",publicationAttribution.getPublication().getZdbID())
                            .executeUpdate() ;
                }
            }
    }

    private static void mergeSuppliers(Marker markerToDelete, Marker markerToMergeInto) {
        // suppliers/source
        if(CollectionUtils.isNotEmpty(markerToDelete.getSuppliers()))
            for(MarkerSupplier markerSupplier : markerToDelete.getSuppliers()){
                if(false==markerToMergeInto.hasSupplier(markerSupplier)){
                    HibernateUtil.currentSession().createQuery(" update MarkerSupplier ms "+
                            " set ms.dataZdbID = :markerToMergeIntoZdbID " +
                            " where ms.dataZdbID = :markerToDeleteZdbID " +
                            " and ms.organization.zdbID = :organizationZdBID " +
                            "")
                            .setString("markerToMergeIntoZdbID",markerToMergeInto.getZdbID())
                            .setString("markerToDeleteZdbID",markerToDelete.getZdbID())
                            .setString("organizationZdBID",markerSupplier.getOrganization().getZdbID())
                            .executeUpdate();
                }
            }
    }

    private static void mergeRelatedMarkers(Marker markerToDelete, Marker markerToMergeInto) {
        // related markers
        if(CollectionUtils.isNotEmpty(markerToDelete.getFirstMarkerRelationships()))
            for(MarkerRelationship markerRelationship: markerToDelete.getFirstMarkerRelationships()){
                if(false==markerRelationship.getFirstMarker().hasFirstMarkerRelationships(markerToMergeInto)){
                    markerRelationship.setFirstMarker(markerToMergeInto);
                    HibernateUtil.currentSession().update(markerRelationship);
                }
            }

        if(CollectionUtils.isNotEmpty(markerToDelete.getSecondMarkerRelationships()))
            for(MarkerRelationship markerRelationship: markerToDelete.getSecondMarkerRelationships()){
                if(false==markerRelationship.getFirstMarker().hasSecondMarkerRelationships(markerToMergeInto)){
                    logger.info("marker relationship: " + markerRelationship);
                    markerRelationship.setSecondMarker(markerToMergeInto);
                    HibernateUtil.currentSession().update(markerRelationship);
                }
            }
    }

    public static boolean deleteMarker(Marker marker) {
        if(marker.isInTypeGroup(Marker.TypeGroup.ATB)){
            return deleteAntibody((Antibody) marker) ;
        }
        throw new RuntimeException("Marker deletion not supported:\n"+ marker);
    }

    public static boolean deleteAntibody(Antibody antibody) {

        try {
            AntibodyWikiWebService.getInstance().dropPageIndividually(antibody.getAbbreviation()); ;
        } catch (Exception e) {
            logger.error("Failed to login to wiki to remove antibody");
        }
        RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(antibody,"Antibody","Antibody Deleted",Person.getCurrentSecurityUser()); ;
        RepositoryFactory.getInfrastructureRepository().deleteActiveDataByZdbID(antibody.getZdbID());
        // this should force a cascade
        HibernateUtil.currentSession().flush();

        // need to remove this from the session
        HibernateUtil.currentSession().evict(antibody);
        return true ;
    }
}

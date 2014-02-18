package org.zfin.infrastructure.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MergeService;
import org.zfin.infrastructure.presentation.DeleteRecordBean;
import org.zfin.mutant.Genotype;
import org.zfin.profile.FeatureSupplier;
import org.zfin.profile.Person;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.CurationPresentation;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.sequence.presentation.DBLinkPresentation;
import org.zfin.wiki.service.AntibodyWikiWebService;

import java.util.*;

/**
 * Attempts to delete a marker and lands on a splash page to indicate success/failure.
 * <p/>
 * 1. If there is a problem durint the
 */
@Controller
public class DeleteRecordController {

    private Logger logger = Logger.getLogger(DeleteRecordController.class);

    @RequestMapping(value ="/deleteRecord/{zdbIDToDelete}")
    public String validateDelete (Model model
            , @PathVariable("zdbIDToDelete") String zdbIDToDelete
            , @ModelAttribute("formBean") DeleteRecordBean formBean
    ) throws Exception {

        formBean.setZdbIDToDelete(zdbIDToDelete);

        String type = zdbIDToDelete.substring(4,8);

        Marker marker = null;

        if (type.startsWith("ATB"))  {
            marker = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean.getZdbIDToDelete());
            formBean.setRecordToDeleteViewString(marker.getAbbreviation());
            formBean.setZdbIDToDelete(marker.getZdbID());

            Set<ExpressionExperiment> expressionExperiments = ((Antibody) marker).getAntibodyLabelings();
            if (CollectionUtils.isNotEmpty(expressionExperiments)) {
                int numExpression = expressionExperiments.size();
                Set<String> pubs = new HashSet<String>();
                for (ExpressionExperiment expressionExperiment : expressionExperiments) {
                    pubs.add(CurationPresentation.getLink(
                            expressionExperiment.getPublication(),
                            CurationPresentation.CurationTab.FX)
                    );
                }
                String argString = "";
                for (String pubString : pubs) {
                    argString = argString + pubString + "<br/>";
                }

                formBean.addError("being used in " + numExpression + " expression records in the following " + pubs.size() + " publication(s): <br>" + argString);
                return "infrastructure/delete-record.page";
            }

        }  else if (type.startsWith("ALT")) {
            Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByID(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(feature.getAbbreviation());
            formBean.setZdbIDToDelete(feature.getZdbID());
            List<Genotype> genotypes = RepositoryFactory.getMutantRepository().getGenotypesByFeature(feature);
            // Can't delete the feature if it is used in a genotype
            if (CollectionUtils.isNotEmpty(genotypes)) {
                String argString = "";
                for (Genotype geno : genotypes) {
                    argString = argString + "<a target=_blank href=/" + geno.getZdbID() + ">" + geno.getName() + "</a><br/>";
                }
                formBean.addError("being used in the following genotype(s): <br>" + argString);
            }
            // Can't delete the feature if it has a source
            if (CollectionUtils.isNotEmpty(feature.getSuppliers())) {
                String argString = "";
                for (FeatureSupplier supplier : feature.getSuppliers()) {
                    argString = argString + "<a target=_blank href=/" + supplier.getOrganization().getZdbID() + ">" + supplier.getOrganization().getName() + "</a><br/>";
                }
                formBean.addError("having the following supplier(s): <br>" + argString);
            }
            // Can't delete the feature if has accession #
            if (CollectionUtils.isNotEmpty(feature.getDbLinks())) {
                String argString = "";
                for (FeatureDBLink dblink : feature.getDbLinks()) {
                    argString = argString + DBLinkPresentation.getLink(dblink) + "<br/>";
                }
                formBean.addError("having the following accession number(s): <br>" + argString);
            }

            // Can't delete the feature if it has more than 1 publications
            SortedSet<Publication> featurePublications = RepositoryFactory.getPublicationRepository().getAllPublicationsForFeature(feature);
            if(CollectionUtils.isNotEmpty(featurePublications) && featurePublications.size() > 1) {
                String argString = "";
                for (Publication publication : featurePublications) {
                    argString = argString + "<a target=_blank href=/" + publication.getZdbID() + ">" + publication.getShortAuthorList() + "</a><br/>";
                }
                formBean.addError("with more than one publications: <br>" + argString);
            }
        }  else if (type.startsWith("JRNL")) {
            Journal journal = RepositoryFactory.getPublicationRepository().getJournalByID(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(journal.getName());
            SortedSet<Publication> publications = RepositoryFactory.getPublicationRepository().getPublicationForJournal(journal);
            // Can't delete the journal if there is any publication associated with it
            if(CollectionUtils.isNotEmpty(publications)) {
                String argString = "";
                for (Publication publication : publications) {
                    argString = argString + "<a target=_blank href=/" + publication.getZdbID() + ">" + publication.getShortAuthorList() + "</a><br/>";
                }
                formBean.addError("associated with publication(s): <br>" + argString);
            }
        }  else {
            formBean.setRecordToDeleteViewString(zdbIDToDelete);
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "delete " + zdbIDToDelete);
        return "infrastructure/delete-record.page";
    }

    @RequestMapping(value ="/record-deleted")
    public String doDelete (Model model
            ,@RequestParam(value = "zdbIDToDelete", required = true) String zdbID
            ,@RequestParam(value = "removeFromTracking", required = false) String deleteFeatureTracking
            ,  @ModelAttribute("formBean") DeleteRecordBean formBean
    ) throws Exception {
        String type = zdbID.substring(4,8);
        Marker marker = null;
        Antibody antibody = null;
        String fieldName = "";
        if (type.startsWith("ATB"))  {
            fieldName = "Antibody";
            marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
            formBean.setRecordToDeleteViewString(marker.getAbbreviation());
        } else if (type.startsWith("ALT")) {
            Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByID(zdbID);
            formBean.setRecordToDeleteViewString(feature.getName());
            formBean.setRemovedFromTracking(false);
        } else if (type.startsWith("JRNL")) {
            Journal journal = RepositoryFactory.getPublicationRepository().getJournalByID(zdbID);
            formBean.setRecordToDeleteViewString(journal.getName());
        } else {
            formBean.setRecordToDeleteViewString(zdbID);
        }

        try {
            HibernateUtil.createTransaction();

            // extra step for deleting antibody
            if (type.startsWith("ATB"))  {
                antibody = (Antibody) marker;
                try {
                    if(ZfinProperties.isPushToWiki()){
                        AntibodyWikiWebService.getInstance().dropPageIndividually(antibody.getAbbreviation());
                    }
                } catch (Exception e) {
                    logger.error("Failed to remove antibody: "+antibody,e);
                }
            }

            // this should force a cascade
            RepositoryFactory.getInfrastructureRepository().deleteActiveDataByZdbID(zdbID);

            HibernateUtil.currentSession().flush();

            // need to remove this from the session
            if (type.startsWith("ATB"))  {
                HibernateUtil.currentSession().evict(antibody);
            }

            // if removing from feature tracking is needed
            if (deleteFeatureTracking != null && deleteFeatureTracking.equalsIgnoreCase("yes")) {
                formBean.setRemovedFromTracking(true);
                int deletedTracking = RepositoryFactory.getFeatureRepository().deleteFeatureFromTracking(zdbID);
                logger.info("deleted record attrs: " + deletedTracking);
            }

            // log the deletion in updates table
            // insertUpdatesTable(String recID, String fieldName, String oldValue, String newValue, String comments)
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(zdbID, fieldName, zdbID, "", "deleted from record-delete interface");

            HibernateUtil.flushAndCommitCurrentSession();

        } catch (Exception e) {
            logger.error("Can not delete " + formBean, e);
            HibernateUtil.rollbackTransaction();
            formBean.addError("Can not delete " + formBean  + "<br>" + e.getMessage());
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "record deleted");
        return "infrastructure/record-deleted.page";
    }

}

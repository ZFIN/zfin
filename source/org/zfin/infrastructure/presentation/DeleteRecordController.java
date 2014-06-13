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
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.feature.Feature;
import org.zfin.feature.FeaturePrefix;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MergeService;
import org.zfin.infrastructure.presentation.DeleteRecordBean;
import org.zfin.marker.presentation.MarkerRelationshipPresentation;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.FeatureSupplier;
import org.zfin.profile.Lab;
import org.zfin.profile.Organization;
import org.zfin.profile.Person;
import org.zfin.profile.presentation.PersonMemberPresentation;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
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
                argString += "<br/>";

                formBean.addError("Being used in " + numExpression + " expression records in the following " + pubs.size() + " publication(s): <br/>" + argString);
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
                argString += "<br/>";
                formBean.addError("Being used in genotype: <br/>" + argString);
            }
            // Can't delete the feature if it has a source
            if (CollectionUtils.isNotEmpty(feature.getSuppliers())) {
                String argString = "";
                for (FeatureSupplier supplier : feature.getSuppliers()) {
                    argString = argString + "<a target=_blank href=/" + supplier.getOrganization().getZdbID() + ">" + supplier.getOrganization().getName() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Having supplier: <br/>" + argString);
            }
            // Can't delete the feature if has accession #
            if (CollectionUtils.isNotEmpty(feature.getDbLinks())) {
                String argString = "";
                for (FeatureDBLink dblink : feature.getDbLinks()) {
                    argString = argString + DBLinkPresentation.getLink(dblink) + "<br/>";
                }
                argString += "<br/>";
                formBean.addError("Having accession number: <br/>" + argString);
            }

            // Can't delete the feature if it has more than 1 publications
            SortedSet<Publication> featurePublications = RepositoryFactory.getPublicationRepository().getAllPublicationsForFeature(feature);
            if(CollectionUtils.isNotEmpty(featurePublications) && featurePublications.size() > 1) {
                String argString = "";
                for (Publication publication : featurePublications) {
                    argString = argString + "<a target=_blank href=/" + publication.getZdbID() + ">" + publication.getShortAuthorList() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Being associated with more than one publications: <br/>" + argString);
            }
        }  else if (type.startsWith("COM")) {
            Organization company = RepositoryFactory.getProfileRepository().getOrganizationByZdbID(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(company.getName());
            // Can't delete the company if it supplies antibody, clone etc.
            List<String> suppliedDataIds = RepositoryFactory.getProfileRepository().getSuppliedDataIds(company);
            if(CollectionUtils.isNotEmpty(suppliedDataIds)) {
                String argString = "";
                for (String zdbId : suppliedDataIds) {
                    argString = argString + "<a target=_blank href=/" + zdbId + ">" + zdbId + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Supplying the following: <br/>" + argString);
            }
            // Can't delete the company if it is a source of genetic feature etc.
            List<String> sourcedDataIds = RepositoryFactory.getProfileRepository().getSourcedDataIds(company);
            if(CollectionUtils.isNotEmpty(sourcedDataIds)) {
                String argString = "";
                for (String zdbId : sourcedDataIds) {
                    argString = argString + "<a target=_blank href=/" + zdbId + ">" + zdbId + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Being source of the following: <br/>" + argString);
            }
        }  else if (type.startsWith("CRIS") || type.startsWith("MRPH") || type.startsWith("TALE")) {
            SequenceTargetingReagent sequenceTargetingReagent = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagent(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(sequenceTargetingReagent.getName());
            Set<Feature> featuresCreatedBySTR = RepositoryFactory.getFeatureRepository().getFeaturesCreatedBySequenceTargetingReagent(sequenceTargetingReagent);
            // Can't delete if used as a mutagen for a feature
            if(CollectionUtils.isNotEmpty(featuresCreatedBySTR)) {
                String argString = "";
                for (Feature feature : featuresCreatedBySTR) {
                    argString = argString + "<a target=_blank href=/" + feature.getZdbID() + ">" + feature.getAbbreviation() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Creating feature: <br/>" + argString);
            }
            List<ExperimentCondition> sequenceTargetingReagentExperiments = RepositoryFactory.getExpressionRepository().getSequenceTargetingReagentExperiments(sequenceTargetingReagent);
            // Can't delete if used in an environment
            if (CollectionUtils.isNotEmpty(sequenceTargetingReagentExperiments)) {
                String argString = "";
                for (ExperimentCondition exp : sequenceTargetingReagentExperiments) {
                    argString = argString + "<a target=_blank href=/" + exp.getExperiment().getZdbID() + ">" + exp.getExperiment().getName() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Being used in environment: <br/>" + argString);
            }
            List<String> publicationListGO = RepositoryFactory.getPublicationRepository().getPublicationIDsForGOwithField(zdbIDToDelete);
            SortedSet<Publication> sortedGOpubs = new TreeSet<Publication>();
            for (String pubId: publicationListGO) {
                Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubId);
                sortedGOpubs.add(publication);
            }
            // Can't delete if used in a GO annotation "inferred from" field
            if (CollectionUtils.isNotEmpty(sortedGOpubs)) {
                String argString = "";
                for (Publication goPub : sortedGOpubs) {
                    argString = argString + "<a target=_blank href=/" + ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() + EntityPresentation.CURATION_URI + "OID=" + goPub.getZdbID() + "&cookie=tabGO" + ">" + goPub.getShortAuthorList() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Being used in  \"inferred from\" field of GO annotation in the following: <br/>" + argString);
            }
            List<Publication> publications = RepositoryFactory.getPublicationRepository().getPubsForDisplay(sequenceTargetingReagent.getZdbID());
            SortedSet<Publication> sortedPublications = new TreeSet<Publication>();
            for (Publication publication : publications) {
                sortedPublications.add(publication);
            }
            // Can't delete if associated with a publication
            if (CollectionUtils.isNotEmpty(sortedPublications)) {
                formBean.addError(formatPublicationList(sortedPublications));
            }
        }  else if (type.startsWith("EFG")) {
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Marker efg = mr.getMarkerByID(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(efg.getAbbreviation());
            Set<MarkerRelationship.Type> types = new HashSet<MarkerRelationship.Type>();
            types.add(MarkerRelationship.Type.PROMOTER_OF);
            types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
            Set<Marker> constructs = MarkerService.getRelatedMarker(efg, types);
            SortedSet<Marker> sortedConstructs = new TreeSet<Marker>();
            for (Marker construct : constructs) {
                sortedConstructs.add(construct);
            }
            // Can't delete an EFG if it has relationships to a construct
            if (CollectionUtils.isNotEmpty(sortedConstructs)) {
                String argString = "";
                for (Marker sortedConstruct : sortedConstructs) {
                    argString = argString + "<a target=_blank href=/" + sortedConstruct.getZdbID() + ">" + sortedConstruct.getAbbreviation() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Having relationships to construct: <br/>" + argString);
            }
            List<MarkerRelationshipPresentation> relatedAntibodies = mr.getRelatedMarkerDisplayForTypes(efg, true, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
            // Can't delete an EFG if it is related to antibody
            if (CollectionUtils.isNotEmpty(relatedAntibodies)) {
                String argString = "";
                for (MarkerRelationshipPresentation antibody : relatedAntibodies) {
                    argString = argString + antibody.getLink() + "<br/>";
                }
                argString += "<br/>";
                formBean.addError("Having relationships to antibody: <br/>" + argString);
            }
            int expressionFigureCount = RepositoryFactory.getExpressionRepository().getExpressionFigureCountForEfg(efg);
            // Can't delete an EFG if it is used in expression data
            if (expressionFigureCount > 0) {
                int expressionPublicationCount = RepositoryFactory.getExpressionRepository().getExpressionPubCountForEfg(efg);
                formBean.addError("Used in expression data: " + expressionFigureCount + " figure(s) of " + expressionPublicationCount + " publication(s) (see " + efg.getAbbreviation() + " page for details)<br/><br/>");
            }
            List<Publication> publications = RepositoryFactory.getPublicationRepository().getPubsForDisplay(efg.getZdbID());
            SortedSet<Publication> sortedPublications = new TreeSet<Publication>();
            for (Publication publication : publications) {
                sortedPublications.add(publication);
            }
            // Can't delete an EGF if it is associated with publication
            if (CollectionUtils.isNotEmpty(sortedPublications)) {
                formBean.addError(formatPublicationList(sortedPublications));
            }
        }  else if (type.startsWith("GENO")) {
            Genotype genotype = RepositoryFactory.getMutantRepository().getGenotypeByID(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(genotype.getName());
            formBean.setZdbIDToDelete(genotype.getZdbID());
            List<ExpressionResult> genoExpressionResults = RepositoryFactory.getExpressionRepository().getExpressionResultsByGenotype(genotype);
            // Can't delete a genotype if it has expression data associated
            if (CollectionUtils.isNotEmpty(genoExpressionResults)) {
                Set<ExpressionExperiment> expressionExperiments = new HashSet<ExpressionExperiment>();
                for (ExpressionResult genoExpressionResult : genoExpressionResults) {
                    expressionExperiments.add(genoExpressionResult.getExpressionExperiment());
                }
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
                argString += "<br/>";

                formBean.addError("Being used in " + numExpression + " expression records in the following " + pubs.size() + " publication(s): <br/>" + argString);
            }
            List<String> publicationListGO = RepositoryFactory.getPublicationRepository().getPublicationIDsForGOwithField(zdbIDToDelete);
            SortedSet<Publication> sortedGOpubs = new TreeSet<Publication>();
            for (String pubId: publicationListGO) {
                Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubId);
                sortedGOpubs.add(publication);
            }
            // Can't delete if used in a GO annotation "inferred from" field
            if (CollectionUtils.isNotEmpty(sortedGOpubs)) {
                String argString = "";
                for (Publication goPub : sortedGOpubs) {
                    argString = argString + "<a target=_blank href=/" + ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() + EntityPresentation.CURATION_URI + "OID=" + goPub.getZdbID() + "&cookie=tabGO" + ">" + goPub.getShortAuthorList() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Being used in  \"inferred from\" field of GO annotation in the following: <br/>" + argString);
            }
            List<PhenotypeStatement> phenotypeStatements = RepositoryFactory.getMutantRepository().getPhenotypeStatementsByGenotype(genotype);
            // Can't delete a genotype if it has phenotypes associated
            if (CollectionUtils.isNotEmpty(phenotypeStatements)) {
                SortedSet<PhenotypeStatement> sortedPhenotypesForGenotype = new TreeSet<PhenotypeStatement>();
                for (PhenotypeStatement pheno : phenotypeStatements) {
                    sortedPhenotypesForGenotype.add(pheno);
                }
                String argString = "";
                for (PhenotypeStatement sortedGenoPheno : sortedPhenotypesForGenotype) {
                    argString = argString + "<a target=_blank href=/action/phenotype/phenotype-statement?id=" + sortedGenoPheno.getId() + ">" + sortedGenoPheno.getDisplayName() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Having phenotype: <br/>" + argString);
            }
            // can not delete if the genotype is associated with more than 1 publications
            SortedSet<Publication> genoPublications = RepositoryFactory.getPublicationRepository().getAllPublicationsForGenotype(genotype);
            if (CollectionUtils.isNotEmpty(genoPublications) && genoPublications.size() > 1) {
                String argString = "";
                for (Publication publication : genoPublications) {
                    argString = argString + "<a target=_blank href=/" + publication.getZdbID() + ">" + publication.getShortAuthorList() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Being associated with more than one publications: <br/>" + argString);
            }
        } else if (type.startsWith("JRNL")) {
            Journal journal = RepositoryFactory.getPublicationRepository().getJournalByID(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(journal.getName());
            SortedSet<Publication> publications = RepositoryFactory.getPublicationRepository().getPublicationForJournal(journal);
            // Can't delete the journal if there is any publication associated with it
            if(CollectionUtils.isNotEmpty(publications)) {
                formBean.addError(formatPublicationList(publications));
            }
        } else if (type.startsWith("LAB")) {
            Organization lab = RepositoryFactory.getProfileRepository().getOrganizationByZdbID(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(lab.getName());
            List<FeaturePrefix> designations = RepositoryFactory.getFeatureRepository().getLabPrefixesById(zdbIDToDelete, true);
            // Can't delete the lab if it has lab designation
            if (CollectionUtils.isNotEmpty(designations)) {
                String argString = "";
                for (FeaturePrefix designation : designations) {
                    argString = argString + "<a target=_blank href=/action/alleles/" +  designation.getPrefixString() + ">" + designation.getPrefixString() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Having lab designation: <br/>" + argString);
            }
            // Can't delete the lab if there are people associated
            List<PersonMemberPresentation> people = RepositoryFactory.getProfileRepository().getLabMembers(zdbIDToDelete);
            if (CollectionUtils.isNotEmpty(people)) {
                String argString = "";
                for (PersonMemberPresentation labMember : people) {
                    argString = argString + "<a target=_blank href=/action/alleles/" +  labMember.getZdbID() + ">" + labMember.getName() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Having lab members: <br/>" + argString);
            }
            // Can't delete the lab if it supplies antibody, clone etc.
            List<String> suppliedDataIds = RepositoryFactory.getProfileRepository().getSuppliedDataIds(lab);
            if(CollectionUtils.isNotEmpty(suppliedDataIds)) {
                String argString = "";
                for (String zdbId : suppliedDataIds) {
                    argString = argString + "<a target=_blank href=/" + zdbId + ">" + zdbId + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Supplying the following: <br/>" + argString);
            }
            // Can't delete the lab if it is a source of genetic feature etc.
            List<String> sourcedDataIds = RepositoryFactory.getProfileRepository().getSourcedDataIds(lab);
            if(CollectionUtils.isNotEmpty(sourcedDataIds)) {
                String argString = "";
                for (String zdbId : sourcedDataIds) {
                    argString = argString + "<a target=_blank href=/" + zdbId + ">" + zdbId + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Being source of the following: <br/>" + argString);
            }
        } else if (type.startsWith("PERS")) {
            Person person = RepositoryFactory.getProfileRepository().getPerson(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(person.getFullName());
            Set<Lab> labs = person.getLabs();
            // Can't delete the person if associated with a lab
            if(CollectionUtils.isNotEmpty(labs)) {
                String argString = "";
                for (Lab lab : labs) {
                    argString = argString + "<a target=_blank href=/" + lab.getZdbID() + ">" + lab.getName() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Being associated with lab: <br/>" + argString);
            }
            Set<Publication> publications = person.getPublications();
            // Can't delete the person if associated with a publication
            if(CollectionUtils.isNotEmpty(publications)) {
                SortedSet<Publication> sortedPubs = new TreeSet<Publication>(publications);
                formBean.addError(formatPublicationList(sortedPubs));
            }
        }  else if (type.startsWith("REGI")) {
            Marker region = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbIDToDelete);
            formBean.setRecordToDeleteViewString(region.getAbbreviation());
            Set<MarkerRelationship.Type> types = new HashSet<MarkerRelationship.Type>();
            types.add(MarkerRelationship.Type.PROMOTER_OF);
            types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
            types.add(MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION);
            Set<Marker> constructs = MarkerService.getRelatedMarker(region, types);
            SortedSet<Marker> sortedConstructs = new TreeSet<Marker>();
            for (Marker construct : constructs) {
                sortedConstructs.add(construct);
            }
            // Can't delete a Region if it has relationships to a construct
            if (CollectionUtils.isNotEmpty(sortedConstructs)) {
                String argString = "";
                for (Marker sortedConstruct : sortedConstructs) {
                    argString = argString + "<a target=_blank href=/" + sortedConstruct.getZdbID() + ">" + sortedConstruct.getAbbreviation() + "</a><br/>";
                }
                argString += "<br/>";
                formBean.addError("Having relationships to construct: <br/>" + argString);
            }
            List<Publication> publications = RepositoryFactory.getPublicationRepository().getPubsForDisplay(region.getZdbID());
            SortedSet<Publication> sortedPublications = new TreeSet<Publication>();
            for (Publication publication : publications) {
                sortedPublications.add(publication);
            }
            // Can't delete a Region if it is associated with publication
            if (CollectionUtils.isNotEmpty(sortedPublications)) {
                formBean.addError(formatPublicationList(sortedPublications));
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
        } else if (type.startsWith("COM")) {
            Organization company = RepositoryFactory.getProfileRepository().getOrganizationByZdbID(zdbID);
            formBean.setRecordToDeleteViewString(company.getName());
        } else if (type.startsWith("CRIS") || type.startsWith("MRPH") || type.startsWith("TALE")) {
            SequenceTargetingReagent sequenceTargetingReagent = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagent(zdbID);
            formBean.setRecordToDeleteViewString(sequenceTargetingReagent.getName());
        } else if (type.startsWith("EFG") || type.startsWith("REGI")) {
            marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
            formBean.setRecordToDeleteViewString(marker.getAbbreviation());
        }  else if (type.startsWith("GENO")) {
            Genotype genotype = RepositoryFactory.getMutantRepository().getGenotypeByID(zdbID);
            formBean.setRecordToDeleteViewString(genotype.getName());
        }  else if (type.startsWith("JRNL")) {
            Journal journal = RepositoryFactory.getPublicationRepository().getJournalByID(zdbID);
            formBean.setRecordToDeleteViewString(journal.getName());
        } else if (type.startsWith("LAB")) {
            Organization lab = RepositoryFactory.getProfileRepository().getOrganizationByZdbID(zdbID);
            formBean.setRecordToDeleteViewString(lab.getName());
        } else if (type.startsWith("PERS")) {
            Person person = RepositoryFactory.getProfileRepository().getPerson(zdbID);
            formBean.setRecordToDeleteViewString(person.getFullName());
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

            if (type.startsWith("COMP") || type.startsWith("JRNL") || type.startsWith("LAB") || type.startsWith("PERS") || type.startsWith("PUB"))  {
                // this should force a cascade
                RepositoryFactory.getInfrastructureRepository().deleteActiveSourceByZdbID(zdbID);
            } else {
                // this should force a cascade
                RepositoryFactory.getInfrastructureRepository().deleteActiveDataByZdbID(zdbID);
            }

            HibernateUtil.currentSession().flush();

            // need to remove this from the session
            if (type.startsWith("ATB"))  {
                HibernateUtil.currentSession().evict(antibody);
            }

            // if removing from feature tracking is needed
            if (type.startsWith("ALT") && deleteFeatureTracking != null && deleteFeatureTracking.equalsIgnoreCase("yes")) {
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

    private String formatPublicationList(SortedSet<Publication> publications) {
        if (CollectionUtils.isEmpty(publications))
            return "";
        String argString = "";
        for (Publication publication : publications) {
            argString = argString + "<a target=_blank href=/" + publication.getZdbID() + ">" + publication.getShortAuthorList() + "</a><br/>";
        }
        argString += "<br/>";
        return "Being associated with publication: <br/>" + argString;
    }

}

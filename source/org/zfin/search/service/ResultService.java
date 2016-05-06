package org.zfin.search.service;

import com.google.common.base.Joiner;
import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.anatomy.presentation.DevelopmentStagePresentation;
import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.expression.presentation.ExperimentPresentation;
import org.zfin.feature.Feature;
import org.zfin.feature.FeaturePrefix;
import org.zfin.fish.presentation.FishPresentation;
import org.zfin.fish.repository.FishService;
import org.zfin.mapping.MappingService;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.presentation.MarkerRelationshipPresentation;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.PhenotypeWarehouse;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.presentation.GenotypePresentation;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.presentation.SearchResult;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.presentation.DBLinkPresentation;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@Service
public class ResultService {

    public static Logger logger = Logger.getLogger(ResultService.class);

    public static String ABSTRACT = "Abstract:";
    public static String ADDRESS = "Address:";
    public static String AUTHORS = "Authors:";
    public static String PREVIOUS_NAME = "Previous Names:";
    public static String LOCATION = "Location:";
    public static String GENE_NAME = "Gene Name:";
    public static String PSEUDOGENE_NAME = "Pseudogene Name:";
    public static String EFG_NAME = "Engineered Foreign Gene Name:";
    public static String SYNONYMS = "Synonyms:";
    public static String AFFECTED_GENES = "Affected Genes:";
    public static String TARGETED_GENES = "Targeted Genes:";
    public static String TYPE = "Type:";
    public static String CONSTRUCT = "Construct:";
    public static String SEQUENCE = "Sequence:";
    public static String CLONE_PROBLEM_TYPE = "Clone Problem Type:";
    public static String CLONE_CONTAINS_GENES = "Clone Contains Genes:";
    public static String CLONE_ENCODED_BY_GENES = "Clone Encoded By Gene:";
    public static String QUALITY = "Quality:";
    public static String GENOTYPE = "Genotype:";
    public static String STAGE = "Stage:";
    public static String CONDITIONS = "Conditions:";
    public static String PUBLICATION = "Publication:";
    public static String CAPTION = "Caption:";
    public static String PHENOTYPE = "Phenotype:";
    public static String EMAIL = "Email:";
    public static String EXPRESSION = "Expression:";
    public static String FISH = "Fish:";
    public static String GENE = "Gene:";
    public static String ANTIBODY = "Antibody:";
    public static String PROBE = "Probe:";
    public static String SCREEN = "Screen:";
    public static String JOURNAL = "Journal:";
    public static String NOTE = "Note:";
    public static String COMMENT = "Comment:";
    public static String LINE_DESIGNATION = "Line Designation:";
    public static String CONSEQUENCE = "Consequence:";
    public static String TRANSCRIPT_NAME = "Transcript Name:";


    public void injectAttributes(Collection<SearchResult> results) {
        for (SearchResult result : results) {
            injectAttributes(result);
        }
    }

    public void injectAttributes(SearchResult result) {
        if (RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(result.getId()) == null) {
            if (StringUtils.equals(result.getCategory(), Category.GENE.getName())) {
                injectGeneAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.FISH.getName())) {
                injectFishAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.FIGURE.getName())) {
                injectFigureAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.MUTANT.getName())) {
                injectFeatureAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.CONSTRUCT.getName())) {
                injectConstructAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.SEQUENCE_TARGETING_REAGENT.getName())) {
                injectSTRAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.MARKER.getName())) {
                injectMarkerCloneAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.EXPRESSIONS.getName())) {
                injectExpressionAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.PHENOTYPE.getName())) {
                injectPhenotypeAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.ANATOMY.getName())
                    || StringUtils.equals(result.getCategory(), Category.DISEASE.getName())) {
                injectTermAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.PUBLICATION.getName())) {
                injectPublicationAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.ANTIBODY.getName())) {
                injectAntibodyAttributes(result);
            } else if (StringUtils.equals(result.getCategory(), Category.COMMUNITY.getName())) {
                if (StringUtils.equals(result.getType(), "Person")) {
                    injectPersonAttributes(result);
                } else if (StringUtils.equals(result.getType(), "Lab")) {
                    injectLabAttributes(result);
                } else if (StringUtils.equals(result.getType(), "Company")) {
                    injectCompanyAttributes(result);
                }
            }
        } else {
            injectMergedAttributes(result);

        }

    }


    private void injectAntibodyAttributes(SearchResult result) {
        result.setDisplayedID(result.getId());
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(result.getId());
        if (antibody == null) {
            return;
        }

        if (CollectionUtils.isNotEmpty(antibody.getAliases())) {
            result.addAttribute(SYNONYMS, withCommas(antibody.getAliases(), "alias"));
        }

        List<MarkerRelationshipPresentation> antigenGeneList = getMarkerRepository().getRelatedMarkerDisplayForTypes(antibody,
                false, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        if (CollectionUtils.isNotEmpty(antigenGeneList)) {
            result.addAttribute("Antigen Gene:", withCommasAndLink(antigenGeneList, "abbreviation", "zdbId"));
        }

        if (StringUtils.isNotEmpty(antibody.getHostSpecies())) {
            result.addAttribute("Host Organism:", antibody.getHostSpecies());
        }

        String type = "";
        if (StringUtils.isNotEmpty(antibody.getClonalType())) {
            type += antibody.getClonalType() + " ";
        }
        List<String> isotypeList = new ArrayList<>(2);
        if (StringUtils.isNotEmpty(antibody.getHeavyChainIsotype())) {
            isotypeList.add(antibody.getHeavyChainIsotype());
        }
        if (StringUtils.isNotEmpty(antibody.getLightChainIsotype())) {
            isotypeList.add(antibody.getLightChainIsotype());
        }

        if (CollectionUtils.isNotEmpty(isotypeList)) {
            type += "[";
            type += Joiner.on(", ").join(isotypeList);
            type += "]";
        }
        if (org.apache.commons.lang.StringUtils.isNotEmpty(type)) {
            result.addAttribute("Type:", type);
        }


        if (CollectionUtils.isNotEmpty(antibody.getSuppliers())) {
            result.addAttribute("Source", withCommas(antibody.getSuppliers(), "organization.name"));
        }
    }

    private void injectTermAttributes(SearchResult result) {
        Term term = RepositoryFactory.getOntologyRepository().getTermByOboID(result.getId());
        result.setDisplayedID(result.getId());
        if (term == null) {
            return;
        }
        if (term.isObsolete()) {
            result.addAttribute("Status", "<span class='red'>Yes</span>");
        }
        if (CollectionUtils.isNotEmpty(term.getAliases())) {
            result.addAttribute(SYNONYMS, withCommas(term.getAliases(), "alias"));
        }
        if (StringUtils.isNotEmpty(term.getDefinition())) {
            result.addAttribute("Definition", term.getDefinition());
        }

        if (term.getOntology() == Ontology.ANATOMY) {
            if (term.getStart() != null && term.getEnd() != null && term.getStart().equals(term.getEnd())) {
                result.addAttribute("Exists During:", term.getStart().getNameLong());
            } else if (term.getStart() != null) {
                String message = term.getStart().getNameLong();
                message += " - ";
                message += term.getEnd().getNameLong();
                result.addAttribute("Exists During:", message);
            }
            if (CollectionUtils.isNotEmpty(term.getImages())) {
                if (term.getImages().size() != 1) {
                    result.setUrl("/" + term.getOboID());
                    Set<Image> images = term.getImages();
                    List<String> imageStringList = new ArrayList<>(images.size());
                    List<String> thumbnailStringList = new ArrayList<>(images.size());
                    for (Image image : images) {
                        imageStringList.add(image.getImageFilename());
                        thumbnailStringList.add(image.getThumbnail());
                    }
                    result.setImages(imageStringList);
                    result.setThumbnails(thumbnailStringList);
                }
            }
        }

        if (term.getOntology() == Ontology.DISEASE_ONTOLOGY) {
            //todo:  add disease specific stuff here
        }

    }

    public void injectFishAttributes(SearchResult result) {
        Fish fish = RepositoryFactory.getMutantRepository().getFish(result.getId());


        result.setDisplayedID(result.getId());

        if (fish != null) {
            result.setFeatureGenes(FishService.getFeatureGenes(fish));
        }

    }

    public void injectFigureAttributes(SearchResult result) {
        Figure figure = RepositoryFactory.getPublicationRepository().getFigure(result.getId());

        result.setDisplayedID(result.getId());

        if (figure != null) {
            result.addAttribute(PUBLICATION, "<a href=\"/" + figure.getPublication().getZdbID()
                    + "\">" + figure.getPublication().getTitle() + "</a>");

            if (StringUtils.isNotEmpty(figure.getCaption())) {
                result.addAttribute(CAPTION, collapsible(figure.getCaption()));
            }
        }

    }

    public void injectGeneAttributes(SearchResult result) {
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(result.getId());

        result.setDisplayedID(result.getId());

        if (gene != null) {

            result.setEntity(gene);

            if (gene.getType() == Marker.Type.GENE) {
                result.addAttribute(GENE_NAME, "<span class=\"genedom\">" + gene.getName() + "</span>");
            }
            if (gene.getType() == Marker.Type.GENEP) {
                result.addAttribute(PSEUDOGENE_NAME, "<span class=\"genedom\">" + gene.getName() + "</span>");
            }
            if (gene.getType() == Marker.Type.EFG) {
                result.addAttribute(EFG_NAME, "<span class=\"genedom\">" + gene.getName() + "</span>");
            }
            if (gene.getType() == Marker.Type.TSCRIPT) {
                result.addAttribute(TRANSCRIPT_NAME, MarkerPresentation.getAbbreviation(gene));
            }
            if (gene.getAliases() != null && gene.getAliases().size() > 0) {
                result.addAttribute(PREVIOUS_NAME, withCommas(gene.getAliases(), "alias"));
            }

            addLocationInfo(result, gene);
        }


    }

    //todo: user  MarppingServie.getChromosomeLocationDisplay & change display code
    protected void addLocationInfo(SearchResult result, Marker gene) {

        String locationDisplay = MappingService.getChromosomeLocationDisplay(gene);
        if (StringUtils.isNotEmpty(locationDisplay)) {
            String mappingDetailsLink = " <a href=\"/action/mapping/detail/" + gene.getZdbID() + "\">Mapping Details/Browsers</a>";
            result.addAttribute(LOCATION, locationDisplay + mappingDetailsLink);
        }
    }


    public void injectFeatureAttributes(SearchResult result) {
        Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByID(result.getId());

        result.setDisplayedID(result.getId());

        if (feature != null) {
            result.addAttribute(SYNONYMS, withCommas(feature.getAliases(), "alias"));

            //todo: these are currently links in the req doc
            List<String> affectedGenes = new ArrayList<>();
            for (Marker gene : feature.getAffectedGenes()) {
                affectedGenes.add(gene.getAbbreviation());
            }
            if (CollectionUtils.isNotEmpty(affectedGenes)) {
                result.addAttribute(AFFECTED_GENES, withCommas(affectedGenes));
            }

            result.addAttribute(TYPE, feature.getType().getDisplay());
            if (feature.getFeatureTranscriptMutationDetailSet() != null && feature.getFeatureTranscriptMutationDetailSet().size() > 0) {
                result.addAttribute(CONSEQUENCE, withCommas(feature.getFeatureTranscriptMutationDetailSet(), "transcriptConsequence.displayName"));
            }
            if (feature.getConstructs() != null && feature.getConstructs().size() > 0) {
                result.addAttribute(CONSTRUCT, withCommas(feature.getConstructs(), "marker.name"));
            }

//            screen used to be here, removed as a result of case 11323
            /*if (feature.getFeaturePrefix() != null) {
                if (feature.getFeaturePrefix().getPrefixString().equals("la"))
                    result.addAttribute(SCREEN, "Burgess / Lin");
                if (feature.getFeaturePrefix().getPrefixString().equals("sa"))
                    result.addAttribute(SCREEN, "Sanger");
                if (feature.getFeaturePrefix().getPrefixString().equals("mn"))
                    result.addAttribute(SCREEN, "Zfishbook");
            }*/
        }


    }

    public void injectConstructAttributes(SearchResult result) {
        Marker construct = RepositoryFactory.getMarkerRepository().getMarkerByID(result.getId());

        result.setDisplayedID(result.getId());

        if (construct != null) {
            addSynonyms(result, construct);

            List<DBLink> dBLinks = RepositoryFactory.getSequenceRepository()
                    .getDBLinksForMarkerAndDisplayGroup(construct, DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE);

            if (CollectionUtils.isNotEmpty(dBLinks)) {
                String link = DBLinkPresentation.getLink(dBLinks.get(0));
                result.addAttribute(SEQUENCE, link);
            }

            if (StringUtils.isNotEmpty(construct.getPublicComments())) {
                result.addAttribute(NOTE, construct.getPublicComments());
            }
        }
    }

    public void injectMergedAttributes(SearchResult result) {
        result.addAttribute(NOTE, "This record has been merged or deleted");
    }

    public void injectSTRAttributes(SearchResult result) {
        result.setDisplayedID(result.getId());

        SequenceTargetingReagent reagent = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagent(result.getId());
        if (reagent != null) {

            if (CollectionUtils.isNotEmpty(reagent.getAliases())) {
                result.addAttribute(SYNONYMS, withCommas(reagent.getAliases(), "alias"));
            }

            result.addAttribute(TYPE, reagent.getMarkerType().getDisplayName());

            List<String> affectedGenes = new ArrayList();
            for (Marker gene : reagent.getTargetGenes()) {
                affectedGenes.add(MarkerPresentation.getAbbreviation(gene));
            }
            if (affectedGenes.size() > 0) {
                result.addAttribute(TARGETED_GENES, withCommas(affectedGenes));
            }

            List<String> sequenceList = new ArrayList<String>();
            if (StringUtils.isNotEmpty(reagent.getSequence().getSequence())) {
                sequenceList.add(reagent.getSequence().getSequence());
            }
            if (StringUtils.isNotEmpty(reagent.getSequence().getSecondSequence())) {
                sequenceList.add(reagent.getSequence().getSecondSequence());
            }
            if (sequenceList.size() > 0) {
                result.addAttribute(SEQUENCE, withBreaks(sequenceList));
            }

        }

    }

    public void injectMarkerCloneAttributes(SearchResult result) {
        String id = result.getId();
        result.setDisplayedID(id);
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(id);
        if (marker == null) {
            return;
        }

        addSynonyms(result, marker);
        if (marker.getType().equals(Marker.Type.REGION))
            addComments(result, marker);
        addLocationInfo(result, marker);
        List<Marker> genesContainedInClones = getMarkerRepository().getMarkersContainedIn(marker, MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        if (CollectionUtils.isNotEmpty(genesContainedInClones)) {
            result.addAttribute(CLONE_CONTAINS_GENES, withCommasAndLink(genesContainedInClones, "abbreviation", "zdbID"));
        }

        List<Marker> genesContainedViaTranscript = getMarkerRepository().getRelatedGenesViaTranscript(marker, MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT, MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
        if (CollectionUtils.isNotEmpty(genesContainedViaTranscript)) {
            result.addAttribute(CLONE_CONTAINS_GENES, withCommasAndLink(genesContainedViaTranscript, "abbreviation", "zdbID"));
        }
        List<Marker> cloneEncodesGene = getMarkerRepository().getMarkersContainedIn(marker, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
        if (CollectionUtils.isNotEmpty(cloneEncodesGene)) {
            result.addAttribute(CLONE_ENCODED_BY_GENES, withCommasAndLink(cloneEncodesGene, "abbreviation", "zdbID"));
        }
        if (marker instanceof Clone) {
            Clone clone = (Clone) marker;
            if (clone.getRating() != null) {
                result.addAttribute(QUALITY, clone.getRating().toString());
            }
            if (clone.getProblem() != null) {
                result.addAttribute(CLONE_PROBLEM_TYPE, clone.getProblem().name() + " <img src=\"/images/warning-noborder.gif\" width=\"20\" height=\"20\" align=\"top\"/>");
            }
            if (clone.getRating() != null) {
                result.addAttribute(QUALITY, "<img src='/images/" + clone.getRating() + "0stars.gif'>");
            }
        }

    }


    public void injectPersonAttributes(SearchResult result) {
        result.setDisplayedID(result.getId());

        Person person = RepositoryFactory.getProfileRepository().getPerson(result.getId());

        if (person != null) {

            if (StringUtils.isNotEmpty(person.getEmail())) {
                result.addAttribute(EMAIL, person.getEmail());
            }

            if (StringUtils.isNotEmpty(person.getAddress())) {
                result.addAttribute(ADDRESS, "<span class=\"result-street-address\">" + person.getAddress() + "</span>");
            }


        }

    }

    public void injectLabAttributes(SearchResult result) {
        result.setDisplayedID(result.getId());

        Lab lab = RepositoryFactory.getProfileRepository().getLabById(result.getId());

        if (lab != null) {
            if (StringUtils.isNotEmpty(lab.getAddress())) {
                result.addAttribute(ADDRESS, "<span class=\"result-street-address\">" + lab.getAddress() + "</span>");
            }

            List<FeaturePrefix> featurePrefixes = RepositoryFactory.getFeatureRepository().getCurrentLabPrefixesById(lab.getZdbID(), false);

            if (CollectionUtils.isNotEmpty(featurePrefixes)) {
                result.addAttribute(LINE_DESIGNATION, withCommas(featurePrefixes, "prefixString"));
            }
        }

    }

    public void injectCompanyAttributes(SearchResult result) {
        result.setDisplayedID(result.getId());

        Company company = RepositoryFactory.getProfileRepository().getCompanyById(result.getId());

        if (company != null) {
            if (StringUtils.isNotEmpty(company.getAddress())) {
                result.addAttribute(ADDRESS, "<span class=\"result-street-address\">" + company.getAddress() + "</span>");
            }
        }

    }

    protected void addSynonyms(SearchResult result, Marker marker) {
        if (CollectionUtils.isNotEmpty(marker.getAliases())) {
            result.addAttribute(SYNONYMS, withCommasAndItalics(marker.getAliases(), "alias"));
        }
    }

    protected void addComments(SearchResult result, Marker marker) {
            result.addAttribute(COMMENT, marker.getPublicComments());
    }

    public void injectExpressionAttributes(SearchResult result) {
        //this is pretty hacky, sorry.  I have to pull the string prefix off of the would-be integer pk id.
        Long id = Long.valueOf(result.getId().replace("xpatex-", ""));

        //don't show for real, but useful to turn this on to get values for the test
        //result.setDisplayedID(result.getId());

        ExpressionFigureStage efs = RepositoryFactory.getExpressionRepository().getExpressionFigureStage(id);

        ExpressionExperiment xpatex = RepositoryFactory.getExpressionRepository().getExpressionExperiment(result.getXpatZdbId());
        Figure figure = RepositoryFactory.getPublicationRepository().getFigure(result.getFigZdbId());

        if (xpatex != null) {

            result.setEntity(xpatex);
            result.setFigure(figure);

            //Show the gene name if there is one, and the probe isn't chimeric (if there is one)
            if (xpatex.getGene() != null) {

                if (xpatex.getProbe() == null) {
                    result.addAttribute(GENE, MarkerPresentation.getAbbreviation(xpatex.getGene()));
                } else if (!xpatex.getProbe().isChimeric()) {
                    result.addAttribute(GENE, MarkerPresentation.getAbbreviation(xpatex.getGene()));
                }
            }
            if (xpatex.getAntibody() != null) {
                result.addAttribute(ANTIBODY, MarkerPresentation.getName(xpatex.getAntibody()));
            }
            if (xpatex.getProbe() != null) {
                result.addAttribute(PROBE, MarkerPresentation.getName(xpatex.getProbe()));
            }

            String conditions = ExperimentPresentation.getLink(xpatex.getFishExperiment().getExperiment(), true);
            // String conditions = ExperimentPresentation.getNameForFaceted(xpatex.getGenotypeExperiment().getExperiment(), true, false);
            if (StringUtils.isNotBlank(conditions)) {
                result.addAttribute(CONDITIONS, conditions);
            }

            if (efs.getStartStage().equals(efs.getEndStage())) {
                result.addAttribute(STAGE, efs.getStartStage().getName());
            } else {
                result.addAttribute(STAGE, efs.getStartStage().getName() + " to " + efs.getEndStage().getName());
            }

            List<String> results = new ArrayList<>();
            List<ExpressionResult> expressionResults = new ArrayList<>();



            //Surprisngly, it turns out that this actually performs better than a query.  Could be caching, but we like caching.
            //expressionResults.addAll(CollectionUtils.intersection(xpatex.getExpressionResults(), figure.getExpressionResults()));


            //Sort expressionResults by start stage, end stage, superterm name, subterm name...
            Collections.sort(expressionResults);

            for (ExpressionResult2 expressionResult : efs.getExpressionResultSet()) {
                StringBuilder sb = new StringBuilder();
/*                if (efs.getStartStage() == efs.getEndStage()) {
                    sb.append(DevelopmentStagePresentation.getName(efs.getStartStage(), true));
                } else {
                    sb.append(DevelopmentStagePresentation.getName(efs.getStartStage(), true))
                            .append(" to ")
                            .append(DevelopmentStagePresentation.getName(efs.getEndStage(), true));
                }

                sb.append(" - ");*/

                if (!expressionResult.isExpressionFound()) {
                    sb.append(" <i>not in</i> ");
                }
                sb.append(TermPresentation.getLink(expressionResult.getEntity(), true));
                results.add(sb.toString());
            }
            result.addAttribute(EXPRESSION, asUnorderedList(results));

            result.setFeatureGenes(FishService.getFeatureGenes(xpatex.getFishExperiment().getFish()));


            //build a more complex title than the one used in the query

            StringBuilder sb = new StringBuilder();

            if (xpatex.getProbe() != null && xpatex.getProbe().getProblem() == Clone.ProblemType.CHIMERIC) {
                sb.append(MarkerPresentation.getAbbreviation(xpatex.getProbe()));
            } else if (xpatex.getGene() != null) {
                sb.append(MarkerPresentation.getAbbreviation(xpatex.getGene()));
            } else if (xpatex.getAntibody() != null) {
                sb.append(MarkerPresentation.getName(xpatex.getAntibody()));
            }
            sb.append(" expression in ");

            sb.append(FishPresentation.getName(xpatex.getFishExperiment().getFish()));
            String experimentText = ExperimentPresentation.getNameForFaceted(xpatex.getFishExperiment().getExperiment());
            if (StringUtils.isNotBlank(experimentText)) {
                sb.append(" + ");
                sb.append(experimentText);
            }

            sb.append(" at ");
            sb.append(efs.getStartStage().getName());
            if (efs.getStartStage() != efs.getEndStage()) {
                sb.append(" to ");
                sb.append(efs.getEndStage().getName());
            }

            sb.append(" from ");

            sb.append(figure.getPublication().getShortAuthorList());
            sb.append(" ");
            sb.append(figure.getLabel());

            result.setName(sb.toString());

            //This needs to be last, it serves as a title for the fish component table below
            if (xpatex.getFishExperiment().getFish().isWildtypeWithoutReagents()) {
                result.addAttribute(FISH, FishPresentation.getName(xpatex.getFishExperiment().getFish()));
            } else {
                result.addAttribute(FISH, "");
            }


        }


    }


    /*
    * What to include (from the req's doc):
    * data table with bits of data needed to make the record unique from others with the same result header.
    * genotype,
    * stage,
    * experimental conditions..is that it?
        From:
        publication title
        links to publication title to publication page
        Figure caption
        first two lines of figure caption with ability to see whole thing
        Figure image off to the right.
    *
    * */
    public void injectPhenotypeAttributes(SearchResult result) {
        //this is pretty hacky, sorry.  I have to pull the string prefix off of the would-be integer pk id.
        Long id = Long.valueOf(result.getId().replace("pg-", ""));
        String psgID = result.getPgcmid();
        List<PhenotypeWarehouse> pWarehouse = RepositoryFactory.getPhenotypeRepository().getPhenotypeWarehouseBySourceID(psgID);
        for (PhenotypeWarehouse phenotypeExperiment : pWarehouse) {

            if (phenotypeExperiment != null) {

                String conditionsLink = ExperimentPresentation.getLink(phenotypeExperiment.getFishExperiment().getExperiment(), true);
                if (StringUtils.isNotBlank(conditionsLink)) {
                    result.addAttribute(CONDITIONS, conditionsLink);
                }


                if (phenotypeExperiment.getStart().equals(phenotypeExperiment.getEnd())) {
                    result.addAttribute(STAGE, phenotypeExperiment.getStart().getName());
                } else {
                    result.addAttribute(STAGE, phenotypeExperiment.getStart().getName() + " to " + phenotypeExperiment.getEnd().getName());
                }

                List<String> statements = new ArrayList<>();
                for (PhenotypeStatementWarehouse statement : phenotypeExperiment.getStatementWarehouseSet()) {
                    statements.add(statement.getShortName());
                }
                if (CollectionUtils.isNotEmpty(statements)) {
                    result.addAttribute(PHENOTYPE, withBreaks(statements));
                }

                result.setFeatureGenes(FishService.getFeatureGenes(phenotypeExperiment.getFishExperiment().getFish()));

                if (!StringUtils.contains(ExperimentPresentation.getLink(phenotypeExperiment.getFishExperiment().getExperiment(), true), "standard or control")) {

                    StringBuilder sb = new StringBuilder();

                    sb.append(FishPresentation.getName(phenotypeExperiment.getFishExperiment().getFish()));

                    String experimentText = ExperimentPresentation.getNameForFaceted(phenotypeExperiment.getFishExperiment().getExperiment());
                    if (StringUtils.isNotBlank(experimentText)) {
                        sb.append(" + ");
                        sb.append(experimentText);
                    }

                    sb.append(" from ");

                    sb.append(phenotypeExperiment.getFigure().getPublication().getShortAuthorList());
                    sb.append(" ");
                    sb.append(phenotypeExperiment.getFigure().getLabel());

                    result.setName(sb.toString());
                }
                //This needs to be last, it serves as a title for the fish component table below
                result.addAttribute(FISH, "");

            }
        }
    }








    public void injectPublicationAttributes(SearchResult result) {
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(result.getId());

        if (publication != null) {
            result.setDisplayedID(publication.getZdbID());
            result.addAttribute(AUTHORS, collapsible(publication.getAuthors()));

            //going null-safe just in case...
            StringBuilder sb = new StringBuilder();
            if (publication.getJournal() != null && publication.getJournal().getAbbreviation() != null) {
                sb.append(publication.getJournal().getAbbreviation());
                sb.append(" ");
            }
            if (publication.getPublicationDate() != null) {
                sb.append(publication.getPublicationDate().get(Calendar.YEAR));
            }

            result.addAttribute(JOURNAL, sb.toString());
            String abstractText = publication.getAbstractText();
            if (StringUtils.isNotEmpty(abstractText)) {
                result.addAttribute(ABSTRACT, collapsible(abstractText));
            }

        }

    }


    public String collapsible(String string) {
        //If it's only a line or two, the css applies a gradient that looks very silly, so only apply
        //this class to strings longer than some reasonable length...

        if (StringUtils.isNotEmpty(string) && string.length() < 200) {
            return string;
        }

        return "<div class=\"collapsible-attribute collapsed-attribute\">" + string + "</div>";
    }


    /**
     * Create a comma separated list from a property of each member of a collection
     *
     * @param collection collection
     * @param property   property path name
     * @return String
     */
    public String withCommas(Collection collection, String property) {

        //get the named property out as a list of strings
        List<String> stringList = (List<String>) CollectionUtils.collect(collection,
                new BeanToPropertyValueTransformer(property));

        //comma separate it
        return Joiner.on(", ").join(stringList);
    }
    public String withCommasAndItalics(Collection collection, String property) {

        //get the named property out as a list of strings
        List<String> stringList = (List<String>) CollectionUtils.collect(collection,
                new BeanToPropertyValueTransformer(property));

        //comma separate it
        return "<i>"+Joiner.on(", ").join(stringList)+"</i>";
    }
    /**
     * Create a comma separated list from a property of each member of a collection
     * including a hyperlink to the zdbID
     *
     * @param collection collection
     * @param property   property path name
     * @return String
     */
    public String withCommasAndLink(Collection collection, String property, String zdbIdProperty) {
        //get the named property out as a list of strings
        List<String> stringList = (List<String>) CollectionUtils.collect(collection,
                new BeanToPropertyValueTransformer(property));
        List<String> idList = (List<String>) CollectionUtils.collect(collection,
                new BeanToPropertyValueTransformer(zdbIdProperty));

        if (CollectionUtils.isNotEmpty(stringList)) {
            int index = 0;
            for (String entry : stringList) {
                stringList.set(index, "<a href='/" + idList.get(index) + "'>" + entry + "</a>");
                index++;
            }
        }
        //comma separate it
        return Joiner.on(", ").join(stringList);
    }


    /**
     * Create a comma separated string from a list of strings
     *
     * @param collection
     * @return
     */
    public String withCommas(Collection collection) {
        return Joiner.on(", ").join(collection);
    }


    public String withBreaks(Collection collection) {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection) {
            String s = (String) o;
            sb.append("<div>");
            sb.append(s);
            sb.append("</div>");
        }
        return sb.toString();
    }

    public String asUnorderedList(Collection collection) {
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(collection)) {
            sb.append("<ul class=\"list-unstyled\">");
            for (Object o : collection) {
                String s = (String) o;
                sb.append("<li>");
                sb.append(s);
                sb.append("</li>");

            }
            sb.append("</ul>");
        }
        return sb.toString();
    }

}

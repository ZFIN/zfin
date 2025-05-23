package org.zfin.feature.repository;

import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.Species;
import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.Figure;
import org.zfin.feature.*;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.genomebrowser.GenomeBrowserBuild;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.genomebrowser.presentation.GenomeBrowserFactory;
import org.zfin.genomebrowser.presentation.GenomeBrowserImageBuilder;
import org.zfin.genomebrowser.presentation.GenomeBrowserImage;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.*;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.PhenotypeOnMarkerBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Fish;
import org.zfin.mutant.GenotypeDisplay;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.presentation.FishGenotypePhenotypeStatistics;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.mutant.repository.FishGenotypeFeature;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;

import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.ForeignDB.AvailableName;
import static org.zfin.sequence.ForeignDBDataType.DataType;


@Service
public class FeatureService {

    private static Logger logger = LogManager.getLogger(FeatureService.class);

    @Autowired
    private MutantRepository mutantRepository;

    public static Set<FeatureMarkerRelationship> getSortedMarkerRelationships(Feature feature) {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<>();
        }
        SortedSet<FeatureMarkerRelationship> affectedGenes = new TreeSet<>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null) {
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                    if (ftrmrkrRelation.getMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                        affectedGenes.add(ftrmrkrRelation);
                    }
                }
            }
        }

        return affectedGenes;
    }

    public static Set<FeatureDBLink> getSummaryDbLinks(Feature feature) {

        Set<FeatureDBLink> summaryLinks = new HashSet<>();
        for (FeatureDBLink featureDBLink : feature.getDbLinks()) {
            if (featureDBLink.getReferenceDatabase().isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE)) {
                summaryLinks.add(featureDBLink);
            }
        }
        return summaryLinks;
    }

    public static Set<FeatureDBLink> getGenbankDbLinks(Feature feature) {
        Set<FeatureDBLink> genbankLinks = new HashSet<>();
        for (FeatureDBLink featureDBLink : feature.getDbLinks()) {
            if (!featureDBLink.getReferenceDatabase().isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE)&&(!featureDBLink.getAccessionNumber().contains("pdf"))) {



                genbankLinks.add(featureDBLink);
            }
        }
        return genbankLinks;
    }

    public static FeatureDBLink getZIRCGenoLink(Feature feature) {


        for (FeatureDBLink featureDBLink : feature.getDbLinks()) {
            if (featureDBLink.getAccessionNumber().contains("pdf")) {
                return featureDBLink;
            }
        }


        return null;





    }

    public static Set<FeatureMarkerRelationship> getCreatedByRelationship(Feature feature) {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return null;
        }
        Set<FeatureMarkerRelationship> createdByRelationship = new HashSet<>();
        for (FeatureMarkerRelationship ftrmrkrRelationship : fmrelationships) {
            if (ftrmrkrRelationship != null) {
                if (ftrmrkrRelationship.getMarker().getMarkerType().getType() == Marker.Type.CRISPR
                        || ftrmrkrRelationship.getMarker().getMarkerType().getType() == Marker.Type.TALEN) {
                    createdByRelationship.add(ftrmrkrRelationship);

                }
            }
        }
        return createdByRelationship;

    }

    public static List<FeatureGenomeLocation> getFeatureGenomeLocationsInGbrowse(Feature feature) {
        List<FeatureGenomeLocation> locations = RepositoryFactory.getLinkageRepository().getGenomeLocation(feature);
        Collections.sort(locations);
        CollectionUtils.filter(locations, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return (o instanceof FeatureGenomeLocation);
            }
        });
        return locations;
    }

    public static List<FeatureGenomeLocation> getPhysicalLocations(Feature feature) {
        List<FeatureGenomeLocation> locations = RepositoryFactory.getLinkageRepository().getGenomeLocation(feature);
        //Collections.sort(locations);
        CollectionUtils.filter(locations, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return ((FeatureGenomeLocation) o).getSource().isPhysicalMappingLocation();
            }
        });
        return locations;
    }

    public static Set<String> getFeatureMap(Feature feature) {
        MarkerRepository mkrRepository = RepositoryFactory.getMarkerRepository();
        List<Marker> mkr = RepositoryFactory.getFeatureRepository().getMarkersByFeature(feature);
        Set<String> delmarklg = new TreeSet<>();
        if (mkr != null) {
            for (Marker mark : mkr) {
                Set<String> lg = mkrRepository.getLG(mark);
                for (String lgchr : lg) {
                    delmarklg.add(lgchr);
                }
            }
        }
        return delmarklg;
    }

    public static List<PublicationAttribution> getFeatureTypeAttributions(Feature feature) {
        return RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(
                feature.getZdbID(), RecordAttribution.SourceType.FEATURE_TYPE);
    }

    public static List<PublicationAttribution> getDnaChangeAttributions(Feature feature) {
        if (feature.getFeatureDnaMutationDetail() == null) {
            return null;
        }

            return RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(
                feature.getFeatureDnaMutationDetail().getZdbID(),
                RecordAttribution.SourceType.STANDARD);
    }

    public static List<PublicationAttribution> getTranscriptConsequenceAttributions(Feature feature) {
        if (CollectionUtils.isEmpty(feature.getFeatureTranscriptMutationDetailSet())) {
            return null;
        }

        SortedSet<PublicationAttribution> attributions = new TreeSet<>();
        for (FeatureTranscriptMutationDetail detail : feature.getFeatureTranscriptMutationDetailSet()) {
            attributions.addAll(RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(
                    detail.getZdbID(), RecordAttribution.SourceType.STANDARD));
        }
        return new ArrayList<>(attributions);
    }

    public static List<PublicationAttribution> getProteinConsequenceAttributions(Feature feature) {
        if (feature.getFeatureProteinMutationDetail() == null) {
            return null;
        }

        return RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(
                feature.getFeatureProteinMutationDetail().getZdbID(),
                RecordAttribution.SourceType.STANDARD);
    }

    public static List<PublicationAttribution> getFlankSeqAttr(Feature feature) {
        VariantSequence varSeq = RepositoryFactory.getFeatureRepository().getFeatureVariant(feature);
        if (varSeq == null) {
            return null;
        }
        return RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(
                varSeq.getZdbID(),
                RecordAttribution.SourceType.STANDARD);
    }


    public static String getAALink(Feature feature) {
        String aaLink = RepositoryFactory.getFeatureRepository().getAALink(feature);

        if (aaLink == null) {
            return null;
        }
        return aaLink;
    }

    public static String getVarType(Feature feature){
        String varType=feature.getType().getDisplay();
        if (feature.getType().getDisplay().contains("Indel")){
             varType="Delins";
        }
        if (feature.getType().getDisplay().contains("Complex")||feature.getType().getDisplay().contains("Translocation")||feature.getType().getDisplay().contains("Deficiency")){
             varType="";
        }
  return varType;

    }

    public static Set<FeatureMarkerRelationship> getSortedConstructRelationships(Feature feature) {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<>();
        }
        SortedSet<FeatureMarkerRelationship> constructMarkers = new TreeSet<>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null) {
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString())
                        || ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString())
                ) {
                    constructMarkers.add(ftrmrkrRelation);
                }
            }
        }
        return constructMarkers;

    }

    public static List<String> getFeatureAliases(Feature feature) {
        Set<FeatureAlias> featureAliases = feature.getAliases();
        List<String> featureAliasList = new ArrayList<>();
        for (FeatureAlias featureAlias : featureAliases) {
            featureAliasList.add(featureAlias.getAlias());
        }
        return featureAliasList;
    }

    public static List<String> getFeatureSequences(Feature feature) {
        Set<FeatureDBLink> featureSequences = feature.getDbLinks();
        List<String> featureDBLinkList = new ArrayList<>();
        for (FeatureDBLink featureDBLink : featureSequences) {
            if (!featureDBLink.getReferenceDatabase().isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE)) {
                featureDBLinkList.add(featureDBLink.getAccessionNumberDisplay());
            }
        }
        return featureDBLinkList;
    }

    public static List<Marker> getPresentMarkerList(Feature feature, FeatureMarkerRelationshipTypeEnum featureMarkerRelationship) {
        List<Marker> featureMarkerList = new ArrayList<>(feature.getFeatureMarkerRelations().size());
        for (FeatureMarkerRelationship rel : feature.getFeatureMarkerRelations()) {
            if (rel.getType().equals(featureMarkerRelationship)) {
                featureMarkerList.add(rel.getMarker());
            }
        }
        Collections.sort(featureMarkerList);
        return featureMarkerList;
    }

    public static List<FeatureNote> getSortedExternalNotes(Feature feature) {
        List<FeatureNote> notes = new ArrayList<FeatureNote>();
        notes.addAll(feature.getExternalNotes());

        Collections.sort(notes);

        return notes;
    }


    public static GenomeBrowserImage getGbrowseImage(Feature feature) {
        Set<FeatureMarkerRelationship> featureMarkerRelationships = feature.getFeatureMarkerRelations();
        List<FeatureGenomeLocation> locations = getFeatureGenomeLocationsInGbrowse(feature);
        Collections.sort(locations, new GenomeVersionComparator<>());
        if (CollectionUtils.isEmpty(locations)) {
            return null;
        }
        FeatureGenomeLocation featureLocation = locations.get(0);

        if (featureLocation.getStart() == null || featureLocation.getEnd() == null) {
            return null;
        }

        // gbrowse has a location for this feature. if there is a feature marker relationship AND we know where
        // that marker is, show the feature in the context of the marker. Otherwise just show the feature with
        // some appropriate amount of padding.
        GenomeBrowserImageBuilder imageBuilder = GenomeBrowserFactory.getStaticImageBuilder()
                .highlight(feature);

        GenomeLocation.Source source;
        GenomeBrowserTrack extraTrack = null;
        if (featureLocation.getAssembly().equals("Zv9")) {
            imageBuilder.genomeBuild(GenomeBrowserBuild.ZV9);
            extraTrack = GenomeBrowserTrack.ZFIN_MUTANT;
            source = GenomeLocation.Source.ZFIN_Zv9;
        } else if (featureLocation.getAssembly().equals("GRCz10")) {
            imageBuilder.genomeBuild(GenomeBrowserBuild.GRCZ10);
            extraTrack = GenomeBrowserTrack.ZFIN_MUTANT;
            source = GenomeLocation.Source.ZFIN_Zv9; //TODO: Should this be Zv10?
        } else {
            imageBuilder.genomeBuild(GenomeBrowserBuild.CURRENT);
            source = GenomeLocation.Source.ZFIN;
        }

        if (featureMarkerRelationships.size() == 1) {
            Marker related = featureMarkerRelationships.iterator().next().getMarker();
            List<MarkerGenomeLocation> markerLocations = RepositoryFactory.getLinkageRepository().getGenomeLocation(related, source);
            if (CollectionUtils.isNotEmpty(markerLocations)) {
                imageBuilder.setLandmarkByGenomeLocation(markerLocations.get(0)).withRelativePadding(0.1);
            } else {
                imageBuilder.setLandmarkByGenomeLocation(featureLocation).withPadding(10000);
            }
        } else {
            imageBuilder.setLandmarkByGenomeLocation(featureLocation).withPadding(10000);
        }
        //currently only ZMP features on previous builds need anything other than the ZFIN_FEATURES track
        GenomeBrowserTrack featureTrack = featureLocation.getGenomeBrowserTrack() == null ? GenomeBrowserTrack.ZFIN_FEATURES : featureLocation.getGenomeBrowserTrack();

        imageBuilder.tracks(new GenomeBrowserTrack[]{GenomeBrowserTrack.GENES, featureTrack, GenomeBrowserTrack.TRANSCRIPTS, extraTrack});

        return imageBuilder.build();
    }

    public static ReferenceDatabase getForeignDbMutationDetailDna(String accessionNumber) {
        // check for Genbank: Genomic and RNA, RefSeq and Ensembl
        ForeignDB.AvailableName[] databases = {AvailableName.GENBANK, AvailableName.REFSEQ};
        ForeignDBDataType.DataType[] dataTypes = {DataType.GENOMIC, DataType.RNA};

        List<ReferenceDatabase> refDatabaseList = getSequenceRepository().getReferenceDatabases(Arrays.asList(databases),
                Arrays.asList(dataTypes),
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.Type.ZEBRAFISH);

        ReferenceDatabase databaseMatch = checkRefDatabase(accessionNumber, refDatabaseList);
        if (databaseMatch != null) {
            return databaseMatch;
        }

        // if not found check Accession, aka accession_bank
        List<Accession> accessionList = getSequenceRepository().getAccessionsByNumber(accessionNumber);
        for (Accession accession : accessionList) {
            if (refDatabaseList.contains(accession.getReferenceDatabase())) {
                return accession.getReferenceDatabase();
            }
        }
        return null;
    }

    private static ReferenceDatabase checkRefDatabase(String accessionNumber, List<ReferenceDatabase> refDatabaseList) {
        ReferenceDatabase databaseMatch = null;
        for (ReferenceDatabase referenceDatabase : refDatabaseList) {
            List<DBLink> links = getSequenceRepository().getDBLinks(accessionNumber, referenceDatabase);
            if (CollectionUtils.isNotEmpty(links)) {
                databaseMatch = referenceDatabase;
                break;
            }
        }
        return databaseMatch;
    }

    public static ReferenceDatabase getForeignDbMutationDetailProtein(String accessionNumber) {
        ForeignDB.AvailableName[] databases = {AvailableName.GENBANK, AvailableName.REFSEQ, AvailableName.UNIPROTKB, AvailableName.GENPEPT};
        ForeignDBDataType.DataType[] dataTypes = {DataType.POLYPEPTIDE};

        List<ReferenceDatabase> genBankRefDB = getSequenceRepository().getReferenceDatabases(Arrays.asList(databases),
                Arrays.asList(dataTypes),
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.Type.ZEBRAFISH);
        for (ReferenceDatabase referenceDatabase : genBankRefDB) {
            List<DBLink> links = getSequenceRepository().getDBLinks(accessionNumber, referenceDatabase);
            if (CollectionUtils.isNotEmpty(links)) {
                return referenceDatabase;
            }
        }
        return null;
    }

    private static Map<String, String> featureGenomeLocationEvidenceCodeMap = new HashMap<>();

    static {
        featureGenomeLocationEvidenceCodeMap.put("ZDB-TERM-170419-250", "TAS");
        featureGenomeLocationEvidenceCodeMap.put("ZDB-TERM-170419-251", "IC");
        featureGenomeLocationEvidenceCodeMap.put("ZDB-TERM-170419-312", "IEA");
    }

    public static String getFeatureGenomeLocationEvidenceCode(String termID) {
        return featureGenomeLocationEvidenceCodeMap.get(termID);
    }

    public static String getFeatureGenomeLocationEvidenceCodeTerm(String evidenceCode) {
        Optional<Map.Entry<String, String>> entry = featureGenomeLocationEvidenceCodeMap.entrySet().stream()
                .filter(evidenceEntry -> evidenceEntry.getValue().equals(evidenceCode))
                .findAny();
        return entry.map(Map.Entry::getKey).orElse(null);
    }

    public static PhenotypeOnMarkerBean getPhenotypeOnFeature(@NotNull Feature feature) {
        // do not include multi-allelic features
        // include TG features if relationship is: innocuous
        if (feature.isMultiAllelic() || (!feature.isInnocuousOnlyTG() && feature.getType().equals(FeatureTypeEnum.TRANSGENIC_INSERTION))) {
            return null;
        }

        List<GenotypeFishResult> fishSummaryList = feature.getGenotypeFeatures().stream()
                .map(genotypeFeature -> FishService.getFishExperimentSummaryForGenotype(genotypeFeature.getGenotype()))
                .flatMap(Collection::stream)
//                .filter(genotypeFishResult -> genotypeFishResult.getFish().isClean())
                .sorted(Comparator.comparing(genotypeFishResult -> genotypeFishResult.getFish().getDisplayName()))
                .collect(Collectors.toList());
        PhenotypeOnMarkerBean phenotypeOnMarkerBean = new PhenotypeOnMarkerBean();

        Set<Figure> figures = fishSummaryList.stream()
                .filter(genotypeFishResult -> genotypeFishResult.getFishGenotypePhenotypeStatistics() != null)
                .map(GenotypeFishResult::getFishGenotypePhenotypeStatistics)
                .filter(fishGenotypePhenotypeStatistics -> fishGenotypePhenotypeStatistics.getFigures() != null)
                .map(FishGenotypePhenotypeStatistics::getFigures)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Set<Publication> publications = fishSummaryList.stream()
                .filter(genotypeFishResult -> genotypeFishResult.getFishGenotypePhenotypeStatistics() != null)
                .map(GenotypeFishResult::getFishGenotypePhenotypeStatistics)
                .filter(fishGenotypePhenotypeStatistics -> fishGenotypePhenotypeStatistics.getPublicationPaginationResult().getTotalCount() > 0)
                .map(FishGenotypePhenotypeStatistics::getPublicationPaginationResult)
                .map(PaginationResult::getPopulatedResults)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        // add the EaPs now
        List<ExpressionResult2> expressionExperiment2s = getExpressionRepository().getPhenotypeFromExpressionsByFeature(feature.getZdbID());
        assertNotNull(expressionExperiment2s);
        figures.addAll(expressionExperiment2s.stream().map(expression -> expression.getExpressionFigureStage().getFigure()).collect(Collectors.toList()));
        publications.addAll(expressionExperiment2s.stream().map(expression -> expression.getExpressionFigureStage().getFigure().getPublication()).collect(Collectors.toList()));

        phenotypeOnMarkerBean.setNumFigures(figures.size());
        phenotypeOnMarkerBean.setNumPublications(publications.size());


        return phenotypeOnMarkerBean;
    }

    @SneakyThrows
    public JsonResultResponse<GenotypeFishResult> getFishContainingFeature(String featureZdbID, boolean excludeFishWithSTR, Pagination pagination) {
        PaginationResult<FishGenotypeFeature> fishByFeature = mutantRepository.getFishByFeature(featureZdbID, excludeFishWithSTR, pagination);

        List<GenotypeFishResult> results = fishByFeature.getPopulatedResults().stream()
                .map(fishGenotypeFeature -> {
                    Fish fish = fishGenotypeFeature.getFish();
                    GenotypeFishResult result = FishService.getFishExperimentSummaryForFish(fish);
                    GenotypeFeature genotypeFeature = fishGenotypeFeature.getGenotypeFeature();
                    GenotypeDisplay genotypeDisplay = new GenotypeDisplay();
                    genotypeDisplay.setGenotype(genotypeFeature.getGenotype());
                    genotypeDisplay.setDadZygosity(genotypeFeature.getDadZygosity());
                    genotypeDisplay.setMomZygosity(genotypeFeature.getMomZygosity());
                    if (CollectionUtils.isNotEmpty(fish.getStrList())) {
                        result.setZygosity(GenotypeDisplay.COMPLEX);
                    } else {
                        result.setZygosity(genotypeDisplay.getZygosity());
                    }
                    if (result.getZygosity().equals(GenotypeDisplay.COMPLEX)){
                        result.setParentalZygosity("");
                    }
                    else {
                        result.setParentalZygosity(genotypeDisplay.getParentalZygosityDisplay());
                    }
                    result.setAffectedMarkers(new TreeSet<>(FishService.getAffectedGenes(fish)));
                    return result;
                })
                .collect(Collectors.toList());

        JsonResultResponse<GenotypeFishResult> response = new JsonResultResponse<>();
        response.setResults(results);
        response.setTotal(fishByFeature.getTotalCount());

        return response;
    }
}
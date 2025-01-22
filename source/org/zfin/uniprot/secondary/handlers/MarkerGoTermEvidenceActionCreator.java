package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Subset;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.DBLinkSlimDTO;
import org.zfin.uniprot.dto.MarkerGoTermEvidenceSlimDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTerm2GoTerm;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zfin.ontology.Subset.GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * Adds to marker_go_term_evidence table
 * It's based on the new entries that are being added as a result of the AddNewFromUniProtsHandler
 * New accessions that are being loaded as a result of that load will then get processed here
 * and new entries in the marker_go_term_evidence table will be created.
 * This uses *2go translation files for converting EC, InterPro, UniProt Keywords(SPKW) to GO terms.
 * (actually, since UniProt Keywords require special handling, that logic is in AddNewSpKeywordTermToGoHandler
 */
@Log4j2
public class MarkerGoTermEvidenceActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
    }
    public static final String EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-031118-3";
    public static final String IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-020724-1";

    public static final String SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-020723-1";
    protected final ForeignDB.AvailableName dbName;

    protected final List<SecondaryTerm2GoTerm> translationRecords;

    public MarkerGoTermEvidenceActionCreator(ForeignDB.AvailableName dbName, List<SecondaryTerm2GoTerm> translationRecords) {
        this.dbName = dbName;
        this.translationRecords = translationRecords;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        //create markerGoTermEvidenceActions from new interpro IDs
        log.info("Creating markerGoTermEvidenceActions from new " + dbName + " IDs");
        List<SecondaryTermLoadAction> markerGoTermEvidenceActions =
                createMarkerGoTermEvidencesFromNewSecondaryTermIDs(uniProtRecords, actions, context);

        log.info("Created " + markerGoTermEvidenceActions.size() + " markerGoTermEvidenceActions before filtering");
        return filterTerms(markerGoTermEvidenceActions);
    }

    public static List<SecondaryTermLoadAction> filterTerms(List<SecondaryTermLoadAction> markerGoTermEvidences) {
        //filter out unknown and root terms
        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences = filterUnknownAndRootTerms(markerGoTermEvidences);
        log.info("After first pass of filtering: " + filteredMarkerGoTermEvidences.size() + " markerGoTermEvidences");

        //filter out terms for WITHDRAWN markers
        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences2 = filterWithdrawnMarkers(filteredMarkerGoTermEvidences);
        log.info("After second pass of filtering: " + filteredMarkerGoTermEvidences2.size() + " markerGoTermEvidences");

        //filter out terms not meant to be annotated
        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences3 = filterNonAnnotatedTerms(filteredMarkerGoTermEvidences2);
        log.info("After third pass of filtering: " + filteredMarkerGoTermEvidences3.size() + " markerGoTermEvidences");

        return filteredMarkerGoTermEvidences3;
    }

    public static List<SecondaryTermLoadAction> filterNonAnnotatedTerms(List<SecondaryTermLoadAction> markerGoTermEvidences) {
        List<Subset> subsets = getOntologyRepository().getAllSubsets();
        Subset notForAnnotations = subsets.stream().filter(subset -> subset.getInternalName().equals(GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS)).findFirst().orElse(null);
        if (notForAnnotations == null) {
            throw new RuntimeException("Could not find subset " + GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS);
        }
        List<String> termZdbIDs = notForAnnotations.getTerms().stream().map(GenericTerm::getZdbID).toList();
        return markerGoTermEvidences.stream()
                .filter(action -> !termZdbIDs.contains(action.getGoTermZdbID()))
                .toList();
    }

    public static List<SecondaryTermLoadAction> filterWithdrawnMarkers(List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences) {
        List<Marker> withdrawnMarkers = getMarkerRepository().getWithdrawnMarkers();
        List<String> withdrawnZdbIDs = withdrawnMarkers.stream()
                .map(Marker::getZdbID)
                .toList();
        return filteredMarkerGoTermEvidences.stream()
                .filter(action -> !withdrawnZdbIDs.contains(action.getGeneZdbID()))
                .toList();
    }

    public static List<SecondaryTermLoadAction> filterUnknownAndRootTerms(List<SecondaryTermLoadAction> markerGoTermEvidences) {
        //unknown and root terms are not allowed
        List<String> unknownAndRootTerms = List.of("GO:0005554", "GO:0000004", "GO:0008372", "GO:0005575", "GO:0003674", "GO:0008150");

        return markerGoTermEvidences.stream()
                .filter(action -> !unknownAndRootTerms.contains(action.getGoID()))
                .toList();
    }

    protected List<SecondaryTermLoadAction> createMarkerGoTermEvidencesFromNewSecondaryTermIDs(
                                                                            UniprotReleaseRecords uniProtRecords,
                                                                            List<SecondaryTermLoadAction> actions,
                                                                            SecondaryLoadContext context) {

        //need to get all existing DBLinks for this DB
        //combine them with the new DBLinks created by the previous handler in the pipeline
        //and then create markerGoTermEvidences for all those DBLinks
        //then filter out the markerGoTermEvidences that are already in the database
        //and then create markerGoTermEvidences for the remaining DBLinks
        //we will need to delete any existing markerGoTermEvidences that do not correspond to any of the db links

        //all existing db links
        List<DBLinkSlimDTO> existingDbLinks = context.getFlattenedDbLinksByDbName(dbName);
        log.info("Count of existing db links (" + dbName + "): " + existingDbLinks.size());

        //all new db links (based on actions)
        List<DBLinkSlimDTO> actionsLoadedFromNewDBLinks = actions.stream()
                .filter(action -> dbName.equals(action.getDbName()) && action.getType().equals(SecondaryTermLoadAction.Type.LOAD))
                .map(
                        action -> DBLinkSlimDTO.builder()
                                .accession(action.getAccession())
                                .dataZdbID(action.getGeneZdbID())
                                .build()
                )
                .toList();

        log.info("Count of new db links (" + dbName + "): " + actionsLoadedFromNewDBLinks.size());

        List<DBLinkSlimDTO> allDbLinks = Stream.concat(existingDbLinks.stream(), actionsLoadedFromNewDBLinks.stream()).toList();
        log.info("Count of all db links (" + dbName + "): " + allDbLinks.size());

        //create markerGoTermEvidences for all db links
        log.info("Joining DBLinks (" + dbName + "): with translation records...");
        //join the set of allDbLinks (load actions and existing db links) to the interpro/ec/spkw translation records
        List<Tuple2<DBLinkSlimDTO, SecondaryTerm2GoTerm>> joined = Seq.seq(allDbLinks)
                .innerJoin(translationRecords,
                        (dbLink, item2go) -> dbLink.getAccession().equals(item2go.dbAccession()))
                .toList();
        log.info("Count of joined records (" + dbName + "): " + joined.size());

        //convert to markerGoTermEvidenceSlimDTOs
        List<MarkerGoTermEvidenceSlimDTO> calculatedMarkerGoTermEvidences = joined.stream().map(
                joinedRecord -> {
                    DBLinkSlimDTO dblink = joinedRecord.v1();
                    SecondaryTerm2GoTerm item2go = joinedRecord.v2();
                    return MarkerGoTermEvidenceSlimDTO.builder()
                            .markerZdbID(dblink.getDataZdbID())
                            .goID("GO:" + item2go.goID())
                            .goTermZdbID(item2go.termZdbID())
                            .publicationID(context.getPubIDForMarkerGoTermEvidenceByDB(dbName))
                            .inferredFrom(dbName + ":" + dblink.getAccession())
                            .build();
                }
        )
        .filter(mgt -> {
            if (mgt.getGoID() == null) {
                log.error("No match to GO for " + mgt.getMarkerZdbID() + " " + mgt.getInferredFrom());
                return false;
            }
            if (mgt.getGoTermZdbID() == null) {
                log.error("No match for ZDB-TERM for " + mgt.getMarkerZdbID() + " " + mgt.getInferredFrom());
                return false;
            }
            return true;
        })
        .toList();

        log.info("Count of calculated marker go term evidences (" + dbName + "): " + calculatedMarkerGoTermEvidences.size());

        List<MarkerGoTermEvidenceSlimDTO> existingMarkerGoTermEvidences = context.getExistingMarkerGoTermEvidenceRecords(dbName);

        List<MarkerGoTermEvidenceSlimDTO> toAdd = ListUtils.subtract(calculatedMarkerGoTermEvidences, existingMarkerGoTermEvidences);
        toAdd = toAdd.stream().distinct().toList();

        List<MarkerGoTermEvidenceSlimDTO> toDelete = ListUtils.subtract(existingMarkerGoTermEvidences, calculatedMarkerGoTermEvidences);

        log.info("Count of marker go term evidences to add (" + dbName + "):  " + toAdd.size());
        //convert marker_go_term_evidence records to load actions
        List<SecondaryTermLoadAction> toAddActions = toAdd.stream()
                .map(
                        markerGoTermEvidence -> {
                            if (markerGoTermEvidence.getGoTermZdbID() == null) {
                                log.error("No GO term ZDB ID for marker go term evidence: " + markerGoTermEvidence.getGoID() + " " + markerGoTermEvidence.getMarkerZdbID());
                                return null;
                            } else {
                                return SecondaryTermLoadAction.builder()
                                                .geneZdbID(markerGoTermEvidence.getMarkerZdbID())
                                                .dbName(dbName)
                                                .relatedEntityFields(markerGoTermEvidence.toMap())
                                                .type(SecondaryTermLoadAction.Type.LOAD)
                                                .subType(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE)
                                                .details("Uniprot release file record(s) for related gene")
                                                .uniprotAccessions(uniProtRecords.getByGeneZdbID(markerGoTermEvidence.getMarkerZdbID())
                                                        .stream()
                                                        .map(RichSequenceAdapter::getAccession)
                                                        .collect(Collectors.toSet()))
                                                .build();
                            }
                        }
                )
                .filter(Objects::nonNull)
                .toList();

        log.info("Count of marker go term evidences to delete (" + dbName + "): " + toDelete.size());
        List<SecondaryTermLoadAction> toDeleteActions = toDelete.stream()
                .map(
                        markerGoTermEvidence -> SecondaryTermLoadAction.builder()
                                .geneZdbID(markerGoTermEvidence.getMarkerZdbID())
                                .dbName(dbName)
                                .relatedEntityFields(markerGoTermEvidence.toMap())
                                .type(SecondaryTermLoadAction.Type.DELETE)
                                .subType(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE)
                                .details("Uniprot release file record(s) for related gene")
                                .uniprotAccessions(uniProtRecords.getByGeneZdbID(markerGoTermEvidence.getMarkerZdbID())
                                        .stream()
                                        .map(RichSequenceAdapter::getAccession)
                                        .collect(Collectors.toSet()))
                                .build()
                )
                .toList();
        return Stream.concat(toAddActions.stream(), toDeleteActions.stream()).toList();
    }

}

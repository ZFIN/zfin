package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.marker.Marker;
import org.zfin.ontology.Subset;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.zfin.ontology.Subset.GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

@Log4j2
public class AddNewSecondaryTermToGoHandler implements SecondaryLoadHandler {

    protected final ForeignDB.AvailableName dbName;
    protected final List<SecondaryTerm2GoTerm> translationRecords;

    public AddNewSecondaryTermToGoHandler(ForeignDB.AvailableName dbName, List<SecondaryTerm2GoTerm> translationRecords) {
        this.dbName = dbName;
        this.translationRecords = translationRecords;
    }

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        List<SecondaryTermLoadAction> secondaryTermLoadActions = actions.stream()
                .filter(action -> dbName.equals(action.getDbName()) && action.getType().equals(SecondaryTermLoadAction.Type.LOAD))
                .toList();

        //create markerGoTermEvidences from new interpro IDs
        log.debug("Creating markerGoTermEvidences from new " + dbName + " IDs");
        List<SecondaryTermLoadAction> markerGoTermEvidences;

        markerGoTermEvidences = createMarkerGoTermEvidencesFromNewSecondaryTermIDs(secondaryTermLoadActions);

        log.debug("Created " + markerGoTermEvidences.size() + " markerGoTermEvidences before filtering");
        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences = filterTerms(markerGoTermEvidences);

        actions.addAll(filteredMarkerGoTermEvidences);
    }

    protected List<SecondaryTermLoadAction> filterTerms(List<SecondaryTermLoadAction> markerGoTermEvidences) {
        //filter out unknown and root terms
        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences = filterUnknownAndRootTerms(markerGoTermEvidences);
        log.debug("After first pass of filtering: " + filteredMarkerGoTermEvidences.size() + " markerGoTermEvidences");

        //filter out terms for WITHDRAWN markers
        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences2 = filterWithdrawnMarkers(filteredMarkerGoTermEvidences);
        log.debug("After second pass of filtering: " + filteredMarkerGoTermEvidences2.size() + " markerGoTermEvidences");

        //filter out terms not meant to be annotated
        List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences3 = filterNonAnnotatedTerms(filteredMarkerGoTermEvidences2);
        log.debug("After third pass of filtering: " + filteredMarkerGoTermEvidences3.size() + " markerGoTermEvidences");

        return filteredMarkerGoTermEvidences3;
    }

    protected List<SecondaryTermLoadAction> filterNonAnnotatedTerms(List<SecondaryTermLoadAction> markerGoTermEvidences) {
        List<Subset> subsets = getOntologyRepository().getAllSubsets();
        Subset notForAnnotations = subsets.stream().filter(subset -> subset.getInternalName().equals(GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS)).findFirst().orElse(null);
        if (notForAnnotations == null) {
            throw new RuntimeException("Could not find subset " + GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS);
        }
        List<String> termZdbIDs = notForAnnotations.getTerms().stream().map(term -> term.getZdbID()).toList();
        return markerGoTermEvidences.stream()
                .filter(action -> !termZdbIDs.contains(action.getGoTermZdbID()))
                .toList();
    }

    protected List<SecondaryTermLoadAction> filterWithdrawnMarkers(List<SecondaryTermLoadAction> filteredMarkerGoTermEvidences) {
        List<Marker> withdrawnMarkers = getMarkerRepository().getWithdrawnMarkers();
        List<String> withdrawnZdbIDs = withdrawnMarkers.stream()
                .map(Marker::getZdbID)
                .toList();
        return filteredMarkerGoTermEvidences.stream()
                .filter(action -> !withdrawnZdbIDs.contains(action.getGeneZdbID()))
                .toList();
    }

    protected List<SecondaryTermLoadAction> filterUnknownAndRootTerms(List<SecondaryTermLoadAction> markerGoTermEvidences) {
        //unknown and root terms are not allowed
        List<String> unknownAndRootTerms = List.of("GO:0005554", "GO:0000004", "GO:0008372", "GO:0005575", "GO:0003674", "GO:0008150");

        return markerGoTermEvidences.stream()
                .filter(action -> !unknownAndRootTerms.contains(action.getGoID()))
                .toList();
    }

    protected List<SecondaryTermLoadAction> createMarkerGoTermEvidencesFromNewSecondaryTermIDs(List<SecondaryTermLoadAction> loads) {

        log.debug("Joining " + loads.size()  + " SecondaryLoadAction against " + translationRecords.size() + " " + dbName + " translation records ");

        List<SecondaryTermLoadAction> newMarkerGoTermEvidences = new ArrayList<>();

        //join the load actions to the interpro/ec/spkw translation records
        List<Tuple2<SecondaryTermLoadAction, SecondaryTerm2GoTerm>> joined = Seq.seq(loads)
                .innerJoin(translationRecords,
                        (action, item2go) -> action.getAccession().equals(item2go.dbAccession()))
                .toList();

        //map joined records to load actions
        for(var joinedRecord : joined) {
            SecondaryTermLoadAction action = joinedRecord.v1();
            SecondaryTerm2GoTerm item2go = joinedRecord.v2();
            SecondaryTermLoadAction newAction = SecondaryTermLoadAction.builder()
                    .accession(action.getAccession())
                    .dbName(dbName)
                    .type(SecondaryTermLoadAction.Type.LOAD)
                    .subType(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE)
                    .geneZdbID(action.getGeneZdbID())
                    .goID(item2go.goID())
                    .goTermZdbID(item2go.termZdbID())
                    .build();
            newMarkerGoTermEvidences.add(newAction);
        }
        return newMarkerGoTermEvidences;
    }

}

package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Subset;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.zfin.ontology.Subset.GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * Adds to marker_go_term_evidence table
 * It's based on the new entries that are being added as a result of the AddNewFromUniProtsHandler
 * New accessions that are being loaded as a result of that load will then get processed here
 * and new entries in the marker_go_term_evidence table will be created.
 * This uses *2go translation files for converting EC, InterPro, UniProt Keywords(SPKW) to GO terms.
 * (actually, since UniProt Keywords require special handling, that logic is in AddNewSpKeywordTermToGoHandler
 */
@Log4j2
public class AddNewSecondaryTermToGoHandler implements SecondaryLoadHandler {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
    }
    public static final String EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-031118-3";
    public static final String IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-020724-1";

    public static final String SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-020723-1";
    protected final ForeignDB.AvailableName dbName;

    protected final List<SecondaryTerm2GoTerm> translationRecords;

    public AddNewSecondaryTermToGoHandler() {
        this.dbName = null;
        this.translationRecords = null;
    }

    public AddNewSecondaryTermToGoHandler(ForeignDB.AvailableName dbName, List<SecondaryTerm2GoTerm> translationRecords) {
        this.dbName = dbName;
        this.translationRecords = translationRecords;
    }

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
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

    public static List<SecondaryTermLoadAction> filterTerms(List<SecondaryTermLoadAction> markerGoTermEvidences) {
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

    public static List<SecondaryTermLoadAction> filterNonAnnotatedTerms(List<SecondaryTermLoadAction> markerGoTermEvidences) {
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
                    .subType(isSubTypeHandlerFor())
                    .geneZdbID(action.getGeneZdbID())
                    .goID(item2go.goID())
                    .goTermZdbID(item2go.termZdbID())
                    .handlerClass(this.getClass().getName())
                    .build();
            newMarkerGoTermEvidences.add(newAction);
        }
        return newMarkerGoTermEvidences;
    }


    @Override
    public void processActions(List<SecondaryTermLoadAction> subTypeActions) {
        for(SecondaryTermLoadAction action : subTypeActions) {
            loadMarkerGoTermEvidence(action);
        }
    }

    private static void loadMarkerGoTermEvidence(SecondaryTermLoadAction action)  {
        MarkerGoTermEvidence markerGoTermEvidence = new MarkerGoTermEvidence();
        markerGoTermEvidence.setExternalLoadDate(null);

        GafOrganization uniprotGafOrganization = getMarkerGoTermEvidenceRepository().getGafOrganization(GafOrganization.OrganizationEnum.UNIPROT);
        markerGoTermEvidence.setGafOrganization(uniprotGafOrganization);

        markerGoTermEvidence.setOrganizationCreatedBy(GafOrganization.OrganizationEnum.ZFIN.name());

        Marker marker = getMarkerRepository().getMarker(action.getGeneZdbID());
        markerGoTermEvidence.setMarker(marker);

        GenericTerm goTerm = HibernateUtil.currentSession().get(GenericTerm.class, action.getGoTermZdbID());
        markerGoTermEvidence.setGoTerm(goTerm);

        GoEvidenceCode goEvidenceCode = getMarkerGoTermEvidenceRepository().getGoEvidenceCode(GoEvidenceCodeEnum.IEA.name());
        markerGoTermEvidence.setEvidenceCode(goEvidenceCode);
        String pubID = null;
        switch(action.getDbName()) {
            case INTERPRO -> {
                markerGoTermEvidence.setNote("ZFIN InterPro 2 GO");
                pubID = IP_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            }
            case EC -> {
                markerGoTermEvidence.setNote("ZFIN EC acc 2 GO");
                pubID = EC_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            }
            case UNIPROTKB -> {
                markerGoTermEvidence.setNote("ZFIN SP keyword 2 GO");
                pubID = SPKW_MRKRGOEV_PUBLICATION_ATTRIBUTION_ID;
            }
            default -> log.error("Unknown marker_go_term_evidence dbname to load " + action.getDbName());
        }

        // set source
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubID);
        markerGoTermEvidence.setSource(publication);

        Date rightNow = new Date();
        markerGoTermEvidence.setModifiedWhen(rightNow);
        markerGoTermEvidence.setCreatedWhen(rightNow);
        getMarkerGoTermEvidenceRepository().addEvidence(markerGoTermEvidence, false);
        getMutantRepository().addInferenceToGoMarkerTermEvidence(markerGoTermEvidence, action.getPrefixedAccession());
    }


}

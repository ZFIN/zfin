package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.gwt.root.dto.GoDefaultPublication;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.uniprot.dto.MarkerGoTermEvidenceSlimDTO;
import org.zfin.uniprot.dto.UniProtLoadSummaryDTO;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.sequence.ForeignDB.AvailableName.*;
import static org.zfin.sequence.ForeignDB.AvailableName.INTERPRO;
import static org.zfin.uniprot.UniProtTools.AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

@Getter
@Setter
@Log4j2
public class SecondaryTermLoadService {


    public static final String DBLINK_PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;
    public static final String EXTNOTE_PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;


    public static final String EXTNOTE_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-47";
    public static final String INTERPRO_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-48";
    public static final String EC_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-49";
    public static final String PFAM_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-50";
    public static final String PROSITE_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-51";



    public static ReferenceDatabase getReferenceDatabaseForAction(SecondaryTermLoadAction action) {
        return getReferenceDatabase(getReferenceDatabaseIDForAction(action));
    }

    public static String getReferenceDatabaseIDForAction(SecondaryTermLoadAction action) {
        String referenceDatabaseID = null;
        switch (action.getDbName()) {
            case INTERPRO -> referenceDatabaseID = INTERPRO_REFERENCE_DATABASE_ID;
            case EC -> referenceDatabaseID = EC_REFERENCE_DATABASE_ID;
            case PFAM -> referenceDatabaseID = PFAM_REFERENCE_DATABASE_ID;
            case PROSITE -> referenceDatabaseID = PROSITE_REFERENCE_DATABASE_ID;
            default -> log.error("Unknown dblink dbname to load " + action.getDbName());
        }
        return referenceDatabaseID;
    }

    public static ReferenceDatabase getReferenceDatabase(String referenceDatabaseID) {
        return getSequenceRepository().getReferenceDatabaseByID(referenceDatabaseID);
    }

    /**
     * Generate a table of statistics like the legacy load did.
     * This is calculable based on the actions that were performed and the context.
     *
     * Example:
     * count of records associated with UniProt     	before load	after load 	percentage change
     * ---------------------------------------------	-----------	-----------	-----------------
     * db_link records                              	     194687	     194462	            -0.12
     * external_note with db_link                   	          0	      25525
     * genes with duplicated db_link notes          	          0	          0	   not calculated
     * ---------------------------------------------	-----------	-----------	-----------------
     * marker_go_term_evidence IEA records          	     112109	     112381	             0.24
     * marker_go_term_evidence records from SP      	      46510	      46861	             0.75
     * marker_go_term_evidence records from IP      	      61647	      61827	             0.29
     * marker_go_term_evidence records from EC      	       3952	       3693	            -6.55
     * ---------------------------------------------	-----------	-----------	-----------------
     * go terms with IEA annotation                 	       3701	       3684	            -0.46
     * component go terms with IEA                  	        432	        431	            -0.23
     * function go terms with IEA                   	       1958	       1943	            -0.77
     * process go terms with IEA                    	       1311	       1310	            -0.08
     * ---------------------------------------------	-----------	-----------	-----------------
     * markers with IEA annotation                  	      18033	      17993	            -0.22
     * markers with IEA annotation component        	        432	        431	            -0.23
     * markers with IEA annotation function         	      13536	      13557	             0.16
     * markers with IEA annotation process          	      10938	      10921	            -0.16
     *
     * @param actions
     * @param context
     */
    public static List<UniProtLoadSummaryDTO> createStatistics(List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        System.out.println("Creating statistics for " + actions.size() + " actions.");

        List<UniProtLoadSummaryDTO> summaryRows = new ArrayList<>();

        //get the marker_go_term_evidence records for before the load
        List<MarkerGoTermEvidenceSlimDTO> mgte = context.getExistingMarkerGoTermEvidenceRecords();

        int spkwBefore = mgte.stream().filter(m -> m.getPublicationID().equals(GoDefaultPublication.UNIPROTKBKW.zdbID())).toList().size();
        int ecBefore = mgte.stream().filter(m -> m.getPublicationID().equals(GoDefaultPublication.EC.zdbID())).toList().size();
        int ipBefore = mgte.stream().filter(m -> m.getPublicationID().equals(GoDefaultPublication.INTERPRO.zdbID())).toList().size();
        int mgteTotalBefore = mgte.size();

        List<SecondaryTermLoadAction> loadActions = actions.stream().filter(a -> a.getType().equals(SecondaryTermLoadAction.Type.LOAD)).toList();
        List<SecondaryTermLoadAction> mgteLoadActions = loadActions
                .stream()
                .filter(a -> a.getSubType().equals(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE))
                .toList();

        List<SecondaryTermLoadAction> spkwLoadActions = mgteLoadActions.stream().filter(a -> a.getDbName().equals(UNIPROTKB)).toList();
        List<SecondaryTermLoadAction> ecLoadActions = mgteLoadActions.stream().filter(a -> a.getDbName().equals(EC)).toList();
        List<SecondaryTermLoadAction> ipLoadActions = mgteLoadActions.stream().filter(a -> a.getDbName().equals(INTERPRO)).toList();

        List<SecondaryTermLoadAction> deleteActions = actions.stream().filter(a -> a.getType().equals(SecondaryTermLoadAction.Type.DELETE)).toList();
        List<SecondaryTermLoadAction> mgteDeleteActions = deleteActions
                .stream()
                .filter(a -> a.getSubType().equals(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE))
                .toList();

        List<SecondaryTermLoadAction> spkwDeleteActions = mgteDeleteActions.stream().filter(a -> a.getDbName().equals(UNIPROTKB)).toList();
        List<SecondaryTermLoadAction> ecDeleteActions = mgteDeleteActions.stream().filter(a -> a.getDbName().equals(EC)).toList();
        List<SecondaryTermLoadAction> ipDeleteActions = mgteDeleteActions.stream().filter(a -> a.getDbName().equals(INTERPRO)).toList();

        summaryRows.add(new UniProtLoadSummaryDTO("marker_go_term_evidence IEA records",
                mgte.size(), mgte.size() - mgteDeleteActions.size() + mgteLoadActions.size()));

        summaryRows.add(new UniProtLoadSummaryDTO("marker_go_term_evidence IEA records from SP",
                spkwBefore, spkwBefore - spkwDeleteActions.size() + spkwLoadActions.size()));

        summaryRows.add(new UniProtLoadSummaryDTO("marker_go_term_evidence IEA records from IP",
                ipBefore, ipBefore - ipDeleteActions.size() + ipLoadActions.size()));

        summaryRows.add(new UniProtLoadSummaryDTO("marker_go_term_evidence IEA records from EC",
                ecBefore, ecBefore - ecDeleteActions.size() + ecLoadActions.size()));

        //get the marker_go_term_evidence records "go terms" breakdown
        //go terms with IEA annotation                 	       3701	       3684	            -0.46
        //component go terms with IEA                  	        432	        431	            -0.23
        //function go terms with IEA                   	       1958	       1943	            -0.77
        //process go terms with IEA                    	       1311	       1310	            -0.08

        generateStatisticsForGoTermsWithIEA(context, summaryRows, mgte, mgteLoadActions, mgteDeleteActions);
        generateStatisticsForMarkersWithIEA(context, summaryRows, mgte, mgteLoadActions, mgteDeleteActions);

        System.out.println("Found " + mgte.size() + " existing marker_go_term_evidence records.");
        summaryRows.forEach(r -> r.debugToStdOut());
        return summaryRows;
    }

    private static void generateStatisticsForGoTermsWithIEA(SecondaryLoadContext context, List<UniProtLoadSummaryDTO> summaryRows, List<MarkerGoTermEvidenceSlimDTO> mgte, List<SecondaryTermLoadAction> mgteLoadActions, List<SecondaryTermLoadAction> mgteDeleteActions) {
        Map<String, String> ontologyMap = context.getTermOntologyMap();
        List<String> goTermsBefore = mgte.stream().map(m -> m.getGoTermZdbID()).toList();
        Map<String, Integer> totalGoTermsBefore = getCardinalityMapOfGoTerms(context, goTermsBefore, null);

        //cellular_component', 'molecular_function', 'biological_process
        Map<String, Integer> componentGoTermsBefore = getCardinalityMapOfGoTerms(context, goTermsBefore, "cellular_component");
        Map<String, Integer> functionGoTermsBefore = getCardinalityMapOfGoTerms(context, goTermsBefore, "molecular_function");
        Map<String, Integer> processGoTermsBefore = getCardinalityMapOfGoTerms(context, goTermsBefore, "biological_process");
        List<String> unmatchedGoTerms = new ArrayList<>(goTermsBefore.stream().filter(g -> null == ontologyMap.get(g)).toList());

        List<String> goTermsAdded = mgteLoadActions.stream().map(a -> a.getGoTermZdbID()).toList();
        Map<String, Integer> totalGoTermsAdded = getCardinalityMapOfGoTerms(context, goTermsAdded, null);
        Map<String, Integer> componentGoTermsAdded = getCardinalityMapOfGoTerms(context, goTermsAdded, "cellular_component");
        Map<String, Integer> functionGoTermsAdded = getCardinalityMapOfGoTerms(context, goTermsAdded, "molecular_function");
        Map<String, Integer> processGoTermsAdded = getCardinalityMapOfGoTerms(context, goTermsAdded, "biological_process");
        unmatchedGoTerms.addAll(goTermsAdded.stream().filter(g -> null == ontologyMap.get(g)).toList());

        List<String> goTermsDeleted = mgteDeleteActions.stream().map(a -> a.getGoTermZdbID()).toList();
        Map<String, Integer> totalGoTermsDeleted = getCardinalityMapOfGoTerms(context, goTermsDeleted, null);
        Map<String, Integer> componentGoTermsDeleted = getCardinalityMapOfGoTerms(context, goTermsDeleted, "cellular_component");
        Map<String, Integer> functionGoTermsDeleted = getCardinalityMapOfGoTerms(context, goTermsDeleted, "molecular_function");
        Map<String, Integer> processGoTermsDeleted = getCardinalityMapOfGoTerms(context, goTermsDeleted, "biological_process");
        unmatchedGoTerms.addAll(goTermsDeleted.stream().filter(g -> null == ontologyMap.get(g)).toList());

        if (!unmatchedGoTerms.isEmpty()) {
            log.error("Unmatched go terms: " + unmatchedGoTerms.size());
            unmatchedGoTerms.forEach(g -> System.out.println(g));
            //System.exit(1);
        }

        //to get the number of total distinct go terms (cellular_component), combine the cardinality maps of the existing terms + new terms - deleted terms
        //this should work since if for example, a term "A" exists before the load 5 times, and is deleted 2 times, and added 3 times, the cardinality map will be 6
        //and if a term "B" exists before the load 2 times, and is deleted 2 times, the cardinality map will be 0 and therefore omitted as a term
        Map<String, Integer> afterAddition = addOrSubtractCardinalityMaps(componentGoTermsBefore, componentGoTermsAdded, true);
        Map<String, Integer> afterSubtraction = addOrSubtractCardinalityMaps(afterAddition, componentGoTermsDeleted, false);
        summaryRows.add(new UniProtLoadSummaryDTO("component go terms with IEA", componentGoTermsBefore.size(), afterSubtraction.size()));

        afterAddition = addOrSubtractCardinalityMaps(functionGoTermsBefore, functionGoTermsAdded, true);
        afterSubtraction = addOrSubtractCardinalityMaps(afterAddition, functionGoTermsDeleted, false);
        summaryRows.add(new UniProtLoadSummaryDTO("function go terms with IEA", functionGoTermsBefore.size(), afterSubtraction.size()));

        afterAddition = addOrSubtractCardinalityMaps(processGoTermsBefore, processGoTermsAdded, true);
        afterSubtraction = addOrSubtractCardinalityMaps(afterAddition, processGoTermsDeleted, false);
        summaryRows.add(new UniProtLoadSummaryDTO("process go terms with IEA", processGoTermsBefore.size(), afterSubtraction.size()));

        afterAddition = addOrSubtractCardinalityMaps(totalGoTermsBefore, totalGoTermsAdded, true);
        afterSubtraction = addOrSubtractCardinalityMaps(afterAddition, totalGoTermsDeleted, false);
        summaryRows.add(new UniProtLoadSummaryDTO("total go terms with IEA", totalGoTermsBefore.size(), afterSubtraction.size()));
    }

    private static void generateStatisticsForMarkersWithIEA(SecondaryLoadContext context, List<UniProtLoadSummaryDTO> summaryRows, List<MarkerGoTermEvidenceSlimDTO> mgte, List<SecondaryTermLoadAction> mgteLoadActions, List<SecondaryTermLoadAction> mgteDeleteActions) {
        List<String> markersBefore = mgte.stream().map(m -> m.getMarkerZdbID()).toList();
        Map<String, Integer> totalsPerMarkerBefore = CollectionUtils.getCardinalityMap(markersBefore);
        Map<String, Integer> totalsPerMarkerAdded = CollectionUtils.getCardinalityMap(mgteLoadActions.stream().map(a -> a.getGeneZdbID()).toList());
        Map<String, Integer> totalsPerMarkerDeleted = CollectionUtils.getCardinalityMap(mgteDeleteActions.stream().map(a -> a.getGeneZdbID()).toList());

        //Existing marker_go_term_evidence rows by: cellular_component', 'molecular_function', 'biological_process
        Map<String, Integer> componentGoTermMarkersBefore = getMarkersByGoTermCategory(context, mgte, "cellular_component");
        Map<String, Integer> functionGoTermMarkersBefore = getMarkersByGoTermCategory(context, mgte, "molecular_function");
        Map<String, Integer> processGoTermMarkersBefore = getMarkersByGoTermCategory(context, mgte, "biological_process");

        //Added marker_go_term_evidence rows by: cellular_component', 'molecular_function', 'biological_process
        Map<String, Integer> componentGoTermMarkersAdded = getMarkersByGoTermCategory(context,
                mgteLoadActions.stream().map( action -> MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields()) ).toList(),
                "cellular_component");
        Map<String, Integer> functionGoTermMarkersAdded = getMarkersByGoTermCategory(context,
                mgteLoadActions.stream().map( action -> MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields()) ).toList(),
                "molecular_function");
        Map<String, Integer> processGoTermMarkersAdded = getMarkersByGoTermCategory(context,
                mgteLoadActions.stream().map( action -> MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields()) ).toList(),
                "biological_process");

        //Deleted marker_go_term_evidence rows by: cellular_component', 'molecular_function', 'biological_process
        Map<String, Integer> componentGoTermMarkersDeleted = getMarkersByGoTermCategory(context,
                mgteDeleteActions.stream().map( action -> MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields()) ).toList(),
                "cellular_component");
        Map<String, Integer> functionGoTermMarkersDeleted = getMarkersByGoTermCategory(context,
                mgteDeleteActions.stream().map( action -> MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields()) ).toList(),
                "molecular_function");
        Map<String, Integer> processGoTermMarkersDeleted = getMarkersByGoTermCategory(context,
                mgteDeleteActions.stream().map( action -> MarkerGoTermEvidenceSlimDTO.fromMap(action.getRelatedEntityFields()) ).toList(),
                "biological_process");

        //calculate changes in marker_go_term_evidence rows by: cellular_component', 'molecular_function', 'biological_process
        Map<String, Integer> afterAddition = addOrSubtractCardinalityMaps(componentGoTermMarkersBefore, componentGoTermMarkersAdded, true);
        Map<String, Integer> afterSubtraction = addOrSubtractCardinalityMaps(afterAddition, componentGoTermMarkersDeleted, false);
        summaryRows.add(new UniProtLoadSummaryDTO("markers with IEA annotation component", componentGoTermMarkersBefore.size(), afterSubtraction.size()));

        afterAddition = addOrSubtractCardinalityMaps(functionGoTermMarkersBefore, functionGoTermMarkersAdded, true);
        afterSubtraction = addOrSubtractCardinalityMaps(afterAddition, functionGoTermMarkersDeleted, false);
        summaryRows.add(new UniProtLoadSummaryDTO("markers with IEA annotation function", functionGoTermMarkersBefore.size(), afterSubtraction.size()));

        afterAddition = addOrSubtractCardinalityMaps(processGoTermMarkersBefore, processGoTermMarkersAdded, true);
        afterSubtraction = addOrSubtractCardinalityMaps(afterAddition, processGoTermMarkersDeleted, false);
        summaryRows.add(new UniProtLoadSummaryDTO("markers with IEA annotation process", processGoTermMarkersBefore.size(), afterSubtraction.size()));

        //calculate changes in total marker_go_term_evidence rows
        afterAddition = addOrSubtractCardinalityMaps(totalsPerMarkerBefore, totalsPerMarkerAdded, true);
        afterSubtraction = addOrSubtractCardinalityMaps(afterAddition, totalsPerMarkerDeleted, false);
        summaryRows.add(new UniProtLoadSummaryDTO("markers with IEA annotation", totalsPerMarkerBefore.size(), afterSubtraction.size()));
    }

    private static Map<String, Integer> getMarkersByGoTermCategory(SecondaryLoadContext context, List<MarkerGoTermEvidenceSlimDTO> mgte, String cellularComponent) {
        Map<String, String> map = context.getTermOntologyMap();
        return CollectionUtils.getCardinalityMap(
                mgte.stream()
                        .filter(m -> cellularComponent.equals(map.get(m.getGoTermZdbID())))
                        .map(m -> m.getMarkerZdbID())
                        .toList()
        );
    }

    private static Map<String, Integer> getCardinalityMapOfGoTerms(SecondaryLoadContext context, List<String> goTerms, String filteredOntology) {
        Map<String, String> ontologyMap = context.getTermOntologyMap();

        if (null == filteredOntology) {
            return CollectionUtils.getCardinalityMap(goTerms);
        }
        return CollectionUtils.getCardinalityMap(
                goTerms.stream().filter(g -> filteredOntology.equals(ontologyMap.get(g))).toList()
        );
    }

    private static Map<String, Integer> addOrSubtractCardinalityMaps(Map<String, Integer> baseMap, Map<String, Integer> modifierMap, boolean isAdd) {
        Map<String, Integer> copyOfBaseMap = new HashMap<>(baseMap);
        for (String term : modifierMap.keySet()) {
            int count = baseMap.getOrDefault(term, 0);
            count = isAdd ? count + modifierMap.get(term) : count - modifierMap.get(term);
            if(count <= 0) {
                copyOfBaseMap.remove(term);
            } else {
                copyOfBaseMap.put(term, count);
            }
        }
        return copyOfBaseMap;
    }

}

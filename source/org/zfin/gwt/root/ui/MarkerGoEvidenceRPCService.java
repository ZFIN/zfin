package org.zfin.gwt.root.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.List;
import java.util.Set;

/**
 */
@RemoteServiceRelativePath("MarkerGoEvidenceRPCService")
public interface MarkerGoEvidenceRPCService extends RemoteService {

    public static class App {
        private static MarkerGoEvidenceRPCServiceAsync ourInstance = null;

        public static synchronized MarkerGoEvidenceRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (MarkerGoEvidenceRPCServiceAsync) GWT.create(MarkerGoEvidenceRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/termservice");
            }
            return ourInstance;
        }
    }


    GoEvidenceDTO getMarkerGoTermEvidenceDTO(String zdbID);

    GoEvidenceDTO editMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO) throws DuplicateEntryException ;

    List<MarkerDTO> getGenesForGOAttributions(GoEvidenceDTO dto);

    List<RelatedEntityDTO> getGenotypesAndMorpholinosForGOAttributions(GoEvidenceDTO dto);

    List<GoEvidenceDTO> getGOTermsForPubAndMarker(GoEvidenceDTO dto);

    Set<String> getInferencesByMarkerAndType(GoEvidenceDTO dto, String inferenceCategory);

    boolean validateAccession(String accession, String inferenceCategory);

    GoEvidenceDTO createMarkerGoTermEvidence(GoEvidenceDTO goEvidenceDTO) throws DuplicateEntryException ;

    void deleteMarkerGoTermEvidence(String zdbID);

    TermDTO getGOTermByName(String value);

    List<GoEvidenceDTO> getMarkerGoTermEvidencesForPub(String publicationID);

    List<GoEvidenceDTO> getMarkerGoTermEvidencesForMarker(String markerID);

    List<MarkerDTO> getGenesForPub(String publicationID);

    String createInferenceLink(String inference);
}

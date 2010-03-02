package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.GoTermDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;

import java.util.List;
import java.util.Set;

/**
 */
@RemoteServiceRelativePath("TermRPCService")
public interface TermRPCService extends RemoteService {

    public static class App {
        private static TermRPCServiceAsync ourInstance = null;

        public static synchronized TermRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (TermRPCServiceAsync) GWT.create(TermRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/termservice");
            }
            return ourInstance;
        }
    }


    GoEvidenceDTO getMarkerGoTermEvidenceDTO(String zdbID) ;

    GoEvidenceDTO editMarkerHeaderGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO);

    GoEvidenceDTO editMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO);

    List<MarkerDTO> getGenesForGOAttributions(GoEvidenceDTO dto);

    List<RelatedEntityDTO> getGenoTypesAndMorpholinosForGOAttributions(GoEvidenceDTO dto);

    GoEvidenceDTO addInference(GoEvidenceDTO dto, String value,String inferenceCategory);

    GoEvidenceDTO removeInference(RelatedEntityDTO dto);

    List<GoEvidenceDTO> getGOTermsForPubAndMarker(GoEvidenceDTO dto);

    Set<String> getInferencesByMarkerAndType(GoEvidenceDTO dto, String inferenceCategory);

    boolean validateAccession(String accession, String inferenceCategory) ;

    GoEvidenceDTO createMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO);

    GoTermDTO getGOTermByName(String value);
}

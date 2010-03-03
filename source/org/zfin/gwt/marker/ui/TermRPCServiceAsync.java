package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.GoTermDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;

import java.util.List;
import java.util.Set;

/**
 */
public interface TermRPCServiceAsync {

    void getMarkerGoTermEvidenceDTO(String zdbID,AsyncCallback<GoEvidenceDTO> async);

    void editMarkerHeaderGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO, AsyncCallback<GoEvidenceDTO> async);

    void getGenoTypesAndMorpholinosForGOAttributions(GoEvidenceDTO dto, AsyncCallback<List<RelatedEntityDTO>> async);

    void addInference(GoEvidenceDTO dto, String zdbID, String inferenceCategory,AsyncCallback<GoEvidenceDTO> asyncCallback);

    void removeInference(RelatedEntityDTO dto, AsyncCallback<GoEvidenceDTO> asyncCallback);

    void getGOTermsForPubAndMarker(GoEvidenceDTO dto, AsyncCallback<List<GoEvidenceDTO>> asyncCallback);

    void getGenesForGOAttributions(GoEvidenceDTO dto, AsyncCallback<List<MarkerDTO>> async);

    void getInferencesByMarkerAndType(GoEvidenceDTO dto, String inferenceCategory, AsyncCallback<Set<String>> async);

    void editMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO, AsyncCallback<GoEvidenceDTO> async);

    void createMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO, AsyncCallback<GoEvidenceDTO> async);

    void getGOTermByName(String value, AsyncCallback<GoTermDTO> asyncCallback);

    void validateAccession(String accession, String inferenceCategory, AsyncCallback<Boolean> async);
}

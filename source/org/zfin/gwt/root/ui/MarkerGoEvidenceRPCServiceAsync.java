package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.List;
import java.util.Set;

/**
 */
public interface MarkerGoEvidenceRPCServiceAsync {

    void getMarkerGoTermEvidenceDTO(String zdbID, AsyncCallback<GoEvidenceDTO> async);

    void getGenotypesAndMorpholinosForGOAttributions(GoEvidenceDTO dto, AsyncCallback<List<RelatedEntityDTO>> async);

    void getGOTermsForPubAndMarker(GoEvidenceDTO dto, AsyncCallback<List<GoEvidenceDTO>> asyncCallback);

    void getGenesForGOAttributions(GoEvidenceDTO dto, AsyncCallback<List<MarkerDTO>> async);

    void getInferencesByMarkerAndType(GoEvidenceDTO dto, String inferenceCategory, AsyncCallback<Set<String>> async);

    void editMarkerGoTermEvidenceDTO(GoEvidenceDTO goEvidenceDTO, AsyncCallback<GoEvidenceDTO> async);

    void createMarkerGoTermEvidence(GoEvidenceDTO goEvidenceDTO, AsyncCallback<GoEvidenceDTO> async);

    void getGOTermByName(String value, AsyncCallback<TermDTO> asyncCallback);

    void validateAccession(String accession, String inferenceCategory, AsyncCallback<Boolean> async);

    void getMarkerGoTermEvidencesForPub(String publicationID, AsyncCallback<List<GoEvidenceDTO>> async);

    void getMarkerGoTermEvidencesForMarker(String markerID, AsyncCallback<List<GoEvidenceDTO>> markerEditCallBack);

    void getGenesForPub(String publicationID, AsyncCallback<List<MarkerDTO>> async);

    void deleteMarkerGoTermEvidence(String zdbID, AsyncCallback<Void> async);

    void createInferenceLink(String inference, AsyncCallback<String> async);
}

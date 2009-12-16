package org.zfin.marker.presentation.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.framework.presentation.client.TermNotFoundException;
import org.zfin.marker.presentation.dto.*;

import java.util.List;

/**
 * Marker related service methods.
 */
public interface MarkerRPCService extends RemoteService {

    /**
     * Utility/Convenience class.
     * Use TranscriptService.App.getInstance() to access static instance of TranscriptServiceAsync
     */
    public static class App {
        private static MarkerRPCServiceAsync ourInstance = null;

        public static synchronized MarkerRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (MarkerRPCServiceAsync) GWT.create(MarkerRPCService.class);
//                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(
//                        GWT.getModuleBaseURL() + "org.zfin.marker.presentation.Marker/TranscriptService");
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/markerservice");
            }
            return ourInstance;
        }
    }

    // publication access method

    PublicationAbstractDTO getPublicationAbstract(String zdbID);

    // note methods

    void addCuratorNote(NoteDTO noteDTO);

    void editCuratorNote(NoteDTO noteDTO);

    void removeCuratorNote(NoteDTO noteDTO);

    void editPublicNote(NoteDTO noteDTO);

    // self-attribution

    void addMarkerAttribution(String zdbID, String pubZdbID);

    void removeMarkerAttribution(String zdbID, String pubZdbID);


    /**
     * @param zdbID ZdbID of Marker to return attributions for.
     * @return Returns accession numbers as strings.
     */
    List<String> getMarkerAttributions(String zdbID);

    // alias attribution list

    RelatedEntityDTO addDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO);

    RelatedEntityDTO addDataAliasAttribution(RelatedEntityDTO relatedEntityDTO);

    void removeDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO);

    void removeDataAliasAttribution(RelatedEntityDTO relatedEntityDTO);

    DBLinkDTO addInternalProteinSequence(String markerZdbID, String sequence, String pubZdbID, String referenceZdbID) throws BlastDatabaseAccessException;

    DBLinkDTO addInternalNucleotideSequence(String markerZdbID, String sequence, String pubZdbID, String referenceZdbID) throws BlastDatabaseAccessException;


    /**
     * @return List of dblinks
     */
    List<DBLinkDTO> getMarkerDBLinksForAccession(String accession);

    // MarkerDBLink methods

    DBLinkDTO getDBLink(String zdbID);

    DBLinkDTO addDBLink(DBLinkDTO dbLinkDTO, List<ReferenceDatabaseDTO> referenceDatabaseDTOs) throws DBLinkNotFoundException;

    SequenceDTO addSequenceAttribution(SequenceDTO dbLinkDTO);

    DBLinkDTO addDBLinkAttribution(DBLinkDTO dbLinkDTO);

    DBLinkDTO removeDBLink(DBLinkDTO dbLinkDTO);

    DBLinkDTO removeDBLinkAttribution(DBLinkDTO dbLinkDTO);

    DBLinkDTO updateDBLinkLength(DBLinkDTO dbLinkDTO);

    // marker update

    void updateMarkerName(MarkerDTO markerDTO);

    MarkerDTO getMarkerForName(String name);

    MarkerDTO getGeneForZdbID(String zdbID);

    // supplier methods

    /**
     * @return List of supplier names
     */
    List<String> getAllSupplierNames();

    void addMarkerSupplier(String name, String markerZdbID);

    void removeMarkerSupplier(String name, String markerZdbID);

    String getWebDriverPath();


    // general methods

    MarkerDTO addRelatedMarker(MarkerDTO markerDTO) throws TermNotFoundException;

    MarkerDTO addRelatedMarkerAttribution(MarkerDTO markerDTO);

    void removeRelatedMarker(MarkerDTO markerDTO);

    void removeRelatedMarkerAttribution(MarkerDTO markerDTO);

    String validateDBLink(DBLinkDTO dbLinkDTO);
}

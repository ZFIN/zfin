package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 */
public interface TranscriptRPCService extends RemoteService {

    /**
     * Utility/Convenience class.
     * Use TranscriptService.App.getInstance() to access static instance of TranscriptServiceAsync
     */
    public static class App {
        private static TranscriptRPCServiceAsync ourInstance = null;

        public static synchronized TranscriptRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (TranscriptRPCServiceAsync) GWT.create(TranscriptRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/transcriptservice");
            }
            return ourInstance;
        }
    }

    // transcript methods

    TranscriptDTO changeTranscriptHeaders(TranscriptDTO transcriptDO) throws TranscriptTypeStatusMismatchException;

    TranscriptDTO getTranscriptForZdbID(String zdbID) throws BlastDatabaseAccessException;

    String getTranscriptTypeForZdbID(String zdbID);

    // sequence method

    SequenceDTO getProteinSequenceForAccessionAndRefDB(String accession, String refDBName) throws BlastDatabaseAccessException;

    DBLinkDTO addProteinSequence(String transcriptZdbID, String sequence, String pubZdbID, String referenceDatabaseZdbID) throws BlastDatabaseAccessException;

    List<ReferenceDatabaseDTO> getTranscriptSupportingSequencesReferenceDatabases();

    List<ReferenceDatabaseDTO> getTranscriptAddableNucleotideSequenceReferenceDatabases(TranscriptDTO transcriptDTO);

    List<ReferenceDatabaseDTO> getTranscriptEditAddProteinSequenceReferenceDatabases();

    List<ReferenceDatabaseDTO> getGeneEditAddableStemLoopNucleotideSequenceReferenceDatabases();

    List<ReferenceDatabaseDTO> getGeneEditAddProteinSequenceReferenceDatabases();


    List<String> getTranscriptTypes();

    List<String> getTranscriptStatuses();

    // rna transcript sequence method

    SequenceDTO addNucleotideSequenceToTranscript(TranscriptDTO transcriptDTO, SequenceDTO sequenceDTO, ReferenceDatabaseDTO referenceDatabaseDTO) throws BlastDatabaseAccessException;

    // related protein attribution list

    DBLinkDTO addProteinRelatedEntity(RelatedEntityDTO relatedEntityDTO) throws TermNotFoundException;

    DBLinkDTO addProteinAttribution(DBLinkDTO dbLinkDTO);

    DBLinkDTO removeProteinRelatedEntity(DBLinkDTO dbLinkDTO);

    DBLinkDTO removeProteinAttribution(DBLinkDTO dbLinkDTO);

}

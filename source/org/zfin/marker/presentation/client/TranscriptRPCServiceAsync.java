package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.marker.presentation.dto.*;

import java.util.List;

/**
 */
public interface TranscriptRPCServiceAsync {


    // transcript methods
    void changeTranscriptHeaders(TranscriptDTO transcriptDO, AsyncCallback<TranscriptDTO> async);

    void getTranscriptForZdbID(String zdbID, AsyncCallback<TranscriptDTO> async);

    void getTranscriptTypeForZdbID(String zdbID, AsyncCallback<String> async);

    // sequence method
    void getProteinSequenceForAccessionAndRefDB(String accession, String refDBName, AsyncCallback<SequenceDTO> async);

    void addProteinSequence(String transcriptZdbID, String sequence, String pubZdbID, String referenceDatabaseZdbID, AsyncCallback<DBLinkDTO> async);

    void getTranscriptSupportingSequencesReferenceDatabases(AsyncCallback<List<ReferenceDatabaseDTO>> async);

    void getTranscriptAddableNucleotideSequenceReferenceDatabases(TranscriptDTO transcriptDTO, AsyncCallback<List<ReferenceDatabaseDTO>> async);

    void getTranscriptEditAddProteinSequenceReferenceDatabases(AsyncCallback<List<ReferenceDatabaseDTO>> async);

    void getGeneEditAddableStemLoopNucleotideSequenceReferenceDatabases(AsyncCallback<List<ReferenceDatabaseDTO>> async);

    void getGeneEditAddProteinSequenceReferenceDatabases(AsyncCallback<List<ReferenceDatabaseDTO>> async);

    void getTranscriptTypes(AsyncCallback<List<String>> async);

    void getTranscriptStatuses(AsyncCallback<List<String>> async);

    // rna transcript sequence method
    void addNucleotideSequenceToTranscript(TranscriptDTO transcriptDTO, SequenceDTO sequenceDTO, ReferenceDatabaseDTO referenceDatabaseDTO, AsyncCallback<SequenceDTO> async);

    //    public DBLinkDTO addProteinRelatedEntity(String transcriptZdbID,String proteinName,String pub ) throws TermNotFoundException;
    void addProteinRelatedEntity(RelatedEntityDTO relatedEntityDTO, AsyncCallback<DBLinkDTO> async);

    void addProteinAttribution(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    void removeProteinRelatedEntity(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    void removeProteinAttribution(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);
}

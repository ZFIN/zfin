package org.zfin.sequence;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptStatus;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.service.TranscriptService;

import java.util.Set;

import static org.junit.Assert.*;


public class TranscriptServiceTest extends AbstractDatabaseTest {

    private final static Logger logger = Logger.getLogger(SequenceRepositoryTest.class) ;

    @Test
    public void relatedTranscriptDisplayTest() {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        Marker gene = markerRepository.getMarkerByAbbreviation("pax6a");

        Set<RelatedMarker> relatedTranscripts = TranscriptService.getRelatedTranscripts(gene);
        RelatedMarker relatedTranscript = relatedTranscripts.iterator().next();
        Transcript transcript = TranscriptService.convertMarkerToTranscript(relatedTranscript.getMarker());

        assertNotNull("pax6a is a valid gene abbrev", gene);
        assertNotNull("pax6a relatedTranscripts object is not null", relatedTranscripts);
        assertTrue("pax6a relatedTranscripts object is not empty", relatedTranscripts.size() > 0);
        assertNotNull("A transcript of pax6a can be converted to Transcript", transcript );
        assertNotNull("A pax6a transcript has a Transcript.transcriptType", transcript.getTranscriptType());
        
        Set<RelatedMarker> relatedGenes = TranscriptService.getRelatedGenes(transcript);
        Marker relatedGene = ((RelatedMarker)relatedGenes.iterator().next()).getMarker();
        assertNotNull("transcript has at least one gene", relatedGene);
        assertTrue("gene related to transcript is genedom typegroup",
                relatedGene.isInTypeGroup(Marker.TypeGroup.GENEDOM));

        Set<RelatedMarker> siblingRelatedTranscripts = TranscriptService.getRelatedTranscriptsForTranscript(transcript);
        Marker siblingTranscriptAsMarker = ((RelatedMarker)siblingRelatedTranscripts.iterator().next()).getMarker();
        Transcript siblingTranscript = TranscriptService.convertMarkerToTranscript(siblingTranscriptAsMarker);
        assertNotNull("sibling transcript exists", siblingTranscript);
        assertTrue("sibling transcript has typegroup TRANSCRIPT", siblingTranscript.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT));
        assertNotNull("sibling transcript is has a Transcript.transcriptType", siblingTranscript.getTranscriptType());

    }



    @Test
    public void TranscriptStatusCompareTest() {
        TranscriptStatus A = RepositoryFactory.getMarkerRepository().getTranscriptStatusForName("ambiguous orf");
        TranscriptStatus alsoA = RepositoryFactory.getMarkerRepository().getTranscriptStatusForName("ambiguous orf");
        TranscriptStatus B = RepositoryFactory.getMarkerRepository().getTranscriptStatusForName("predicted");
        TranscriptStatus C = RepositoryFactory.getMarkerRepository().getTranscriptStatusForName(null);
        TranscriptStatus alsoC = RepositoryFactory.getMarkerRepository().getTranscriptStatusForName(null);

        assertTrue("Two equivalent TranscriptStatus records are equal", TranscriptStatus.equals(A,alsoA));
        assertTrue("Two null-typed TranscriptStatus records are equal", TranscriptStatus.equals(C,alsoC));
        assertFalse("Two unequal TranscriptStatus records are unequal", TranscriptStatus.equals(A,B));
        assertTrue("TranscriptStatus.equals returns equal for actual nulls", TranscriptStatus.equals(null,null));
        assertFalse("One real TranscriptStatus and one null are not equal", TranscriptStatus.equals(B,null));
        assertFalse("One real TranscriptStatus and one null-typed are not equal", TranscriptStatus.equals(A,B));

    }

}

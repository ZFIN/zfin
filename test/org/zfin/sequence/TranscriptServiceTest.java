package org.zfin.sequence;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptStatus;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.repository.HibernateSequenceRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;

import java.util.Set;
import static org.junit.Assert.*;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;


public class TranscriptServiceTest {

    private final static Logger logger = Logger.getLogger(SequenceRepositoryTest.class) ;

    private static SequenceRepository repository ;

    static{
        if(repository==null){
            repository = new HibernateSequenceRepository() ;
        }

        SessionFactory sessionFactory=HibernateUtil.getSessionFactory();

        if(sessionFactory == null){
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration() ) ;
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    @After
    public void closeSession(){
        HibernateUtil.closeSession();
    }



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

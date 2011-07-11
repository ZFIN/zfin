package org.zfin.marker.repository;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptType;
import org.zfin.marker.presentation.TranscriptAddBean;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.TranscriptDBLink;
import org.zfin.sequence.service.TranscriptService;

import java.util.List;

import static org.junit.Assert.*;

public class TranscriptRepositoryTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(TranscriptRepositoryTest.class) ;

   @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }

    @Test
    public void testDTOReferenceDatabaseMapping() {
        List<ReferenceDatabase> referenceDatabases =  RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                DisplayGroup.GroupName.ADDABLE_NUCLEOTIDE_SEQUENCE) ;
        List<ReferenceDatabaseDTO> referenceDatabaseDTOs = DTOConversionService.convertToReferenceDatabaseDTOs(referenceDatabases) ;

        ReferenceDatabase referenceDatabase ;
        ReferenceDatabaseDTO referenceDatabaseDTO ;

        for(int i = 0 ; i < referenceDatabases.size() ; i++){
            referenceDatabase = referenceDatabases.get(i)  ;
            referenceDatabaseDTO = referenceDatabaseDTOs.get(i)  ;
            assertEquals(referenceDatabase.getForeignDB().getDbName().toString(),referenceDatabaseDTO.getName());
            assertEquals(referenceDatabase.getForeignDBDataType().getDataType().toString(),referenceDatabaseDTO.getType());
            assertEquals(referenceDatabase.getForeignDBDataType().getSuperType().toString(),referenceDatabaseDTO.getSuperType());
        }
    }

    @Test
    public void testTranscript(){
        Transaction tx = null;
        Session session = HibernateUtil.currentSession() ;
        try {
            tx = session.beginTransaction();

            TranscriptAddBean transcriptAddBean = new TranscriptAddBean() ;
            transcriptAddBean.setName("testtranscript");
            transcriptAddBean.setChosenType(TranscriptType.Type.ABERRANT_PROCESSED_TRANSCRIPT.toString());
            transcriptAddBean.setOwnerZdbID("ZDB-PERS-960805-676");
            Transcript newTranscript  = TranscriptService.createTranscript(transcriptAddBean) ;
            session.flush();
            assertNotNull(newTranscript);
            String zdbID = newTranscript.getZdbID() ;
            logger.info("zdbid of newly created transcript: "+ zdbID);

            Transcript transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(zdbID) ;
            assertNotNull(transcript);
//            logger.info(clone.toString());
            assertNotSame("These need to be different for the next test to work",TranscriptType.Type.TRANSPOSABLE_ELEMENT.toString(), transcript.getTranscriptType());
            transcript.setTranscriptType(RepositoryFactory.getMarkerRepository().getTranscriptTypeForName(TranscriptType.Type.TRANSPOSABLE_ELEMENT.toString()));
            // NOTE: must be CDNA or EST to set as a non-null problem type
            session.update(transcript);
            session.flush();
            Transcript transcript2 = (Transcript) session.createQuery("from Transcript c where c.zdbID = '"+zdbID+"'").uniqueResult() ;
            assertEquals(transcript2.getTranscriptType().getType(), TranscriptType.Type.TRANSPOSABLE_ELEMENT);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            tx.rollback();
        }
    }

    @Test
    public void getTranscriptDBLinks(){
        Transcript t = RepositoryFactory.getMarkerRepository().getTranscriptByName("pax6a-001");
        assertNotNull(t);
        List<TranscriptDBLink> dblinks = RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForMarkerAndDisplayGroup(t, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
        assertEquals(1,dblinks.size());

    }

}

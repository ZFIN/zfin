package org.zfin.datatransfer;

import org.apache.log4j.Logger;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.WebHostWublastBlastService;
import org.zfin.sequence.blast.MountedWublastBlastService;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.repository.SequenceRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class PullTaar16 {

    RichSequenceIterator iterator ;

    private Logger logger = Logger.getLogger(PullTaar16.class) ;
    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private ReferenceDatabase referenceDatabase ;

    public static String[] getHibernateConfiguration() {
        return new String[]{
				"filters.hbm.xml",
                "antibody.hbm.xml",
                "anatomy.hbm.xml",
                "blast.hbm.xml",
                "expression.hbm.xml",
                "general.hbm.xml",
                "infrastructure.hbm.xml",
                "mapping.hbm.xml",
                "marker.hbm.xml",
                "mutant.hbm.xml",
                "orthology.hbm.xml",
                "people.hbm.xml",
                "publication.hbm.xml",
                "reno.hbm.xml",
                "sequence.hbm.xml",
        };
    }

    static{
        SessionFactory sessionFactory=HibernateUtil.getSessionFactory();

        if(sessionFactory == null){
            new HibernateSessionCreator(false, getHibernateConfiguration() ) ;
        }

        ZfinProperties.init(".","zfin-properties-pullTaar16.xml");
    }


    boolean geneDBLinksHaveLength(Marker gene){
        List<MarkerDBLink> dbLinks = sequenceRepository.getDBLinksForMarker(gene) ;
        for(DBLink dbLink : dbLinks){
            if(dbLink.getReferenceDatabase().getForeignDBDataType().getDataType()== ForeignDBDataType.DataType.POLYPEPTIDE){
                if(dbLink.getLength()!=null && dbLink.getLength()>0){
                    return true ;
                }
            }
        }
        return false ;
    }

    void retrieveSequences(){

        List<Sequence> unaddedSequences = new ArrayList<Sequence>() ;
        List<Marker> genes = new ArrayList<Marker>() ;

        try{
            HibernateUtil.createTransaction();
            BufferedReader br = new BufferedReader(new FileReader("taar16_fb2595.fa"));
            iterator = RichSequence.IOTools.readFasta(br,  RichSequence.IOTools.getProteinParser(), new SimpleNamespace("fasta-in") ) ;
            while(iterator.hasNext()){
                RichSequence richSequence = iterator.nextRichSequence() ;
                String geneZdbID = richSequence.getAccession() ;
                Marker gene = markerRepository.getMarkerByID(geneZdbID) ;
		logger.debug("got here 1");

                // if we don't have it then we need to add it
                if(false ==geneDBLinksHaveLength(gene)){
                    genes.add(gene) ;

		    logger.debug("got here 2");
                    logger.debug("Inserting sequence for gene: " + gene.getZdbID() + " " + gene.getAbbreviation());
                    MountedWublastBlastService.getInstance().addProteinToMarker(gene,richSequence.getInternalSymbolList().seqString(),null,referenceDatabase) ;
		    logger.debug("got here 3");
		}
                // otherwise we report it
                else{
                    logger.debug("Not inserted: " + gene.getZdbID() + " " + gene.getAbbreviation());
                    Sequence sequence = new Sequence() ;
                    sequence.setData(richSequence.getInternalSymbolList().seqString());
                    sequence.setDefLine(new BioJavaDefline(richSequence));
                    unaddedSequences.add(sequence) ;
                }
            }

            logger.info("Sequences added for genes:");
            for(Marker gene : genes){
                logger.info(gene.getZdbID() + " " + gene.getAbbreviation());
            }

            logger.info("Sequences not added:");
            for(Sequence sequence : unaddedSequences){
                logger.info("\n"+sequence.getFormattedData());
            }
            HibernateUtil.flushAndCommitCurrentSession();
        }catch(Exception e){
            logger.error(e);
            HibernateUtil.currentSession().getTransaction().rollback();
        }
    }

    void initDB(){
        referenceDatabase = sequenceRepository.getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName.PUBPROT,
                ForeignDBDataType.DataType.POLYPEPTIDE) ;
    }

    public void run() {
        initDB() ;
        retrieveSequences();
    }

    public static void main(String args[]){
        PullTaar16 pullTaar16 = new PullTaar16();
        pullTaar16.run() ;
    }
}

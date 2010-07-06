package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.db.HashSequenceDB;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 */
public class BlastDownloadService {

    private static final Logger logger = Logger.getLogger(BlastDownloadService.class) ;

    public static String getMorpholinoDownload(){

        // generates fasta file of the form:
        // > (morpholino zdbID) (morpholino name)
        // sequence
        List<MorpholinoSequence> morpholinoList = RepositoryFactory.getMutantRepository().getMorpholinosWithMarkerRelationships();

        // method A
        // this is a slower method
//        return getFastaStringFromStringBuilder(morpholinoList) ;


        // method B, if there are no weird defline dependencies
        return getFastaStringFromBioJava(morpholinoList) ;

    }

    private static String getFastaStringFromBioJava(List<MorpholinoSequence> morpholinoList) {
        SequenceDB sequenceDB = new HashSequenceDB() ;
        for(MorpholinoSequence morpholino: morpholinoList){
            String header = morpholino.getZdbID()+ " "+morpholino.getName() ;
            try {
                Sequence mrphSeq = DNATools.createDNASequence(morpholino.getSequence(),header) ;
                sequenceDB.addSequence(mrphSeq);
            } catch (BioException e) {
                logger.error("Failed to add sequence for morpholino: "+ morpholino,e);
            }
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
        try {
            RichSequence.IOTools.writeFasta(byteArrayOutputStream,sequenceDB.sequenceIterator(),null);
            return byteArrayOutputStream.toString();
        } catch (IOException e) {
            logger.error("Failed to write sequence to file",e);
            return null ;
        }
    }

    private static String getFastaStringFromStringBuilder(List<MorpholinoSequence> morpholinoList) {
        StringBuilder stringBuilder = new StringBuilder();
        for(MorpholinoSequence morpholino: morpholinoList){
            String header = ">"+morpholino.getZdbID()+ " "+morpholino.getName() ;
            stringBuilder.append(header).append("\n") ;
            stringBuilder.append(morpholino.getSequence()).append("\n") ;
        }

        return stringBuilder.toString();
    }

    private static String buildStringFromList(Collection<String> list){
        StringBuilder builder = new StringBuilder() ;
        for(String dblink : list){
            builder.append(dblink).append("\n") ;
        }
        return builder.toString();
    }


    public static String getGenbankAllDownload() {
        return buildStringFromList(RepositoryFactory.getSequenceRepository().getGenbankSequenceDBLinks()) ;
    }

    public static String getGenbankCdnaDownload() {
        return buildStringFromList(RepositoryFactory.getSequenceRepository().getGenbankCdnaDBLinks()) ;
    }

    public static String getGenbankXpatCdnaDownload() {
        Set<String> accessions = RepositoryFactory.getSequenceRepository().getGenbankXpatCdnaDBLinks() ;
        logger.debug("accessions found: "+ accessions.size());
        return buildStringFromList(accessions) ;
    }

    public static String getGenomicRefseqDownload() {
        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getSequenceRepository()
                .getSequenceReferenceDatabases(ForeignDB.AvailableName.REFSEQ
                        ,ForeignDBDataType.DataType.GENOMIC
                ) ;
        Set<String> accessions = RepositoryFactory.getSequenceRepository()
                .getAccessions(referenceDatabases.toArray(new ReferenceDatabase[referenceDatabases.size()]));
        logger.debug("accessions found: "+ accessions.size());
        return buildStringFromList(accessions) ;
    }

    public static String getGenomicGenbankDownload() {
        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getSequenceRepository()
                .getSequenceReferenceDatabases(ForeignDB.AvailableName.GENBANK
                        ,ForeignDBDataType.DataType.GENOMIC
                ) ;
        Set<String> accessions = RepositoryFactory.getSequenceRepository()
                .getAccessions(referenceDatabases.toArray(new ReferenceDatabase[referenceDatabases.size()]));
        logger.debug("accessions found: "+ accessions.size());
        return buildStringFromList(accessions) ;
    }
}

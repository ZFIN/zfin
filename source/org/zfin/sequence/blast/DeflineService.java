package org.zfin.sequence.blast;

import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.sequence.*;

/**
 * This service is responsible for handling deflines.
 */
public abstract class DeflineService {

    // maybe should go somewhere else
    public static Defline createDeflineForDBLink(DBLink dbLink){
        if(dbLink.getDataZdbID().startsWith("ZDB-TSCRIPT-")){
            return new TranscriptDefline((TranscriptDBLink) dbLink) ;
        }
        else{
            return new MarkerDefline((MarkerDBLink) dbLink) ;
        }
    }


    /**
     * Description:
     * the blast database it is found in is important <limitable?>
     *  a unique  identifier <accession> is important  <searchable>
     *  the zfin object the sequence is associated with  <searchable> is good
     *  the origin of the sequence would be *awesome* <attribution>
     *  the <chromosome> if possible is nice
     *  the sequence length is good
     *
     * Proposal: <accession*> <data id*> <blastdb> <other data info [attribution chromosome]> <length>
     * Example:  <ZFINNUCL000000123*>  <ZDB-TSCRIPT-12345-1*> <unpublishedRNA> <ddx56-001 mRNA non-coding ZDB-PUB-1234-1 lg3> <154 bp>
     * Note: * is searchable.
     *
     * @param dbLink  DBLink to generate for
     * @return Returns generated defline
     */
    public static String generateInternalNucleotideDefline(MarkerDBLink dbLink){

        Marker marker = dbLink.getMarker() ;

        String defLine = "";
        defLine += ">lcl";
        defLine += "|";
        defLine += dbLink.getAccessionNumber();
        defLine += "|";
        defLine += marker.getZdbID() ;
        if(dbLink.getReferenceDatabase()!=null && dbLink.getReferenceDatabase().getPrimaryBlastDatabase()!=null){
            defLine += " ";
            defLine += dbLink.getReferenceDatabase().getPrimaryBlastDatabase().getAbbrev();
        }
        defLine += " ";
        defLine += marker.getAbbreviation() ;
        defLine += " ";
        defLine += dbLink.getLength() ;
        defLine += " bp" ;

        return defLine ;
    }

    /**
     * Description:
     * the blast database it is found in is important <limitable?>
     *  a unique  identifier <accession> is important  <searchable>
     *  the zfin object the sequence is associated with  <searchable> is good
     *  the origin of the sequence would be *awesome* <attribution>
     *  the <chromosome> if possible is nice
     *  the sequence length is good
     *
     * Proposal: <accession*> <data id*> <blastdb> <other data info [attribution chromosome]> <length>
     * Example:  <ZFINNUCL000000123*>  <ZDB-TSCRIPT-12345-1*> <unpublishedRNA> <ddx56-001 mRNA non-coding ZDB-PUB-1234-1 lg3> <154 bp>
     * Note: * is searchable.
     *
     * @param dbLink  DBLink to generate for
     * @return Returns generated defline
     */
    public static String generateInternalNucleotideDefline(TranscriptDBLink dbLink){

        Transcript transcript = dbLink.getTranscript() ;

        String defLine = "";
        defLine += ">lcl";
        defLine += "|";
        defLine += dbLink.getAccessionNumber();
        defLine += "|";
        defLine += transcript.getZdbID() ;
        defLine += " ";
        defLine += dbLink.getReferenceDatabase().getPrimaryBlastDatabase().getAbbrev();
        defLine += " ";
        defLine += transcript.getAbbreviation() ;
        defLine += " ";
        defLine += transcript.getTranscriptType().getDisplay() ;
        defLine += (transcript.getStatus()!=null ? " "+transcript.getStatus().getDisplay() : "") ;
        defLine += " ";
        defLine += dbLink.getLength() ;
        defLine += " bp" ;

        return defLine ;
    }


    /**
     * Description:
     * the blast database it is found in is important <limitable?>
     *  a unique  identifier <accession> is important  <searchable>
     *  the zfin object the sequence is associated with  <searchable> is good
     *  the origin of the sequence would be *awesome* <attribution>
     *  the <chromosome> if possible is nice
     *  the sequence length is good
     *
     * Proposal: <accession*> <data id*> <blastdb> <other data info [attribution chromosome]> <length>
     * Example:  <ZFINPROT000000123*>  <ZDB-TSCRIPT-12345-1*> <unpublishedRNA> <ddx56-001 mRNA non-coding ZDB-PUB-1234-1 lg3> <154 bp>
     * Note: * is searchable.
     *
     * @param dbLink DBLink to generate defline for
     * @return The generated defline
     */
    public static String generateInternalProteinDefline(TranscriptDBLink dbLink){

        Transcript transcript = dbLink.getTranscript() ;

        String defLine = "";
        defLine += ">lcl";
        defLine += "|";
        defLine += dbLink.getAccessionNumber();
        defLine += "|";
        defLine += transcript.getZdbID() ;
        defLine += " ";
        defLine += dbLink.getReferenceDatabase().getPrimaryBlastDatabase().getAbbrev();
        defLine += " ";
        defLine += transcript.getAbbreviation() ;
        defLine += " ";
        defLine += transcript.getTranscriptType().getDisplay() ;
        defLine += (transcript.getStatus()!=null ? " "+transcript.getStatus().getDisplay() : "") ;
        defLine += " ";
        defLine += dbLink.getLength() ;
        defLine += " aa" ;

        return defLine ;
    }


    public static String generateInternalProteinDefline(MarkerDBLink dbLink){

        Marker marker = dbLink.getMarker() ;

        String defLine = "";
        defLine += ">lcl";
        defLine += "|";
        defLine += dbLink.getAccessionNumber();
        defLine += "|";
        defLine += marker.getZdbID() ;
        defLine += " ";
        defLine += dbLink.getReferenceDatabase().getPrimaryBlastDatabase().getAbbrev();
        defLine += " ";
        defLine += marker.getAbbreviation() ;
        defLine += " ";
        defLine += marker.getMarkerType().getDisplayName() ;
        defLine += " ";
        defLine += marker.getPublicComments() ;
        defLine += " ";
        if(marker.getLG().size()>0){
            for(String lg : marker.getLG()){
                defLine += lg ;
            }
        }
        defLine += " ";
        defLine += dbLink.getLength() ;
        defLine += " aa" ;

        return defLine ;
    }
}

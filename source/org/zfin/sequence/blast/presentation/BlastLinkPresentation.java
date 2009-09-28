package org.zfin.sequence.blast.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.sequence.Accession;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.blast.results.view.HitViewBean;

/**
 * Presentation Class to create output from a HitViewBean object.
 */
public class BlastLinkPresentation extends EntityPresentation{

    public final static String LINK_PREFIX = "<a target=_blank style=\"font-size: small;\" href=\"" ;

    public static String getLink(HitViewBean hitViewBean) {
        if(hitViewBean.getHitDBLink()!=null){
            DBLink dbLink =  hitViewBean.getHitDBLink() ;
            StringBuilder sb = new StringBuilder("") ;
            sb.append(dbLink.getAccessionNumber());
            sb.append("&nbsp;");
            sb.append( LINK_PREFIX );
            sb.append(dbLink.getReferenceDatabase().getForeignDB().getDbUrlPrefix() );
            sb.append(dbLink.getAccessionNumber());
            if( dbLink.getReferenceDatabase().getForeignDB().getDbUrlSuffix() != null){
                sb.append(dbLink.getReferenceDatabase().getForeignDB().getDbUrlSuffix() );
            }
            sb.append("\"") ;
            sb.append("title=\"") ;
            sb.append(dbLink.getAccessionNumber()).append(" at ") ;
            String foreignDBName ;
            if(dbLink.getReferenceDatabase().getForeignDB().getDbName()== ForeignDB.AvailableName.VEGA
                    ||
                    dbLink.getReferenceDatabase().getForeignDB().getDbName()== ForeignDB.AvailableName.VEGA_TRANS
                    ){
                foreignDBName = "Vega" ;
            }
            else
            if( dbLink.getReferenceDatabase().getForeignDB().getDbName() == ForeignDB.AvailableName.ZFIN_PROT
                    ||
                    dbLink.getReferenceDatabase().getForeignDB().getDbName() == ForeignDB.AvailableName.PUBPROT
                    ||
                    dbLink.getReferenceDatabase().getForeignDB().getDbName() == ForeignDB.AvailableName.PUBRNA
                    ){
                foreignDBName = "ZFIN" ;
            }
            else{
                foreignDBName = dbLink.getReferenceDatabase().getForeignDB().getDbName().toString() ;
            }
            sb.append(foreignDBName) ;
            sb.append("\"") ;
            sb.append(">" );
            sb.append("["+foreignDBName+"]");
            sb.append( "</a>" );
            return sb.toString();
        }
        else
        if(hitViewBean.getZfinAccession()!=null){
            Accession accession = hitViewBean.getZfinAccession() ;
            StringBuilder sb = new StringBuilder("") ;
            sb.append(accession.getNumber());
            sb.append("&nbsp;");
            sb.append( LINK_PREFIX );
            sb.append(accession.getReferenceDatabase().getForeignDB().getDbUrlPrefix() );
            sb.append(accession.getNumber());
            if( accession.getReferenceDatabase().getForeignDB().getDbUrlSuffix() != null){
                sb.append(accession.getReferenceDatabase().getForeignDB().getDbUrlSuffix() );
            }
            sb.append("\"") ;
            sb.append("title=\"") ;
            sb.append(accession.getNumber() + " at " + accession.getReferenceDatabase().getForeignDB().getDbName()) ;
            sb.append("\"") ;
            sb.append(">" );
            sb.append("["+accession.getReferenceDatabase().getForeignDB().getDbName()+"]") ;
            sb.append( "</a>" );
            return sb.toString();
        }
        else
        if(hitViewBean.getHitMarker()!=null
                &&
                hitViewBean.getHitMarker().isInTypeGroup(Marker.TypeGroup.MRPHLNO)
                ){
            Marker m = hitViewBean.getHitMarker() ;
            return MarkerPresentation.getLink(m) ;
        }
        // we don't have a proper database for these ones, unfortuantely . . . just accession headers
        else
        if(hitViewBean.getAccessionNumber().startsWith("ENSDART")) {
            return createLink(hitViewBean.getAccessionNumber(),
                    "http://www.ensembl.org/Danio_rerio/transview?transcript=",
                    "Ensembl") ;
        }
        else
        if(hitViewBean.getAccessionNumber().startsWith("TC")) {
            return createLink(hitViewBean.getAccessionNumber(),
                    "http://compbio.dfci.harvard.edu/tgi/cgi-bin/tgi/tc_report.pl?species=zebrafish&tc="
                    ,"DFCI"  ) ;
        }
        // handle refseq protein
        else
        if(hitViewBean.getAccessionNumber().startsWith("XP_")
                ||
                hitViewBean.getAccessionNumber().startsWith("NP_")
                ) {
            return createLink(hitViewBean.getAccessionNumber(),
                    "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&val=",
                    "GenBank" ) ;
        }
        // here we just have the database precursor
        // handle uniprot protein
        else
        if(hitViewBean.getId().startsWith("sp") ) {
            return createLink(hitViewBean.getAccessionNumber(),
                    "http://www.uniprot.org/uniprot/",
                    "Uniprot" ) ;
        }
        // handle trace
        else
        if(hitViewBean.getId().startsWith("gnl|ti") ) {
            return createLink(hitViewBean.getAccessionNumber(),
                    "http://www.ncbi.nlm.nih.gov/Traces/trace.cgi?cmd=retrieve&s=search&m=obtain&retrieve\n" +
                            ".x=0&retrieve.y=0&val=",
                    "GenBank" ) ;
        }
        // handle genbank nucleotide
        else
        if( hitViewBean.getId().startsWith("gb")
                ||
                hitViewBean.getId().startsWith("ref")
                ||
                hitViewBean.getId().startsWith("emb")
                ) {
            return createLink(hitViewBean.getAccessionNumber(),
                    "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Search&db=Nucleotide&doptcmdl=GenBank&term=",
                    "GenBank" ) ;
        }
        // otherwise just display
        else{
            return hitViewBean.getAccessionNumber() ;
        }
    }

    private static String createLink(String accession,String url,String location){
        StringBuilder sb = new StringBuilder("") ;
        sb.append(accession);
        sb.append("&nbsp;");
        sb.append( LINK_PREFIX );
        sb.append(url) ;
        sb.append(accession);
        sb.append("\"") ;
        sb.append("title=\"") ;
        sb.append(accession + " at " + location) ;
        sb.append("\"") ;
        sb.append(">" );
        sb.append("[" + location+"]") ;
        sb.append( "</a>" );
        return sb.toString();
    }

}

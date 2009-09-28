package org.zfin.sequence.presentation;

import org.zfin.sequence.Accession ;


/**
 *  Class AccessionPresentation.
 */

// don't need the EntityPresentation at this point
//public class AccessionPresentation extends EntityPresentation {
public class AccessionPresentation {

    /**
     * Generates an Accession link 
     *
     * @return html for marker link
     * @param accession Accession
     */
    public static String getLink(Accession accession) {
        StringBuilder sb = new StringBuilder("") ;
        if(accession!=null){
            if(accession.getReferenceDatabase()!=null){
                sb.append( "<a href=\"" );
                sb.append(accession.getReferenceDatabase().getForeignDB().getDbUrlPrefix() );
                sb.append(accession.getNumber());
                if( accession.getReferenceDatabase().getForeignDB().getDbUrlSuffix() != null){
                    sb.append(accession.getReferenceDatabase().getForeignDB().getDbUrlSuffix() );
                }
                sb.append( "\"/>" );
                sb.append(accession.getNumber());
                sb.append( "</a>" );
            }else{
                sb.append(accession.getNumber());
            }
        }
        return sb.toString() ;
    }


} 



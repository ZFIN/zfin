package org.zfin.sequence.presentation;

import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.sequence.Accession;

import java.io.Serializable;


/**
 * Class AccessionPresentation.
 */

// don't need the EntityPresentation at this point
//public class AccessionPresentation extends EntityPresentation {
public class AccessionPresentation implements Serializable {
    private String accessionNumber;
    private String url;

    /**
     * Generates an Accession link
     *
     * @param accession Accession
     * @return html for marker link
     */
    public static String getLink(Accession accession) {
        StringBuilder sb = new StringBuilder("");
        if (accession != null) {
            if (accession.getReferenceDatabase() != null) {
                sb.append("<a href=\"");
                sb.append(accession.getReferenceDatabase().getForeignDB().getDbUrlPrefix());
                sb.append(accession.getNumber());
                if (accession.getReferenceDatabase().getForeignDB().getDbUrlSuffix() != null) {
                    sb.append(accession.getReferenceDatabase().getForeignDB().getDbUrlSuffix());
                }
                sb.append("\"/>");
                sb.append(accession.getNumber());
                sb.append("</a>");
            } else {
                sb.append(accession.getNumber());
            }
        }
        return sb.toString();
    }


    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int hashCode() {
        return accessionNumber.hashCode();
    }
}



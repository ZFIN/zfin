package org.zfin.framework.presentation.tags;

import org.apache.commons.lang.StringUtils;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.blast.Database;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Creates a blast URL
 */
public class BlastAccessionURL extends TagSupport {

    private DBLink dbLink;
    private Database blastDB;

//            <c:choose>
//    <c:when test="${empty blastDB.location}">
//    <c:choose>
//    <c:when test="${blastDB.type eq 'nucleotide'}">
//    <c:set var="sequenceType" value="nt"/>
//    <c:set var="program" value="blastn"/>
//    </c:when>
//    <c:otherwise>
//    <c:set var="sequenceType" value="pt"/>
//    <c:set var="program" value="blastp"/>
//    </c:otherwise>
//    </c:choose>
//            /action/blast/blast?&program=${program}&sequenceType=${sequenceType}&queryType=SEQUENCE_ID&dataLibraryString=RNASequences&sequenceID=${dbLink.accessionNumber}
//    </c:when>
//    <c:otherwise>
//    ${blastDB.location}${dbLink.accessionNumber}
//    </c:otherwise>
//    </c:choose>

    public int doStartTag() throws JspException {

        StringBuilder linkBuffer = new StringBuilder();

        dbLink = getDbLink();
        blastDB = getBlastDB();

        if (dbLink == null || blastDB == null) {
            return SKIP_BODY;
        }

        try {
            String sequenceType;
            String program;
            String blastDBString;
            if (StringUtils.isEmpty(blastDB.getLocation())) {
                if (blastDB.getType().isNucleotide()) {
                    sequenceType = "nt";
                    program = "blastn";
                    blastDBString = "RNASequences";
                } else {
                    sequenceType = "pt";
                    program = "blastp";
                    blastDBString = "zfin_all_aa";
                }
                linkBuffer.append("/action/blast/blast?");
                linkBuffer.append("program=").append(program);
                linkBuffer.append("&sequenceType=").append(sequenceType);
                linkBuffer.append("&queryType=SEQUENCE_ID&dataLibraryString=" + blastDBString);
                linkBuffer.append("&sequenceID=").append(dbLink.getAccessionNumber());
            } else {
                linkBuffer.append(blastDB.getLocation()).append(dbLink.getAccessionNumber());
            }

            pageContext.getOut().print(linkBuffer.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return SKIP_BODY;

    }

    public int doEndTag() throws JspException {
        return Tag.EVAL_PAGE;
    }

    public DBLink getDbLink() {
        return dbLink;
    }

    public void setDbLink(DBLink dbLink) {
        this.dbLink = dbLink;
    }

    public Database getBlastDB() {
        return blastDB;
    }

    public void setBlastDB(Database blastDB) {
        this.blastDB = blastDB;
    }
}

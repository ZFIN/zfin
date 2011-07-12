package org.zfin.framework.presentation.tags;

import org.zfin.sequence.Sequence;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DeflineTag extends TagSupport {

    private Object defLine;

    public int doStartTag() throws JspException {

        Object o = getDefLine();
        StringBuilder linkBuffer = new StringBuilder();

        String deflineString = null;

        if (o == null) {
            return SKIP_BODY;
        } else if (o instanceof Sequence) {

        } else if (o instanceof String) {
            deflineString = (String) o;
        } else {
            return SKIP_BODY;
        }

        deflineString = replaceMarkerWithLink(deflineString);
        deflineString = replaceEndargsWithLink(deflineString);
        linkBuffer.append(deflineString);

        try {
            pageContext.getOut().print(linkBuffer.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return SKIP_BODY;

    }

    public String replaceEndargsWithLink(String defline) {
        String patternString = "(gene:)(ENSDARG*[^\\s]+)";
        String replacementString = "<a href=http://www.ensembl.org/Danio_rerio/geneview?gene=$2>$1$2</a>";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(defline);
        String output = matcher.replaceFirst(replacementString);
        return output;
    }

    public String replaceMarkerWithLink(String defline) {
        String patternString = "(ZDB-[^\\s]+)";
        String replacementString = "<a href=/action/marker/view/$1>$1</a>";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(defline);
        String output = matcher.replaceFirst(replacementString);
        return output;
    }

    public int doEndTag() throws JspException {
        return Tag.EVAL_PAGE;
    }

    public Object getDefLine() {
        return defLine;
    }

    public void setDefLine(Object defLine) {
        this.defLine = defLine;
    }
}
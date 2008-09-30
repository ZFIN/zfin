package org.zfin.framework.presentation.tags;

import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Tag that creates a abbreviation html tag for an object of type provided.
 */
public class ToggleTextLengthTag extends TagSupport {

    private String text;
    private int shortLength;
    private boolean shortVersion;
    private String escape;

    public int doStartTag() throws JspException {

        try {
            // Just to make sure the rest does not fail.
            if (text == null){
                text = "";
            }
            // treat short version just like long version. no difference.
            if (text.length() < shortLength) {
                shortVersion = false;
            }
            String outputText = null;
            if (shortVersion) {
                if (escape == null || escape.equals("html")) {
                    outputText = text.substring(0, shortLength);
                    int indexOf = outputText.lastIndexOf(" ");
                    // if there is a white space found truncate at the blank character.
                    // otherwise leave the text untruncated.
                    if (indexOf > -1)
                        outputText = outputText.substring(0, indexOf);

                }
            } else {
                outputText = text;
            }
            outputText = StringEscapeUtils.escapeHtml(outputText);
            outputText = escapeNewlines(outputText);
            pageContext.getOut().print(outputText);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return SKIP_BODY;
    }

    private String escapeNewlines(String shortText) {
        if (shortText.indexOf("\r\n") > -1)
            shortText = shortText.replaceAll("\r\n", "<br/>");
        if (shortText.indexOf("\n") > -1)
            shortText = shortText.replaceAll("\n", "<br/>");
        return shortText;
    }

    public int doEndTag() throws JspException {
        return Tag.EVAL_PAGE;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getShortLength() {
        return shortLength;
    }

    public void setShortLength(int shortLength) {
        this.shortLength = shortLength;
    }

    public boolean isShortVersion() {
        return shortVersion;
    }

    public void setShortVersion(boolean shortVersion) {
        this.shortVersion = shortVersion;
    }

    public String getEscape() {
        return escape;
    }

    public void setEscape(String escape) {
        this.escape = escape;
    }
}
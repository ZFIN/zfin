package org.zfin.framework.presentation;

import org.zfin.properties.ZfinProperties;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class HyperlinkTag extends TagSupport {

    private String name;
    private String apgFileName;

    public void setName(String name) {
        this.name = name;
    }

    public void setApgFileName(String apgFileName) {
        this.apgFileName = apgFileName;
    }

    public int doStartTag() throws JspException {
        String serverName = ZfinProperties.getServer();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<a href='");
            sb.append(serverName);
            if (apgFileName != null)
                sb.append(ZfinProperties.getWebDriver());
            sb.append("?");
            sb.append(apgFileName + "'>");
            sb.append(name);
            sb.append("</a>");
            pageContext.getOut().print(sb.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return EVAL_PAGE;
    }

}
package org.zfin.framework.presentation.tags;

import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.Authentication;
import org.zfin.people.User;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class UsernameTag extends TagSupport {

    public int doStartTag() throws JspException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object o = authentication.getPrincipal();
        if (o instanceof User) {
            User user = (User) o;
            String name = user.getName();
            try {
                pageContext.getOut().print(name);
            } catch (IOException ioe) {
                throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
            }
        }
        return EVAL_PAGE;
    }

}

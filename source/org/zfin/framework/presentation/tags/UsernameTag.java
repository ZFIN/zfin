package org.zfin.framework.presentation.tags;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zfin.profile.AccountInfo;
import org.zfin.profile.Person;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class UsernameTag extends TagSupport {

    public int doStartTag() throws JspException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object o = authentication.getPrincipal();
        if (o instanceof Person person) {
            AccountInfo accountInfo = person.getAccountInfo();
            if (accountInfo == null)
                return EVAL_PAGE;

            String name = accountInfo.getName();
            try {
                pageContext.getOut().print(name);
            } catch (IOException ioe) {
                throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
            }
        }
        return EVAL_PAGE;
    }

}

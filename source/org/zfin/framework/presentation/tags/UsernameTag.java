package org.zfin.framework.presentation.tags;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.zfin.people.AccountInfo;
import org.zfin.people.Person;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class UsernameTag extends TagSupport {

    public int doStartTag() throws JspException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object o = authentication.getPrincipal();
        if (o instanceof Person) {
            Person person = (Person) o;
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

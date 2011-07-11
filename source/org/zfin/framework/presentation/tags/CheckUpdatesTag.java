package org.zfin.framework.presentation.tags;

import org.zfin.framework.filter.UpdatesCheckFilter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Convenience Tag that checks if the database is marked for updates.
 */
public class CheckUpdatesTag extends TagSupport {

    private boolean locked;

    public int doStartTag() throws JspException {
        boolean disableSystemUpdates = UpdatesCheckFilter.getSystemUpdatesFlag();

        if (locked) {
            if (disableSystemUpdates)
                return Tag.EVAL_BODY_INCLUDE;
            else
                return Tag.SKIP_BODY;
        } else {
            if (disableSystemUpdates)
                return Tag.SKIP_BODY;
            else
                return Tag.EVAL_BODY_INCLUDE;
        }
    }

    public int doEndTag() throws JspException {
        return Tag.EVAL_PAGE;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
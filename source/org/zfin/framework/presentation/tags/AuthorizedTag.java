package org.zfin.framework.presentation.tags;

import org.apache.commons.lang.StringUtils;
import org.zfin.people.UserService;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Check if the secure user has a given role or is the owner of the record.
 * role: role name. If the user has this role the body of the tag will be granted
 * beanName; alias name under which the object is stored (hard-coded to Request scope)
 * className: Fully qualified class name for the business object in question.
 * primaryKey: the attribute of the business object (class name) in question.
 * This key is compared with the ownerID for this object from which we can derive if
 * the secure user is the owner of the record or not.
 * The owner criterium is only checked if the role is not given or is not the required role.
 */
public class AuthorizedTag extends TagSupport {

    private String role;
    private String owner;
    private String entityZdbID;
    private String className;


    public int doStartTag() throws JspException {

        if (StringUtils.isEmpty(role) && StringUtils.isEmpty(owner)) {
            return Tag.SKIP_BODY;
        }

        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // if user has the role include body
        // note that role could be a comma delimited list.s
        if (role != null && UserService.hasRole(role)) {
            return Tag.EVAL_BODY_INCLUDE;
        }
        // if user is owner of the record than display it.
        if (StringUtils.isEmpty(owner))
            return Tag.SKIP_BODY;
        else {
            if (UserService.isOwner(entityZdbID, clazz))
                return Tag.EVAL_BODY_INCLUDE;
        }
        return Tag.SKIP_BODY;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getEntityZdbID() {
        return entityZdbID;
    }

    public void setEntityZdbID(String entityZdbID) {
        this.entityZdbID = entityZdbID;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    
}

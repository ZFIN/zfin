package org.zfin.framework.presentation.tags;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static javax.servlet.jsp.PageContext.*;

/**
 * This class provides a means to retrieve a property value from a
 * bean being found in a pageContext of a JSP page.
 * <p/>
 * Most of this code is taken from TagUtils out of the Struts library.
 */
public class ZfinTagUtils {

    /**
     * The Singleton instance.
     */
    private static final ZfinTagUtils instance = new ZfinTagUtils();

    /**
     * Maps lowercase JSP scope names to their PageContext integer constant
     * values.
     */
    private static final Map<String, Integer> scopes = new HashMap<String, Integer>();

    /**
     * Initialize the scope names map.
     */
    static {
        scopes.put("page", PAGE_SCOPE);
        scopes.put("request", REQUEST_SCOPE);
        scopes.put("session", SESSION_SCOPE);
        scopes.put("application", APPLICATION_SCOPE);
    }

    private static final Logger LOG = Logger.getLogger(ZfinTagUtils.class);

    /**
     * Constructor for TagUtils.
     */
    protected ZfinTagUtils() {
    }

    /**
     * Returns the Singleton instance of TagUtils.
     *
     * @return ZfinTagUtils object
     */
    public static ZfinTagUtils getInstance() {
        return instance;
    }

    /**
     * Locate and return the specified property of the specified bean, from
     * an optionally specified scope, in the specified page context.
     *
     * @param pageContext Page context to be searched
     * @param name        Name of the bean to be retrieved
     * @param property    Name of the property to be retrieved, or
     *                    <code>null</code> to retrieve the bean itself
     * @param scope       Scope to be searched (page, request, session, application)
     *                    or <code>null</code> to use <code>findAttribute()</code> instead
     * @return property of specified JavaBean
     * @throws javax.servlet.jsp.JspException if the specified bean is not found
     */
    public static Object lookup(
            PageContext pageContext,
            String name,
            String property,
            String scope)
            throws JspException {

        // Look up the requested bean, and return if requested
        Object bean = lookup(pageContext, name, scope);
        if (bean == null) {
            throw new JspException("No bean provided");
        }

        if (property == null) {
            return bean;
        }

        // Locate and return the specified property
        try {
            return PropertyUtils.getProperty(bean, property);
        } catch (IllegalAccessException e) {
            LOG.error(e);
            throw new JspException("No Found");
        } catch (IllegalArgumentException e) {
            LOG.error(e);
            throw new JspException("No Found");
        } catch (InvocationTargetException e) {
            LOG.error(e);
            throw new JspException("No Found");
        } catch (NoSuchMethodException e) {
            LOG.error(e);
            throw new JspException("No Found");
        }

    }

    /**
     * Locate and return the specified bean, from an optionally specified
     * scope, in the specified page context.  If no such bean is found,
     * return <code>null</code> instead.  If an exception is thrown, it will
     * have already been saved via a call to <code>saveException()</code>.
     *
     * @param pageContext Page context to be searched
     * @param name        Name of the bean to be retrieved
     * @param scopeName   Scope to be searched (page, request, session, application)
     *                    or <code>null</code> to use <code>findAttribute()</code> instead
     * @return JavaBean in the specified page context
     * @throws JspException if an invalid scope name
     *                      is requested
     */
    public static Object lookup(PageContext pageContext, String name, String scopeName)
            throws JspException {

        if (scopeName == null) {
            return pageContext.findAttribute(name);
        }

        try {
            return pageContext.getAttribute(name, instance.getScope(scopeName));

        } catch (JspException e) {
            LOG.error("No Attribute found with name " + name + " in scope " + scopeName);
            throw e;
        }

    }

    /**
     * Converts the scope name into its corresponding PageContext constant value.
     *
     * @param scopeName Can be "page", "request", "session", or "application" in any
     *                  case.
     * @return The constant representing the scope (ie. PageContext.REQUEST_SCOPE).
     * @throws JspException if the scopeName is not a valid name.
     */
    public int getScope(String scopeName) throws JspException {
        Integer scope = scopes.get(scopeName.toLowerCase());

        if (scope == null) {
            throw new JspException("lookup.scope " + scopeName + " not found");
        }

        return scope;
    }
}

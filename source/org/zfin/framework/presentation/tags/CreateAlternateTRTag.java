package org.zfin.framework.presentation.tags;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.LoopTagStatus;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Create the TR tag for alternating records, e.g.
 * different css styles depending if the index is
 * even or odd or if a grouping shade is wished.
 * <p/>
 * The output is a row tag: <tr class=" ... ">
 * The main loop index creates either
 * even or odd class names
 * The group index creates classes:
 * evengroup or oddgroup
 * while a new group is indicated by the class name addition
 * newgroup.
 * For example the first element in a collection will always result in:
 * <tr class="odd newgroup oddgroup">
 */
public class CreateAlternateTRTag extends TagSupport {

    // Name of the main loop
    private String loopName;
    // name of the attribute on the collection that is used to decide if a new group is being started
    // within the collection loop. This can be a nested object path.
    private String groupByBean;
    // the collection object
    private List groupBeanCollection;
    // class names for the tr-element
    private String trClassNames;

    // this index is used to indicate if a new group is started with a new element
    private int groupIndex;
    private static final Logger LOG = Logger.getLogger(GroupByDisplayTag.class);


    public int doStartTag() throws JspException {

        LoopTagStatus loop = (LoopTagStatus) pageContext.getAttribute(loopName, PageContext.PAGE_SCOPE);
        if (loop == null)
            throw new RuntimeException("No counter named " + loopName + " being found in page context");
        int loopIndex = loop.getCount();
        init(loopIndex);

        boolean isNewGroup = true;
        if (loopIndex > 1 && groupByBean != null) {
            Object previousObject = groupBeanCollection.get(loopIndex - 2);
            Object currentObject = groupBeanCollection.get(loopIndex - 1);
            try {
                Object previousGroupBeanAttribute = PropertyUtils.getProperty(previousObject, groupByBean);
                Object currentGroupBeanAttribute = PropertyUtils.getProperty(currentObject, groupByBean);
                isNewGroup = !previousGroupBeanAttribute.equals(currentGroupBeanAttribute);
                LOG.debug("previousGroupBeanAttribute: " + previousGroupBeanAttribute);
                LOG.debug("currentGroupBeanAttribute:" + currentGroupBeanAttribute);
            } catch (IllegalAccessException e) {
                LOG.error(e);
            } catch (InvocationTargetException e) {
                LOG.error(e);
            } catch (NoSuchMethodException e) {
                LOG.error(e);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<tr class=\"");
        if (trClassNames != null)
            sb.append(trClassNames);

        if (loopIndex % 2 != 0)
            sb.append(" odd ");
        else
            sb.append(" even ");

        //if not grouping by anything, treat every row as a new group
        if (isNewGroup || groupByBean == null) {
            sb.append(" newgroup ");
            groupIndex++;
        }

        if (groupByBean != null) {
            if (groupIndex % 2 != 0)
                sb.append(" oddgroup ");
            else
                sb.append(" evengroup ");
        }
        sb.append("\">");

        try {
            pageContext.getOut().print(sb.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return Tag.EVAL_BODY_INCLUDE;
    }

    private void init(int index) {
        // if start of the loop set group index accordingly
        if (index == 1) {
            groupIndex = 0;
        }
    }

    /**
     * Close the TR tag.
     *
     * @return value indicating if the rest of the page should be evaluated or not.
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().print("</tr>");
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return Tag.EVAL_PAGE;
    }

    /**
     * Release all allocated resources except the groupIndex parameter which is needed
     */
    public void release() {
        super.release();

        loopName = null;
        groupBeanCollection = null;
        groupByBean = null;
        trClassNames = null;
    }


    public String getLoopName() {
        return loopName;
    }

    public void setLoopName(String loopName) {
        this.loopName = loopName;
    }

    public String getGroupByBean() {
        return groupByBean;
    }

    public void setGroupByBean(String groupByBean) {
        this.groupByBean = groupByBean;
    }

    public List getGroupBeanCollection() {
        return groupBeanCollection;
    }

    public void setGroupBeanCollection(List groupBeanCollection) {
        this.groupBeanCollection = groupBeanCollection;
    }

    public String getTrClassNames() {
        return trClassNames;
    }

    public void setTrClassNames(String trClassNames) {
        this.trClassNames = trClassNames;
    }
}

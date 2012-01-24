<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.anatomy.presentation.AnatomySearchBean" required="true" %>
<%@ attribute name="showSearchBox" type="java.lang.Boolean" required="false" %>
<%@ attribute name="linkStageTerms" type="java.lang.Boolean" required="true" %>

<table class="primary-entity-attributes">
    <tr>
        <th width=5%>
            <span class="name-label">Name:</span>
        </th>
        <td>
            <span class="name-value">${formBean.anatomyItem.termName}</span>
            <c:if test="${formBean.anatomyItem.obsolete}"><span style="color:red">(obsolete)</span> </c:if>
        </td>
        <c:if test="${showSearchBox}">
            <td valign="top" align="right" width="5%">
                Search:
            </td>
            <td rowspan="3" valign="top" align="right" width="5%">
                <zfin2:lookup ontology="<%= Ontology.ANATOMY%>"
                              action="<%= LookupComposite.ACTION_ANATOMY_SEARCH %>"
                              wildcard="true" useIdAsTerm="false"/>
            </td>
        </c:if>
    </tr>
    <c:if test="${not empty formBean.anatomyItem.synonyms}">
        <tr>
            <th>
                Synonyms:
            </th>
            <td>
                    ${formBean.formattedSynonymList}
            </td>
        </tr>
    </c:if>

    <c:if test="${formBean.anatomyItem.definition != null  }">
        <tr>
            <th valign="top">
                Definition:
            </th>
            <td>
                    ${formBean.anatomyItem.definition}
            </td>
        </tr>
    </c:if>
    <tr>
        <th>Appears&nbsp;at:</th>
        <td>
            <c:choose>
                <c:when test="${linkStageTerms}">
                    <zfin:link entity="${formBean.anatomyItem.start}" longVersion="true"/>
                </c:when>
                <c:otherwise>
                    <zfin:name entity="${formBean.anatomyItem.start}" longVersion="true"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <th>Evident&nbsp;until:</th>
        <td>
            <c:choose>
                <c:when test="${linkStageTerms}">
                    <zfin:link entity="${formBean.anatomyItem.end}" longVersion="true"/>
                </c:when>
                <c:otherwise>
                    <zfin:name entity="${formBean.anatomyItem.end}" longVersion="true"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>

</table>



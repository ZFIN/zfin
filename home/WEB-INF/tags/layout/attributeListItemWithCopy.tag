<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="label" required="true" rtexprvalue="true" %>
<%@ attribute name="link" required="false" rtexprvalue="true" %>
<%@ attribute name="statsLink" required="false" rtexprvalue="true" type="java.lang.String" %>

<%@ attribute name="dtColSize" required="false" type="java.lang.Integer" %>
<c:set var="dtColSize" value="${(empty dtColSize) ? 2 : dtColSize}"/>

<%@ attribute name="ddColSize" required="false" type="java.lang.Integer" %>
<c:set var="ddColSize" value="${(empty ddColSize) ? 12 - dtColSize : ddColSize}"/>

<c:choose>
    <c:when test="${link != null}">
        <dt class="col-sm-${dtColSize} mb-sm-2"><a href="${link}">${label}</a></dt>
    </c:when>
    <c:otherwise>
        <dt class="col-sm-${dtColSize} mb-sm-2">${label} <c:if test="${not empty statsLink}"><a
                class="popup-link info-popup-link"
                href="/action/publication/stats/view?section=${statsLink}"></a></c:if>
        </dt>
    </c:otherwise>
</c:choose>

<style>
    .copy-attribute-target {
        display: inline-block;
        cursor: pointer;
    }
    .copy-attribute-target:hover {
        /*text-decoration: underline;*/
    }
    .copy-attribute-icon {
        display: none;
        cursor: pointer;
    }
    .copy-attribute-target:hover + .copy-attribute-icon {
        display: inline;
    }
</style>

<dd class="col-sm-${ddColSize} copy-attribute-container">
    <span class="copy-attribute-target"><jsp:doBody/></span>
    <i class="far fa-copy copy-attribute-icon"></i>
</dd>

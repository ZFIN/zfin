<%@ tag import="org.zfin.publication.PublicationType" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="figure" type="org.zfin.expression.Figure" rtexprvalue="true" required="true" %>

<c:set var="UNPUBLISHED" value="${PublicationType.UNPUBLISHED}"/>

<p class="fig">
    <strong>
        <c:choose>
            <c:when test="${figure.type == 'TOD'}">
                <%-- don't show anything as a label for Text Only --%>
            </c:when>
            <c:otherwise>
                ${figure.label}
            </c:otherwise>
        </c:choose>
    </strong>

    <c:choose>
        <c:when test="${figure.type == 'TOD'}">
            Unillustrated author statements
        </c:when>
        <c:when test="${!figure.publication.canShowImages || (empty figure.images && empty figure.caption && figure.publication.type != UNPUBLISHED)}">
            <c:choose>
                <c:when test="${figure.comments == 'GELI'}">
                    This is a summary of gene expression assays reported in this publication.
                    Associated figures and anatomical structures have not yet been added to ZFIN.
                </c:when>
                <c:otherwise>
                    ZFIN is incorporating published figure images and captions as part of an ongoing project.
                    Figures from some publications have not yet been curated, or are not available for display because of copyright restrictions.
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise>
            ${figure.caption}
        </c:otherwise>
    </c:choose>
</p>

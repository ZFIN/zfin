<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.antibody.presentation.AntibodyBean" scope="request"/>

<script type="text/javascript" src="/javascript/prototype.js"></script>

<zfin2:dataManager zdbID="${formBean.antibody.zdbID}"
                   editURL="${formBean.editURL}"
                   deleteURL="${formBean.deleteURL}"
                   mergeURL="${formBean.mergeURL}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.antibody.name}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.antibody.zdbID}"/>
    </tiles:insertTemplate>
</div>

<zfin2:antibodyHead antibody="${formBean.antibody}"  antibodyStat="${formBean.antibodyStat}"/>


<p></p>
<b>NOTES:</b>
<c:if test="${formBean.numOfUsageNotes eq null || formBean.numOfUsageNotes ==0 }">
    None Submitted
</c:if>

<c:if test="${formBean.numOfUsageNotes > 0}">
    <table width=100% border=0 cellspacing=0>
        <tr bgcolor="#cccccc">
            <td width="30%"><b>Reference</b></td>
            <td><b>Comment</b></td>
        </tr>
        <c:forEach var="extnote" items="${formBean.notesSortedByPubTime}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td valign="top">
                    <zfin:link entity="${extnote.singlePubAttribution.publication}"/>
                </td>
                <td>
                    <zfin2:toggleTextLength text="${extnote.note}" idName="${loop.index}" shortLength="80"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:if>
<p></p>

<b>ANATOMICAL LABELING</b>&nbsp;
<c:import url="/WEB-INF/jsp/antibody/antibody_labeling_detail.jsp"/>

<p></p>
<b>SOURCE:</b>
<br/>
<c:choose>
    <c:when test="${formBean.antibody.suppliers ne null && fn:length(formBean.antibody.suppliers) > 0}">
        <table width=100% border=0 cellspacing=0>
            <c:forEach var="supplier" items="${formBean.antibody.suppliers}" varStatus="status">
                <zfin:alternating-tr loopName="status">
                    <td>
                        <c:choose>
                            <c:when test="${supplier.organization.url == null}">
                                <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${supplier.organization.zdbID}"
                                   id="${supplier.organization.zdbID}">
                                        ${supplier.organization.name}
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${supplier.organization.url}" id="${supplier.organization.zdbID}">${supplier.organization.name}</a>
                            </c:otherwise>
                        </c:choose>
                            <%--
                                                    <c:if test="${supplier.orderURL != null && supplier.available ne null}">
                                                        &nbsp;&nbsp;&nbsp;
                                                        <font size="-1">
                                                            <a href="${supplier.orderURL}">
                                                                (order antibody / more info)
                                                            </a>
                                                        </font>
                                                    </c:if>
                            --%>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </c:when>
    <c:otherwise>
        <table width=100% border=0 cellspacing=0>
            <tr class="odd">
                <td width="30%">None Submitted</td>
            </tr>
        </table>
    </c:otherwise>
</c:choose>

<hr width="80%">
<a href="publication-list?antibody.zdbID=${formBean.antibody.zdbID}&orderBy=author">CITATIONS</a>&nbsp;&nbsp;(${formBean.numOfPublications})


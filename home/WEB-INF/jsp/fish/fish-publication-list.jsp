<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishPublicationBean" scope="request"/>


<table width=100%>
    <tr>
        <td bgcolor=#cccccc>
            <font size=+2><b>CITATIONS</b></font>

            (${formBean.numOfPublications} total)

        </td>
    </tr>
</table>

<font size=+1>
    <b>
        Genotype + Morpholinos:&nbsp;
        <a href="fish-detail/${formBean.fish.fishID}"> ${formBean.fish.name}</a><a
        <br/>
    </b>
</font>

<form:form commandName="formBean" name="Update Antibody Publication List" id="Update Antibody Publication List">

    <c:if test="${formBean.numOfPublishedPublications > 1 || formBean.numOfUnpublishedPublications > 1}">
        <c:choose>
            <c:when test="${formBean.orderBy == 'author'}">
                <input type=button name=resultOrder
                       onClick="document.location.replace('fish-publication-list?antibodyID=${formBean.fishID}&orderBy=date<c:if test="${formBean.update}">&update=true</c:if>')"
                       value="Order By Date">
            </c:when>
            <c:otherwise>
                <input type=button name=resultOrder
                       onClick="document.location.replace('fish-publication-list?antibodyID=${formBean.fishID}&orderBy=author<c:if test="${formBean.update}">&update=true</c:if>')"
                       value="Order By Author">
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;&nbsp;
    </c:if>

    <table class="summary rowstripes">
        <c:forEach var="publishedPublication" items="${formBean.sortedPublishedPublications}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td align=left>
                    <div class="show_pubs">
                        <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${publishedPublication.zdbID}"
                                id="${publishedPublication.zdbID}">${publishedPublication.authors}
                            &nbsp;(${publishedPublication.year})&nbsp;${publishedPublication.title}.&nbsp;${publishedPublication.journal.abbreviation}&nbsp;<c:if
                                    test="${publishedPublication.volume != null}">${publishedPublication.volume}:</c:if>${publishedPublication.pages}
                        </a><authz:authorize ifAnyGranted="root">&nbsp;&nbsp;&nbsp;<c:if
                            test="${publishedPublication.open}">OPEN</c:if><c:if
                            test="${!publishedPublication.open}">CLOSE</c:if><c:if
                            test="${publishedPublication.indexed}">,&nbsp;INDEXED</c:if></authz:authorize>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>

        <c:if test="${formBean.numOfUnpublishedPublications > 0}">
            <tr>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td><b>Other Citations (${formBean.numOfUnpublishedPublications}):</b></td>
            </tr>
            <c:forEach var="unpublishedPublication" items="${formBean.sortedUnpublishedPublications}"
                       varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td align=left>
                        <c:if test="${formBean.update && unpublishedPublication.deletable}">
                            <font size=-1><input type=button value=delete
                                                 onclick="disassociatePublication('disassociate-publication?antibodyID=${formBean.antibody.zdbID}&disassociatedPubId=${unpublishedPublication.zdbID}<c:if test="${formBean.orderBy == 'author'}">&orderBy=author</c:if><c:if test="${formBean.orderBy == 'date'}">&orderBy=date</c:if>&update=true')"></font>
                        </c:if>

                        <div class="show_pubs">
                            <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${unpublishedPublication.zdbID}">${unpublishedPublication.authors}
                                &nbsp;(${unpublishedPublication.year})&nbsp;${unpublishedPublication.title}
                            </a><authz:authorize ifAnyGranted="root">&nbsp;&nbsp;&nbsp;<c:if
                                test="${unpublishedPublication.open}">OPEN</c:if><c:if
                                test="${!unpublishedPublication.open}">CLOSE</c:if><c:if
                                test="${publishedPublication.indexed}">,&nbsp;INDEXED</c:if></authz:authorize>
                        </div>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </c:if>
    </table>
</form:form>

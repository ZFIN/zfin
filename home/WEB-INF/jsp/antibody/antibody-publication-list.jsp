<%--
  Created by IntelliJ IDEA.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.zfin.antibody.presentation.AntibodyBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<script type="text/javascript">

    function disassociatePublication(url) {
        if (window.confirm("Are you sure you want to disassociate this publication from the antibody?")) {
            form = document.getElementById("Update Antibody Publication List");
            form.action = url;
            form.submit();
        }
    }

    function associatePublication(url) {
        form = document.getElementById("Update Antibody Publication List");
        form.action = url;
        form.submit();
    }

</script>

<c:choose>
    <c:when test="${formBean.update}">
        <form>
            <center>
                <a style="font-size: large;" href="javascript:"
                   onClick="document.location.replace('/action/marker/view/${formBean.antibody.zdbID}')">[View
                    Antibody]</a>
            </center>
        </form>
    </c:when>
    <c:otherwise>
        <table class="summary rowstripes">
            <tbody>
            <tr>
                <td>
                    <font size="-1"><b>ZFIN ID:</b>
                        ${formBean.antibody.zdbID}
                    </font>
                </td>
                <authz:authorize access="hasRole('root')">
                    <td>
                        <a href="antibody-publication-list?antibodyID=${formBean.antibody.zdbID}&update=true">
                            <font size=-1 color=red> Add/Update this Record </font>
                        </a>
                    </td>
                    <td>

                    </td>
                    <td>
                        &nbsp;
                    </td>
                    <td>
                        <a href="/action/updates/${formBean.antibody.zdbID}">
                            <font size=-1><b>Updated:</b>
                                <c:choose>
                                    <c:when test="${zfn:getLastUpdate(zdbID) != null}">
                                        <fmt:formatDate value="${zfn:getLastUpdate(zdbID).dateUpdated}"
                                                        type="date"/>
                                    </c:when>
                                    <c:otherwise>
                                        Never modified
                                    </c:otherwise>
                                </c:choose>
                            </font>
                        </a>
                    </td>
                </authz:authorize>
            </tr>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>
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
        Antibody Name:&nbsp;<a href="/action/marker/view/${formBean.antibody.zdbID}">${formBean.antibody.name}</a>
        <br/>
    </b>
</font>

<form:form commandName="formBean" name="Update Antibody Publication List" id="Update Antibody Publication List">

    <c:if test="${formBean.numOfPublishedPublications > 1 || formBean.numOfUnpublishedPublications > 1}">
        <c:choose>
            <c:when test="${formBean.orderBy == 'author'}">
                <input type=button name=resultOrder
                       onClick="document.location.replace('antibody-publication-list?antibodyID=${formBean.antibody.zdbID}&orderBy=date<c:if test="${formBean.update}">&update=true</c:if>')"
    value="Order By Date">
    </c:when>
    <c:otherwise>
        <input type=button name=resultOrder
               onClick="document.location.replace('antibody-publication-list?antibodyID=${formBean.antibody.zdbID}&orderBy=author<c:if test="${formBean.update}">&update=true</c:if>')"
        value="Order By Author">
    </c:otherwise>
    </c:choose>
    &nbsp;&nbsp;&nbsp;
    </c:if>
    <authz:authorize access="hasRole('root')">
        <span id="enter-pub-id" style="display: none;">
                <strong title="Global Reference used on this page" id="Def-Pub-field">Enter Pub ID:</strong><form:input
                path="<%= AntibodyBean.AB_NEWPUB_ZDB_ID%>" size="20"></form:input>
              <input value="Add this publication"
                     onclick="associatePublication('antibody-citation-associate-publication?entityID=${formBean.antibody.zdbID}<c:if test="${formBean.orderBy == 'author'}">&orderBy=author</c:if><c:if test="${formBean.orderBy == 'date'}">&orderBy=date</c:if><c:if test="${formBean.update}">&update=true</c:if>');"
                     type="button">
        </span>
        <span id="show-enter-pub-id">
	     <c:if test="${formBean.update}">
             <input value="Add Publication" onclick="jQuery('#enter-pub-id').show();
                                       jQuery('#show-enter-pub-id').hide();" type="button">
         </c:if>
        </span>
    </authz:authorize>
    <form:errors path="*" cssClass="error indented-error"/>

    <table class="summary rowstripes">
        <tbody>
        <c:forEach var="publishedPublication" items="${formBean.sortedPublishedPublications}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <authz:authorize access="hasRole('root')">
                    <c:if test="${formBean.update && publishedPublication.deletable}">
                        <font size=-1><input type=button value=delete
                                             onclick="disassociatePublication('antibody-citation-disassociate-publication?antibodyID=${formBean.antibody.zdbID}&disassociatedPubId=${publishedPublication.zdbID}<c:if test="${formBean.orderBy == 'author'}">&orderBy=author</c:if><c:if test="${formBean.orderBy == 'date'}">&orderBy=date</c:if>&update=true')"></font>
                    </c:if>
                    </authz:authorize>

                    <div class="show_pubs">
                        <a href="/${publishedPublication.zdbID}">${publishedPublication.authors}
                            &nbsp;(${publishedPublication.year})&nbsp;${publishedPublication.title}.&nbsp;${publishedPublication.journal.abbreviation}&nbsp;<c:if
                                    test="${publishedPublication.volume != null}">${publishedPublication.volume}:</c:if>${publishedPublication.pages}
                        </a><authz:authorize access="hasRole('root')">&nbsp;&nbsp;&nbsp;<c:if
                            test="${publishedPublication.open}">OPEN</c:if><c:if
                            test="${!publishedPublication.open}">CLOSE</c:if><c:if
                            test="${publishedPublication.indexed}">,&nbsp;INDEXED</c:if></authz:authorize>
                    </div>
                </td>
            </zfin:alternating-tr>        
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${formBean.numOfUnpublishedPublications > 0}">
      <br/>
      <b>Additional Citations (${formBean.numOfUnpublishedPublications}):</b>
      <table class="summary rowstripes">
        <tbody>
        <c:forEach var="unpublishedPublication" items="${formBean.sortedUnpublishedPublications}"
                       varStatus="loop">
            <zfin:alternating-tr loopName="loop">
            <td>
                        <c:if test="${formBean.update && unpublishedPublication.deletable}">
                            <font size=-1><input type=button value=delete
                                                 onclick="disassociatePublication('disassociate-publication?antibodyID=${formBean.antibody.zdbID}&disassociatedPubId=${unpublishedPublication.zdbID}<c:if test="${formBean.orderBy == 'author'}">&orderBy=author</c:if><c:if test="${formBean.orderBy == 'date'}">&orderBy=date</c:if>&update=true')"></font>
                        </c:if>

            <div class="show_pubs">
            <a href="/${unpublishedPublication.zdbID}">${unpublishedPublication.authors}
                &nbsp;(${unpublishedPublication.year})&nbsp;${unpublishedPublication.title}
            </a><authz:authorize access="hasRole('root')">&nbsp;&nbsp;&nbsp;<c:if
                test="${unpublishedPublication.open}">OPEN</c:if><c:if
                test="${!unpublishedPublication.open}">CLOSE</c:if><c:if
                test="${publishedPublication.indexed}">,&nbsp;INDEXED</c:if></authz:authorize>
             </div>
             </td>
          </zfin:alternating-tr>
        </c:forEach>
        </tbody>
      </table>
    </c:if>
</form:form>

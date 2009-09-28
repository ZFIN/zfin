<%--
  Created by IntelliJ IDEA.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.zfin.antibody.presentation.AntibodyBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>

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
                                     onClick="document.location.replace('detail?antibody.zdbID=${formBean.antibody.zdbID}')">[View Antibody]</a>
            </center>
        </form>
    </c:when>
    <c:otherwise>
        <table bgcolor="#eeeeee" border="0" width="100%">
            <tbody>
                <tr align="center">
                    <td>
                        <font size="-1"><b>ZFIN ID:</b>
                            ${formBean.antibody.zdbID}
                        </font>
                    </td>
                        <authz:authorize ifAnyGranted="root">
                            <td>
                                <a href="publication-list?antibody.zdbID=${formBean.antibody.zdbID}&update=true">
                                    <font size=-1 color=red> Add/Update this Record </font>
                                </a>
                            </td>
                            <td>

                            </td>
                            <td>
		              &nbsp;
                            </td>
                            <td>
                                <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-update-vframeset.apg&OID=${formBean.antibody.zdbID}&rtype=antibody">
                                    <font size=-1><b>Updated:</b>
                                        <c:choose>
                                            <c:when test="${formBean.latestUpdate != null}">
                                                <fmt:formatDate value="${formBean.latestUpdate.dateUpdated}"
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
                Antibody Name:&nbsp;<a href="detail?antibody.zdbID=${formBean.antibody.zdbID}">${formBean.antibody.name}</a>
                <br/>
            </b>
        </font>

    <form:form commandName="formBean" name="Update Antibody Publication List" id="Update Antibody Publication List">

        <c:if test="${formBean.numOfPublishedPublications > 1 || formBean.numOfUnpublishedPublications > 1}">
            <c:choose>
                <c:when test="${formBean.orderBy == 'author'}">
                    <input type=button name=resultOrder
                           onClick="document.location.replace('publication-list?antibody.zdbID=${formBean.antibody.zdbID}&orderBy=date<c:if test="${formBean.update}">&update=true</c:if><c:if test="${formBean.addPublication}">&addPublication=true</c:if>')"
                           value="Order By Date">
                </c:when>
                <c:otherwise>
                    <input type=button name=resultOrder
                           onClick="document.location.replace('publication-list?antibody.zdbID=${formBean.antibody.zdbID}&orderBy=author<c:if test="${formBean.update}">&update=true</c:if><c:if test="${formBean.addPublication}">&addPublication=true</c:if>')"
                           value="Order By Author">
                </c:otherwise>
            </c:choose>
            &nbsp;&nbsp;&nbsp;
        </c:if>

        <c:choose>
           <c:when test="${formBean.addPublication}">
                <strong title="Global Reference used on this page" id="Def-Pub-field">Enter Pub ID:</strong><form:input path="<%= AntibodyBean.AB_NEWPUB_ZDB_ID%>" size="20"></form:input>
              <input value="Add this publication" onclick="associatePublication('add-publication?antibody.zdbID=${formBean.antibody.zdbID}<c:if test="${formBean.orderBy == 'author'}">&orderBy=author</c:if><c:if test="${formBean.orderBy == 'date'}">&orderBy=date</c:if><c:if test="${formBean.update}">&update=true</c:if>&addPublication=true');" type="button">
           </c:when>
           <c:otherwise>
	     <c:if test="${formBean.update}">
	       <input value="Add Publication" onclick="window.location.replace('publication-list?antibody.zdbID=${formBean.antibody.zdbID}<c:if test="${formBean.orderBy == 'author'}">&orderBy=author</c:if><c:if test="${formBean.orderBy == 'date'}">&orderBy=date</c:if><c:if test="${formBean.update}">&update=true</c:if>&addPublication=true');" type="button">
             </c:if>
           </c:otherwise>
        </c:choose>
        <form:errors path="<%= AntibodyBean.AB_NEWPUB_ZDB_ID%>" cssClass="error indented-error"/>

        <table class="summary rowstripes">
            <c:forEach var="publishedPublication" items="${formBean.sortedPublishedPublications}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td align=left>
                        <c:if test="${formBean.update && publishedPublication.deletable}">
                            <font size=-1><input type=button value=delete
                                                 onclick="disassociatePublication('disassociate-publication?antibody.zdbID=${formBean.antibody.zdbID}&disassociatedPubId=${publishedPublication.zdbID}<c:if test="${formBean.orderBy == 'author'}">&orderBy=author</c:if><c:if test="${formBean.orderBy == 'date'}">&orderBy=date</c:if>&update=true')"></font>
                        </c:if>

                        <div class="show_pubs">
                            <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${publishedPublication.zdbID}">${publishedPublication.authors}
                                &nbsp;(${publishedPublication.year})&nbsp;${publishedPublication.title}.&nbsp;${publishedPublication.journal.abbreviation}&nbsp;<c:if test="${publishedPublication.volume != null}">${publishedPublication.volume}:</c:if>${publishedPublication.pages}
                            </a>
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
                                                     onclick="disassociatePublication('disassociate-publication?antibody.zdbID=${formBean.antibody.zdbID}&disassociatedPubId=${unpublishedPublication.zdbID}<c:if test="${formBean.orderBy == 'author'}">&orderBy=author</c:if><c:if test="${formBean.orderBy == 'date'}">&orderBy=date</c:if>&update=true')"></font>
                            </c:if>

                            <div class="show_pubs">
                                <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${unpublishedPublication.zdbID}">${unpublishedPublication.authors}
                                    &nbsp;(${unpublishedPublication.year})&nbsp;${unpublishedPublication.title}
                                </a>
                            </div>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>
            </c:if>
        </table>
    </form:form>

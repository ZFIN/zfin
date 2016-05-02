<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="person" type="org.zfin.profile.Person" required="true" %>
<%--list of company presentations--%>
<%@ attribute name="companies" type="java.util.Collection" required="true" %>
<%--list of lab presentations--%>
<%@ attribute name="labs" type="java.util.Collection" required="true" %>

<%@ attribute name="deleteURL" type="java.lang.String" required="false" %>
<%@ attribute name="editURL" type="java.lang.String" required="false" %>
<%@ attribute name="isOwner" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<zfin2:dataManager zdbID="${person.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   rtype="person"
                   isOwner="${isOwner}"
        />

<zfin2:listAllFromOrganization/>

<table>
    <tr>
        <td width="60%" style="vertical-align: middle;">
            <span class="entity-header">${person.fullName}
            </span>
            <c:if test="${person.deceased}"> (Deceased) </c:if>
            <table class="primary-entity-attributes">
                <authz:authorize access="hasRole('root')">
                    <tr style="background-color: #a9a9a9;">
                        <th>Login:</th>
                        <td>${person.accountInfo.login}
                            - ${person.shortName}
                            <br> Access - ${person.accountInfo.role}
                            <br> Last Login - ${person.accountInfo.previousLoginDate}
                            <c:if test="${person.emailList}">
                                <div>Is on email distribution list</div>
                            </c:if>
                        </td>
                    </tr>
                </authz:authorize>
                <tr>
                    <th>Email:</th>
                    <td><a href="mailto:${person.email}">${person.email}</a></td>
                </tr>
                <tr>
                    <th>URL:</th>
                    <td><a href="${person.url}">${person.url}</a></td>
                </tr>
                <tr>
                    <th>Affiliation:</th>
                    <td>
                        <c:forEach var="lab" items="${labs}" varStatus="status">
                            <c:choose>
                                <c:when test="${status.first}">
                                    <zfin:link entity="${lab}"/> <br>
                                </c:when>
                                <c:otherwise>
                                    ${status.index == 1 ? "<font size=-1>and also: " : ""}
                                    <zfin:link entity="${lab}"/> <br>
                                    ${status.last ? "</font>" : ""}
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                        <c:forEach var="company" items="${companies}">
                            <zfin:link entity="${company}"/> <br>
                        </c:forEach>
                    </td>
                </tr>
                <tr>
                    <th>Address:</th>
                    <td class="postal-address">${person.address}</td>
                </tr>
                <tr>
                    <th>Phone:</th>
                    <td>${person.phone} </td>
                </tr>
                <tr>
                    <th>Fax:</th>
                    <td>${person.fax}</td>
                </tr>
                <tr>
                    <th>Orcid ID:</th>
                    <td><a href="http://orcid.org/${person.orcidID}">${person.orcidID}</a></td>
                </tr>
            </table>
        </td>

        <td width="30%" style="vertical-align: top; text-align: right;">
            <zfin2:viewSnapshot value="${person}"/>
        </td>
    </tr>
</table>

<br>
<br>

<span class="summaryTitle">BIOGRAPHY AND RESEARCH INTERESTS</span>
<br>

<div id='bio'>
    <zfin2:splitLines input="${person.personalBio}"/>
</div>

<br>
<br>

<span class="summaryTitle">PUBLICATIONS</span>

<zfin2:listPublications publications="${person.publications}"/>

<br>

<span class="summaryTitle">NON-ZEBRAFISH PUBLICATIONS</span>
<br>
<zfin2:splitLines input="${person.nonZfinPublications}"/>


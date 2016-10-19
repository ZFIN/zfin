<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="company" type="org.zfin.profile.Company" required="true" %>
<%--list of Publication--%>
<%@ attribute name="publications" type="java.util.Collection" required="true" %>
<%--list of PersonMemberPresentation --%>
<%@ attribute name="members" type="java.util.Collection" required="true" %>
<%--list of OrganizationFeaturePrefix --%>
<%@ attribute name="prefixes" type="java.util.Collection" required="true" %>
<%@ attribute name="deleteURL" type="java.lang.String" required="false" %>
<%@ attribute name="editURL" type="java.lang.String" required="false" %>
<%@ attribute name="isOwner" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<zfin2:dataManager zdbID="${company.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   isOwner="${isOwner}"
        />

<zfin2:listAllFromOrganization/>

<table>
    <tr>
        <td width="60%" style="vertical-align: top;">
            <span class="entity-header">${company.name}</span>
            <table class="primary-entity-attributes">
                <tr>
                    <th>Contact Person:</th>
                    <td>
                        <c:choose>
                            <c:when test="${empty company.contactPerson}">
                                <div class="error-inline">No Contact Person Assigned</div>
                            </c:when>
                            <c:otherwise>
                                <%--${company.contactPerson.zdbID}--%>
                                <zfin:link entity="${company.contactPerson}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td><a href="mailto:${company.email}">${company.email}</a></td>
                </tr>
                <tr>
                    <th>URL:</th>
                    <td><a href="${company.url}">${company.url}</a></td>
                </tr>
                <tr>
                    <th>Address:</th>
                    <td class="postal-address">${company.address}</td>
                </tr>
                <tr>
                    <th>Phone:</th>
                    <td>${company.phone} </td>
                </tr>
                <tr>
                    <th>Fax:</th>
                    <td>${company.fax}</td>
                </tr>
                <tr>
                    <th>Line Designation:</th>
                    <td>
                        <c:choose>
                            <c:when test="${empty prefixes}">
                                <span class="no-data-tag">None assigned</span>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="prefix" items="${prefixes}">
                                    ${prefix.activeForSet ? prefix.prefixString : ''}
                                    <authz:authorize access="hasRole('root')">
                                        <div style="color: #a9a9a9; display: inline-block;"> ${prefix.activeForSet ? prefix.institute : ''}</div>
                                    </authz:authorize>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>

            </table>
        </td>
        <td width="30%"  style="vertical-align: top; text-align: right; ">
            <zfin2:viewSnapshot value="${company}" className="profile-image"/>
        </td>
    </tr>
</table>


<br>
<br>

<span class="summaryTitle">PRODUCTS AND SERVICES</span>
<br>
<div id='bio'><zfin2:splitLines input="${company.bio}"/></div>

<br>
<br>

<span class="summaryTitle">COMPANY REPRESENTATIVES</span>
<br>
<zfin2:listMembersInTable members="${members}" columns="3"/>

<br>

<span class="summaryTitle">ZEBRAFISH PUBLICATIONS OF COMPANY REPRESENTATIVES</span>

<zfin2:listPublications publications="${publications}"/>


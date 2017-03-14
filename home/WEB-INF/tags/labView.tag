<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="lab" type="org.zfin.profile.Lab" required="true" %>
<%--list of Publication--%>
<%@ attribute name="publications" type="java.util.Collection" required="true" %>
<%--list of PersonMemberPresentation --%>
<%@ attribute name="members" type="java.util.Collection" required="true" %>
<%--list of OrganizationFeaturePrefix --%>
<%@ attribute name="prefixes" type="java.util.Collection" required="true" %>

<%@ attribute name="deleteURL" type="java.lang.String" required="false" %>
<%@ attribute name="editURL" type="java.lang.String" required="false" %>
<%@ attribute name="isOwner" type="java.lang.Boolean" rtexprvalue="true" required="true" %>
<%@ attribute name="hasCoPi" type="java.lang.Boolean" rtexprvalue="true" required="true" %>
<%@ attribute name="noPrefixes" type="java.lang.Boolean" rtexprvalue="true" required="true" %>
<%@ attribute name="numOfFeatures" type="java.lang.Long" required="true" %>

<zfin2:dataManager zdbID="${lab.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   isOwner="${isOwner}"
/>

<zfin2:listAllFromOrganization/>


<table>
    <tr>
        <td width="60%" style="vertical-align: top;">
            <span class="entity-header">${lab.name}</span>
            <table class="primary-entity-attributes">
                <tr>
                    <th>PI/Director:</th>
                    <td>
                        <zfin2:listMembers members="${members}" only="1" suppressTitle="true" suffix="<br>"/>
                    </td>
                </tr>
                <c:if test="${hasCoPi}">
                    <tr>
                        <th>Co-PI / Senior<br/> Researcher:</th>
                        <td>
                            <zfin2:listMembers members="${members}" only="2" suppressTitle="true" suffix="<br>"/>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <th>Contact Person:</th>
                    <td><zfin:link entity="${lab.contactPerson}"/></td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td><a href="mailto:${lab.email}">${lab.email}</a></td>
                </tr>
                <tr>
                    <th>URL:</th>
                    <td><a href="${lab.url}">${lab.url}</a></td>
                </tr>

                <tr>
                    <th>Address:</th>
                    <td class="postal-address">${lab.address}</td>
                </tr>
                <tr>
                    <th>Phone:</th>
                    <td>${lab.phone} </td>
                </tr>
                <tr>
                    <th>Fax:</th>
                    <td>${lab.fax}</td>
                </tr>
                <tr>
                    <th>Line Designation:</th>
                    <td>
                        <c:choose>
                            <c:when test="${noPrefixes}">
                                <span class="no-data-tag">None assigned</span>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="prefix" items="${prefixes}">
                                    ${prefix.activeForSet ? prefix.prefixString : ''}
                                    <authz:authorize access="hasRole('root')">
                                        &nbsp;<div style="color: #a9a9a9; display: inline-block;"> ${prefix.activeForSet ? prefix.institute : ''}</div>
                                    </authz:authorize>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </table>
        </td>
        <td width="30%" style="vertical-align: top; text-align: right;">
            <zfin2:viewSnapshot value="${lab}" className="profile-image"/>
        </td>
</table>

<br>
<br>

<script type="text/javascript">
    function showAlleles(labZdbID) {
        alleleDiv = document.getElementById('alleleDesignation');
        alleleDiv.style.display = 'inline';
        jQuery('#alleleDesignation').load('/action/feature/features-for-lab/' + labZdbID);
        alleleShowButton = document.getElementById('showAlleleLink');
        alleleShowButton.style.display = 'none';
        alleleHideButton = document.getElementById('hideAlleleLink');
        alleleHideButton.style.display = 'inline';
    }

    function hideAlleles() {
        alleleDiv = document.getElementById('alleleDesignation');
        alleleDiv.style.display = 'none';
        alleleShowButton = document.getElementById('showAlleleLink');
        alleleShowButton.style.display = 'inline';
        alleleHideButton = document.getElementById('hideAlleleLink');
        alleleHideButton.style.display = 'none';
    }
</script>

<zfin2:subsection title="GENOMIC FEATURES ORIGINATING FROM THIS LAB"
                  test="${numOfFeatures>0}" showNoData="true">
    <c:choose>
        <c:when test="${numOfFeatures > 50}">
            <a id="showAlleleLink" href="javascript:" onclick="showAlleles('${lab.zdbID}');">Show first 50
                of ${numOfFeatures}</a> genomic features
        </c:when>
        <c:otherwise>
            <a id="showAlleleLink" href="javascript:" onclick="showAlleles('${lab.zdbID}');">Show
                all </a> ${numOfFeatures} genomic features
        </c:otherwise>
    </c:choose>
    <a id="hideAlleleLink" style="display: none;" href="javascript:hideAlleles()" onclick="hideAlleles()">Hide</a>

</zfin2:subsection>

<div style="display: none;" id="alleleDesignation"></div>
<br>


<br>


<span class="summaryTitle">STATEMENT OF RESEARCH INTERESTS</span>
<br/>

<div id='bio'><zfin2:splitLines input="${lab.bio}"/></div>

<br>
<br>

<span class="summaryTitle">LAB MEMBERS</span>
<br>
<zfin2:listMembersInTable members="${members}" greaterThan="2" columns="3"/>

<br>
<br>

<span class="summaryTitle">ZEBRAFISH PUBLICATIONS OF LAB MEMBERS</span>
x
<zfin2:listPublications publications="${publications}"/>



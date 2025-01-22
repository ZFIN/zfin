<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table>
    <tr>
        <td>
            <z:attributeList>
                <z:attributeListItem label="Person ID" copyable="true">
                    <span id="marker-id">${person.zdbID}</span>
                </z:attributeListItem>

                <authz:authorize access="hasRole('root')">
                    <z:attributeListItem label="Login">
                        ${person.accountInfo.login}
                    </z:attributeListItem>
                    <z:attributeListItem label="Access">
                        ${person.accountInfo.role}
                    </z:attributeListItem>
                    <z:attributeListItem label="Last Login">
                        ${person.accountInfo.previousLoginDate}
                    </z:attributeListItem>
                </authz:authorize>

                <c:if test="${!empty person.email && person.emailPrivacyPreference.visible}">
                    <z:attributeListItem label="Email">
                        <a href="mailto:${person.email}">${person.email}</a>
                    </z:attributeListItem>
                </c:if>

                <z:attributeListItem label="URL">
                    <zfin2:uriDisplay uri="${person.url}"/>
                </z:attributeListItem>

                <z:attributeListItem label="Affiliation">
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
                </z:attributeListItem>

                <z:attributeListItem label="Address">
                    <span class="postal-address">${person.address}</span>
                </z:attributeListItem>

                <z:attributeListItem label="Country">
                    ${country}
                </z:attributeListItem>

                <z:attributeListItem label="Phone">
                    ${person.phone}
                </z:attributeListItem>

                <z:attributeListItem label="Fax">
                    ${person.fax}
                </z:attributeListItem>

                <z:attributeListItem label="ORCID ID">
                    <c:if test="${!empty person.orcidID}">
                    <a href="http://orcid.org/${person.orcidID}">${person.orcidID}</a>
                    </c:if>
                </z:attributeListItem>

            </z:attributeList>
        </td>
        <td width="30%" style="vertical-align: top; text-align: left;">
            <zfin2:profileImage value="${person}" className="profile-image"/>
        </td>
    </tr>
</table>
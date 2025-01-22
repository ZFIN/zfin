<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="company-view-summary-table">
    <tr>
        <td>
            <z:attributeList>
                <z:attributeListItem dtColSize="3" label="Company ID" copyable="true">
                    <span id="marker-id">${company.zdbID}</span>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Contact Person">
                    <c:choose>
                        <c:when test="${empty company.contactPerson}">
                            <div class="error-inline">No Contact Person Assigned</div>
                        </c:when>
                        <c:otherwise>
                            <zfin:link entity="${company.contactPerson}"/>
                        </c:otherwise>
                    </c:choose>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Email">
                    <a href="mailto:${company.email}">${company.email}</a>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="URL">
                    <zfin2:uriDisplay uri="${company.url}"/>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Address">
                    <span class="postal-address">${company.address}</span>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Country">
                    ${country}
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Phone">
                    ${company.phone}
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Fax">
                    ${company.fax}
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Line Designation">
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
                </z:attributeListItem>

            </z:attributeList>
        </td>
        <td width="30%" style="vertical-align: top; text-align: left;">
            <zfin2:profileImage value="${company}" className="profile-image"/>
        </td>
    </tr>
</table>
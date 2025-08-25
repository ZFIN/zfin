<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="lab-view-summary-table">
    <tr>
        <td>
            <z:attributeList>
                <z:attributeListItem dtColSize="3" label="Lab ID" copyable="true">
                    <span id="marker-id">${formBean.zdbID}</span>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="PI / Director">
                    <zfin2:listMembers members="${members}" only="1" suppressTitle="true" suffix="<br>"/>
                </z:attributeListItem>

                <c:if test="${hasCoPi}">
                    <z:attributeListItem dtColSize="3" label="Co-PI / Senior<br/> Researcher">
                        <zfin2:listMembers members="${members}" only="2" suppressTitle="true" suffix="<br>"/>
                    </z:attributeListItem>
                </c:if>

                <z:attributeListItem dtColSize="3" label="Contact Person">
                    <zfin:link entity="${formBean.contactPerson}"/>
                </z:attributeListItem>

                <c:if test="${!empty formBean.email && formBean.emailPrivacyPreference.visible}">
                    <z:attributeListItem dtColSize="3" label="Email">
                        <a href="mailto:${formBean.email}">${formBean.email}</a>
                    </z:attributeListItem>
                </c:if>

                <z:attributeListItem dtColSize="3" label="URL">
                    <zfin2:uriDisplay uri="${formBean.url}"/>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Address">
                    <span class="postal-address">${formBean.address}</span>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Country">
                    <zfin2:uriDisplay uri="${country}"/>
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Phone">
                    ${formBean.phone}
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Fax">
                    ${formBean.fax}
                </z:attributeListItem>

                <z:attributeListItem dtColSize="3" label="Line Designation">
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
                </z:attributeListItem>

            </z:attributeList>
        </td>
        <td width="30%" style="vertical-align: top; text-align: left;">
            <zfin2:profileImage value="${formBean}" className="profile-image"/>
        </td>
    </tr>
</table>
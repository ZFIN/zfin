<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="Lab ID">
        <span id="marker-id">${formBean.zdbID}</span>
    </z:attributeListItem>

    <z:attributeListItem label="PI / Directory">
        <zfin2:listMembers members="${members}" only="1" suppressTitle="true" suffix="<br>"/>
    </z:attributeListItem>

    <c:if test="${hasCoPi}">
        <z:attributeListItem label="Co-PI / Senior<br/> Researcher">
            <zfin2:listMembers members="${members}" only="2" suppressTitle="true" suffix="<br>"/>
        </z:attributeListItem>
    </c:if>

    <z:attributeListItem label="Contact Person">
        <zfin:link entity="${formBean.contactPerson}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Email">
        <a href="mailto:${formBean.email}">${formBean.email}</a>
    </z:attributeListItem>

    <z:attributeListItem label="URL">
        <zfin2:uriDisplay uri="${formBean.url}" />
    </z:attributeListItem>

    <z:attributeListItem label="Address">
        <span class="postal-address">${formBean.address}</span>
    </z:attributeListItem>

    <z:attributeListItem label="Country">
        <zfin2:uriDisplay uri="${country}" />
    </z:attributeListItem>

    <z:attributeListItem label="Phone">
        ${formBean.phone}
    </z:attributeListItem>

    <z:attributeListItem label="Fax">
        ${formBean.fax}
    </z:attributeListItem>

    <z:attributeListItem label="Line Designation">
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
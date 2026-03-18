<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="dbLink" type="org.zfin.sequence.DBLink" scope="request"/>

<z:page bootstrap="true">
    <div class="container-fluid">
        <h3 class="page-header">
            DBLink: ${dbLink.accessionNumber}
            <z:dataManagerDropdown>
                <a class="dropdown-item" href="/action/infrastructure/deleteRecord/${dbLink.zdbID}">
                    <i class="fas fa-trash"></i> Delete
                </a>
                <a class="dropdown-item" href="/action/updates/${dbLink.zdbID}">
                    <i class="fas fa-history"></i> Updates
                </a>
            </z:dataManagerDropdown>
        </h3>

        <z:attributeList>
            <z:attributeListItem label="ZDB ID" copyable="true">
                ${dbLink.zdbID}
            </z:attributeListItem>

            <z:attributeListItem label="Accession Number" copyable="true">
                ${dbLink.accessionNumber}
            </z:attributeListItem>

            <z:attributeListItem label="Accession Display">
                <c:if test="${!empty dbLink.accessionNumberDisplay}">${dbLink.accessionNumberDisplay}</c:if>
            </z:attributeListItem>

            <z:attributeListItem label="Linked Entity">
                <a href="/${dbLink.dataZdbID}">${dbLink.dataZdbID}</a>
            </z:attributeListItem>

            <z:attributeListItem label="Reference Database">
                <c:if test="${dbLink.referenceDatabase != null}">
                    ${dbLink.referenceDatabase.foreignDB.dbName} / ${dbLink.referenceDatabase.foreignDBDataType.dataType} / ${dbLink.referenceDatabase.foreignDBDataType.superType}
                </c:if>
            </z:attributeListItem>

            <z:attributeListItem label="Length">
                <c:if test="${dbLink.length != null}">${dbLink.HRLength} ${dbLink.units}</c:if>
            </z:attributeListItem>

            <z:attributeListItem label="Link Info">
                <c:if test="${!empty dbLink.linkInfo}">${dbLink.linkInfo}</c:if>
            </z:attributeListItem>

            <z:attributeListItem label="External URL">
                <c:if test="${!empty dbLink.url}">
                    <a href="${dbLink.url}" target="_blank">${dbLink.url}</a>
                </c:if>
            </z:attributeListItem>

            <z:attributeListItem label="Publications">
                ${dbLink.publicationCount}
                <c:if test="${dbLink.publicationCount > 0}">
                    <ul class="list-unstyled mb-0">
                        <c:forEach var="pubAttr" items="${dbLink.publications}">
                            <li><a href="/${pubAttr.publication.zdbID}">${pubAttr.publication.zdbID}</a></li>
                        </c:forEach>
                    </ul>
                </c:if>
            </z:attributeListItem>
        </z:attributeList>
    </div>
</z:page>

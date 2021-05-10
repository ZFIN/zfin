<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bodyClass="data-page" bootstrap="true">
    <div class="data-page-content-container">
        <h1 class="mb-3">
            Orthology established by <zfin:link entity="${publication}"/> (${formBean.totalRecords} genes)
        </h1>

        <z:section>
            <c:forEach var="formBean" items="${orthologyBeanList}">
                <z:section>
                    <jsp:attribute name="title">
                        Orthology for <zfin:link entity="${formBean.marker}"/> (<zfin2:displayLocation entity="${formBean.marker}" hideLink="true"/>)
                    </jsp:attribute>
                    <jsp:body>
                        <c:if test="${!empty formBean.marker.orthologyNote.note}">
                            <z:attributeList>
                                <z:attributeListItem label="Note">
                                    ${formBean.marker.orthologyNote.note}
                                </z:attributeListItem>
                            </z:attributeList>
                        </c:if>
                        <div class="__react-root"
                             id="OrthologyTable__${formBean.marker.zdbID}"
                             data-gene-id="${formBean.marker.zdbID}">
                        </div>
                    </jsp:body>
                </z:section>
            </c:forEach>

            <div class="mt-4">
                <zfin2:pagination paginationBean="${formBean}"/>
            </div>
        </z:section>
    </div>

    <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>
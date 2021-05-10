<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="downloadFileList" type="java.util.Collection" required="true" %>
<%@attribute name="identifier" type="java.lang.String" required="true"
             description="needed to generate unique div ids. Has to be unique per page." %>

<table class="summary groupstripes" width="90%">
    <tr>
        <th width="200">Category</th>
        <th width="500">Download File</th>
        <th width="100">Download <br/>w Header</th>
        <th width="50">Column Headers</th>
        <th width="100" style="text-align: right">File Size</th>
        <th width="100" style="text-align: right">Number of Records</th>
        <th width="10"></th>
    </tr>
    <c:forEach var="fileInfo" items="${downloadFileList}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${downloadFileList}"
                             groupByBean="downloadFile.category" newGroup="true">
            <td class="bold">
                <zfin:groupByDisplay loopName="loop" groupBeanCollection="${downloadFileList}"
                                     groupByBean="downloadFile.category">
                    ${fileInfo.downloadFile.category}
                </zfin:groupByDisplay>
            </td>
            <td>
                <c:choose>
                    <c:when test="${formBean.currentDate}">
                        <a href="downloads/${fileInfo.name}" id="${fileInfo.name}">${fileInfo.downloadFile.name}</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${formBean.date}/${fileInfo.name}">${fileInfo.downloadFile.name}</a>
                    </c:otherwise>
                </c:choose>
                <br/>
                ${fileInfo.downloadFile.description}
            </td>
            <td>
                <form:form action="downloads/file/" commandName="formBean"
                           method="GET" id="${fileInfo.name}">
                    <input value="${fileInfo.downloadFile.fileFormat}"
                           onclick="downloadFile('${formBean.date}','${fileInfo.name}')"
                           type="button" class="${fileInfo.downloadFile.fileFormat}">
                </form:form>
            </td>
            <td>
                <a href="#" class="header-toggle" data-toggle="#header-info-${identifier}-${loop.index}">Show</a>
            </td>
            <td style="text-align: right">
                    ${fileInfo.byteCountDisplay}
            </td>
            <td style="text-align: right">
                <fmt:formatNumber type="number" pattern="###,###" value="${fileInfo.numberOfLines}"/>
            </td>
            <td/>
        </zfin:alternating-tr>
        <tr>
            <td colspan="7" class="download-header-row" id="header-info-${identifier}-${loop.index}">
                <table>
                    <tr>
                        <c:forEach var="column" items="${fileInfo.downloadFile.columnHeaders}" varStatus="ind">
                            <td>${ind.index+1}</td>
                        </c:forEach>
                    </tr>
                    <tr>
                        <c:forEach var="column" items="${fileInfo.downloadFile.columnHeaders}">
                            <td>${column.column}</td>
                        </c:forEach>
                    </tr>
                </table>
                <authz:authorize access="hasRole('root')">
                <c:if test="${!empty fileInfo.downloadFile.query}">
                     <table>
                        <tr>
                            <td>SQL Query</td>
                        </tr>
                        <tr>
                            <td>${fileInfo.downloadFile.query}</td>
                        </tr>
                     </table>
                </c:if>
                </authz:authorize>
            </td>
        </tr>
    </c:forEach>
</table>

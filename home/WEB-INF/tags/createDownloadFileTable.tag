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
                        <a href="downloads/${fileInfo.name}">${fileInfo.downloadFile.name}</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${formBean.date}/${fileInfo.name}">${fileInfo.downloadFile.name}</a>
                    </c:otherwise>
                </c:choose>
                <br/>
                    ${fileInfo.downloadFile.description}
            </td>
            <td nowrap="nowrap">
                <form:form action="downloads/file/" commandName="formBean"
                           method="GET" id="${fileInfo.name}">
                    <input value="${fileInfo.downloadFile.fileFormat}"
                           onclick="downloadFile('${formBean.date}','${fileInfo.name}')"
                           type="button" class="${fileInfo.downloadFile.fileFormat}">
                </form:form>
            </td>
            <td>
                <div style="font-size: 14px; text-align: left">
                                            <span style="text-align:center"
                                                  id="header-info-show-link-${identifier}-${loop.index}">
                            <a style="margin-right: 1em;" class="clickable showAll"
                               onclick="jQuery('#header-info-show-link-${identifier}-${loop.index}').hide();
                                       jQuery('#header-info-hide-detail-${identifier}-${loop.index}').show();
                                       jQuery('#header-info-${identifier}-${loop.index}').show();">
                                Show</a>
                    </span>
                        <span style="text-align:left; display: none;"
                              id="header-info-hide-detail-${identifier}-${loop.index}">
                            <a style="margin-right: 1em;" class="clickable hideAll"
                               onclick="jQuery('#header-info-${identifier}-${loop.index}').hide();
                                       jQuery('#header-info-hide-detail-${identifier}-${loop.index}').hide();
                                       jQuery('#header-info-show-link-${identifier}-${loop.index}').show();">Hide </a>
                        </span>
                </div>
            </td>
            <td style="text-align: right">
                    ${fileInfo.byteCountDisplay}
            </td>
            <td style="text-align: right">
                <fmt:formatNumber type="number" pattern="###,###" value="${fileInfo.numberOfLines}"/>
            </td>
            <td/>
        </zfin:alternating-tr>
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${downloadFileList}"
                             groupByBean="downloadFile.category" newGroup="false">
            <td colspan="7" style="font-size:smaller; background-color: #066; display: none"
                id="header-info-${identifier}-${loop.index}">
                <div align="center">
                    <table class="summary1">
                        <tr>
                            <c:forEach var="column" items="${fileInfo.downloadFile.columnHeaders}" varStatus="ind">
                                <td style="font-weight: bold; background-color: #CCFFFF; text-align: center">${ind.index+1}</td>
                            </c:forEach>
                        </tr>
                        <tr>
                            <c:forEach var="column" items="${fileInfo.downloadFile.columnHeaders}">
                                <td style="font-weight: bold; background-color: #CCFFFF;">${column.column}</td>
                            </c:forEach>
                        </tr>
                    </table>
                    <authz:authorize ifAnyGranted="root">
                        <table>
                            <tr>
                                <td style="background-color: #CCFFFF; font-weight: bold;">SQL Query</td>
                            </tr>
                            <tr>
                                <td style="font-size:smaller; background-color: #066;">
                                    <div align="center" style="background-color: #CCFFFF;">
                                            ${fileInfo.downloadFile.query}
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </authz:authorize>
                </div>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

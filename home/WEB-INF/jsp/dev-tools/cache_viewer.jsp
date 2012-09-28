<%@ page import="org.zfin.framework.presentation.CacheForm" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.framework.presentation.CacheForm" scope="request"/>

<table cellpadding="3" cellspacing="1" border="0" width="90%">

    <tr>
        <td colspan="3" class="sectionTitle">Cache Contents</td>
    </tr>
    <tr>
        <td class="sectionTitle" valign="top">Cache names</td>
        <td colspan="2" class="sectionTitle">
            <table>
                <tr>
                    <td class="titlebar">Entity Name</td>
                    <td class="titlebar"># of objects</td>
                    <td class="titlebar">Max elements</td>
                    <td class="titlebar">status</td>
                    <td class="titlebar"><a
                            href="?regionName=${cacheItem.cache.name}&action=<%= CacheForm.ACTION_SHOW_SIZE %>">Show
                        Size</a></td>
                </tr>
                <c:forEach var="cacheItem" items="${formBean.caches}">
                    <tr>
                        <td> ${cacheItem.cache.name} </td>
                        <td>
                            <a href="?action=<%= CacheForm.ACTION_SHOW_OBJECTS %>&regionName=${cacheItem.cache.name}">${cacheItem.cache.size}</a>
                        </td>
                        <td> ${cacheItem.cache.memoryStoreSize} </td>
                        <td> ${cacheItem.cache.diskStoreSize} </td>
                        <td>  </td>
                        <td>  </td>
                        <td>  </td>
                        <td> heItem.cache.status} </td>
                        <td>  </td>
                        <td><c:if test="${formBean.showSize}">
                            <c:if test="${cacheItem.memorySize > 1024}">
                            ${cacheItem.memorySizeKB} KB
                            </c:if>
                            <c:if test="${cacheItem.memorySize < 1024}">
                            ${cacheItem.memorySize} B
                            </c:if>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </td>
    </tr>
    <c:if test="${formBean.showObjects}">
        <tr>
            <td class="sectionTitle" valign="top">Cached Objects</td>
            <td class="sectionTitle" valign="top">
                    ${formBean.regionName}
            </td>
            <td class="sectionTitle">
                <table>
                    <c:forEach var="key" items="${formBean.regionCache.keys}">
                        <tr>
                            <td>
                                    ${key}
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </td>
        </tr>
    </c:if>
</table>

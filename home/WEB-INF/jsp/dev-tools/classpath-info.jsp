<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Classpath">
    <table cellpadding="0" cellspacing="2" border="0" width="80%" bgcolor="#003366">
    <tr>
        <td class="pageTitle">Classpath Tester</td>
    </tr>
    <tr>
    <td bgcolor="#FFFFFF">
    <table cellpadding="0" cellspacing="0" width="100%" border="0">
    <tr>
        <td></td>
    </tr>
    <tr>
        <td align="center">
            <form:form method="GET" action="/action/devtool/classpath-info" commandName="formBean">
                <table cellpadding="2" cellspacing="1" border="0"
                       bgcolor="#003366">
                    <tr>
                        <td bgcolor="#FFFFFF">
                            <table cellpadding="1" cellspacing="0" border="0">
                                <tr>
                                    <td><b>Class to load:</b></td>
                                    <td>
                                        <form:input path="className" size="60"/>
                                    </td>
                                    <td><INPUT TYPE="submit" name="type" VALUE="Submit"></td>
                                </tr>
                            </table>
                            <c:out value="${formBean.errorMessage}"/>
                        </td>
                    </tr>
                </table>
            </form:form>
        </td>
    </tr>

    <tr>
        <td align="center">

            <c:if test="${formBean.fullClassName != null}">
            <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                    <td colspan="2" class="sectionTitle">
                        Successfully loaded class: ${formBean.fullClassName}
                    </td>
                </tr>
                <tr>
                    <td class="listContent">
                        ClassLoader:
                    </td>
                    <td class="listContent">
                            ${formBean.classLoaderName}
                    </td>
                </tr>

                <c:forEach var="library" items="${formBean.classLoaderParents}">
                    <tr>
                        <td class="listContent">Parent:</td>
                        <td class="listContent">
                                ${library.libaryFileName}
                        </td>
                    </tr>
                </c:forEach>
                <td class="listContent">
                    Class loaded from file:
                </td>
                <td class="listContent">
                        ${formBean.classFileName}
                </td>

            </table>
            </c:if>

    <%--
    trying to remove scriptlets from JSP on 10/13/2020. this page is broken due to problems
    on the backend (ClasspathInfoController) probably caused by not keeping this page in sync
    with Java version upgrades. instead of attempt to fix the entire page, i am just replacing
    the scriptlets with comments. when the overall page is fixed, we'll need to figure out
    how to recreate the commented-out functionality without scriptlets, if we want to keep that
    functionality at all
    --%>
    <tr>
        <td align="center">
            <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                    <td colspan=2 class="sectionTitle">Classloaders for this JSP</td>
                </tr>

                <tr>
                    <td class="listContent">
                        ClassLoader:
                    </td>
                    <td class="listContent">
    <%--                    this.getClass().getClassLoader().getClass().getName()--%>

                    </td>
                </tr>
    <%--            --%>
    <%--                ClassLoader loader = this.getClass().getClassLoader().getParent();--%>
    <%--                boolean firstTime = true;--%>
    <%--            --%>
                <tr>
                    <td class="listContent">Parent(s):</td>
    <%--                --%>
    <%--                    while (loader != null) {--%>
    <%--                        if (firstTime) {--%>
    <%--                            firstTime = false;--%>
    <%--                        } else {--%>
    <%--                --%>
                <tr>
                    <td class="listContent"></td>
    <%--                 } --%>
                    <td class="listContent">
    <%--                     loader.getClass().getName() --%>
                    </td>
                </tr>
    <%--                    loader = loader.getParent();--%>
    <%--                }--%>
    <%--                if (!firstTime) {--%>
    <%--            --%>
                <tr>
                    <td class="listContent"></td>
    <%--                 } --%>
                    <td class="listContent">Bootstrap classloader</td>
                </tr>
            </table>
        </td>
    </tr>

    <tr>
        <td align="center">
            <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                    <td class="sectionTitle">Boot Classes <i>(from
                        sun.boot.class.path)</i></td>
                </tr>
                <c:forEach var="library" items="${formBean.bootLibraries}">
                    <tr>
                        <td class="listContent">
                                ${library.libaryFileName}
                            <c:if test="${!library.libraryFileExists}">
                                not found
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </td>
    </tr>
    <tr>
        <td align="center">
            <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                    <td class="sectionTitle">Extension Classes <i>(from
                        java.ext.dirs)</i></td>
                </tr>
                <c:forEach var="library" items="${formBean.extensionLibraries}">
                    <tr>
                        <td class="listContent">
                                ${library.libaryFileName}
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </td>
    </tr>

    <tr>
        <td align="center">
            <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                    <td class="sectionTitle">Application Classes <i>(from
                        java.class.path)</i></td>
                </tr>
                <c:forEach var="library" items="${formBean.applicationLibraries}">
                    <tr>
                        <td class="listContent">
                                ${library.libaryFileName}
                            <c:if test="${!library.libraryFileExists}">
                                not found
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </td>
    </tr>

    <tr>
        <td align="center">
            <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                    <td class="sectionTitle">WEB-INF Classes</td>
                </tr>
                <c:forEach var="library" items="${formBean.classesLibraries}">
                    <tr>
                        <td class="listContent">
                                ${library.libaryFileName}
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </td>
    </tr>
    </table>
    </td>
    </tr>
    </table>
</z:devtoolsPage>
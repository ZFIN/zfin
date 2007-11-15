<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


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
        <form action="/action/dev-tools/classpath-info" method="POST">
            <table cellpadding="2" cellspacing="1" border="0"
                   bgcolor="#003366">
                <tr>
                    <td bgcolor="#FFFFFF">
                        <table cellpadding="1" cellspacing="0" border="0">
                            <tr>
                                <td><b>Class to load:</b></td>
                                <td><html:text name="classpathForm" property="className"/></td>
                                <td><INPUT TYPE="submit" name="type" VALUE="Submit"></td>
                            </tr>
                        </table>
                        <c:out value="${classpathForm.errorMessage}"/>
                    </td>
                </tr>
            </table>
        </form>
    </td>
</tr>

<tr>
<td align="center">

    <logic:notEmpty name="classpathForm" property="fullClassName">
<table cellpadding="2" cellspacing="1" border="0" width="95%">
    <tr>
        <td colspan="2" class="sectionTitle">
            Successfully loaded class: <bean:write name="classpathForm" property="fullClassName"/>
        </td>
    </tr>
    <tr>
        <td class="listContent">
            ClassLoader:
        </td>
        <td class="listContent">
            <bean:write name="classpathForm" property="classLoaderName"/>
        </td>
    </tr>

    <logic:iterate id="library" name="classpathForm" property="classLoaderParents"
                   type="org.zfin.framework.presentation.ClassLibraryWrapper">
        <tr>
            <td class="listContent">Parent:</td>
            <td class="listContent">
                <bean:write name="library" property="libaryFileName"/>
            </td>
        </tr>
    </logic:iterate>
    <td class="listContent">
        Class loaded from file:
    </td>
    <td class="listContent">
        <bean:write name="classpathForm" property="classFileName"/>
    </td>

</table>
</logic:notEmpty>
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
                    <%= this.getClass().getClassLoader().getClass().getName()
                    %>
                </td>
            </tr>
            <%
                ClassLoader loader = this.getClass().getClassLoader().getParent();
                boolean firstTime = true;
            %>
            <tr><td class="listContent">Parent(s):</td>
                <%

              while (loader != null) {
                  if (firstTime) {
                  firstTime = false;
                  } else {

                %>
                <tr><td class="listContent"></td>
                    <% } %>
                    <td class="listContent">
                        <%= loader.getClass().getName() %>
                    </td></tr>
                <%

            loader = loader.getParent();
            }
            if (!firstTime) {
                %>
                <tr><td class="listContent"></td>
                    <% } %>
                    <td class="listContent">Bootstrap classloader</td></tr>
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

            <logic:iterate id="library" name="classpathForm" property="bootLibraries"
                           type="org.zfin.framework.presentation.ClassLibraryWrapper">
                <tr>
                    <td class="listContent">
                        <bean:write name="library" property="libaryFileName"/>
                        <logic:equal value="false" name="library" property="libraryFileExists">
                            not found
                        </logic:equal>
                    </td>
                </tr>
            </logic:iterate>
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
            <logic:iterate id="library" name="classpathForm" property="extensionLibraries"
                           type="org.zfin.framework.presentation.ClassLibraryWrapper">
                <tr>
                    <td class="listContent">
                        <bean:write name="library" property="libaryFileName"/>
                    </td>
                </tr>
            </logic:iterate>
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
            <logic:iterate id="library" name="classpathForm" property="applicationLibraries"
                           type="org.zfin.framework.presentation.ClassLibraryWrapper">
                <tr>
                    <td class="listContent">
                        <bean:write name="library" property="libaryFileName"/>
                        <logic:equal value="false" name="library" property="libraryFileExists">
                            not found
                        </logic:equal>
                    </td>
<%--
                    <td class="listContent">
                        <bean:write name="library" property="version"/>
                    </td>
--%>

                </tr>
            </logic:iterate>
        </table>
    </td>
</tr>

<tr>
    <td align="center">
        <table cellpadding="2" cellspacing="1" border="0" width="95%">
            <tr>
                <td class="sectionTitle">WEB-INF Classes</td>
            </tr>
            <logic:iterate id="library" name="classpathForm" property="classesLibraries"
                           type="org.zfin.framework.presentation.ClassLibraryWrapper">
                <tr>
                    <td class="listContent">
                        <bean:write name="library" property="libaryFileName"/>
                    </td>
                </tr>
            </logic:iterate>
        </table>
    </td>
</tr>
</table>
</td>
</tr>
</table>

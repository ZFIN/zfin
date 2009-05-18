<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table cellpadding="2" cellspacing="1" border="0" width="80%">

    <tr><td colspan="3" class="sectionTitle">ZFIN Application:</td></tr>
    <tr><td class="listContentBold">&nbsp;</td><td colspan="2" class="listContent">&nbsp;</td></tr>

    <tr>
        <td valign=top class="listContentBold">
            Database Info: </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/database-info">Database Info</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            JDBC Driver Info:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/jdbc-driver-info">JDBC Driver Info</a>
        </td>
    </tr>

    <tr>
        <td valign=top class="listContentBold">
            Java Properties: </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/java-properties">Java Properties</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Thread info:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/thread-info">Thread Info</a>
        </td>
    </tr>

    <tr>
        <td valign=top class="listContentBold">
            Zfin Properties: </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/zfin-properties">Zfin Properties</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Log4J Configuration:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/log4j-configuration">Edit Log4J</a>
        </td>
    </tr>

    <tr>
        <td valign=top class="listContentBold">
            Classpath Info:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/classpath-info">View Classpath Info</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Tiles Configuration:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/tiles-configuration">Tiles Configuration</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Web Configuration:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/file-content?fileName=/WEB-INF/web.xml">web.xml</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Acegi Security Configuration:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/file-content?fileName=/WEB-INF/conf/applicationContext-acegi-security.xml">Acegi Security</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Test Browser:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/test-browser">Check HTTP header</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Individual Session Information:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/view-session-info">Session Info</a>
        </td>
    </tr>
<%--
ToDo: enable once we have the session table back in place.
      For now it is removed to keep the db changes small
    <tr>
        <td valign=top class="listContentBold">
            Global Session Information:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/view-global-session-info">Global Session Info</a>
        </td>
    </tr>
--%>
    <tr>
        <td valign=top class="listContentBold">
            Hibernate Information:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/hibernate-info">Hibernate Info</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Hibernate Statistics:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/view-hibernate-statistics">Hibernate Statistics (Global)</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            ZFIN Servlet Context Info:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/servlet-context">Servlet Context Info</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            Spring Application Context:
        </td>
        <td class="listContent">
            <a href="/action/dev-tools/application-context">Application Context Info</a>
        </td>
        <td class="listContent">
            This provides information for the web application context.
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            User Request Tracking:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/view-user-request-tracks">User Request Tracking</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            ZFIN Statistics:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/zfin-statistics">ZFIN Statistics</a>
        </td>
    </tr>
    <tr>
        <td valign=top class="listContentBold">
            GWT modules:
        </td>
        <td colspan="2" class="listContent">
            <a href="/action/dev-tools/gwt/modules">GWT Modules</a>
        </td>
    </tr>
</table>

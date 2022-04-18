<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="title" rtexprvalue="true" required="false" type="java.lang.String" %>

<z:page title="${title}" bootstrap="true">

        <nav class="navbar navbar-light admin text-center border-bottom pb-0 pt-3">
            <a class="col-sm" href="/">Home</a>
            <a class="col-sm" href="/action/devtool/home">Dev Tools</a>
            <a class="col-sm" href="/action/devtool/log4j-configuration">Log4J</a>
            <a class="col-sm" href="/action/logout">Logout</a>
        </nav>

    <jsp:doBody />

</z:page>
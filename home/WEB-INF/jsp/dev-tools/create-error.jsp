<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Create Error Page">
    <ul>
        <li><a href="/action/dev-tools/page-is-not-there">Page not found1</a>
        <li><a href="basdfasdf">Page not found2</a>
        <li><a href="/action/notthere">Page not found3</a>
        <li><a href="/action/dev-tools/test-error-page">Runtime Exception</a>
    </ul>

    <form:form>
        <input type="submit"/>
    </form:form>
</z:devtoolsPage>

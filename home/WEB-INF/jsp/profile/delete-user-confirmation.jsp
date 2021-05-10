<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div>
    <h1 align=center>CONFIRMATION</h1>
    <big>You have successfully deleted the ZFIN registration of
        <b>${formBean.person.name}</b>.
        The changes you have specified are effective immediately.</big>

    <p/>
    Note that this doesn't mean you've deleted the person's ZFIN record,
    just their ability to log in as a registered user.

    <p/>

    <form:form>
        <a href="javascript:" onClick="window.location.href='/action/profile/view/${formBean.zdbID}'">[View Person]</a>
    </form:form>
</div>


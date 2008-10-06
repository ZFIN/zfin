<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="allcontent">
    <h1 align=center>CONFIRMATION</h1>
    <big>You have successfully deleted the ZFIN registration of
        <b>${formBean.user.name}</b>.
        The changes you have specified are effective immediately.</big>

    <p/>
    Note that this doesn't mean you've deleted the person's ZFIN record,
    just their ability to log in as a registered user.

    <p/>

    <form:form>
        <input type=button name="done"
               value="Back to viewing PERSON record"
               onClick="window.location.href='/<%=ZfinProperties.getWebDriver()%>?MIval=aa-persview.apg&OID=${formBean.user.zdbID}'">
    </form:form>
</div>


<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="allcontent" >
      <h1 align=center>CONFIRMATION</h1>
      <big>Your update was successfully completed. The changes you have
      specified are effective immediately.</big>
      <p>
      <form:form>

          <input type=button name="done"
		 value="Back to viewing PERSON record"
		 onClick="window.location.href='/<%=ZfinProperties.getWebDriver()%>?MIval=aa-persview.apg&OID=${formBean.person.zdbID}'">

      </form:form>
      <script>document.success_form.done.focus() </script>
</div> 
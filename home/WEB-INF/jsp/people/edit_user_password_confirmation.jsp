<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="allcontent" >
      <h1 align=center>CONFIRMATION</h1>
      <big>Your update was successfully completed. The changes you have
      specified are effective immediately.</big>
      <p>
      <form:form>

        <a href="javascript:" onClick="window.location.href='/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-persview.apg&OID=${formBean.zdbID}'">[View Person]</a>

      </form:form>
      <script>document.success_form.done.focus() </script>
</div> 

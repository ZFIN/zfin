<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.feature.presentation.LineDesignationBean" %>

<SCRIPT type="text/javascript">
function checkPrefix(lineDesigVal) {
            if (lineDesigVal == '') {

               window.alert("Line Designation cannot be blank");
                return;

        }
}
    function checkLocation(locationVal) {
            if (locationVal == '') {

               window.alert("Location cannot be blank");
                return;

        }
}
</SCRIPT>
 <form:form  modelAttribute="formBean" method="post">
<authz:authorize ifAnyGranted="root">

<p><b>Enter new line designation:</b> <br>
    <br>
    <label>Line Designation</label>

   <form:input path="lineDesig" size="3" onblur="checkPrefix(this.value);"></form:input>
   <form:errors path="lineDesig" cssClass="error indented-error"/>

    &nbsp;&nbsp;
    <label>Location</label>


  <form:input path="lineLocation" size="50" onblur="checkLocation(this.value);"></form:input>
    <form:errors path="lineLocation" cssClass="error indented-error"/>

    <p>
    <input type=submit name=s_new value="Submit new Line Designation">

</p>
 </authz:authorize>
      </form:form>
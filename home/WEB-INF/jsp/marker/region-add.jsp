<%@ page import="org.zfin.marker.presentation.RegionAddBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.RegionAddBean" scope="request"/> 

<html>

<h1>Describe new Engineered Region</h1>

<form:form action="region-do-submit" modelAttribute="formBean" method="post">
    <div>
       <form:label path="${RegionAddBean.NEW_REGION_NAME}" class="curation-form-label">Engineered Region name:</form:label>
       <form:input path="${RegionAddBean.NEW_REGION_NAME}" size="80"
                onkeypress="return noenter(event)"></form:input>
       <form:errors path="${RegionAddBean.NEW_REGION_NAME}" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
       <form:label path="${RegionAddBean.NEW_REGION_NAME}" class="curation-form-label">Engineered Region alias:</form:label>
       <form:input onkeypress="return noenter(event)" path="${RegionAddBean.NEW_REGION_ALIAS}" size="50"></form:input>
       <form:errors path="${RegionAddBean.NEW_REGION_ALIAS}" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
       <form:label path="${RegionAddBean.NEW_REGION_NAME}" class="curation-form-label">Note:</form:label>
    </div>
    <div>
       <form:textarea path="${RegionAddBean.NEW_REGION_COMMENT}" rows="5" cols="50" />
       <form:errors path="${RegionAddBean.NEW_REGION_COMMENT}" cssClass="error indented-error"/>
    </div>
    <p>
    <div>
       <form:label path="${RegionAddBean.NEW_REGION_NAME}" class="curation-form-label">Curator Note:</form:label>
    </div>
    <div>
      <form:textarea path="${RegionAddBean.NEW_REGION_CURNOTE}" rows="5" cols="60" />
      <form:errors path="${RegionAddBean.NEW_REGION_CURNOTE}" cssClass="error indented-error"/>
    </div>
    <p/>
    <div>
      <form:label path="${RegionAddBean.NEW_REGION_NAME}" class="curation-form-label">Publication:</form:label>
      <form:input path="${RegionAddBean.REGION_PUBLICATION_ZDB_ID}" size="25"
                        onkeypress="return noenter(event)" value="${formBean.regionPublicationZdbID}"></form:input>
      <form:errors path="${RegionAddBean.REGION_PUBLICATION_ZDB_ID}" cssClass="error indented-error"/>
    </div>
    <p/>
      <input type=submit name=s_new value="Submit new Engineered region">

</form:form>

</html>

<script type="text/javascript">

    function noenter(e) {
        var ENTER_KEY = 13;
        var code = "";

        if (window.event) // IE
        {
            code = e.keyCode;
        }
        else if (e.which) // Netscape/Firefox/Opera
        {
            code = e.which;
        }

        if (code == ENTER_KEY) {
            return false;
        }
    }
</script>


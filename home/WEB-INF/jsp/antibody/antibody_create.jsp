<%@ page import="org.zfin.antibody.presentation.CreateAntibodyFormBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<form:form action="/action/antibody/create" method="post">
              <label for="antibodyName" class="indented-label">Antibody name:</label>
              <form:input path="<%= CreateAntibodyFormBean.NEW_AB_NAME%>" size="25"></form:input>
              <form:errors path="<%= CreateAntibodyFormBean.NEW_AB_NAME%>" cssClass="error indented-error"/>
               <p>
              <label for="antibodyPublicationZdbID" class="indented-label">Publication:</label>
              <form:input path="<%= CreateAntibodyFormBean.AB_PUBLICATION_ZDB_ID%>" size="25"></form:input>
              <form:errors path="<%= CreateAntibodyFormBean.AB_PUBLICATION_ZDB_ID%>" cssClass="error indented-error"/>
                <p>
               <input type=submit name=s_new value="Submit new Antibody">
                    </p>
    



     <!-- ends specifying params -->

    <!-- if marker values submitted -->


   <!-- ends Authorized -->

    </form:form>


</html>
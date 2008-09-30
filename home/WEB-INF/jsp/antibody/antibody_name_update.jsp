
<%@ page import="org.zfin.antibody.presentation.UpdateAntibodyFormBean" %>
<%@ page import="org.zfin.antibody.presentation.CreateAntibodyFormBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<form:form action="updateName?antibody.zdbID=${formBean.antibody.zdbID}">
<p>
<center><font size=6>
      Update <u>Antibody Name</u> for Antibody: ${formBean.antibody.name}

      </font></center>

<table width=100% cellspacing=0>
    <tr>
        <td width="15%">
            <FONT size=+1><b>
               <label for="antibodyNewName" class="indented-label">Antibody name:</label>
              <form:input path="<%= UpdateAntibodyFormBean.NEW_AB_NAME%>" size="25"></form:input>
               <form:errors path="<%= UpdateAntibodyFormBean.NEW_AB_NAME%>" cssClass="error indented-error"/>
         </b></FONT>
        </td>
    </tr>
        <tr>
        <td width="15%">
            <FONT size=+1><b>
            <label for="antibodyRenamePubZdbID" class="indented-label">Publication:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
              <form:input path="<%= UpdateAntibodyFormBean.AB_RENAMEPUB_ZDB_ID%>" size="25"></form:input>
              <form:errors path="<%= UpdateAntibodyFormBean.AB_RENAMEPUB_ZDB_ID%>" cssClass="error indented-error"/>
                </b></FONT>
                      </td>
    </tr>
    <tr>
        <td>
            <FONT size=+1><b>
                <label for="antibodyRenameComments" class="indented-label">Comments:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
                 <form:textarea path="antibodyRenameComments" id="antibodyRenameComments" cols="50" rows="6"></form:textarea>
            </b></FONT>
        </td>
            </tr>
    <tr>
        <td>
            <form:checkbox path="createAlias"></form:checkbox>
        <label for="createAlias">Create Alias?: </label>
        </td>
    </tr>
        <tr>
        <td width="65%">
                <input type=submit name=s_new value="Submit">
                <input type=button value="Cancel" onClick="window.history.go(-1)">
         </td>
    </tr>
</table>
</form:form>

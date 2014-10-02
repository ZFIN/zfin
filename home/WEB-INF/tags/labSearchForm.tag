<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="searchBean" type="org.zfin.profile.presentation.LabSearchBean" %>


<div class="search-form-top-bar">
    <div class="search-form-title" style="display: inline-block;">
        Search for Labs
        <a href="/action/profile/help/international-characters" class="popup-link help-popup-link"></a>
    </div>


    <div class="search-form-your-input-welcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Lab Search"/>
        </tiles:insertTemplate>
    </div>
</div>


<%--style="border: 2px solid gray; padding:  10px;  background-color: #FEF7D6; width: 70%;"--%>
<form:form name="formBean" commandName="<%=LookupStrings.FORM_BEAN%>"
           method="get" action="/action/profile/lab/search/execute"
        >

    <table width="100%" class="searchform">
        <tr>
            <td>

                <table width=10%>

                    <TR>
                        <TD class='fullwidth'>
                            <b>Name</b>
                        </TD>
                        <Td>
                            <form:input path="name" size="30" cssClass="default-input"/>
                            <form:hidden path="maxDisplayRecords"/>
                        </Td>
                    </tr>
                    <tr class="optionalSearch">
                        <TD>
                            <b>Address</b>
                        </TD>
                        <td>
                            <form:input path="address" size="30"/>
                        </td>
                    </TR>
                    <tr class="optionalSearch">
                        <TD>
                                <form:select path="containsType">
                                    <form:option value="bio">Research Interests</form:option>
                                    <form:option value="email">Email Address</form:option>
                                    <form:option value="url">URL</form:option>
                                    <form:option value="phone">phone</form:option>
                                    <form:option value="fax">fax</form:option>
                                    <form:option value="zdb_id">ZFIN record ID</form:option>
                                </form:select>
                                    <%--contains--%>
                                    <%--<input type=text name=anon1text size=15 >--%>
                        </TD>
                        <td>
                            <form:input path="contains" size="30"/>
                        </td>
                    </TR>
                </TABLE>

                <div class="search-form-bottom-bar" style="text-align:left;">
                    <input type=submit name=action value="Search"/>
                    <input value="Reset" type="button" onclick="
                            document.getElementById('name').value = '' ;
                            document.getElementById('address').value= '' ;
                            document.getElementById('contains').value = '' ;
                            document.getElementById('containsType').value = 'bio' ;
                            return true ;
                    "/>
                </div>
            </td>
        </tr>
    </table>
</form:form>


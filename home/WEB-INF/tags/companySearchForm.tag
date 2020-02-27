<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="searchBean" type="org.zfin.profile.presentation.CompanySearchBean" %>

<div class="search-form-top-bar">
    <div class="search-form-title" style="display: inline-block;">
        Search for Companies
        <a href="/action/profile/help/international-characters" class="popup-link help-popup-link"></a>
        &nbsp;
        &nbsp;
        &nbsp;
        <small>
            <a href="/action/profile/company/search/disclaimer" class="popup-link info-popup-link">
                Disclaimer
            </a>
        </small>
    </div>
</div>

<%--<span class="header">Company Search</span>--%>
<form:form name="formBean" commandName="<%=LookupStrings.FORM_BEAN%>"
           method="get" action="/action/profile/company/search/execute"
        >

    <table width="100%" class="searchform">
        <tr>
            <td>
                <table width="10%">

                    <TR>
                        <TD class='fullwidth'>
                            <b>Name</b>
                        </td>
                        <td>
                            <form:input path="name" size="30" cssClass="default-input"/>
                            <form:hidden path="maxDisplayRecords"/>
                        </TD>
                    </tr>
                    <tr class="optionalSearch">
                        <TD>
                            <b>Address</b>
                                <%--<input type=text name=address size=30  />--%>
                        </td>
                        <td>
                            <form:input path="address" size="30"/>
                        </TD>
                    </tr>
                    <tr>
                        <td>
                            <b>
                                <form:select path="containsType">
                                    <form:option value="bio">Products/Services</form:option>
                                    <form:option value="email">Email Address</form:option>
                                    <form:option value="url">URL</form:option>
                                    <form:option value="phone">phone</form:option>
                                    <form:option value="fax">fax</form:option>
                                    <form:option value="zdb_id">ZFIN record ID</form:option>
                                </form:select>
                            </b>
                        </td>
                        <td>
                            <form:input path="contains" size="30"/>
                        </td>
                    </tr>
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


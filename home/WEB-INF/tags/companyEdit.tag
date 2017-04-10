<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="company" type="org.zfin.profile.Company" required="true" %>
<%--list of PersonMemberPresentation --%>
<%@ attribute name="members" type="java.util.Collection" required="true" %>
<%--list of string --%>
<%@ attribute name="positions" type="java.util.Collection" required="true" %>
<%--list of String--%>
<%@ attribute name="prefixes" type="java.util.Collection" required="true" %>


<script src="/javascript/profile-edit.js"></script>
<link rel=stylesheet type="text/css" href="/css/tabEdit.css">


<div id='companyEdit'>
    <ul class="tabs">
        <li>
            <a id="information-tab" href="#information" ${(empty company.contactPerson && !empty members) ? 'style="color: red;"' : '' }>Information</a>
        </li>
        <li>
            <a id="members-tab" href="#members" ${empty members ? 'style="color: red;"' : '' }>Members</a>
        </li>
        <li>
            <a href="#picture" ${empty company.snapshot ? 'style="color: red;"' : '' }>Picture</a>
        </li>
    </ul>


    <div class='panes'>
        <div id='information'>

            <form:form method="post" commandName="<%=LookupStrings.FORM_BEAN%>"
                       action="/action/profile/company/edit/${company.zdbID}" enctype="multipart/form-data"
                       cssClass="edit-box mark-dirty">
                <table>
                    <tr>
                        <td>
                            <form:label path="name">Name:</form:label>
                        </td>
                        <td>
                            <form:input  cssClass="information-first-field"  size="50" path="name"/>
                            <zfin2:errors errorResult="${errors}" path="name"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="phone">Phone:</form:label>
                        </td>
                        <td>
                            <form:input path="phone"/>
                            <zfin2:errors errorResult="${errors}" path="phone"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="fax">Fax:</form:label>
                        </td>
                        <td>
                            <form:input path="fax"/>
                            <zfin2:errors errorResult="${errors}" path="fax"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="email">Email:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="email"/>
                            <zfin2:errors errorResult="${errors}" path="email"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="url">URL:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="url"/>
                            <zfin2:errors errorResult="${errors}" path="url"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label cssStyle="vertical-align: top;" path="address">Address:</form:label>
                        </td>
                        <td>
                            <form:textarea path="address" rows="5" cols="80"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="contactPerson">Contact Person:</form:label>
                        </td>
                        <td>
                            <form:select path="contactPerson.zdbID" id="contact-person">
                                <form:option value="none">-- Select Contact Person --</form:option>
                                <form:options items="${members}" itemLabel="name" itemValue="zdbID" />
                            </form:select>
                            ${(empty company.contactPerson.zdbID && !empty members) ? '<span style="color: red;">Please provide a contact person</span>' : '' }
                        </td>
                    </tr>
                    <authz:authorize access="hasRole('root')">
                    <tr>
                        <td>
                            <form:label path="prefix">Line Designation:</form:label>
                        </td>
                        <td>
                            <form:select path="prefix" items="${prefixes}" />
                        </td>
                    </tr>
                    </authz:authorize>
                </table>

                <authz:authorize access="!hasAnyRole('root')">
                    <form:hidden path="prefix"/>
                </authz:authorize>

                <form:label path="bio">Products and Services</form:label>
                <br>
                <form:textarea cols="80" rows="10" htmlEscape="false" path="bio"/>
                <zfin2:errors errorResult="${errors}" path="personalBio"/>
                <br/>

                <input type="submit" value="Save"/>
                <zfin:link entity="${company}">Cancel</zfin:link>

            </form:form>

        </div>

        <div style="align: right; display: inline-block;" id='members'>


            <%--this is just a dummy form--%>
            <form class="edit-box" onsubmit="return false;">
                ${empty members ? '<div class="no-member-error" style="color: red;">Please add at least one person to this company.</div>' : '' }

                <input class="members-first-field" id="addMemberBox" type="text"/>

                <select id="addMemberPosition"
                        onchange=" personToAddPosition = this.options[this.selectedIndex].value; "
                        >
                    <option value='none'>-- Select Position --</option>
                    <c:forEach var="position" items="${positions}">
                        <option value="${position.id}">${position.name}</option>
                    </c:forEach>
                </select>
                <input id="addMemberButton" value="Add Member" type="button"
                       onclick="addMember( personToAddZdbID,'${company.zdbID}' , personToAddPosition, jQuery('#addMemberBox').val()); "/>
                <br>
                <div class="error" id="add-member-error" style="display: none;"></div>
                <div id=memberList></div>

<%--
                <input type="button" value="Refresh" onclick="
                        jQuery('#memberList').html('') ;
                        listMembers('${company.zdbID}');
                        ">
--%>

              <authz:authorize access="hasRole('root')">
                 <div style="text-align: right"><a href="/action/profile/person/create?organization=${company.zdbID}">add new person</a></div>
              </authz:authorize>
            </form>
            <form:form>

                <label>Change Member Position: </label>
                <select id="change-position-members">
                    <option value="none">-- Select Member --</option>
                </select>
                <select id="change-position-positions">
                    <option value='none' selected="true">-- Select Position --</option>
                    <c:forEach var="position" items="${positions}">
                        <option value="${position.id}">${position.name}</option>
                    </c:forEach>
                </select>
                <input type="button" value="Change Position" onclick="changePosition(jQuery('#change-position-members option:selected').val(),
                                                '${company.zdbID}',
                                                jQuery('#change-position-positions option:selected').val()); "/>

            </form:form>


        </div>


        <div id='picture'>
            <zfin2:editSnapshot value='${company}'/>
        </div>

    </div>

</div>

<script>
    $(document).ready(function () {

        $('#addMemberBox').autocompletify('/action/profile/find-member?term=%QUERY');
        $('#addMemberBox').bind('typeahead:select', function(obj, datum, name) {
            personToAddZdbID = datum.id;
        });

        listMembers('${company.zdbID}');
    });
    $('.tabs a').tabbify('.panes > div');

</script>



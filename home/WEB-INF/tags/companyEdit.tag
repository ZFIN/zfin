<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="company" type="org.zfin.profile.Company" required="true" %>
<%--list of PersonMemberPresentation --%>
<%@ attribute name="members" type="java.util.Collection" required="true" %>
<%--list of string --%>
<%@ attribute name="positions" type="java.util.Collection" required="true" %>
<%--list of String--%>
<%@ attribute name="prefixes" type="java.util.Collection" required="true" %>


<script src="/javascript/jquery-ui-1.8.16.custom.min.js"></script>
<link rel=stylesheet type="text/css" href="/css/jquery-ui-1.8.16.custom.css">

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
                       cssClass="edit-box mark-dirty"
                    >
                <form:label path="name">Name:</form:label>
                <form:input  cssClass="information-first-field"  size="50" path="name"/>
                <zfin2:errors errorResult="${errors}" path="name"/>

                <br>
                <form:label path="phone">Phone:</form:label>
                <form:input path="phone"/>
                <zfin2:errors errorResult="${errors}" path="phone"/>
                <br>
                <form:label path="fax">Fax:</form:label>
                <form:input path="fax"/>
                <zfin2:errors errorResult="${errors}" path="fax"/>
                <br>
                <form:label path="email">Email:</form:label>
                <form:input size="50" path="email"/>
                <zfin2:errors errorResult="${errors}" path="email"/>
                <br>
                <form:label path="url">URL:</form:label>
                <form:input size="50" path="url"/>
                <zfin2:errors errorResult="${errors}" path="url"/>
                <br>
                <form:label cssStyle="vertical-align: top;" path="address">Address:</form:label>
                <form:textarea path="address" rows="5" cols="80"/>
                <br>
                <br>


                <%-- contact person--%>
                <form:label path="contactPerson">Contact Person:</form:label>
                <form:select path="contactPerson.zdbID" id="contact-person">
                    <form:option value="none">-- Select Contact Person --</form:option>
                    <form:options items="${members}" itemLabel="name" itemValue="zdbID"></form:options>
                </form:select>
                ${(empty company.contactPerson.zdbID && !empty members) ? '<span style="color: red;">Please provide a contact person</span>' : '' }

                <%--/ line designation--%>
                <authz:authorize ifAnyGranted="root">
                    <br/>
                    <form:label path="prefix">Line Designation:</form:label>
                    <form:select path="prefix" items="${prefixes}"
                            >
                    </form:select>
                    <br/>
                </authz:authorize>
                <authz:authorize ifNotGranted="root">
                    <form:hidden path="prefix"/>
                </authz:authorize>

                <br/>
                <br/>
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
            <form class="edit-box">
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
                       onclick="addMember( personToAddZdbID,'${company.zdbID}' , personToAddPosition); "/>
                <br>

                <div id=memberList></div>

<%--
                <input type="button" value="Refresh" onclick="
                        jQuery('#memberList').html('') ;
                        listMembers('${company.zdbID}');
                        ">
--%>

              <authz:authorize ifAnyGranted="root">
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
    jQuery(document).ready(function() {
        jQuery('#addMemberBox').autocomplete({
            source:  '/action/profile/find-member'
            ,minLength:  3
            ,select: function(event, ui) {
                personToAddZdbID = ui.item.id;
            }
        });

// on load
        listMembers('${company.zdbID}');
//            var tabs = jQuery('#companyEdit').tabs('div.panes > div');
    });
    jQuery('ul.tabs').tabs('div.panes > div');

    <c:if test="${not empty selectedTab}">
    var api = jQuery("ul.tabs").data("tabs");

    switch('${selectedTab}'){
        case 'information':
            api.click(0);
            jQuery('.information-first-field').focus();
            break;
        case 'members':
            api.click(1);
            jQuery('.members-first-field').focus();
            break;
        case 'picture':
            api.click(2);
            break;
        default:
            console.log('unknown tab selected has error on it') ;
    }
    </c:if>


</script>



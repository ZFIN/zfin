<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="lab" type="org.zfin.profile.Lab" required="true" %>
<%--list of PersonMemberPresentation --%>
<%@ attribute name="members" type="java.util.Collection" required="true" %>
<%--list of string --%>
<%@ attribute name="positions" type="java.util.Collection" required="true" %>
<%--list of String--%>
<%@ attribute name="prefixes" type="java.util.Collection" required="true" %>

<script src="/javascript/jquery-ui-1.8.16.custom.min.js"></script>
<link rel=stylesheet type="text/css" href="/css/jquery-ui-1.8.16.custom.css">

<script src="/javascript/profile-edit.js"></script>

<%--<script src="http://cdn.jquerytools.org/1.2.6/full/jquery.tools.min.js"></script>--%>
<link rel=stylesheet type="text/css" href="/css/tabEdit.css">


<div id='labEdit'>
    <ul class="tabs">
        <li>
            <a id="information-tab" href="#information" ${(empty lab.contactPerson && !empty members) ? 'style="color: red;"' : '' }>Information</a>
        </li>
        <li>
            <a id="members-tab" href="#members" ${empty members ? 'style="color: red;"' : '' }>Members</a>
        </li>
        <li>
            <a href="#picture" ${empty lab.snapshot ? 'style="color: red;"' : '' }>Picture</a>
        </li>
    </ul>

    <div class='panes'>
        <div id='information'>
            <form:form method="post" commandName="<%=LookupStrings.FORM_BEAN%>"
                       action="/action/profile/lab/edit/${lab.zdbID}" enctype="multipart/form-data"
                       cssClass="edit-box mark-dirty"
                    >
                <form:label cssClass="information-first-field" path="name">Name:</form:label>
                <form:input size="50" path="name"/>
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
                <form:label  cssStyle="vertical-align: top;" path="address">Address:</form:label>
                <form:textarea path="address" rows="5" cols="80"/>
                <br>

                <%-- contact person--%>
                <form:label path="contactPerson">Contact Person:</form:label>
                <form:select path="contactPerson.zdbID" id="contact-person">
                    <form:option value="none">-- Select Contact Person --</form:option>
                    <form:options items="${members}" itemLabel="name" itemValue="zdbID"></form:options>
                </form:select>
                <c:if test="${(empty lab.contactPerson) && (!empty members)}">
                  <div style="color: red;">Please provide a contact person</div>
                </c:if>

                ${(empty lab.contactPerson.zdbID && !empty members) ? '<span style="color: red;">Please provide a contact person</span>' : '' }



                <%--/ line designation--%>
                <authz:authorize ifAnyGranted="root">
                    <br/>
                    <form:label path="prefix">Line Designation:</form:label>
                    <form:select path="prefix" items="${prefixes}"
                            >
                    </form:select>
                </authz:authorize>

                <authz:authorize ifNotGranted="root">
                    <form:hidden path="prefix"/>
                </authz:authorize>

                <br/>
                <br>
                <form:label path="bio">Statement of Research Interests</form:label>
                <br/>
                <form:textarea cols="80" rows="10" htmlEscape="false" path="bio"/>
                <zfin2:errors errorResult="${errors}" path="bio"/>
                <br/>
                <br/>

                <input type="submit" value="Save"/>
                <zfin:link entity="${lab}">Cancel</zfin:link>

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
                       onclick="addMember( personToAddZdbID,'${lab.zdbID}' , personToAddPosition, jQuery('#addMemberBox').val()); "/>
                <br>
                <div class="error" id="add-member-error" style="display: none;"></div>
                <div id=memberList></div>

<%--
                <input type="button" value="Refresh" onclick="
                        jQuery('#memberList').html('') ;
                        listMembers('${lab.zdbID}');
                        ">
--%>
            <authz:authorize ifAnyGranted="root">
               <div style="text-align: right"><a href="/action/profile/person/create?organization=${lab.zdbID}">add new person</a></div>
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
                                                '${lab.zdbID}',
                                                jQuery('#change-position-positions option:selected').val()); "/>

            </form:form>
        </div>

        <div id='picture'>
            <zfin2:editSnapshot value='${lab}'/>
        </div>
    </div>

</div>

<script>
    jQuery(document).ready(function () {
        jQuery('#addMemberBox').autocomplete({
            source:'/action/profile/find-member', minLength:2, select:function (event, ui) {
                jQuery('#add-member-box').val(ui.item.label);
                personToAddZdbID = ui.item.id;
            }
        });

// on load
        listMembers('${lab.zdbID}');
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


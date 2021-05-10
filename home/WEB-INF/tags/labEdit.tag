<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="lab" type="org.zfin.profile.Lab" required="true" %>
<%--list of PersonMemberPresentation --%>
<%@ attribute name="members" type="java.util.Collection" required="true" %>
<%--list of string --%>
<%@ attribute name="positions" type="java.util.Collection" required="true" %>
<%--list of String--%>
<%@ attribute name="prefixes" type="java.util.Collection" required="true" %>

<div id='labEdit'>
    <ul class="tabs">
        <li>
            <a id="information-tab" href="#information" ${(empty lab.contactPerson && !empty members) ? 'style="color: red;"' : '' }>Information</a>
        </li>
        <li>
            <a id="members-tab" href="#members" ${empty members ? 'style="color: red;"' : '' }>Members</a>
        </li>
        <li>
            <a href="#picture" ${empty lab.image ? 'style="color: red;"' : '' }>Picture</a>
        </li>
    </ul>

    <div class='panes'>
        <div id='information'>
            <form:form method="post" commandName="${LookupStrings.FORM_BEAN}"
                       action="/action/profile/lab/edit/${lab.zdbID}" enctype="multipart/form-data"
                       cssClass="edit-box mark-dirty">
                <table>
                    <tr>
                        <td>
                            <form:label cssClass="information-first-field" path="name">Name:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="name"/>
                            <form:errors path="name" cssClass="error-inline"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="phone">Phone:</form:label>
                        </td>
                        <td>
                            <form:input path="phone"/>
                            <form:errors path="phone" cssClass="error-inline"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="fax">Fax:</form:label>
                        </td>
                        <td>
                            <form:input path="fax"/>
                            <form:errors path="fax" cssClass="error-inline"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="email">Email:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="email"/>
                            <form:errors errorResult="${errors}" path="email" cssClass="error-inline"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="url">URL:</form:label>
                        </td>
                        <td>
                            <form:input size="50" path="url"/>
                            <form:errors errorResult="${errors}" path="url" cssClass="error-inline"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label  cssStyle="vertical-align: top;" path="address">Address:</form:label>
                        </td>
                        <td>
                            <form:textarea path="address" rows="5" cols="80"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="country">Country:</form:label>
                        </td>
                        <td>
                            <form:select path="country">
                                <form:option value="" />
                                <form:options items="${countryList}" />
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form:label path="contactPerson">Contact Person:</form:label>
                        </td>
                        <td>
                            <form:select path="contactPerson.zdbID" id="contact-person">
                                <form:option value="none">-- Select Contact Person --</form:option>
                                <form:options items="${members}" itemLabel="name" itemValue="zdbID"></form:options>
                            </form:select>
                            <c:if test="${(empty lab.contactPerson) && (!empty members)}">
                                <div style="color: red;">Please provide a contact person</div>
                            </c:if>
                            ${(empty lab.contactPerson.zdbID && !empty members) ? '<span style="color: red;">Please provide a contact person</span>' : '' }
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

                <form:label path="bio">Statement of Research Interests</form:label>
                <br/>
                <form:textarea cols="80" rows="10" htmlEscape="false" path="bio"/>
                <form:errors path="bio" cssClass="error-inline"/>
                <br/>

                <input type="submit" value="Save"/>
                <zfin:link entity="${lab}">Cancel</zfin:link>

            </form:form>
        </div>

        <div style="align: right; display: inline-block;" id='members'>


            <%--this is just a dummy form--%>
            <form class="edit-box" onsubmit="return false;">
                ${empty members ? '<div class="no-member-error" style="color: red;">Please add at least one person to this company.</div>' : '' }

                <input type="hidden" id="orgZdbID" value="${lab.zdbID}"/>
                <input class="members-first-field" id="addMemberBox" type="text"/>
                <select id="addMemberPosition">
                    <option value='none'>-- Select Position --</option>
                    <c:forEach var="position" items="${positions}">
                        <option value="${position.id}">${position.name}</option>
                    </c:forEach>
                </select>
                <input id="addMemberButton" value="Add Member" type="button" />
                <br>
                <div class="error" id="add-member-error" style="display: none;"></div>
                <div id=memberList></div>

            <authz:authorize access="hasRole('root')">
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
                <input id="change-position-button" type="button" value="Change Position" />

            </form:form>
        </div>

        <div id='picture'>
            <zfin2:editProfileImage value='${lab}'/>
        </div>
    </div>

</div>

<script>
    $('.tabs a').tabbify('.panes > div');
</script>


<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <authz:authorize access="hasRole('root')">

        <h3>Convert Marker Type</h3>

        <form:form method="POST" action="/action/marker/convert-type"
                   modelAttribute="formBean"
                   onsubmit="return confirm('Convert ${formBean.marker.abbreviation} from ${formBean.marker.markerType.displayName} to the selected type? This will change the ZDB ID.');">
            <form:hidden path="zdbIDToConvert"/>

            <table class="primary-entity-attributes">
                <tr>
                    <th>Marker</th>
                    <td><zfin:link entity="${formBean.marker}"/></td>
                </tr>
                <tr>
                    <th>Current Type</th>
                    <td>${formBean.marker.markerType.displayName}</td>
                </tr>
                <tr>
                    <th>New Type</th>
                    <td>
                        <form:select path="newMarkerTypeName">
                            <form:option value="" label="-- Select Type --"/>
                            <form:options items="${formBean.availableTypes}"
                                          itemValue="name" itemLabel="displayName"/>
                        </form:select>
                    </td>
                </tr>
            </table>

            <form:errors path="*" cssClass="error"/><br>

            <input type="submit" value="Convert"/>
        </form:form>

    </authz:authorize>
</z:page>

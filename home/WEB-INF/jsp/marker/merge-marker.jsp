<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>

<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>
<script type="text/javascript">
    var LookupProperties0 = {
        divName: "antibodyLookup",
        inputName: "markerToMergeIntoViewString",
        showError: true,
        type: "ANTIBODY_LOOKUP",
	useTermTable: false,
        wildcard: false
    };
    
    function confirmMerge(){
        var markerToMergeInto = document.getElementById('markerToMergeIntoViewString').value ;
        if(confirm('Merge and delete ${formBean.markerToDeleteViewString} into '+markerToMergeInto+'?')){
            return true ; 
        }
        else{
            return false ;
        }
    }
</script>


<form:form commandName="formBean" action="/action/marker/merge" onsubmit="return confirmMerge();" >
    <form:hidden path="zdbIDToDelete"/>
    <form:errors path="*" cssClass="error"/><br>

    <table>
        <tr>
            <td valign="top">
                Merge <a target="_blank" class="external"
                         href="/action/marker/view/${formBean.zdbIDToDelete}">
                    ${formBean.markerToDeleteViewString}</a>
                    <%--<a target="_blank"  class="external"--%>
                    <%--href="/action/marker/marker-edit?zdbID=${formBean.zdbIDToDelete}">--%>
                    <%--[Edit]</a>--%>
                into
            </td>
            <td valign="top">
                <div id="antibodyLookup" style="display:inline;"></div>
            </td>

    </table>
    <br>
    <%--<input type="button" onclick="confirmMerge();" value="Merge"/>--%>
    <input type="submit" value="Merge"/>

</form:form>


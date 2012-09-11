<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script type="text/javascript" src="/javascript/jquery-1.4.4.js"></script>
<script type="text/javascript" src="/javascript/jquery.jeditable.mini.js"></script>

<script type="text/javascript">
    jQuery.noConflict();
    jQuery(function(){
        jQuery(".editable_select").editable("/webservice/profile/saveLabPrefix/${lab.zdbID}", {
            indicator : '<img src="/images/ajax-loader.gif">',
            data: "{<c:forEach var="aPrefix" items="${prefixes}" varStatus="index">'${aPrefix}':'${aPrefix}'${!index.last ? ",":""}</c:forEach> }",
            type   : "select",
            name: "prefix",
            submit : "Save",
            cancel: "Cancel",
            tooltip   : "Click to edit...",
            style  : "inherit"
        });
    });
</script>

${lab.name}
<br>
Phone: ${lab.phone}
<br>
Fax: ${lab.fax}
<br>
Email: ${lab.email}
<br>
Lab Prefix: <span id="prefixId "class="editable_select">${prefix}</span>

<br>
<input type="button" value="Create Prefix" id="createPrefix">
<br>

<%--<input type="text" id="add" name="add"/>--%>
<%--<input type="button" id="addButton" value="Add New Prefix"/>--%>
<%--<br>--%>

<%--<select id="select">--%>
<%--<c:forEach var="aPrefix" items="${prefixes}">--%>
<%--<option--%>
<%--${aPrefix eq prefix ? "selected": ""}--%>
<%-->${aPrefix}</option>--%>
<%--</c:forEach>--%>
<%--</select>--%>
<%--<input type="button" id="saveButton" value="Save Prefix"/>--%>




<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:errors errorResult="${errors}"/>

<script src="/javascript/jquery.dirtyFields.packed.js"></script>
<script src="/javascript/tabbify.js"></script>


<div align="center">
    <a href="/${formBean.zdbID}">[View]</a>
</div>

<div style="width: 98%; margin-left: 1%">

<c:choose>
    <c:when test="${fn:startsWith(formBean.zdbID,'ZDB-PERS')}">
        <zfin2:personEdit person="${formBean}" securityPersonZdbID="${securityPersonZdbID}" showDeceasedCheckBox="${showDeceasedCheckBox}"/>
    </c:when>
    <c:when test="${fn:startsWith(formBean.zdbID,'ZDB-COMPANY')}">
        <zfin2:companyEdit company="${formBean}" members="${members}"  prefixes="${prefixes}" positions="${positions}"/>
    </c:when>
    <c:when test="${fn:startsWith(formBean.zdbID,'ZDB-LAB')}">
        <zfin2:labEdit lab="${formBean}" members="${members}" prefixes="${prefixes}" positions="${positions}"/>
    </c:when>
</c:choose>
</div>


<script>
    var options= {
        denoteDirtyForm: true,
        denoteDirtyOptions: true,
        fieldChangeCallback: function(originalValue,isDirty) {
            if(isDirty) {
                $(this).addClass("dirty");
            }
            else {
                $(this).removeClass("dirty");
            }
        }
    };

    $('form.mark-dirty').dirtyFields(options);
</script>


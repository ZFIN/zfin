<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:dataManager zdbID="${experiment.zdbID}"/>

<div class="data-sub-page-title">Environment Description</div>

<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${experiment.zdbID}"/>
        <tiles:putAttribute name="subjectID" value="${experiment.zdbID}"/>
    </tiles:insertTemplate>
</div>


<table class="primary-entity-attributes">
    <tr>
        <th>Publication:</th>
        <td><zfin:link entity="${experiment.publication}"/> </td>
    </tr>
</table>

<zfin2:experimentConditions nonMorpholinoConditions="${nonMorpholinoConditions}"
                            morpholinoConditions="${morpholinoConditions}"/>


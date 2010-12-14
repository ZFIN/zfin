<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="lineDesignationBean" type="org.zfin.feature.presentation.LineDesignationBean" required="true" %>

<p><b>Allocated institution line designations:</b> <br>
    <br>
</p>

<script type="text/javascript">
    function showAlleles(num,prefix){
        alleleTableDiv = document.getElementById('alleleDesignationTable'+num);
        alleleTableDiv.show() ;
        alleleDiv = document.getElementById('alleleDesignation'+num);
        alleleDiv.show() ;
        new Ajax.Updater('alleleDesignation'+num, '/action/feature/alleles/'+prefix, {Method: 'get'});
        alleleShowButton = document.getElementById('showAlleleLink'+num);
        alleleShowButton.hide();
        alleleHideButton = document.getElementById('hideAlleleLink'+num);
        alleleHideButton.show();
    }

    function hideAlleles(num) {
        alleleTableDiv= document.getElementById('alleleDesignationTable'+num);
        alleleDiv.hide() ;
        alleleTableDiv = document.getElementById('alleleDesignationTable'+num);
        alleleTableDiv.hide() ;
        alleleShowButton = document.getElementById('showAlleleLink'+num);
        alleleShowButton.show();
        alleleHideButton = document.getElementById('hideAlleleLink'+num);
        alleleHideButton.hide();
    }
</script>


<table width="75%" align="center" border="1" cellspacing="1" cellpadding="1">
    <tr> <th align="left" bgcolor="cccccc"><strong>Designation</strong></th>
        <th align="left" bgcolor="cccccc"><strong>Location</strong></th>
        <th align="left" bgcolor="cccccc"><strong>Labs</strong></th>
    </tr>

    <c:forEach var="labFeaturePrefixRow" items="${lineDesignationBean.featurePrefixLightList}" varStatus="index">
        <tr>
            <td align="left" valign="top">
                &nbsp;
                <a id="showAlleleLink${index.index}" href="javascript:;" onclick="showAlleles(${index.index}, '${labFeaturePrefixRow.prefix}')
                "><img src="/images/plus-13.png" style="border:none;"></a>
                <a id="hideAlleleLink${index.index}" style="display: none;" href="javascript:;hideAlleles(${index.index})" onclick="hideAlleles(${index.index})
                "><img src="/images/minus-13.png"  style="border:none;"></a>
                &nbsp;
                <strong>${labFeaturePrefixRow.prefix}</strong>
            </td>
            <td>
                    ${labFeaturePrefixRow.instituteDisplay}
            </td>
            <td>
                <c:forEach var="lab" items="${labFeaturePrefixRow.labList}" varStatus="stat">
                    <a href="/<%=ZfinPropertiesEnum.CGI_BIN_DIR_NAME.value()%>/ZFIN_jump?record=${lab.zdbID}">${lab.name}</a>${!stat.last? "," : ""}
                </c:forEach>
            </td>
        </tr>
        <tr>
            <td id="alleleDesignationTable${index.index}" colspan="3" style="display:none;">
                <div id="alleleDesignation${index.index}"></div>
            </td>
        </tr>
    </c:forEach>


</table>


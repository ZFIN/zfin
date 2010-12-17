<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="lineDesignationBean" type="org.zfin.feature.presentation.LineDesignationBean" required="true" %>

<p><b>Allocated institution line designations:</b> <br>
    <br>
</p>

<?MIVAR>
<script type="text/javascript">
    function showAlleles(num,prefix){
        alleleRowDiv = document.getElementById('alleleDesignationRow'+num);
        alleleRowDiv.style.display = 'table-row' ;
        alleleDiv = document.getElementById('alleleDesignation'+num);
        alleleDiv.style.display = 'inline' ;
         new Ajax.Updater('alleleDesignation'+num, '/action/feature/alleles/'+prefix, {Method: 'get'});
        alleleShowButton = document.getElementById('showAlleleLink'+num);
        alleleShowButton.style.display = 'none' ;
        alleleHideButton = document.getElementById('hideAlleleLink'+num);
        alleleHideButton.style.display = 'inline' ;
    }

    function hideAlleles(num) {
        alleleRowDiv= document.getElementById('alleleDesignationRow'+num);
        alleleRowDiv.style.display = 'none' ;
        alleleDiv = document.getElementById('alleleDesignation'+num);
        alleleDiv.style.display = 'none' ;
        alleleShowButton = document.getElementById('showAlleleLink'+num);
        alleleShowButton.style.display = 'inline' ;
        alleleHideButton = document.getElementById('hideAlleleLink'+num);
        alleleHideButton.style.display = 'none' ;
    }
</script>
<?/MIVAR>


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
        <tr id="alleleDesignationRow${index.index}" style="display:none;" >
            <td colspan="3" width="100%">
                <div id="alleleDesignation${index.index}"></div>
            </td>
        </tr>
    </c:forEach>


</table>


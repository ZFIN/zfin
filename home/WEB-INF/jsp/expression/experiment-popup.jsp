<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
     Environment Description: <zfin:name entity="${experiment}"/>
</div>
<div class="popup-body">
    <div>
        <table class="primary-entity-attributes">
            <tr>
                <th>Publication:</th>
                <td><zfin:link entity="${experiment.publication}"/> </td>
            </tr>

        </table>


        <zfin2:experimentConditions nonMorpholinoConditions="${nonMorpholinoConditions}"
                                    morpholinoConditions="${morpholinoConditions}"/>


    </div>
</div>
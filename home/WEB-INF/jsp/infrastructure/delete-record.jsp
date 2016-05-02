<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="/javascript/jquery-ui-1.10.4.custom.js"></script>
<link rel=stylesheet type="text/css" href="/css/jquery-ui-1.10.4.custom.css">

<authz:authorize access="hasRole('root')">

    <c:set var="zdbIdString" value="${formBean.zdbIDToDelete}"/>

    <script type="text/javascript">

        function changeAction() {
            if (jQuery('#removeFromFeatureTracking').prop('checked')) {
                jQuery("#deleteForm").attr("action", "/action/infrastructure/record-deleted?zdbIDToDelete=${formBean.zdbIDToDelete}" + "&removeFromTracking=yes");
            } else {
                jQuery("#deleteForm").attr("action", "/action/infrastructure/record-deleted?zdbIDToDelete=${formBean.zdbIDToDelete}");
            }
        }


        jQuery(document).ready(function () {

            jQuery("#confirm-dialog").css({'display': 'none'});
            jQuery("#confirm-dialog").dialog({
                autoOpen: false,
                show: "fade",
                hide: "fade",
                modal: true,
                height: 300,
                width: 550,
                title: "Confirmation for deletion",
                buttons: {
                    OK: function () {
                        jQuery("#deleteForm").submit();
                    },
                    Cancel: function () {
                        jQuery("#confirm-dialog").dialog("close");
                    }
                }
            });

            jQuery("#triggerConfirm").click(function (e) {
                if (jQuery('#removeFromFeatureTracking').prop('checked')) {
                    jQuery("#confirm-dialog").html('<br/><br/><span style="font-weight:1500; font-size: large; color: red">I am absolutely sure I want to delete ${formBean.recordToDeleteViewString} and delete it from tracking as well.</span>');
                } else {
                    jQuery("#confirm-dialog").html('<br/><br/><span style="font-weight:1500; font-size: large; color: red">I am absolutely sure I want to delete ${formBean.recordToDeleteViewString}.</span>');
                }
                jQuery("#confirm-dialog").css({'display': 'inline-block'});
                jQuery("#confirm-dialog").dialog("open");
                //prevent the submit
                e.preventDefault();
            });

        });


    </script>

    <c:choose>
        <c:when test="${!empty validationReportList}">
            <div class="caution-text">
                <br/>Can not delete <zfin:link entity="${entity}"/> (${formBean.zdbIDToDelete})
            </div>
            <ul>
                <c:forEach var="error" items="${validationReportList}">
                    <li><span class="error">${error.validationMessage}</span></li>
                    <%-- This is a hack as the ExperimentPresentation class is abused to display STR info instaed
                    of experiment detail --%>
                    <c:choose>
                        <c:when test="${fn:startsWith(error.entityType, 'Experiment')}">
                            <c:forEach var="experiment" items="${error.entityCollection}">
                               <a href="/${experiment.zdbID}"> ${experiment.name}</a> <br/>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <zfin2:createExpandCollapse items="${error.entityCollection}" id="${error.entityType}" itemName="items"/>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </ul>
            <c:choose>
                <c:when test="${fn:startsWith(zdbIdString, 'ZDB-ATB-')}">
                    <a href="/action/marker/marker-edit?zdbID=${formBean.zdbIDToDelete}">[Edit this antibody]</a>
                </c:when>
            </c:choose>
        </c:when>
        <c:otherwise>
            <form:form id="deleteForm" commandName="formBean"
                       action="/action/infrastructure/record-deleted?zdbIDToDelete=${formBean.zdbIDToDelete}">
                <table>
                    <tr class="spaceUnder">
                        <td valign="top">
                            <span class="caution-head">CAUTION</span>
                        </td>
                    </tr>
                    <tr class="spaceUnder">
                        <td valign="top">
                            <span class="caution-text">You are about to</span> <span
                                class="caution-emphasis">DELETE</span> <span
                                class="caution-text">the following record:</span>
                        </td>
                    </tr>
                    <tr class="spaceUnder">
                        <td valign="top">
                          <span class="caution-text">
                             <a target="_blank" class="external"
                                href="/${formBean.zdbIDToDelete}">${formBean.recordToDeleteViewString}</a>  (${formBean.zdbIDToDelete})
                          </span>
                        </td>
                    </tr>
                    <tr class="spaceUnder">
                        <td valign="top">
                            <c:choose>
                                <c:when test="${fn:startsWith(zdbIdString, 'ZDB-ALT-')}">
                                    <input type="button" value="Delete this feature" id="triggerConfirm"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                    <input type="checkbox" id="removeFromFeatureTracking"
                                           value="removeFromFeatureTracking"
                                           onClick="changeAction();"/>Remove the feature from feature tracking
                                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <input type="button" value="Delete this record" id="triggerConfirm"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                            <input type="button" value="Cancel"
                                   onClick="location.replace('/${formBean.zdbIDToDelete}');"/>
                        </td>
                    </tr>
                </table>
                <div id="confirm-dialog">

                </div>
            </form:form>

        </c:otherwise>
    </c:choose>


</authz:authorize>

<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>

<script type="text/javascript" src="/javascript/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="/javascript/jquery.tools.min.js"></script>

<script src="/javascript/jquery-ui-1.8.16.custom.min.js"></script>
<link rel=stylesheet type="text/css" href="/css/jquery-ui-1.8.16.custom.css">

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

    function confirmMergeAntibody(){
        var markerAbbrevToMergeInto = document.getElementById('markerToMergeIntoViewString').value ;
        if(confirm('Merge and delete ${formBean.markerToDeleteViewString} into '+markerToMergeInto+'?')){
            return true ;
        }
        else{
            return false ;
        }
    }

    function confirmMerge(){
   // alert("Confirm!");
        var abbrevToMergeInto = document.getElementById('intoMarkerAbbrev').value ;
        if(confirm('Are you absolutely sure about merging ${formBean.markerToDeleteViewString} into ' + abbrevToMergeInto + '? ${formBean.markerToDeleteViewString} will be deleted after the merge!')){
            return true ;
        }
        else{
            return false ;
        }
    }

    jQuery(document).ready(function () {
        jQuery("#submitMerge").attr("disabled", "disabled");
        jQuery("#mergedIntoGeneAbbrev").autocomplete({
            source: '/action/marker/find-gene-to-merge-into',
            minLength: 2,
            autoFocus: true,
            select: function (event, ui) {
                geneAbbrevToMergeInto = ui.item.label;
                jQuery('#mergedIntoGeneAbbrev').val(geneAbbrevToMergeInto);
                //geneZdbId = ui.item.id;
                geneZdbIdToDelete = "${formBean.markerToDelete.zdbID}";
                geneZdbIdToBeMergedInto = ui.item.id;
                jQuery('#merge_oid').val(geneZdbIdToBeMergedInto);
                jQuery('#intoMarkerAbbrev').val(geneAbbrevToMergeInto);
                jQuery('#into').html('<a target="_blank" class="external" href="/action/marker/view/' + geneZdbIdToBeMergedInto + '">' + geneAbbrevToMergeInto + '</a>');
                validateGeneWithTranscript(geneZdbIdToDelete, geneZdbIdToBeMergedInto, geneAbbrevToMergeInto);
            }
        });
        
        
        jQuery("#confirm-dialog").css({'display' : 'none'});
        jQuery("#confirm-dialog").dialog({
             autoOpen: false,
             show: "fade",
             hide: "fade",
             modal: true,
             height: 300,
             width: 550,
             title: "Confirmation for merging",
             buttons: {
                OK: function() {
                   jQuery("#mergeTheGenes").submit();
                },
                Cancel: function() {
                   jQuery("#confirm-dialog").dialog("close");
                }
             }
        });
         
        jQuery("#submitMerge").click(function(e) {
        
             jQuery("#confirm-dialog").html('<br/><br/><span style="font-weight:900; font-size: large; color: red">I am absolutely sure I want to merge these records.</span>');
             jQuery("#confirm-dialog").css({'display' : 'inline-block'});
             jQuery("#confirm-dialog").dialog("open");
             //prevent the submit
             e.preventDefault();
        });        
        
    });

    var validateGeneWithTranscript = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
//alert("geneZdbIdMergedInto is " + geneZdbIdMergedInto + " and geneAbbrevMergedInto is " + geneAbbrevMergedInto);

        var numberOfTranscripts = 0;

        jQuery.ajax(
                {
                    url: "/action/marker/get-transcripts-for-geneId?geneZdbId=" + geneIDdelete,
                    type: "GET",
                    success: function(data) {
                        for (transcript in data) {
                            numberOfTranscripts++;
                            if (numberOfTranscripts == 1) 
                                jQuery('#validationText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following ranscripts:</h3>');
                                
                              
                            jQuery('#validationText').append("<div>"
                                    + "<a target='_blank' href='/action/marker/view/"+data[transcript].zdbID+"'>"+data[transcript].name+"</a>"
                                    + "</div>");
                        }
                        
                        if (numberOfTranscripts > 0) {
                        
                           jQuery('#mergedIntoGeneAbbrev').attr("disabled","disabled"); 
                           

                        
          jQuery.ajax(
                {
                    url: "/action/marker/get-transcripts-for-geneId?geneZdbId=" + geneZdbIdMergedInto,
                    type: "GET",
                    success: function(data) {
                        var numberOfTranscriptsOfMarker2 = 0;
                        for (transcript in data) {
                            numberOfTranscriptsOfMarker2++;
                            if (numberOfTranscriptsOfMarker2 == 1)
                                jQuery('#validationText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following ranscripts:</h3>');
                              
                            jQuery('#validationText').append("<div>"
                                    + "<a target='_blank' href='/action/marker/view/"+data[transcript].zdbID+"'>"+data[transcript].name+"</a>"
                                    + "</div>");
                        }
                            if (numberOfTranscriptsOfMarker2 == 0)
                                jQuery('#validationText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto +'">' + geneAbbrevMergedInto + '</a> has no ranscript.</h3>');                        
                        jQuery('#validationText').append('<br/><form><input type="button" value="Ignore transcripts" onclick="ignoreTranscripts(this);" /></form>');
                    },
                    error: function(data) {
                        alert('There was a problem with your the second ajax call: ' + data);
                    }
                }
          );                         
                           
                        }   // end of if (numberOfTranscripts > 0)
                        
                                                else {			                           
			                           jQuery("#submitMerge").removeAttr('disabled'); 
			                        }

                        
                        
                    },
                    error: function(data) {
                        alert('There was a problem with your the first ajax call: ' + data);
                    }
                }
        );   
        
    };

    function doDebug() {
        var merger = document.getElementById("mergedIntoGeneAbbrev");
        alert("merger = " + merger.value + "   mergee = ${formBean.markerToDelete.abbreviation}");
    }

    function doMergeGenes() {
        var geneToMergeInto = document.getElementById('mergedIntoGeneAbbrev').value ;
        if(confirm('Merge ${formBean.markerToDeleteViewString} into ' + geneToMergeInto + ' And ${formBean.markerToDeleteViewString} will be deleted. \nAre you sure to do that?')){
            alert("merger = " + merger.value + "   mergee = ${formBean.markerToDelete.abbreviation}");
            return true;
        } else{
            return false;
        }
    }
    
    function ignoreTranscripts(formObj) {
        jQuery('#validationText').hide(); 
        jQuery("#submitMerge").removeAttr('disabled');
    }

</script>

<c:if test="${formBean.markerToDelete.markerType.name ne 'ATB'}">
    <form id="mergeTheGenes" action="/cgi-bin/merge_markers.pl">
        <input type="hidden" name="OID" value="${formBean.zdbIDToDelete}">
        <input type="hidden" id="merge_oid" name="merge_oid" value="none">
        <input type="hidden" id="intoMarkerAbbrev" name="intoMarkerAbbrev" value="none">
        <br/>
        <p>
          Merge <a target="_blank" class="external" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> into
          <span id="into"><input id="mergedIntoGeneAbbrev" type="text" size="15" />
          </span>&nbsp;&nbsp;
          <span id="warning" style="font-size: large; color: red">(Warning: <a target="_blank" class="external" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> will be deleted after the merging!)
          </span>
        <p/>
        <input type="button" value="Merge these two markers" id="submitMerge">
    </form>
    <div id="validationText"></div>
</c:if>


<c:if test="${formBean.markerToDelete.markerType.name eq 'ATB'}">
    <form:form commandName="formBean" action="/action/marker/merge" onsubmit="return confirmMergeAntibody();" >
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
            </tr>
        </table>
        <br>

        <input type="submit" value="Merge Antibodies"/>
    </form:form>
</c:if>



                <div id="confirm-dialog">
		   
                </div>




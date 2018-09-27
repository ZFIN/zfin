<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.10.4.custom.css"/>

<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>
<script type="text/javascript" src="/javascript/jquery-ui-1.10.4.custom.js"></script>

<authz:authorize access="hasRole('root')">

    <script type="text/javascript">
        var mergedIntoId;

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
            var abbrevToMergeInto = document.getElementById('intoMarkerAbbrev').value ;
            if(confirm('Are you absolutely sure about merging ${formBean.markerToDeleteViewString} into ' + abbrevToMergeInto + '? ${formBean.markerToDeleteViewString} will be deleted after the merge!')){
                return true ;
            }
            else{
                return false ;
            }
        }

        jQuery(document).ready(function () {
            differentCloneTypes = false;
            differentHeavyChainIsotypes = false;
            differentLightChainIsotypes = false;
            differentHostSpecies = false;
            differentImmunogenSpecies = false;

            autocompleteSource = '/action/marker/find-antibody-to-merge-into';
            dropdownWidth = 100;
            jQuery('#mergedIntoAbbrev').autocomplete({
                source: function(request, response) {
                    jQuery.ajax({
                        url: autocompleteSource,
                        dataType: "json",
                        data: {
                            term : request.term,
                            exclude : jQuery("#zdbIDToDelete").val()
                        },
                        success: function(data) {
                            response(data);
                        }
                    });
                },
                minLength: 2,
                autoFocus: true,
                open: function () {
                    jQuery(this).data("autocomplete").menu.element.width(dropdownWidth);
                },
                select: function (event, ui) {
                    markerAbbrevToMergeInto = ui.item.label;
                    jQuery('#mergedIntoAbbrev').val(markerAbbrevToMergeInto);
                    markerZdbIdToDelete = "${formBean.markerToDelete.zdbID}";
                    markerZdbIdToBeMergedInto = ui.item.id;
                    mergedIntoId = markerZdbIdToBeMergedInto;
                    jQuery('#mergedIntoAbbrev').val("");
                    jQuery('#mergedIntoAbbrev').next().val("");
                    event.preventDefault();
                    jQuery('#merge_oid').val(markerZdbIdToBeMergedInto);
                    jQuery('#intoMarkerAbbrev').val(markerAbbrevToMergeInto);
                    jQuery('#into').html('<a target="_blank" class="external" href="/' + markerZdbIdToBeMergedInto + '">' + markerAbbrevToMergeInto + '</a>');
                    validateClonalType(markerZdbIdToDelete, markerZdbIdToBeMergedInto);
                    return false;
                }
            });


            jQuery('#submitMerge').attr("disabled", "disabled");

            jQuery('#confirm-dialog').css({'display' : 'none'});
            jQuery('#confirm-dialog').dialog({
                autoOpen: false,
                show: "fade",
                hide: "fade",
                modal: true,
                height: 260,
                width: 850,
                title: "Confirmation for merging",
                buttons: {
                    OK: function() {
                        jQuery('#mergeTheAntibodies').submit();
                    },
                    Cancel: function() {
                        jQuery('#confirm-dialog').dialog('close');
                    }
                }
            });

            jQuery('#submitMerge').click(function(e) {

                jQuery('#confirm-dialog').html('<br/><br/><span style="font-weight:900; font-size: large; color: red">I am absolutely sure I want to merge these records.</span>');
                jQuery('#confirm-dialog').css({'display' : 'inline-block'});
                jQuery('#confirm-dialog').dialog('open');
                //prevent the submit
                e.preventDefault();
            });

            // select all desired input fields and attach tooltips to them
            jQuery(':input').tipsy({gravity: 'w'});

        });

        var validateClonalType = function(antibodyIDdelete, antibodyZdbIdMergedInto) {

            var clonalType1 = jQuery.ajax({url: "/action/marker/get-antibody-clonal-type?antibodyZdbId=" + antibodyIDdelete,
                async: false
            }).responseText;

            if (!isBlank(clonalType1)) {
                var clonalType2 = jQuery.ajax({url: "/action/marker/get-antibody-clonal-type?antibodyZdbId=" + antibodyZdbIdMergedInto,
                    async: false
                }).responseText;
                if (!isBlank(clonalType2) && clonalType2 != clonalType1)
                    differentCloneTypes = true;
            }
            if(differentCloneTypes)
                jQuery('#blockMerge').append('<span id="formBean.errors" class="error">Clonal type must be the same to merge.</span><br/>');

            validateHeavyChainIsotype(antibodyIDdelete, antibodyZdbIdMergedInto);
        };

        var validateHeavyChainIsotype = function(antibodyIDdelete, antibodyZdbIdMergedInto) {

            var heavyChainIsotype1 = jQuery.ajax({url: "/action/marker/get-antibody-heavy-chain-isotype?antibodyZdbId=" + antibodyIDdelete,
                async: false
            }).responseText;

            if (!isBlank(heavyChainIsotype1)) {
                var heavyChainIsotype2 = jQuery.ajax({url: "/action/marker/get-antibody-heavy-chain-isotype?antibodyZdbId=" + antibodyZdbIdMergedInto,
                    async: false
                }).responseText;
                if (!isBlank(heavyChainIsotype2) && heavyChainIsotype2 !== heavyChainIsotype1) {
                    differentHeavyChainIsotypes = true;
                }
            }

            if(differentHeavyChainIsotypes)
                jQuery('#blockMerge').append('<span id="formBean.errors" class="error">Heavy chain isotypes must be the same to merge.</span><br/>');

            validateLightChainIsotype(antibodyIDdelete, antibodyZdbIdMergedInto);

        };

        var validateLightChainIsotype = function(antibodyIDdelete, antibodyZdbIdMergedInto) {

            var lightChainIsotype1 = jQuery.ajax({url: "/action/marker/get-antibody-light-chain-isotype?antibodyZdbId=" + antibodyIDdelete,
                async: false
            }).responseText;

            if (!isBlank(lightChainIsotype1)) {
                var lightChainIsotype2 = jQuery.ajax({url: "/action/marker/get-antibody-light-chain-isotype?antibodyZdbId=" + antibodyZdbIdMergedInto,
                    async: false
                }).responseText;
                if (!isBlank(lightChainIsotype2) && lightChainIsotype2 !== lightChainIsotype1) {
                    differentLightChainIsotypes = true;
                }
            }

            if(differentLightChainIsotypes)
                jQuery('#blockMerge').append('<span id="formBean.errors" class="error">Light chain isotypes must be the same to merge.</span><br/>');

            validateHostSpecies(antibodyIDdelete, antibodyZdbIdMergedInto);

        };

        var validateHostSpecies = function(antibodyIDdelete, antibodyZdbIdMergedInto) {

            var hostSpecies1 = jQuery.ajax({url: "/action/marker/get-antibody-host-species?antibodyZdbId=" + antibodyIDdelete,
                async: false
            }).responseText;

            if (!isBlank(hostSpecies1)) {
                var hostSpecies2 = jQuery.ajax({url: "/action/marker/get-antibody-host-species?antibodyZdbId=" + antibodyZdbIdMergedInto,
                    async: false
                }).responseText;
                if (!isBlank(hostSpecies2) && hostSpecies2 !== hostSpecies1) {
                    differentHostSpecies = true;
                }
            }

            if(differentHostSpecies)
                jQuery('#blockMerge').append('<span id="formBean.errors" class="error">Host Species must be the same to merge.</span><br/>');

            validateImmunogenSpecies(antibodyIDdelete, antibodyZdbIdMergedInto);

        };

        var validateImmunogenSpecies = function(antibodyIDdelete, antibodyZdbIdMergedInto) {

            var immunogenSpecies1 = jQuery.ajax({url: "/action/marker/get-antibody-immunogen-species?antibodyZdbId=" + antibodyIDdelete,
                async: false
            }).responseText;

            if (!isBlank(immunogenSpecies1)) {
                var immunogenSpecies2 = jQuery.ajax({url: "/action/marker/get-antibody-immunogen-species?antibodyZdbId=" + antibodyZdbIdMergedInto,
                    async: false
                }).responseText;
                if (!isBlank(immunogenSpecies2) && immunogenSpecies2 !== immunogenSpecies1) {
                    differentImmunogenSpecies = true;
                }
            }

            if(differentImmunogenSpecies)
                jQuery('#blockMerge').append('<span id="formBean.errors" class="error">Immunogen species must be the same to merge.</span><br/>');

            enableMerge();

        };

        function enableMerge() {
            if (!differentCloneTypes && !differentHeavyChainIsotypes && !differentLightChainIsotypes && !differentHostSpecies && !differentImmunogenSpecies) {
                jQuery('#submitMerge').removeAttr('disabled');
            }
        }

        function goToEdit() {
            url = '/action/str/'+ mergedIntoId +'/edit';
            window.location.replace(url);
        }

        function isBlank(str) {
            return (!str || /^\s*$/.test(str));
        }

    </script>


    <form id="mergeTheAntibodies" action="/cgi-bin/merge_markers.pl">
        <input type="hidden" id="zdbIDToDelete" name="OID" value="${formBean.zdbIDToDelete}">
        <input type="hidden" id="merge_oid" name="merge_oid" value="none">
        <input type="hidden" id="intoMarkerAbbrev" name="intoMarkerAbbrev" value="none">
        <p/>
        Merge <a target="_blank" class="external" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> into
          <span id="into"><input id="mergedIntoAbbrev" value="" type="text" size="15" />
          </span>&nbsp;&nbsp;
          <span id="warning" style="font-size: large; color: red">(Warning: <a target="_blank" class="external" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> will be deleted after the merging!)
          </span>
        <br/><br/>
        <input type="button" value="Merge these two antibodies" id="submitMerge" title="Perform the merge action">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input type="button" value="Cancel" id="cancelMerge" onclick="window.history.back();" title="Cancel the merge and go back to antibody page">
    </form>
    <div id="blockMerge"></div>

    <div id="confirm-dialog">

    </div>

</authz:authorize>


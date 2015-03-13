<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.10.4.custom.css"/>

<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>
<script type="text/javascript" src="/javascript/jquery-ui-1.10.4.custom.js"></script>

<authz:authorize ifAnyGranted="root">

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
    var abbrevToMergeInto = document.getElementById('intoMarkerAbbrev').value ;
    if(confirm('Are you absolutely sure about merging ${formBean.markerToDeleteViewString} into ' + abbrevToMergeInto + '? ${formBean.markerToDeleteViewString} will be deleted after the merge!')){
        return true ;
    }
    else{
        return false ;
    }
}

jQuery(document).ready(function () {
    unspecifiedAllelesIgnored = true;
    sequenceTargetingReagentsIgnored = true;
    antibodiesIgnored = true;
    ncbiGeneIdsIgnored = true;
    uniGeneIdsIgnored = true;
    vegaIdsIgnored = true;
    ensemblZv9IdsIgnored = true;
    transcriptsIgnored = true;
    orthologyIgnored = true;
    mapInfoIgnored = true;
    differentTargets = false;

    <c:if test="${formBean.markerToDelete.markerType.name eq 'GENE' || formBean.markerToDelete.markerType.name eq 'GENEP'}">
            autocompleteSource = '/action/marker/find-gene-to-merge-into';
    dropdownWidth = 85;
    </c:if>
    <c:if test="${formBean.markerToDelete.markerType.name eq 'MRPHLNO'}">
            autocompleteSource = '/action/marker/find-mo-to-merge-into';
    dropdownWidth = 100;
    </c:if>
    <c:if test="${formBean.markerToDelete.markerType.name eq 'TALEN'}">
            autocompleteSource = '/action/marker/find-talen-to-merge-into';
    dropdownWidth = 140;
    </c:if>
    <c:if test="${formBean.markerToDelete.markerType.name eq 'CRISPR'}">
            autocompleteSource = '/action/marker/find-crispr-to-merge-into';
    dropdownWidth = 140;
    </c:if>
    jQuery('#submitMerge').attr("disabled", "disabled");
    jQuery('#mergedIntoGeneAbbrev').autocomplete({
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
            jQuery('#mergedIntoGeneAbbrev').val(markerAbbrevToMergeInto);
            //geneZdbId = ui.item.id;
            markerZdbIdToDelete = "${formBean.markerToDelete.zdbID}";
            markerZdbIdToBeMergedInto = ui.item.id;
            jQuery('#mergedIntoGeneAbbrev').val("");
            jQuery('#mergedIntoGeneAbbrev').next().val("");
            event.preventDefault();
            jQuery('#merge_oid').val(markerZdbIdToBeMergedInto);
            jQuery('#intoMarkerAbbrev').val(markerAbbrevToMergeInto);
            jQuery('#into').html('<a target="_blank" class="external" href="/action/marker/view/' + markerZdbIdToBeMergedInto + '">' + markerAbbrevToMergeInto + '</a>');
            <c:if test="${formBean.markerToDelete.markerType.name eq 'GENE' || formBean.markerToDelete.markerType.name eq 'GENEP'}">
                    validateUnspecifiedAlleles(markerZdbIdToDelete, markerZdbIdToBeMergedInto, markerAbbrevToMergeInto);
            </c:if>
            <c:if test="${formBean.markerToDelete.markerType.name eq 'MRPHLNO' || formBean.markerToDelete.markerType.name eq 'TALEN' || formBean.markerToDelete.markerType.name eq 'CRISPR'}">
                    validateTargetGenesForMergingSRTs(markerZdbIdToDelete, markerZdbIdToBeMergedInto, markerAbbrevToMergeInto);
            </c:if>
            return false;
        }
    });

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
                jQuery('#mergeTheGenes').submit();
            },
            Cancel: function() {
                jQuery('#confirm-dialog').dialog('close');
            }
        }
    });

    <c:if test="${formBean.markerToDelete.markerType.name ne 'ATB'}">
            jQuery('#submitMerge').click(function(e) {

                jQuery('#confirm-dialog').html('<br/><br/><span style="font-weight:900; font-size: large; color: red">I am absolutely sure I want to merge these records.</span>');
                jQuery('#confirm-dialog').css({'display' : 'inline-block'});
                jQuery('#confirm-dialog').dialog('open');
                //prevent the submit
                e.preventDefault();
            });

    </c:if>

    // select all desired input fields and attach tooltips to them
    jQuery(':input').tipsy({gravity: 'w'});

    jQuery('#sameUnspecifiedAllele').hide();
    jQuery('#renameUnspecifiedAllele').hide();
    jQuery('#ignoreSTR').hide();
    jQuery('#ignoreAntibody').hide();
    jQuery('#ignoreNCBIGeneId').hide();
    jQuery('#ignoreUniGeneId').hide();
    jQuery('#ignoreVegaId').hide();
    jQuery('#ignoreEnsemblZv9Id').hide();
    jQuery('#ignoreTranscript').hide();
    jQuery('#ignoreOrth').hide();
    jQuery('#ignoreMapping').hide();

});

var validateUnspecifiedAlleles = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var checkUnspecifiedAllelesDone = 0;
    var bothHavingUnspecifiedAllels = 0;

    var unspecifiedAlleleDataOfGene1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-unspecified-allele?markerZdbId=" + geneIDdelete,
        dataType: "json",
        async: false
    }).responseText);

    var unspecifiedAlleleNameOfGene1 = unspecifiedAlleleDataOfGene1.name;
    var unspecifiedAlleleIdOfGene1 = unspecifiedAlleleDataOfGene1.zdbID;

    if (null !== unspecifiedAlleleNameOfGene1) {

        unspecifiedAllelesIgnored = false;

        jQuery('#validationUnspecifiedAllelesText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following unspecified allele:</h3>');
        jQuery('#validationUnspecifiedAllelesText').append('<div>'
                + '<a target="_blank" href="/' + unspecifiedAlleleIdOfGene1 +'">'
                + unspecifiedAlleleNameOfGene1 + '</a>'
                + '</div>');

        var unspecifiedAlleleDataOfGene2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-unspecified-allele?markerZdbId=" + geneZdbIdMergedInto,
            dataType: "json",
            async: false
        }).responseText);

        var unspecifiedAlleleNameOfGene2 = unspecifiedAlleleDataOfGene2.name;
        var unspecifiedAlleleIdOfGene2 = unspecifiedAlleleDataOfGene2.zdbID;

        if (null !== unspecifiedAlleleNameOfGene2) {

            jQuery('#validationUnspecifiedAllelesText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following unspecified allele:</h3>');
            jQuery('#validationUnspecifiedAllelesText').append('<div>'
                    + '<a target="_blank" href="/' + unspecifiedAlleleIdOfGene2 +'">'
                    + unspecifiedAlleleNameOfGene2 + '</a>'
                    + '</div>');
            if (unspecifiedAlleleIdOfGene2 !== unspecifiedAlleleIdOfGene1) {
                jQuery('#validationUnspecifiedAllelesText').append('<h3>You can not merge these two genes because they have different unspecified alleles.</h3>');
                bothHavingUnspecifiedAllels = 1;
            } else {
                jQuery('#sameUnspecifiedAllele').show();
            }

        } else {
            jQuery('#validationUnspecifiedAllelesText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has no unspecified allele.</h3>');
            jQuery('#renameUnspecifiedAllele').show();
        }
    }

    checkUnspecifiedAllelesDone = 1;

    if (checkUnspecifiedAllelesDone == 1 && bothHavingUnspecifiedAllels == 0)
        validateSTRs(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
};

var validateSTRs = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var numberOfSTRs = 0;
    var checkSTRsDone = 0;
    sequenceTargetingReagentsIgnored = false;

    jQuery.ajax(
            {
                url: "/action/marker/get-STR-for-gene?geneZdbID=" + geneIDdelete,
                type: "GET",
                success: function(data) {
                    for (sequenceTargetingReagent in data) {
                        numberOfSTRs++;
                        if (numberOfSTRs == 1)
                            jQuery('#validationSequenceTargetingReagentText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following sequence targeting reagents:</h3>');


                        jQuery('#validationSequenceTargetingReagentText').append("<div>"
                                + "<a target='_blank' href='/action/marker/view/"+data[sequenceTargetingReagent].id+"'>"+data[sequenceTargetingReagent].label+"</a>"
                                + "</div>");
                    }

                    if (numberOfSTRs > 0) {
                        sequenceTargetingReagentsIgnored = false;

                        jQuery('#mergedIntoGeneAbbrev').attr("disabled","disabled");

                        jQuery.ajax(
                                {
                                    url: "/action/marker/get-STR-for-gene?geneZdbID=" + geneZdbIdMergedInto,
                                    type: "GET",
                                    success: function(data) {
                                        var numberOfSTRsForGene2 = 0;
                                        for (sequenceTargetingReagent in data) {
                                            numberOfSTRsForGene2++;
                                            if (numberOfSTRsForGene2 == 1)
                                                jQuery('#validationSequenceTargetingReagentText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following sequence targeting reagents:</h3>');

                                            jQuery('#validationSequenceTargetingReagentText').append("<div>"
                                                    + "<a target='_blank' href='/action/marker/view/"+data[sequenceTargetingReagent].id+"'>"+data[sequenceTargetingReagent].label+"</a>"
                                                    + "</div>");
                                        }
                                        if (numberOfSTRsForGene2 == 0)
                                            jQuery('#validationSequenceTargetingReagentText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto +'">' + geneAbbrevMergedInto + '</a> has no sequence targeting reagent.</h3>');

                                        jQuery('#ignoreSTR').show();
                                    },
                                    error: function(data) {
                                        alert('There was a problem with the second ajax call to get sequence targeting reagent data: ' + data);
                                    }
                                }
                        );

                    }   // end of if (numberOfSTRs > 0)

                    else {
                        sequenceTargetingReagentsIgnored = true;
                    }

                },
                error: function(data) {
                    alert('There was a problem with the first ajax call to get sequence targeting reagent data: ' + data);
                }
            }
    );

    checkSTRsDone = 1;

    if (checkSTRsDone == 1)
        validateAntibodies(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
};

var validateAntibodies = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var numberOfAntibodies = 0;
    var checkAntibodiesDone = 0;
    antibodiesIgnored = false;

    jQuery.ajax(
            {
                url: "/action/marker/get-antibody-for-gene?geneZdbID=" + geneIDdelete,
                type: "GET",
                success: function(data) {
                    for (antibody in data) {
                        numberOfAntibodies++;
                        if (numberOfAntibodies == 1)
                            jQuery('#validationAntibodyText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following antibody:</h3>');


                        jQuery('#validationAntibodyText').append("<div>"
                                + "<a target='_blank' href='/action/marker/view/"+data[antibody].id+"'>"+data[antibody].label+"</a>"
                                + "</div>");
                    }

                    if (numberOfAntibodies > 0) {
                        antibodiesIgnored = false;

                        jQuery('#mergedIntoGeneAbbrev').attr("disabled","disabled");

                        jQuery.ajax(
                                {
                                    url: "/action/marker/get-antibody-for-gene?geneZdbID=" + geneZdbIdMergedInto,
                                    type: "GET",
                                    success: function(data) {
                                        var numberOfAntibodiesForGene2 = 0;
                                        for (antibody in data) {
                                            numberOfAntibodiesForGene2++;
                                            if (numberOfAntibodiesForGene2 == 1)
                                                jQuery('#validationAntibodyText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following antibody:</h3>');

                                            jQuery('#validationAntibodyText').append("<div>"
                                                    + "<a target='_blank' href='/action/marker/view/"+data[antibody].id+"'>"+data[antibody].label+"</a>"
                                                    + "</div>");
                                        }
                                        if (numberOfAntibodiesForGene2 == 0)
                                            jQuery('#validationAntibodyText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto +'">' + geneAbbrevMergedInto + '</a> has no antibody.</h3>');

                                        jQuery('#ignoreAntibody').show();
                                    },
                                    error: function(data) {
                                        alert('There was a problem with the second ajax call to get antibody data: ' + data);
                                    }
                                }
                        );

                    }   // end of if (numberOfAntibodies > 0)

                    else {
                        antibodiesIgnored = true;
                    }

                },
                error: function(data) {
                    alert('There was a problem with the first ajax call to get antibody data: ' + data);
                }
            }
    );

    checkAntibodiesDone = 1;

    if (checkAntibodiesDone == 1)
        validateNCBIgeneIds(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
};

var validateNCBIgeneIds = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var checkNCBIgeneIdsDone = 0;
    var ncbiGenesOfGene1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=NcbiGene&markerZdbId=" + geneIDdelete,
        dataType: "json",
        async: false
    }).responseText);

    var ncbiGeneIdsOfGene1 = new Array();
    var ncbiGeneIdLinksOfGene1 = new Array();
    for (ncbiGene in ncbiGenesOfGene1) {
        ncbiGeneIdsOfGene1.push(ncbiGenesOfGene1[ncbiGene].accessionNumber);
        ncbiGeneIdLinksOfGene1.push(ncbiGenesOfGene1[ncbiGene].url);
    }

    var ncbiGenesOfGene2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=NcbiGene&markerZdbId=" + geneZdbIdMergedInto,
        dataType: "json",
        async: false
    }).responseText);

    var ncbiGeneIdsOfGene2 = new Array();
    var ncbiGeneIdLinksOfGene2 = new Array();
    for (ncbiGene in ncbiGenesOfGene2) {
        ncbiGeneIdsOfGene2.push(ncbiGenesOfGene2[ncbiGene].accessionNumber);
        ncbiGeneIdLinksOfGene2.push(ncbiGenesOfGene2[ncbiGene].url);
    }

    var differentNCBIgeneIds = false;
    if(ncbiGeneIdsOfGene1.length !== 0 && ncbiGeneIdsOfGene2.length !== 0) {
        if (ncbiGeneIdsOfGene1.length !== ncbiGeneIdsOfGene2.length) {
            differentNCBIgeneIds = true;
        } else {
            for (var i = 0; i < ncbiGeneIdsOfGene1.length; i++) {
                if (ncbiGeneIdsOfGene1[i] !== ncbiGeneIdsOfGene2[i])
                    differentNCBIgeneIds = true;
            }
        }
    }

    if (differentNCBIgeneIds) {
        ncbiGeneIdsIgnored = false;
        jQuery('#validationNCBIgeneIdsText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following NCBI Gene Id:</h3>');
        for (var i = 0; i < ncbiGeneIdsOfGene1.length; i++) {
            jQuery('#validationNCBIgeneIdsText').append('<div>'
                    + '<a target="_blank" href="' + ncbiGeneIdLinksOfGene1[i] +'">'
                    + ncbiGeneIdsOfGene1[i] + '</a>'
                    + '</div>');
        }

        jQuery('#validationNCBIgeneIdsText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following NCBI Gene Id:</h3>');
        for (var i = 0; i < ncbiGeneIdsOfGene2.length; i++) {
            jQuery('#validationNCBIgeneIdsText').append('<div>'
                    + '<a target="_blank" href="' + ncbiGeneIdLinksOfGene2[i] +'">'
                    + ncbiGeneIdsOfGene2[i] + '</a>'
                    + '</div>');
        }
        jQuery('#ignoreNCBIGeneId').show();

    }

    checkNCBIgeneIdsDone = 1;

    if (checkNCBIgeneIdsDone == 1)
        validateUniGeneIds(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
};

var validateUniGeneIds = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var checkUniGeneIdsDone = 0;
    var uniGenesOfGene1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=UniGene&markerZdbId=" + geneIDdelete,
        dataType: "json",
        async: false
    }).responseText);

    var uniGeneIdsOfGene1 = new Array();
    var uniGeneIdLinksOfGene1 = new Array();
    for (uniGene in uniGenesOfGene1) {
        uniGeneIdsOfGene1.push(uniGenesOfGene1[uniGene].accessionNumber);
        uniGeneIdLinksOfGene1.push(uniGenesOfGene1[uniGene].url);
    }

    var uniGenesOfGene2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=UniGene&markerZdbId=" + geneZdbIdMergedInto,
        dataType: "json",
        async: false
    }).responseText);

    var uniGeneIdsOfGene2 = new Array();
    var uniGeneIdLinksOfGene2 = new Array();
    for (uniGene in uniGenesOfGene2) {
        uniGeneIdsOfGene2.push(uniGenesOfGene2[uniGene].accessionNumber);
        uniGeneIdLinksOfGene2.push(uniGenesOfGene2[uniGene].url);
    }

    var differentUniGeneIds = false;
    if(uniGeneIdsOfGene1.length !== 0 && uniGeneIdsOfGene2.length !== 0) {
        if (uniGeneIdsOfGene1.length !== uniGeneIdsOfGene2.length) {
            differentUniGeneIds = true;
        } else {
            for (var i = 0; i < uniGeneIdsOfGene1.length; i++) {
                if (uniGeneIdsOfGene1[i] !== uniGeneIdsOfGene2[i])
                    differentUniGeneIds = true;
            }
        }
    }

    if (differentUniGeneIds) {
        uniGeneIdsIgnored = false;
        jQuery('#validationUniGeneIdsText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following UniGene Id:</h3>');
        for (var i = 0; i < uniGeneIdsOfGene1.length; i++) {
            jQuery('#validationUniGeneIdsText').append('<div>'
                    + '<a target="_blank" href="' + uniGeneIdLinksOfGene1[i] +'">'
                    + uniGeneIdsOfGene1[i] + '</a>'
                    + '</div>');
        }

        jQuery('#validationUniGeneIdsText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following UniGene Id:</h3>');
        for (var i = 0; i < uniGeneIdsOfGene2.length; i++) {
            jQuery('#validationUniGeneIdsText').append('<div>'
                    + '<a target="_blank" href="' + uniGeneIdLinksOfGene2[i] +'">'
                    + uniGeneIdsOfGene2[i] + '</a>'
                    + '</div>');
        }

        jQuery('#ignoreUniGeneId').show();
    }

    checkUniGeneIdsDone = 1;

    if (checkUniGeneIdsDone == 1)
        validateVegaIds(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
};

var validateVegaIds = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var checkVegaIdsDone = 0;
    var vegaIdGene1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=Vega&markerZdbId=" + geneIDdelete,
        dataType: "json",
        async: false
    }).responseText);

    var vegaIdsOfGene1 = new Array();
    var vegaIdLinksOfGene1 = new Array();
    for (vega in vegaIdGene1) {
        vegaIdsOfGene1.push(vegaIdGene1[vega].accessionNumber);
        vegaIdLinksOfGene1.push(vegaIdGene1[vega].url);
    }
    var vegaIdGene2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=Vega&markerZdbId=" + geneZdbIdMergedInto,
        dataType: "json",
        async: false
    }).responseText);

    var vegaIdsOfGene2 = new Array();
    var vegaIdLinksOfGene2 = new Array();
    for (vega in vegaIdGene2) {
        vegaIdsOfGene2.push(vegaIdGene2[vega].accessionNumber);
        vegaIdLinksOfGene2.push(vegaIdGene2[vega].url);
    }
    var differentVegaIds = false;
    if(vegaIdsOfGene1.length !== 0 && vegaIdsOfGene2.length !== 0) {
        if (vegaIdsOfGene1.length !== vegaIdsOfGene2.length) {
            differentVegaIds = true;
        } else {
            for (var i = 0; i < vegaIdsOfGene1.length; i++) {
                if (vegaIdsOfGene1[i] !== vegaIdsOfGene2[i])
                    differentVegaIds = true;
            }
        }
    }

    if (differentVegaIds) {
        vegaIdsIgnored = false;
        jQuery('#validationVegaIdsText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following Vega Id:</h3>');
        for (var i = 0; i < vegaIdsOfGene1.length; i++) {
            jQuery('#validationVegaIdsText').append('<div>'
                    + '<a target="_blank" href="' + vegaIdLinksOfGene1[i] +'">'
                    + vegaIdsOfGene1[i] + '</a>'
                    + '</div>');
        }

        jQuery('#validationVegaIdsText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following Vega Id:</h3>');
        for (var i = 0; i < vegaIdsOfGene2.length; i++) {
            jQuery('#validationVegaIdsText').append('<div>'
                    + '<a target="_blank" href="' + vegaIdLinksOfGene2[i] +'">'
                    + vegaIdsOfGene2[i] + '</a>'
                    + '</div>');
        }

        jQuery('#ignoreVegaId').show();
    }

    checkVegaIdsDone = 1;

    if (checkVegaIdsDone == 1)
        validateEnsemblZv9Ids(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
};

var validateEnsemblZv9Ids = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var checkEnsemblZv9IdsDone = 0;
    var ensemblZv9OfGene1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=EnsemblZV9&markerZdbId=" + geneIDdelete,
        dataType: "json",
        async: false
    }).responseText);

    var ensemblZv9IdsOfGene1 = new Array();
    var ensemblZv9LinksOfGene1 = new Array();
    for (ensemblZv9 in ensemblZv9OfGene1) {
        ensemblZv9IdsOfGene1.push(ensemblZv9OfGene1[ensemblZv9].accessionNumber);
        ensemblZv9LinksOfGene1.push(ensemblZv9OfGene1[ensemblZv9].url);
    }
    var ensemblZv9OfGene2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=EnsemblZV9&markerZdbId=" + geneZdbIdMergedInto,
        dataType: "json",
        async: false
    }).responseText);

    var ensemblZv9IdsOfGene2 = new Array();
    var ensemblZv9LinksOfGene2 = new Array();
    for (ensemblZv9 in ensemblZv9OfGene2) {
        ensemblZv9IdsOfGene2.push(ensemblZv9OfGene2[ensemblZv9].accessionNumber);
        ensemblZv9LinksOfGene2.push(ensemblZv9OfGene2[ensemblZv9].url);
    }
    var differentEnsemblZv9Ids = false;
    if(ensemblZv9IdsOfGene1.length !== 0 && ensemblZv9IdsOfGene2.length !== 0) {
        if (ensemblZv9IdsOfGene1.length !== ensemblZv9IdsOfGene2.length) {
            differentEnsemblZv9Ids = true;
        } else {
            for (var i = 0; i < ensemblZv9IdsOfGene1.length; i++) {
                if (ensemblZv9IdsOfGene1[i] !== ensemblZv9IdsOfGene2[i])
                    differentEnsemblZv9Ids = true;
            }
        }
    }

    if (differentEnsemblZv9Ids) {
        ensemblZv9IdsIgnored = false;
        jQuery('#validationEnsemblZv9IdsText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following Ensembl(Zv9) Id:</h3>');
        for (var i = 0; i < ensemblZv9IdsOfGene1.length; i++) {
            jQuery('#validationEnsemblZv9IdsText').append('<div>'
                    + '<a target="_blank" href="' + ensemblZv9LinksOfGene1[i] +'">'
                    + ensemblZv9IdsOfGene1[i] + '</a>'
                    + '</div>');
        }

        jQuery('#validationEnsemblZv9IdsText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following Ensembl(Zv9) Id:</h3>');
        for (var i = 0; i < ensemblZv9IdsOfGene2.length; i++) {
            jQuery('#validationEnsemblZv9IdsText').append('<div>'
                    + '<a target="_blank" href="' + ensemblZv9LinksOfGene2[i] +'">'
                    + ensemblZv9IdsOfGene2[i] + '</a>'
                    + '</div>');
        }

        jQuery('#ignoreEnsemblZv9Id').show();
    }

    checkEnsemblZv9IdsDone = 1;

    if (checkEnsemblZv9IdsDone == 1)
        validateGeneWithTranscript(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
};

var validateGeneWithTranscript = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var numberOfTranscripts = 0;
    var checkTranscriptsDone = 0;
    transcriptsIgnored = false;

    jQuery.ajax(
            {
                url: "/action/marker/get-transcripts-for-geneId?geneZdbId=" + geneIDdelete,
                type: "GET",
                success: function(data) {
                    for (transcript in data) {
                        numberOfTranscripts++;
                        if (numberOfTranscripts == 1)
                            jQuery('#validationTranscriptText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following transcripts:</h3>');


                        jQuery('#validationTranscriptText').append("<div>"
                                + "<a target='_blank' href='/action/marker/view/"+data[transcript].zdbID+"'>"+data[transcript].name+"</a>"
                                + "</div>");
                    }

                    if (numberOfTranscripts > 0) {
                        transcriptsIgnored = false;

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
                                                jQuery('#validationTranscriptText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following transcripts:</h3>');

                                            jQuery('#validationTranscriptText').append("<div>"
                                                    + "<a target='_blank' href='/action/marker/view/"+data[transcript].zdbID+"'>"+data[transcript].name+"</a>"
                                                    + "</div>");
                                        }
                                        if (numberOfTranscriptsOfMarker2 == 0)
                                            jQuery('#validationTranscriptText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto +'">' + geneAbbrevMergedInto + '</a> has no transcript.</h3>');

                                        jQuery('#ignoreTranscript').show();
                                    },
                                    error: function(data) {
                                        alert('There was a problem with the second ajax call to get transcript data: ' + data);
                                    }
                                }
                        );

                    }   // end of if (numberOfTranscripts > 0)

                    else {
                        transcriptsIgnored = true;
                    }



                },
                error: function(data) {
                    alert('There was a problem with the first ajax call to get transcript data: ' + data);
                }
            }
    );

    checkTranscriptsDone = 1;

    if (checkTranscriptsDone == 1)
        validateGeneWithOrthology(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);

};

var validateGeneWithOrthology = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var numberOfOrthology = 0;
    var checkOrthologyDone = 0;
    orthologyIgnored = false;

    jQuery.ajax(
            {
                url: "/action/marker/get-orthology-for-geneId?geneZdbId=" + geneIDdelete,
                type: "GET",
                success: function(data) {
                    for (orthology in data) {
                        numberOfOrthology++;
                        if (numberOfOrthology == 1)
                            jQuery('#validationOrthologyText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following orthology:</h3>');


                        jQuery('#validationOrthologyText').append("<div>"
                                +data[orthology].organism+"&nbsp;&nbsp;&nbsp;"+data[orthology].orthologySymbol
                                + "</div>");
                    }

                    if (numberOfOrthology > 0) {
                        orthologyIgnored = false;

                        jQuery('#mergedIntoGeneAbbrev').attr("disabled","disabled");

                        jQuery.ajax(
                                {
                                    url: "/action/marker/get-orthology-for-geneId?geneZdbId=" + geneZdbIdMergedInto,
                                    type: "GET",
                                    success: function(data) {
                                        var numberOfOrthologyOfMarker2 = 0;
                                        for (orthology in data) {
                                            numberOfOrthologyOfMarker2++;
                                            if (numberOfOrthologyOfMarker2 == 1)
                                                jQuery('#validationOrthologyText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following orthology:</h3>');

                                            jQuery('#validationOrthologyText').append("<div>"
                                                    +data[orthology].organism+"&nbsp;&nbsp;&nbsp;"+data[orthology].orthologySymbol
                                                    + "</div>");
                                        }
                                        if (numberOfOrthologyOfMarker2 == 0)
                                            jQuery('#validationOrthologyText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto +'">' + geneAbbrevMergedInto + '</a> has no orthology.</h3>');


                                        jQuery('#ignoreOrth').show();
                                    },
                                    error: function(data) {
                                        alert('There was a problem with the second ajax call to get orthology data: ' + data);
                                    }
                                }
                        );

                    }   // end of if (numberOfOrthology > 0)

                    else {
                        orthologyIgnored = true;;
                    }



                },
                error: function(data) {
                    alert('There was a problem with the first ajax call to get orthology data: ' + data);
                }
            }
    );

    checkOrthologyDone = 1;

    if (checkOrthologyDone == 1)
        validateGeneWithMappingInfo(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);

};

var validateGeneWithMappingInfo = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
    var chromosomeGeneDelete = "Chr: ";
    var chromosomeGeneMergeInto = "Chr: ";

    chromosomeGeneDelete += jQuery.ajax({url: "/action/marker/get-mapping-info?markerZdbId=" + geneIDdelete,
        async: false
    }).responseText;
    chromosomeGeneMergeInto += jQuery.ajax({url: "/action/marker/get-mapping-info?markerZdbId=" + geneZdbIdMergedInto,
        async: false
    }).responseText;

    if (!(chromosomeGeneDelete === "Chr: " && chromosomeGeneMergeInto === "Chr: ")) {
        mapInfoIgnored = false;
        if (chromosomeGeneDelete === "Chr: ")
            chromosomeGeneDelete = "Unmapped";
        else
            chromosomeGeneDelete = chromosomeGeneDelete + '  <a target="_blank" href="/action/mapping/detail/' + geneIDdelete + '">Details</a>';
        if (chromosomeGeneMergeInto === "Chr: ")
            chromosomeGeneMergeInto = "Unmapped";
        else
            chromosomeGeneMergeInto = chromosomeGeneMergeInto + '  <a target="_blank" href="/action/mapping/detail/' + geneZdbIdMergedInto + '">Details</a>';

        jQuery('#validationMapInfoText').append('<h3><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following mapping info:</h3>');
        jQuery('#validationMapInfoText').append("<div>" + chromosomeGeneDelete + "</div>");
        jQuery('#validationMapInfoText').append('<h3><a target="_blank" href="/action/marker/view/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following mapping info:</h3>');
        jQuery('#validationMapInfoText').append("<div>" + chromosomeGeneMergeInto + "</div>");
        jQuery('#ignoreMapping').show();
    }

    enableMerge();
};

var validateTargetGenesForMergingSRTs = function(strIDdelete, strZdbIdMergedInto, strAbbrevMergedInto) {
    var targetGenesSTR1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-targetGenes-for-sequenceTargetingReagentZdbId?sequenceTargetingReagentZdbId=" + strIDdelete,
        dataType: "json",
        async: false
    }).responseText);

    var targetGeneSymbolsOfSTR1 = new Array();
    var targetIDsOfSTR1 = new Array();
    for (targetGene in targetGenesSTR1) {
        targetGeneSymbolsOfSTR1.push(targetGenesSTR1[targetGene].symbol);
        targetIDsOfSTR1.push(targetGenesSTR1[targetGene].zdbID);
    }

    var targetGenesSTR2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-targetGenes-for-sequenceTargetingReagentZdbId?sequenceTargetingReagentZdbId=" + strZdbIdMergedInto,
        dataType: "json",
        async: false
    }).responseText);

    var targetGeneSymbolsOfSTR2 = new Array();
    var targetIDsOfSTR2 = new Array();
    for (targetGene in targetGenesSTR2) {
        targetGeneSymbolsOfSTR2.push(targetGenesSTR2[targetGene].symbol);
        targetIDsOfSTR2.push(targetGenesSTR2[targetGene].zdbID);
    }

    if(targetIDsOfSTR1.length !== 0 && targetIDsOfSTR2.length !== 0) {
        if (targetIDsOfSTR1.length !== targetIDsOfSTR2.length) {
            differentTargets = true;
        } else {
            for (var i = 0; i < targetIDsOfSTR1.length; i++) {
                if (targetIDsOfSTR1[i] !== targetIDsOfSTR2[i])
                    differentTargets = true;
            }
        }
    }

    if (differentTargets) {
        jQuery('#validationSTRText').append('<h4>Merging these two sequence targeting reagents is not allowed because they have different target genes.</h4>');
        if (targetGeneSymbolsOfSTR1.length > 0) {
            jQuery('#validationSTRText').append('<h4><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following target gene:</h4>');
            for (var i = 0; i < targetGeneSymbolsOfSTR1.length; i++) {
                jQuery('#validationSTRText').append('<div>'
                        + '<a target="_blank" href="/action/marker/view/' + targetIDsOfSTR1[i] +'">'
                        + targetGeneSymbolsOfSTR1[i] + '</a>'
                        + '</div>');
            }
        } else {
            jQuery('#validationSTRText').append('<h4><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has no target gene.</h4>');
        }

        if (targetGeneSymbolsOfSTR2.length > 0) {
            jQuery('#validationSTRText').append('<h4><a target="_blank" href="/action/marker/view/' + strZdbIdMergedInto + '">' + strAbbrevMergedInto + '</a> has the following target gene:</h4>');
            for (var i = 0; i < targetGeneSymbolsOfSTR2.length; i++) {
                jQuery('#validationSTRText').append('<div>'
                        + '<a target="_blank" href="/action/marker/view/' + targetIDsOfSTR2[i] +'">'
                        + targetGeneSymbolsOfSTR2[i] + '</a>'
                        + '</div>');
            }
        } else {
            jQuery('#validationSTRText').append('<h4><a target="_blank" href="/action/marker/view/' + strZdbIdMergedInto + '">' + strAbbrevMergedInto + '</a> has no target gene.</h4>');
        }
    }

    validateSequencesForMergingSRTs(strIDdelete, strZdbIdMergedInto, strAbbrevMergedInto);
};

var validateSequencesForMergingSRTs = function(strIDdelete, strZdbIdMergedInto, strAbbrevMergedInto) {
    var talen = new RegExp("TALEN");
    var isTALEN = talen.test(strIDdelete);

    var sequenceDataSTR1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-sequence-for-sequenceTargetingReagent?sequenceTargetingReagentZdbId=" + strIDdelete,
        dataType: "json",
        async: false
    }).responseText);

    var sequenceSTR1 = sequenceDataSTR1.sequence;
    if (isTALEN)
        var secondSequenceSTR1 = sequenceDataSTR1.secondSequence;

    var sequenceDataSTR2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-sequence-for-sequenceTargetingReagent?sequenceTargetingReagentZdbId=" + strZdbIdMergedInto,
        dataType: "json",
        async: false
    }).responseText);

    var sequenceSTR2 = sequenceDataSTR2.sequence;
    if (isTALEN)
        var secondSequenceSTR2 = sequenceDataSTR2.secondSequence;

    var differentSequence = true;
    if(isTALEN) {
        if (sequenceSTR1 === sequenceSTR2 && secondSequenceSTR1 === secondSequenceSTR2 || sequenceSTR1 === '' && secondSequenceSTR1 === '' || sequenceSTR2 === '' && secondSequenceSTR2 === '') {
            differentSequence = false;
        }
    } else {
        if (sequenceSTR1 === sequenceSTR2 || sequenceSTR1 === '' || sequenceSTR2 === '') {
            differentSequence = false;
        }
    }

    if (differentSequence) {
        jQuery('#validationSTRText').append('<h4>Merging these two sequence targeting reagents is not allowed because they have different sequences:</h4>');
        jQuery('#validationSTRText').append('<h4><a target="_blank" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following sequence:</h4>');
        if(isTALEN) {
            jQuery('#validationSTRText').append('<div>Sequence 1: '+ sequenceSTR1 + '</div>');
            jQuery('#validationSTRText').append('<div>Sequence 2: '+ secondSequenceSTR1 + '</div>');
        } else {
            jQuery('#validationSTRText').append('<div>'+ sequenceSTR1 + '</div>');
        }

        jQuery('#validationSTRText').append('<h4><a target="_blank" href="/action/marker/view/' + strZdbIdMergedInto + '">' + strAbbrevMergedInto + '</a> has the following sequence:</h4>');
        if(isTALEN) {
            jQuery('#validationSTRText').append('<div>Sequence 1: '+ sequenceSTR2 + '</div>');
            jQuery('#validationSTRText').append('<div>Sequence 2: '+ secondSequenceSTR2 + '</div>');
        } else {
            jQuery('#validationSTRText').append('<div>'+ sequenceSTR2 + '</div>');
        }
    }

    if (!differentTargets && !differentSequence)
        enableMerge();
};

function ignoreUnspecifiedAlleles(formObj) {
    unspecifiedAllelesIgnored = true;
    jQuery('#validationUnspecifiedAllelesText').hide();
    jQuery('#sameUnspecifiedAllele').hide();
    jQuery('#renameUnspecifiedAllele').hide();

    enableMerge();
}

function ignoreSTRs(formObj) {
    sequenceTargetingReagentsIgnored = true;
    jQuery('#validationSequenceTargetingReagentText').hide();
    jQuery('#ignoreSTR').hide();

    enableMerge();
}


function ignoreAntibodies(formObj) {
    antibodiesIgnored = true;
    jQuery('#validationAntibodyText').hide();
    jQuery('#ignoreAntibody').hide();

    enableMerge();
}

function ignoreNCBIgeneIds(formObj) {
    ncbiGeneIdsIgnored = true;
    jQuery('#validationNCBIgeneIdsText').hide();
    jQuery('#ignoreNCBIGeneId').hide();

    enableMerge();
}

function ignoreUniGeneIds(formObj) {
    uniGeneIdsIgnored = true;
    jQuery('#validationUniGeneIdsText').hide();
    jQuery('#ignoreUniGeneId').hide();

    enableMerge();
}

function ignoreVegaIds(formObj) {
    vegaIdsIgnored = true;
    jQuery('#validationVegaIdsText').hide();
    jQuery('#ignoreVegaId').hide();

    enableMerge();
}

function ignoreEnsemblZv9Ids(formObj) {
    ensemblZv9IdsIgnored = true;
    jQuery('#validationEnsemblZv9IdsText').hide();
    jQuery('#ignoreEnsemblZv9Id').hide();

    enableMerge();
}

function ignoreTranscripts(formObj) {
    transcriptsIgnored = true;
    jQuery('#validationTranscriptText').hide();
    jQuery('#ignoreTranscript').hide();

    enableMerge();
}

function ignoreOrthology(formObj) {
    orthologyIgnored = true;
    jQuery('#validationOrthologyText').hide();
    jQuery('#ignoreOrth').hide();

    enableMerge();
}

function ignoreMappingInfo(formObj) {
    mapInfoIgnored = true;
    jQuery('#validationMapInfoText').hide();
    jQuery('#ignoreMapping').hide();

    enableMerge();
}

function enableMerge() {
    if (unspecifiedAllelesIgnored && sequenceTargetingReagentsIgnored && antibodiesIgnored && ncbiGeneIdsIgnored && uniGeneIdsIgnored && vegaIdsIgnored && ensemblZv9IdsIgnored && transcriptsIgnored && orthologyIgnored && mapInfoIgnored) {
        jQuery('#submitMerge').removeAttr('disabled');
    }
}

</script>

<c:if test="${formBean.markerToDelete.markerType.name ne 'ATB'}">
    <form id="mergeTheGenes" action="/cgi-bin/merge_markers.pl">
        <input type="hidden" id="zdbIDToDelete" name="OID" value="${formBean.zdbIDToDelete}">
        <input type="hidden" id="merge_oid" name="merge_oid" value="none">
        <input type="hidden" id="intoMarkerAbbrev" name="intoMarkerAbbrev" value="none">
        <p/>
        Merge <a target="_blank" class="external" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> into
          <span id="into"><input id="mergedIntoGeneAbbrev" value="" type="text" size="15" />
          </span>&nbsp;&nbsp;
          <span id="warning" style="font-size: large; color: red">(Warning: <a target="_blank" class="external" href="/action/marker/view/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> will be deleted after the merging!)
          </span>
        <br/><br/>
        <input type="button" value="Merge these two markers" id="submitMerge" title="Perform the merge action">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input type="button" value="Cancel" id="cancelMerge" onclick="window.history.back();" title="Cancel the merge and go back to gene page">
    </form>
    <div id="validationUnspecifiedAllelesText"></div>
    <form id="sameUnspecifiedAllele">
        <input type="button" value="Ignore Unspecified Alleles" onclick="ignoreUnspecifiedAlleles(this);" title="because these two unspecified alleles are the same.">
    </form>
    <form id="renameUnspecifiedAllele">
        <input type="button" value="Rename Unspecified Allele And Related Genotypes" onclick="ignoreUnspecifiedAlleles(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the unspecified allele and its related genotypes with ${formBean.markerToDeleteViewString}, if any, will be renamed after the symbol of the gene retained.">
    </form>
    <div id="validationSequenceTargetingReagentText"></div>
    <form id="ignoreSTR">
        <input type="button" value="Ignore Sequence Targeting Reagent" onclick="ignoreSTRs(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the above sequence targeting reagents with ${formBean.markerToDeleteViewString} will be associated with the gene retained. The names of these sequence targeting reagents will need to be updated manually after the merge.">
    </form>
    <div id="validationAntibodyText"></div>
    <form id="ignoreAntibody">
        <input type="button" value="Ignore Antibody" onclick="ignoreAntibodies(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the above antibodies with ${formBean.markerToDeleteViewString} will be associated with the gene retained. The names of these antibodies will need to be updated manually after the merge.">
    </form>
    <div id="validationNCBIgeneIdsText"></div>
    <form id="ignoreNCBIGeneId">
        <input type="button" value="Ignore NCBI Gene Ids" onclick="ignoreNCBIgeneIds(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, all the above NCBI Gene Ids will be associated with the gene retained.">
    </form>
    <div id="validationUniGeneIdsText"></div>
    <form id="ignoreUniGeneId">
        <input type="button" value="Ignore UniGene Gene Ids" onclick="ignoreUniGeneIds(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, all the above UniGene Ids will be associated with the gene retained.">
    </form>
    <div id="validationVegaIdsText"></div>
    <form id="ignoreVegaId">
        <input type="button" value="Ignore Vega Ids" onclick="ignoreVegaIds(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, all the above Vega Ids will be associated with the gene retained.">
    </form>
    <div id="validationEnsemblZv9IdsText"></div>
    <form id="ignoreEnsemblZv9Id">
        <input type="button" value="Ignore Ensembl(Zv9) Ids" onclick="ignoreEnsemblZv9Ids(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, all the above Ensembl(Zv9) Ids will be associated with the gene retained.">
    </form>
    <div id="validationTranscriptText"></div>
    <form id="ignoreTranscript">
        <input type="button" value="Ignore Transcripts" onclick="ignoreTranscripts(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the above transcripts (no automatic re-naming) with ${formBean.markerToDeleteViewString} will be associated with the gene retained.">
    </form>
    <div id="validationOrthologyText"></div>
    <form id="ignoreOrth">
        <input type="button" value="Ignore Orthology" onclick="ignoreOrthology(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, orthology data of ${formBean.markerToDeleteViewString} conflicting with the gene retained willl be deleted; non-conflicting orthology data will be associated with the gene retained.">
    </form>
    <div id="validationMapInfoText"></div>
    <form id="ignoreMapping">
        <input type="button" value="Ignore Mapping Info" onclick="ignoreMappingInfo(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the mapping info with ${formBean.markerToDeleteViewString} will be associated with the gene retained regardless of whether it conflicts or not.">
    </form>
    <div id="validationSTRText"></div>
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

</authz:authorize>


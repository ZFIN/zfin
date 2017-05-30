<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.10.4.custom.css"/>

<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>
<script type="text/javascript" src="/javascript/jquery-ui-1.10.4.custom.js"></script>

<authz:authorize access="hasRole('root')">

    <script type="text/javascript">
        var LookupProperties0 = {
            divName: "antibodyLookup",
            inputName: "markerToMergeIntoViewString",
            showError: true,
            type: "ANTIBODY_LOOKUP",
            useTermTable: false,
            wildcard: false
        };

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
            unspecifiedAllelesIgnored = true;
            sequenceTargetingReagentsIgnored = true;
            antibodiesIgnored = true;
            ncbiGeneIdsIgnored = true;
            uniGeneIdsIgnored = true;
            vegaIdsIgnored = true;
            EnsemblGRCz10IdsIgnored = true;
            transcriptsIgnored = true;
            orthologyIgnored = true;
            mapInfoIgnored = true;
            differentTargets = false;
            differentSequence = false;
            differentFish = false;

            <c:if test="${formBean.markerToDelete.markerType.name eq 'GENE' || formBean.markerToDelete.markerType.name eq 'GENEP'}">
            autocompleteSource = '/action/marker/find-gene-to-merge-into';
            dropdownWidth = 85;
            </c:if>
            <c:if test="${formBean.markerToDelete.markerType.name ne 'GENE' && formBean.markerToDelete.markerType.name ne 'GENEP'
                          && formBean.markerToDelete.markerType.name ne 'MRPHLNO' && formBean.markerToDelete.markerType.name ne 'TALEN' && formBean.markerToDelete.markerType.name ne 'CRISPR'}">
            autocompleteSource = '/action/marker/find-region-to-merge-into';
            dropdownWidth = 100;
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
                    mergedIntoId = markerZdbIdToBeMergedInto;
                    jQuery('#mergedIntoGeneAbbrev').val("");
                    jQuery('#mergedIntoGeneAbbrev').next().val("");
                    event.preventDefault();
                    jQuery('#merge_oid').val(markerZdbIdToBeMergedInto);
                    jQuery('#intoMarkerAbbrev').val(markerAbbrevToMergeInto);
                    jQuery('#into').html('<a target="_blank" class="external" href="/' + markerZdbIdToBeMergedInto + '">' + markerAbbrevToMergeInto + '</a>');
                    <c:if test="${formBean.markerToDelete.markerType.name eq 'GENE' || formBean.markerToDelete.markerType.name eq 'GENEP'
                                  || formBean.markerToDelete.markerType.name eq 'LINCRNAG' || formBean.markerToDelete.markerType.name eq 'NCRNAG'
                                  || formBean.markerToDelete.markerType.name eq 'SNORNAG' || formBean.markerToDelete.markerType.name eq 'LNCRNAG'
                                  || formBean.markerToDelete.markerType.name eq 'SCRNAG' || formBean.markerToDelete.markerType.name eq 'MIRNAG'
                                  || formBean.markerToDelete.markerType.name eq 'TRNAG' || formBean.markerToDelete.markerType.name eq 'SRPRNAG'
                                  || formBean.markerToDelete.markerType.name eq 'PIRNAG' || formBean.markerToDelete.markerType.name eq 'RRNAG'}">
                          validateEap(markerZdbIdToDelete, markerZdbIdToBeMergedInto, markerAbbrevToMergeInto);
                    </c:if>
                    <c:if test="${formBean.markerToDelete.markerType.name eq 'MRPHLNO' || formBean.markerToDelete.markerType.name eq 'TALEN' || formBean.markerToDelete.markerType.name eq 'CRISPR'}">
                          validateTargetGenesForMergingSRTs(markerZdbIdToDelete, markerZdbIdToBeMergedInto, markerAbbrevToMergeInto);
                    </c:if>
                    <c:if test="${formBean.markerToDelete.markerType.name eq 'NCCR' || formBean.markerToDelete.markerType.name eq 'TFBS'
                                  || formBean.markerToDelete.markerType.name eq 'RNAMO' || formBean.markerToDelete.markerType.name eq 'NCBS'
                                  || formBean.markerToDelete.markerType.name eq 'MDNAB' || formBean.markerToDelete.markerType.name eq 'ENHANCER'
                                  || formBean.markerToDelete.markerType.name eq 'EMR' || formBean.markerToDelete.markerType.name eq 'HMR'
                                  || formBean.markerToDelete.markerType.name eq 'NUCMO' || formBean.markerToDelete.markerType.name eq 'BR'
                                  || formBean.markerToDelete.markerType.name eq 'PROTBS' || formBean.markerToDelete.markerType.name eq 'TRR'
                                  || formBean.markerToDelete.markerType.name eq 'BINDSITE' || formBean.markerToDelete.markerType.name eq 'RR'
                                  || formBean.markerToDelete.markerType.name eq 'EBS' || formBean.markerToDelete.markerType.name eq 'TLNRR'
                                  || formBean.markerToDelete.markerType.name eq 'LCR' || formBean.markerToDelete.markerType.name eq 'DNAMO'
                                  || formBean.markerToDelete.markerType.name eq 'PROMOTER'}">
                          validateGeneWithOrthology(markerZdbIdToDelete, markerZdbIdToBeMergedInto, markerAbbrevToMergeInto);
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
            jQuery('#ignoreEnsemblGRCz10Id').hide();
            jQuery('#ignoreTranscript').hide();
            jQuery('#ignoreOrth').hide();
            jQuery('#ignoreMapping').hide();
            jQuery('#useSeqencesForSTR').hide();
            jQuery('#ignoreSeqencesForSTR').hide();
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

            if (null !== unspecifiedAlleleNameOfGene1 && typeof unspecifiedAlleleNameOfGene1 != 'undefined') {

                unspecifiedAllelesIgnored = false;

                jQuery('#validationUnspecifiedAllelesText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following unspecified allele:</h3>');
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

                if (null !== unspecifiedAlleleNameOfGene2 && typeof unspecifiedAlleleNameOfGene2 != 'undefined') {

                    jQuery('#validationUnspecifiedAllelesText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following unspecified allele:</h3>');
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
                    jQuery('#validationUnspecifiedAllelesText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has no unspecified allele.</h3>');
                    jQuery('#renameUnspecifiedAllele').show();
                }
            }

            checkUnspecifiedAllelesDone = 1;

            if (checkUnspecifiedAllelesDone == 1 && bothHavingUnspecifiedAllels == 0) {
                validateSTRs(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
            }
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
                                    jQuery('#validationSequenceTargetingReagentText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following sequence targeting reagents:</h3>');


                                jQuery('#validationSequenceTargetingReagentText').append("<div>"
                                        + "<a target='_blank' href='/"+data[sequenceTargetingReagent].id+"'>"+data[sequenceTargetingReagent].label+"</a>"
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
                                                        jQuery('#validationSequenceTargetingReagentText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following sequence targeting reagents:</h3>');

                                                    jQuery('#validationSequenceTargetingReagentText').append("<div>"
                                                            + "<a target='_blank' href='/"+data[sequenceTargetingReagent].id+"'>"+data[sequenceTargetingReagent].label+"</a>"
                                                            + "</div>");
                                                }
                                                if (numberOfSTRsForGene2 == 0)
                                                    jQuery('#validationSequenceTargetingReagentText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto +'">' + geneAbbrevMergedInto + '</a> has no sequence targeting reagent.</h3>');

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

            if (checkSTRsDone == 1) {
                validateAntibodies(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
            }
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
                                    jQuery('#validationAntibodyText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following antibody:</h3>');


                                jQuery('#validationAntibodyText').append("<div>"
                                        + "<a target='_blank' href='/"+data[antibody].id+"'>"+data[antibody].label+"</a>"
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
                                                        jQuery('#validationAntibodyText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following antibody:</h3>');

                                                    jQuery('#validationAntibodyText').append("<div>"
                                                            + "<a target='_blank' href='/"+data[antibody].id+"'>"+data[antibody].label+"</a>"
                                                            + "</div>");
                                                }
                                                if (numberOfAntibodiesForGene2 == 0)
                                                    jQuery('#validationAntibodyText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto +'">' + geneAbbrevMergedInto + '</a> has no antibody.</h3>');

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

            if (checkAntibodiesDone == 1) {
                validateNCBIgeneIds(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
            }
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
                jQuery('#validationNCBIgeneIdsText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following NCBI Gene Id:</h3>');
                for (var i = 0; i < ncbiGeneIdsOfGene1.length; i++) {
                    jQuery('#validationNCBIgeneIdsText').append('<div>'
                            + '<a target="_blank" href="' + ncbiGeneIdLinksOfGene1[i] +'">'
                            + ncbiGeneIdsOfGene1[i] + '</a>'
                            + '</div>');
                }

                jQuery('#validationNCBIgeneIdsText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following NCBI Gene Id:</h3>');
                for (var i = 0; i < ncbiGeneIdsOfGene2.length; i++) {
                    jQuery('#validationNCBIgeneIdsText').append('<div>'
                            + '<a target="_blank" href="' + ncbiGeneIdLinksOfGene2[i] +'">'
                            + ncbiGeneIdsOfGene2[i] + '</a>'
                            + '</div>');
                }
                jQuery('#ignoreNCBIGeneId').show();

            }

            checkNCBIgeneIdsDone = 1;

            if (checkNCBIgeneIdsDone == 1) {
                validateUniGeneIds(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
            }
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
                jQuery('#validationUniGeneIdsText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following UniGene Id:</h3>');
                for (var i = 0; i < uniGeneIdsOfGene1.length; i++) {
                    jQuery('#validationUniGeneIdsText').append('<div>'
                            + '<a target="_blank" href="' + uniGeneIdLinksOfGene1[i] +'">'
                            + uniGeneIdsOfGene1[i] + '</a>'
                            + '</div>');
                }

                jQuery('#validationUniGeneIdsText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following UniGene Id:</h3>');
                for (var i = 0; i < uniGeneIdsOfGene2.length; i++) {
                    jQuery('#validationUniGeneIdsText').append('<div>'
                            + '<a target="_blank" href="' + uniGeneIdLinksOfGene2[i] +'">'
                            + uniGeneIdsOfGene2[i] + '</a>'
                            + '</div>');
                }

                jQuery('#ignoreUniGeneId').show();
            }

            checkUniGeneIdsDone = 1;

            if (checkUniGeneIdsDone == 1) {
                validateVegaIds(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
            }
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
                jQuery('#validationVegaIdsText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following Vega Id:</h3>');
                for (var i = 0; i < vegaIdsOfGene1.length; i++) {
                    jQuery('#validationVegaIdsText').append('<div>'
                            + '<a target="_blank" href="' + vegaIdLinksOfGene1[i] +'">'
                            + vegaIdsOfGene1[i] + '</a>'
                            + '</div>');
                }

                jQuery('#validationVegaIdsText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following Vega Id:</h3>');
                for (var i = 0; i < vegaIdsOfGene2.length; i++) {
                    jQuery('#validationVegaIdsText').append('<div>'
                            + '<a target="_blank" href="' + vegaIdLinksOfGene2[i] +'">'
                            + vegaIdsOfGene2[i] + '</a>'
                            + '</div>');
                }

                jQuery('#ignoreVegaId').show();
            }

            checkVegaIdsDone = 1;

            if (checkVegaIdsDone == 1) {
                validateEnsemblGRCz10Ids(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
            }
        };

        var validateEnsemblGRCz10Ids = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {
            var checkEnsemblGRCz10IdsDone = 0;
            var EnsemblGRCz10OfGene1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=EnsemblGRCz10&markerZdbId=" + geneIDdelete,
                dataType: "json",
                async: false
            }).responseText);

            var EnsemblGRCz10IdsOfGene1 = new Array();
            var EnsemblGRCz10LinksOfGene1 = new Array();
            for (EnsemblGRCz10 in EnsemblGRCz10OfGene1) {
                EnsemblGRCz10IdsOfGene1.push(EnsemblGRCz10OfGene1[EnsemblGRCz10].accessionNumber);
                EnsemblGRCz10LinksOfGene1.push(EnsemblGRCz10OfGene1[EnsemblGRCz10].url);
            }
            var EnsemblGRCz10OfGene2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-accession?db=EnsemblGRCz10&markerZdbId=" + geneZdbIdMergedInto,
                dataType: "json",
                async: false
            }).responseText);

            var EnsemblGRCz10IdsOfGene2 = new Array();
            var EnsemblGRCz10LinksOfGene2 = new Array();
            for (EnsemblGRCz10 in EnsemblGRCz10OfGene2) {
                EnsemblGRCz10IdsOfGene2.push(EnsemblGRCz10OfGene2[EnsemblGRCz10].accessionNumber);
                EnsemblGRCz10LinksOfGene2.push(EnsemblGRCz10OfGene2[EnsemblGRCz10].url);
            }
            var differentEnsemblGRCz10Ids = false;
            if(EnsemblGRCz10IdsOfGene1.length !== 0 && EnsemblGRCz10IdsOfGene2.length !== 0) {
                if (EnsemblGRCz10IdsOfGene1.length !== EnsemblGRCz10IdsOfGene2.length) {
                    differentEnsemblGRCz10Ids = true;
                } else {
                    for (var i = 0; i < EnsemblGRCz10IdsOfGene1.length; i++) {
                        if (EnsemblGRCz10IdsOfGene1[i] !== EnsemblGRCz10IdsOfGene2[i])
                            differentEnsemblGRCz10Ids = true;
                    }
                }
            }

            if (differentEnsemblGRCz10Ids) {
                EnsemblGRCz10IdsIgnored = false;
                jQuery('#validationEnsemblGRCz10IdsText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following Ensembl(GRCz10) Id:</h3>');
                for (var i = 0; i < EnsemblGRCz10IdsOfGene1.length; i++) {
                    jQuery('#validationEnsemblGRCz10IdsText').append('<div>'
                            + '<a target="_blank" href="' + EnsemblGRCz10LinksOfGene1[i] +'">'
                            + EnsemblGRCz10IdsOfGene1[i] + '</a>'
                            + '</div>');
                }

                jQuery('#validationEnsemblGRCz10IdsText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following Ensembl(GRCz10) Id:</h3>');
                for (var i = 0; i < EnsemblGRCz10IdsOfGene2.length; i++) {
                    jQuery('#validationEnsemblGRCz10IdsText').append('<div>'
                            + '<a target="_blank" href="' + EnsemblGRCz10LinksOfGene2[i] +'">'
                            + EnsemblGRCz10IdsOfGene2[i] + '</a>'
                            + '</div>');
                }

                jQuery('#ignoreEnsemblGRCz10Id').show();
            }

            checkEnsemblGRCz10IdsDone = 1;

            if (checkEnsemblGRCz10IdsDone == 1) {
                <c:if test="${formBean.markerToDelete.markerType.name eq 'GENE' || formBean.markerToDelete.markerType.name eq 'GENEP'
                              || formBean.markerToDelete.markerType.name eq 'LINCRNAG' || formBean.markerToDelete.markerType.name eq 'NCRNAG'
                              || formBean.markerToDelete.markerType.name eq 'SNORNAG' || formBean.markerToDelete.markerType.name eq 'LNCRNAG'
                              || formBean.markerToDelete.markerType.name eq 'SCRNAG' || formBean.markerToDelete.markerType.name eq 'MIRNAG'
                              || formBean.markerToDelete.markerType.name eq 'TRNAG' || formBean.markerToDelete.markerType.name eq 'SRPRNAG'
                              || formBean.markerToDelete.markerType.name eq 'PIRNAG' || formBean.markerToDelete.markerType.name eq 'RRNAG'}">
                     validateGeneWithTranscript(markerZdbIdToDelete, markerZdbIdToBeMergedInto, markerAbbrevToMergeInto);
                </c:if>
                <c:if test="${formBean.markerToDelete.markerType.name eq 'NCCR' || formBean.markerToDelete.markerType.name eq 'TFBS'
                              || formBean.markerToDelete.markerType.name eq 'RNAMO' || formBean.markerToDelete.markerType.name eq 'NCBS'
                              || formBean.markerToDelete.markerType.name eq 'MDNAB' || formBean.markerToDelete.markerType.name eq 'ENHANCER'
                              || formBean.markerToDelete.markerType.name eq 'EMR' || formBean.markerToDelete.markerType.name eq 'HMR'
                              || formBean.markerToDelete.markerType.name eq 'NUCMO' || formBean.markerToDelete.markerType.name eq 'BR'
                              || formBean.markerToDelete.markerType.name eq 'PROTBS' || formBean.markerToDelete.markerType.name eq 'TRR'
                              || formBean.markerToDelete.markerType.name eq 'BINDSITE' || formBean.markerToDelete.markerType.name eq 'RR'
                              || formBean.markerToDelete.markerType.name eq 'EBS' || formBean.markerToDelete.markerType.name eq 'TLNRR'
                              || formBean.markerToDelete.markerType.name eq 'LCR' || formBean.markerToDelete.markerType.name eq 'DNAMO'
                              || formBean.markerToDelete.markerType.name eq 'PROMOTER'}">
                      validateGeneWithMappingInfo(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
                </c:if>
            }
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
                                    jQuery('#validationTranscriptText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following transcripts:</h3>');


                                jQuery('#validationTranscriptText').append("<div>"
                                        + "<a target='_blank' href='/"+data[transcript].zdbID+"'>"+data[transcript].name+"</a>"
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
                                                        jQuery('#validationTranscriptText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following transcripts:</h3>');

                                                    jQuery('#validationTranscriptText').append("<div>"
                                                            + "<a target='_blank' href='/"+data[transcript].zdbID+"'>"+data[transcript].name+"</a>"
                                                            + "</div>");
                                                }
                                                if (numberOfTranscriptsOfMarker2 == 0)
                                                    jQuery('#validationTranscriptText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto +'">' + geneAbbrevMergedInto + '</a> has no transcript.</h3>');

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

            if (checkTranscriptsDone == 1) {
                validateGeneWithMappingInfo(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
            }

        };

        var validateGeneWithOrthology = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {

            var numberOfOrthology = 0;
            var numberOfOrthologyOfMarker2 = 0;
            var checkOrthologyEvidenceAndPubDone = 0;
            var blockMergeDueToOrth = "No";
            orthologyIgnored = false;

            var orthologyData1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-orthology-for-geneId?geneZdbId=" + geneIDdelete,
                dataType: "json",
                async: false
            }).responseText);

            if (orthologyData1.length > 0) {
                var orthologyData2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-orthology-for-geneId?geneZdbId=" + geneZdbIdMergedInto,
                    dataType: "json",
                    async: false
                }).responseText);

                if (orthologyData2.length > 0) {
                    for (orthology1 in orthologyData1) {
                        numberOfOrthology++;
                        if (numberOfOrthology == 1)
                            jQuery('#validationOrthologyText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following orthology:</h3>');


                        jQuery('#validationOrthologyText').append("<div>"
                                +orthologyData1[orthology1].organism+"&nbsp;&nbsp;&nbsp;"+orthologyData1[orthology1].orthologySymbol+"&nbsp;&nbsp;&nbsp;"+orthologyData1[orthology1].evidenceCode+"&nbsp;&nbsp;&nbsp;"+orthologyData1[orthology1].publication
                                + "</div>");
                        for (orthology2 in orthologyData2) {
                            if (orthologyData1[orthology1].orthologySymbol == orthologyData2[orthology2].orthologySymbol &&
                                    (orthologyData1[orthology1].evidenceCode != orthologyData2[orthology2].evidenceCode || orthologyData1[orthology1].publication != orthologyData2[orthology2].publication) &&
                                    !(orthologyData1[orthology1].evidenceCode == orthologyData2[orthology2].evidenceCode && orthologyData1[orthology1].publication == orthologyData2[orthology2].publication)) {
                                blockMergeDueToOrth = "Yes";
                                break;
                            }
                        }
                    }

                    for (orthology2 in orthologyData2) {
                        numberOfOrthologyOfMarker2++;
                        if (numberOfOrthologyOfMarker2 == 1)
                            jQuery('#validationOrthologyText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following orthology:</h3>');

                        jQuery('#validationOrthologyText').append("<div>"
                                +orthologyData2[orthology2].organism+"&nbsp;&nbsp;&nbsp;"+orthologyData2[orthology2].orthologySymbol+"&nbsp;&nbsp;&nbsp;"+orthologyData2[orthology2].evidenceCode+"&nbsp;&nbsp;&nbsp;"+orthologyData2[orthology2].publication
                                + "</div>");
                    }
                }
            }

            checkOrthologyEvidenceAndPubDone = 1;

            if (checkOrthologyEvidenceAndPubDone == 1) {

                if (blockMergeDueToOrth == "Yes") {
                    jQuery('#blockMerge').append('<h3>You cannot merge these two zebrafish genes because they have orthology data to the same human, mouse and/or fly gene.<br/>You need to move that human, mouse and/or orthology data manually before merging the zebrafish genes.</h3>');

                } else {
                    if (orthologyData1.length > 0) {
                        jQuery('#ignoreOrth').show();
                    } else {
                        orthologyIgnored = true;
                    }
                    <c:if test="${formBean.markerToDelete.markerType.name eq 'NCCR' || formBean.markerToDelete.markerType.name eq 'TFBS'
                                  || formBean.markerToDelete.markerType.name eq 'RNAMO' || formBean.markerToDelete.markerType.name eq 'NCBS'
                                  || formBean.markerToDelete.markerType.name eq 'MDNAB' || formBean.markerToDelete.markerType.name eq 'ENHANCER'
                                  || formBean.markerToDelete.markerType.name eq 'EMR' || formBean.markerToDelete.markerType.name eq 'HMR'
                                  || formBean.markerToDelete.markerType.name eq 'NUCMO' || formBean.markerToDelete.markerType.name eq 'BR'
                                  || formBean.markerToDelete.markerType.name eq 'PROTBS' || formBean.markerToDelete.markerType.name eq 'TRR'
                                  || formBean.markerToDelete.markerType.name eq 'BINDSITE' || formBean.markerToDelete.markerType.name eq 'RR'
                                  || formBean.markerToDelete.markerType.name eq 'EBS' || formBean.markerToDelete.markerType.name eq 'TLNRR'
                                  || formBean.markerToDelete.markerType.name eq 'LCR' || formBean.markerToDelete.markerType.name eq 'DNAMO'
                                  || formBean.markerToDelete.markerType.name eq 'PROMOTER'}">
                           validateSTRs(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
                    </c:if>
                    <c:if test="${formBean.markerToDelete.markerType.name eq 'GENE' || formBean.markerToDelete.markerType.name eq 'GENEP'
                                  || formBean.markerToDelete.markerType.name eq 'LINCRNAG' || formBean.markerToDelete.markerType.name eq 'NCRNAG'
                                  || formBean.markerToDelete.markerType.name eq 'SNORNAG' || formBean.markerToDelete.markerType.name eq 'LNCRNAG'
                                  || formBean.markerToDelete.markerType.name eq 'SCRNAG' || formBean.markerToDelete.markerType.name eq 'MIRNAG'
                                  || formBean.markerToDelete.markerType.name eq 'TRNAG' || formBean.markerToDelete.markerType.name eq 'SRPRNAG'
                                  || formBean.markerToDelete.markerType.name eq 'PIRNAG' || formBean.markerToDelete.markerType.name eq 'RRNAG'}">
                           validateUnspecifiedAlleles(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
                    </c:if>
                }

            }
        };

        var validateEap = function(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto) {

            var numberOfEapPubs = 0;

            var eapPubs = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-eap-publication-for-geneId?geneZdbId=" + geneIDdelete,
                dataType: "json",
                async: false
            }).responseText);

            if (eapPubs.length > 0) {
                for (eapPub in eapPubs) {
                    numberOfEapPubs++;
                    if (numberOfEapPubs == 1)
                        jQuery('#blockMerge').append('<h3>You cannot merge the gene with expression as phenotype data to another gene; you have to do some manual work first. <br/><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has been in the following publication(s) with expression as phenotype data:</h3>');


                    jQuery('#blockMerge').append("<div>"
                            + '<a target="_blank" href="/' + eapPubs[eapPub].publicationZdbId +'">'
                            + eapPubs[eapPub].linkContent + '</a>'
                            + '</div>');
                }
            } else {
                validateGeneWithOrthology(geneIDdelete, geneZdbIdMergedInto, geneAbbrevMergedInto);
            }
        }

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

                jQuery('#validationMapInfoText').append('<h3><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following mapping info:</h3>');
                jQuery('#validationMapInfoText').append("<div>" + chromosomeGeneDelete + "</div>");
                jQuery('#validationMapInfoText').append('<h3><a target="_blank" href="/' + geneZdbIdMergedInto + '">' + geneAbbrevMergedInto + '</a> has the following mapping info:</h3>');
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
                    jQuery('#validationSTRText').append('<h4><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following target gene:</h4>');
                    for (var i = 0; i < targetGeneSymbolsOfSTR1.length; i++) {
                        jQuery('#validationSTRText').append('<div>'
                                + '<a target="_blank" href="/' + targetIDsOfSTR1[i] +'">'
                                + targetGeneSymbolsOfSTR1[i] + '</a>'
                                + '</div>');
                    }
                } else {
                    jQuery('#validationSTRText').append('<h4><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has no target gene.</h4>');
                }

                if (targetGeneSymbolsOfSTR2.length > 0) {
                    jQuery('#validationSTRText').append('<h4><a target="_blank" href="/' + strZdbIdMergedInto + '">' + strAbbrevMergedInto + '</a> has the following target gene:</h4>');
                    for (var i = 0; i < targetGeneSymbolsOfSTR2.length; i++) {
                        jQuery('#validationSTRText').append('<div>'
                                + '<a target="_blank" href="/' + targetIDsOfSTR2[i] +'">'
                                + targetGeneSymbolsOfSTR2[i] + '</a>'
                                + '</div>');
                    }
                } else {
                    jQuery('#validationSTRText').append('<h4><a target="_blank" href="/' + strZdbIdMergedInto + '">' + strAbbrevMergedInto + '</a> has no target gene.</h4>');
                }
            } else {
                validateFishListForMergingSRTs(strIDdelete, strZdbIdMergedInto, strAbbrevMergedInto);
            }
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

            differentSequence = true;
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
                jQuery('#validationSTRText').append('<h4><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> has the following sequence:</h4>');
                if(isTALEN) {
                    jQuery('#validationSTRText').append('<div>Sequence 1: '+ sequenceSTR1 + '</div>');
                    jQuery('#validationSTRText').append('<div>Sequence 2: '+ secondSequenceSTR1 + '</div>');
                } else {
                    jQuery('#validationSTRText').append('<div>'+ sequenceSTR1 + '</div>');
                }
                if(!differentTargets)
                    jQuery('#useSeqencesForSTR').show();

                jQuery('#validationSTRText2').append('<h4><a target="_blank" href="/' + strZdbIdMergedInto + '">' + strAbbrevMergedInto + '</a> has the following sequence:</h4>');
                if(isTALEN) {
                    jQuery('#validationSTRText2').append('<div>Sequence 1: '+ sequenceSTR2 + '</div>');
                    jQuery('#validationSTRText2').append('<div>Sequence 2: '+ secondSequenceSTR2 + '</div>');
                } else {
                    jQuery('#validationSTRText2').append('<div>'+ sequenceSTR2 + '</div>');
                }
                if(!differentTargets)
                    jQuery('#ignoreSeqencesForSTR').show();
            }
        };

        var validateFishListForMergingSRTs = function(strIDdelete, strZdbIdMergedInto, strAbbrevMergedInto) {

            var str1UsedInFish = jQuery.ajax({url: "/action/marker/sequenceTargetingReagent-used-in-fish?sequenceTargetingReagentZdbId=" + strIDdelete,
                async: false
            }).responseText;

            if (str1UsedInFish === "Yes") {
                var fishListSTR1 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-fish-for-sequenceTargetingReagentZdbId?sequenceTargetingReagentZdbId=" + strIDdelete,
                    dataType: "json",
                    async: false
                }).responseText);

                var fishNamesOfSTR1 = new Array();
                var fishIDsOfSTR1 = new Array();
                for (fish in fishListSTR1) {
                    fishNamesOfSTR1.push(fishListSTR1[fish].name);
                    fishIDsOfSTR1.push(fishListSTR1[fish].id);
                }
            }

            var str2UsedInFish = jQuery.ajax({url: "/action/marker/sequenceTargetingReagent-used-in-fish?sequenceTargetingReagentZdbId=" + strZdbIdMergedInto,
                async: false
            }).responseText;

            if (str2UsedInFish === "Yes") {
                var fishListSTR2 = jQuery.parseJSON(jQuery.ajax({url: "/action/marker/get-fish-for-sequenceTargetingReagentZdbId?sequenceTargetingReagentZdbId=" + strZdbIdMergedInto,
                    dataType: "json",
                    async: false
                }).responseText);

                var fishNamesOfSTR2 = new Array();
                var fishIDsOfSTR2 = new Array();
                for (fish in fishListSTR2) {
                    fishNamesOfSTR2.push(fishListSTR2[fish].name);
                    fishIDsOfSTR2.push(fishListSTR2[fish].id);
                }
            }

            if(str1UsedInFish === "Yes" && str2UsedInFish === "Yes") {
                if (fishIDsOfSTR1.length !== fishIDsOfSTR2.length) {
                    differentFish = true;
                } else {
                    for (var i = 0; i < fishIDsOfSTR1.length; i++) {
                        if (fishIDsOfSTR1[i] !== fishIDsOfSTR2[i])
                            differentFish = true;
                    }
                }
            } else if (str1UsedInFish === "Yes" && str2UsedInFish === "No") {
                differentFish = true;
            } else if (str1UsedInFish === "No" && str2UsedInFish === "Yes") {
                differentFish = false;
            } else {
                differentFish = false;
            }



            if (differentFish) {
                jQuery('#validationSTRText3').append('<h4>Merging these two sequence targeting reagents is not allowed because they have been with different fish.</h4>');
                if (str1UsedInFish === "Yes") {
                    jQuery('#validationSTRText3').append('<h4><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> is associated with the following fish:</h4>');
                    for (var i = 0; i < fishNamesOfSTR1.length; i++) {
                        jQuery('#validationSTRText3').append('<div>'
                                + '<a target="_blank" href="/' + fishIDsOfSTR1[i] +'">'
                                + fishNamesOfSTR1[i] + '</a>'
                                + '</div>');
                    }
                } else {
                    jQuery('#validationSTRText3').append('<h4><a target="_blank" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> is associated with no fish.</h4>');
                }

                if (str2UsedInFish === "Yes") {
                    jQuery('#validationSTRText3').append('<h4><a target="_blank" href="/' + strZdbIdMergedInto + '">' + strAbbrevMergedInto + '</a> is associated with the following fish:</h4>');
                    for (var i = 0; i < fishNamesOfSTR2.length; i++) {
                        jQuery('#validationSTRText3').append('<div>'
                                + '<a target="_blank" href="/' + fishIDsOfSTR2[i] +'">'
                                + fishNamesOfSTR2[i] + '</a>'
                                + '</div>');
                    }
                } else {
                    jQuery('#validationSTRText3').append('<h4><a target="_blank" href="/' + strZdbIdMergedInto + '">' + strAbbrevMergedInto + '</a> is associated with no fish.</h4>');
                }
            }

            if (!differentFish) {
                jQuery('#submitMerge').removeAttr('disabled');
                validateSequencesForMergingSRTs(strIDdelete, strZdbIdMergedInto, strAbbrevMergedInto);
            }

        }

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

        function ignoreEnsemblGRCz10Ids(formObj) {
            EnsemblGRCz10IdsIgnored = true;
            jQuery('#validationEnsemblGRCz10IdsText').hide();
            jQuery('#ignoreEnsemblGRCz10Id').hide();

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

        function ignoreSequencesForSTRs(formObj) {
            differentSequence = false;
            jQuery('#validationSTRText').hide();
            jQuery('#useSeqencesForSTR').hide();
            jQuery('#validationSTRText2').hide();
            jQuery('#ignoreSeqencesForSTR').hide();

            enableMerge();
        }

        function useSequence(formObj) {
            differentSequence = false;
            jQuery('#validationSTRText').hide();
            jQuery('#useSeqencesForSTR').hide();
            jQuery('#validationSTRText2').hide();
            jQuery('#ignoreSeqencesForSTR').hide();

            jQuery.ajax({type: "POST",
                url: "/action/marker/update-str-sequence?source=${formBean.zdbIDToDelete}&target=" + mergedIntoId,
                success: function() {
                    enableMerge();
                },
                error: function() {
                    alert('There was a problem with the action ');
                }
            });
        }

        function enableMerge() {
            if (unspecifiedAllelesIgnored && sequenceTargetingReagentsIgnored && antibodiesIgnored && ncbiGeneIdsIgnored && uniGeneIdsIgnored && vegaIdsIgnored && EnsemblGRCz10IdsIgnored && transcriptsIgnored && orthologyIgnored && mapInfoIgnored && !differentSequence && !differentFish && !differentTargets) {
                jQuery('#submitMerge').removeAttr('disabled');
            }
        }

        function goToEdit() {
            url = '/action/str/'+ mergedIntoId +'/edit';
            window.location.replace(url);
        }

    </script>

    <c:if test="${formBean.markerToDelete.markerType.name ne 'ATB'}">
        <form id="mergeTheGenes" action="/cgi-bin/merge_markers.pl">
            <input type="hidden" id="zdbIDToDelete" name="OID" value="${formBean.zdbIDToDelete}">
            <input type="hidden" id="merge_oid" name="merge_oid" value="none">
            <input type="hidden" id="intoMarkerAbbrev" name="intoMarkerAbbrev" value="none">
            <p/>
            Merge <a target="_blank" class="external" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> into
          <span id="into"><input id="mergedIntoGeneAbbrev" value="" type="text" size="15" />
          </span>&nbsp;&nbsp;
          <span id="warning" style="font-size: large; color: red">(Warning: <a target="_blank" class="external" href="/${formBean.zdbIDToDelete}">${formBean.markerToDeleteViewString}</a> will be deleted after the merging!)
          </span>
            <br/><br/>
            <input type="button" value="Merge these two markers" id="submitMerge" title="Perform the merge action">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input type="button" value="Cancel" id="cancelMerge" onclick="window.history.back();" title="Cancel the merge and go back to gene page">
        </form>
        <div id="blockMerge"></div>
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
        <div id="validationEnsemblGRCz10IdsText"></div>
        <form id="ignoreEnsemblGRCz10Id">
            <input type="button" value="Ignore Ensembl(GRCz10) Ids" onclick="ignoreEnsemblGRCz10Ids(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, all the above Ensembl(GRCz10) Ids will be associated with the gene retained.">
        </form>
        <div id="validationTranscriptText"></div>
        <form id="ignoreTranscript">
            <input type="button" value="Ignore Transcripts" onclick="ignoreTranscripts(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the above transcripts (no automatic re-naming) with ${formBean.markerToDeleteViewString} will be associated with the gene retained.">
        </form>
        <div id="validationOrthologyText"></div>
        <form id="ignoreOrth">
            <input type="button" value="Ignore Orthology" onclick="ignoreOrthology(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the orthology data of ${formBean.markerToDeleteViewString} will be associated with the gene retained.">
        </form>
        <div id="validationMapInfoText"></div>
        <form id="ignoreMapping">
            <input type="button" value="Ignore Mapping Info" onclick="ignoreMappingInfo(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the mapping info with ${formBean.markerToDeleteViewString} will be associated with the gene retained regardless of whether it conflicts or not.">
        </form>
        <div id="validationSTRText3"></div>
        <div id="validationSTRText"></div>
        <form id="useSeqencesForSTR">
            <input type="button" value="Use the above target sequence" onclick="useSequence(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the above target sequence will be associated with the sequence targeting reagent retained.">
        </form>
        <div id="validationSTRText2"></div>
        <form id="ignoreSeqencesForSTR">
            <input type="button" value="Use the above target sequence" onclick="ignoreSequencesForSTRs(this);" title="By clicking this button, you acknowledge the fact that after the merge is done, the above target sequence will be associated with the sequence targeting reagent retained.">
            &nbsp;&nbsp;<input type="button" value="Edit the above target sequence" onclick="goToEdit();" title="Edit the above sequence">
        </form>
    </c:if>


    <c:if test="${formBean.markerToDelete.markerType.name eq 'ATB'}">
        <form:form commandName="formBean" action="/action/marker/merge" onsubmit="return confirmMergeAntibody();" >
            <form:hidden path="zdbIDToDelete"/>
            <form:errors path="*" cssClass="error"/><br>
            <table>
                <tr>
                    <td valign="top">
                        Merge <a target="_blank" class="external"
                                 href="/${formBean.zdbIDToDelete}">
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


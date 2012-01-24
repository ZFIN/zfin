<%@ tag import="org.zfin.fish.presentation.SortBy" %>
<%@ tag import="org.zfin.framework.presentation.PaginationBean" %>
<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="formBean" type="org.zfin.fish.presentation.FishSearchFormBean" required="true" %>

<style type="text/css">
    .search-form-top-bar {
        border: 1px solid #ddd;
        background: #efefef;
        border-radius: 8px;
        padding: 3px;
        padding-bottom: 7px;
    }

    .search-form-bottom-bar {
        border: 1px solid #ddd;
        background: #efefef;
        border-radius: 8px;
        padding: 3px;
        margin-bottom: 1em;
    }

    .search-form-title {
        font-size: large;
        margin-left: 2px;
        color: #666;
    }

    .search-form-your-input-welcome {
        float: right;
    }

    .search-form-alternate-link {
        font-weight: bold;
        font-size: small;
        color: #888;
    }

    .search-form-alternate-link a {
        opacity: .6;
    }


</style>

<div class="search-form-top-bar">
    <span class="search-form-title">
        Search for Mutants / Morphants / Tg
    </span>
    <a href="/ZFIN/misc_html/fish_search_tips.html" class="popup-link help-popup-link"></a>

    <div class="search-form-your-input-welcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Fish search"/>
            <tiles:putAttribute name="subjectID" value=""/>
        </tiles:insertTemplate>
    </div>

</div>

<form:form method="Get" action="do-search" commandName="formBean" name="fishsearchform" id="fish-search-form"
           onsubmit="return false;">

<table width="100%" class="error-box">
    <tr>
        <td>
            <form:errors path="*" cssClass="Error"/>
        </td>
    </tr>
</table>

<style type="text/css">

    #anatomyTermList {
        margin-left: 10px;
        display: none;
        width: 300px;
        border: 1px solid #bbbbdd;
        background-color: #eeeef8;
        border-radius: 5px;
        padding: 5px;
        z-index: 20;
    }

    .triangle {
        z-index: 19;
        position: relative;
        left: 30px;
        top: 11px;
        width: 0;
        height: 0;
        border-left: 10px solid transparent;
        border-right: 10px solid transparent;
        border-bottom: 10px solid #eeeef8;
        border-top: 0;

    }

    .triangle-border {
        z-index: 18;
        position: relative;
        left: 30px;
        width: 0;
        height: 0;
        border-left: 10px solid transparent;
        border-right: 10px solid transparent;
        border-bottom: 10px solid #bbbbdd;
        border-top: 0;
    }
</style>

<table width="100%" class="searchform">
    <tr>
        <th nowrap="nowrap">
            <label class="namesearchLabel" for="geneOrFeatureName" title="Gene/Allele Name">Gene/Allele Name</label>
        </th>
        <td>
            <form:input id="geneOrFeatureName" path="geneOrFeatureName" size="30"
                        onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
		                        if (k == 13 ) { submitForm(1);}  "/>
        </td>
        <td colspan="2">
            <span class="bold">Mutation Type:</span>
            <form:select path="mutationType" multiple="single">
                <form:options items="${formBean.mutationTypeList}"/>
            </form:select>
        </td>
    </tr>
    <tr>
        <th>
            <label class="namesearchLabel">Phenotype Anatomy</label>
        </th>
        <td>
            <form:hidden path="anatomyTermIDs"/>
            <form:hidden path="anatomyTermNames"/>
            <script type="text/javascript">
                var LookupProperties = {
                    inputDiv:"anatomyTermInput",
                    termListDiv:"anatomyTermList",
                    inputName:"searchTerm",
                    showError:true,
                    <c:if test='${formBean.anatomyTermNames != null}' >
                    previousTableValues:"${formBean.anatomyTermNames}",
                    </c:if>
                    hiddenNames:"anatomyTermNames",
                    hiddenIds:"anatomyTermIDs",
                    type:"<%= LookupComposite.GDAG_TERM_LOOKUP %>",
                    ontologyName:"<%= Ontology.ANATOMY %>",
                    width:30,
                    wildcard:false,
                    useTermTable:true
                }
            </script>
            <style type="text/css">
                .accessoryLabel {
                    font-size: .75em;
                }

            </style>

            <link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
            <script language="javascript"
                    src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js" type=""></script>
            <div id="anatomyTermInput"></div>


        </td>
        <td rowspan=2>
            <span class="bold">Filters:</span>
            <table>
                <tr>
                    <td>
                        <form:radiobutton path="filter1" id="morphantsOnly" value="morphantsOnly"
                                          autocomplete="off"/>
                        <label for="morphantsOnly">Show only morphants</label> <br/>

                        <form:radiobutton path="filter1" id="noMorphants" value="excludeMorphants"
                                          autocomplete="off"/>
                        <label for="noMorphants">Exclude morphants</label> <br/>

                        <form:radiobutton path="filter1" id="transgenicsOnly" value="tgOnly"
                                          autocomplete="off"/>
                        <label for="transgenicsOnly">Show only transgenics</label> <br/>

                        <form:radiobutton path="filter1" id="excludeTg" value="excludeTg"
                                          autocomplete="off"/>
                        <label for="excludeTg">Exclude transgenics</label> <br/>

                        <form:radiobutton path="filter1" id="showAll" value="showAll"
                                          autocomplete="off"/>
                        <label for="showAll">Show All</label>
                    </td>
                </tr>
            </table>
            <form:hidden path="sortBy" id="sort-by"/>
            <form:hidden path="maxDisplayRecords" id="max-display-records-hidden" cssClass="auto-submit"/>
        </td>
    </tr>
    <tr>
        <th></th>
        <td>
            <div id="anatomyTermList">
                <a style="display:none; float: right;" id="term-list-remove-all-link"
                   href="javascript:clearTable();decorateTermList();">remove all</a>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="2"></td>
        <td>
            <div class="search-form-alternate-link">
                Miss our old search form?
                <a href="/cgi-bin/webdriver?MIval=aa-fishselect.apg">It's still available</a>
            </div>
        </td>
    </tr>
</table>

<div class="search-form-bottom-bar" style="text-align:right">
    <input value="Search" onclick="submitForm(1)" type="button">
    <input value="Reset" type="button" onclick="call_reset()">
    <img style="display:none;" id="fish-form-loading-notify" src="/images/ajax-loader1.gif"/>
</div>

<script type="text/javascript">

    //experimental, submit form on any form element change
    jQuery(document).ready(function () {
        jQuery('#fish-search-form .auto-submit').change(function () {
            submitForm(1)
        });
        jQuery('input[name=filter1]:checked + label').addClass('selected-radio-label');

        //set the results per page pulldowns on top and bottom
        jQuery('#max-display-records-top, #max-display-records-bottom').val(jQuery('#max-display-records-hidden').val());

        //handle display details of the term list
        //this is hacky - delay a tad so that it'll be after GWT has loaded.  yuck.
        setTimeout(function () {
            decorateTermList();
        }, 200);
        //try again at longer intervals... just in case.  nothing bad happens if we run this more than once
        setTimeout(function () {
            decorateTermList();
        }, 500);
        setTimeout(function () {
            decorateTermList();
        }, 1000);
        setTimeout(function () {
            decorateTermList();
        }, 3000);


    });

    jQuery('input[name=filter1]').change(function () {
        jQuery('input[name=filter1] + label').removeClass('selected-radio-label');
        jQuery('input[name=filter1]:checked + label').addClass('selected-radio-label');
    });


    //decorate anatomy box when it has terms, add a remove all link when there's 2 or more

    jQuery('input[name=anatomyTermIDs]').change(function () {
        decorateTermList();
    });

    function decorateTermList() {
        termCount = jQuery('#anatomyTermList .gwt-Hyperlink').size();
        if (termCount == 0) {
            jQuery('#anatomyTermList').hide();
            jQuery('#term-list-remove-all-link').hide();

        } else if (termCount == 1) {
            jQuery('#anatomyTermList').show();
            jQuery('#term-list-remove-all-link').hide();

        } else {
            jQuery('#anatomyTermList').show();
            jQuery('#term-list-remove-all-link').show();
        }

    }

    function call_reset() {

        jQuery('input[name=geneOrFeatureName]').val('');
        jQuery('#mutationType').val('0');
        jQuery('input[name=filter1]').filter('[value="showAll"]').attr('checked', true);
        jQuery('input[name=filter1]').change();
        jQuery('#max-display-records-hidden').val('20');
        jQuery('input[name=sortBy]').val('<%= SortBy.BEST_MATCH %>');
        jQuery('#sort-by-pulldown').val('0');

        clearTable();
        decorateTermList();
    }


    function submitForm(page) {
        jQuery('#fish-form-loading-notify').show();
        var pauseIncrement = 500;  // wait up to 4 seconds
        var currentTime = new Date();  // wait up to 4 seconds
        try {
            //validateLookup();
        }
        catch (e) {
            // if not defined, then keep going
        }
        var form = document.getElementById("fish-search-form");
        var pageField = document.getElementById("<%= PaginationBean.PAGE %>");
        if (pageField != null)
            pageField.value = page;
        form.submit();
    }

    function doValidateAndSubmit(currentTime) {
        var maxTime = 5000;
        var diff = (new Date()).getMilliseconds() - currentTime;
        var inputElement = document.getElementById(LookupProperties.inputName);
        termStatus = getValidationStatus();

        // either there was an asynchronous connection failure, or we are still waiting,
        // in which case we should resubmit the query
        if (termStatus == "LOOKING" || termStatus == "FAILURE" && diff < maxTime) {
            validateLookup();
            return;
        }
        // submit regardless of what comes back
        else {
            clearInterval(intervalId);
            document.getElementById("Antibody Search").submit();
        }
    }

</script>


</form:form>

<%@ tag import="org.zfin.framework.presentation.PaginationBean" %>
<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.mutant.presentation.ConstructSearchFormBean" required="true" %>



<form:form method="Get" action="do-search" commandName="formBean" name="constructsearchform" id="construct-search-form" onsubmit="return false;">

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

<style type="text/css">
    .button {
        background: url("/images/toggle.gif") no-repeat;
    }

</style>

<table width="100%"class="searchform">
    <tr>
        <td width=35>
            <label class="namesearchLabel" for="construct" title="Construct Name">Construct</label>
        </td>
        <%--<td colspan=4>&nbsp;&nbsp;&nbsp;&nbsp; </td>--%>
        <td>

            <form:input id="construct" path="construct" size="50"   placeholder="shha, mCherry, lox, UAS, atoh7:GFP, Tg(actc1b:RFP) "
                        cssClass="default-input"
                        onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
		                        if (k == 13 ) { submitForm(1);}  "/>

        </td>
        <td></td>

        <td>


            <form:checkbox path="allTypes" id="allConstructs" value="allConstructs"
                           autocomplete="off" checked="true" onclick="checkUncheckAll()"/> All Constructs</label>


        </td>

    </tr>
    <form:hidden path="maxDisplayRecords" id="max-display-records-hidden" cssClass="auto-submit"/>
    <input type="hidden" name="page" id="page"/>






    <%--<table width="100%"class="searchform" id="advsearch">--%>

        <tr>
            <td width=35><label class="namesearchLabel" for="promoterOfGene" title="Gene/Allele Name">Promoter</label></td>
            <%--</th>--%>
            <td>

                <form:input id="promoterOfGene" path="promoterOfGene" size="30"  placeholder="UAS, shha, ntl"
                            onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
		                        if (k == 13 ) { submitForm(1);}  "/>
            </td>
            <td>  </td>



            <td align=left>


                &nbsp;&nbsp;&nbsp;<form:checkbox path="allTg" id="tgConstruct" value="tgConstruct"
                                                 autocomplete="off"  onclick="UncheckAll(allTg)" />    Transgenic Construct


            </td>
        </tr>

        <tr>
            <td nowrap="nowrap"><label class="namesearchLabel" for="drivesExpressionOfGene" title="Drives Expression of  Gene">Coding Sequence</label></td>
            <td><form:input id="drivesExpressionOfGene" path ="drivesExpressionOfGene" size="30" placeholder="actb, GFP, mCherry, Gal4"
                            onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
                if (k == 13 ) { submitForm(1);}  "/></td>

            <td>  </td>
            <td>

                &nbsp;&nbsp;&nbsp;<form:checkbox path="allEt" id="etConstruct" value="etConstruct"
                                                 autocomplete="off"   onclick="UncheckAll(allEt)" /> Enhancer Trap Construct
            </td>



        </tr>


        <tr>

                <td nowrap="nowrap">    <label class="namesearchLabel" for="affectedGene" title="Affected Gene">Inserted in gene</label></td>

                <td>
                    <form:input id="affectedGene" path="affectedGene" size="30"   placeholder="sox9, tbx, tnnt"
                                onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
                if (k == 13 ) { submitForm(1);}  "/>
                </td>

            <td>  </td>
            <td>
                &nbsp;&nbsp;&nbsp;<form:checkbox path="allGt" id="gtConstruct" value="gtConstruct"
                                                 autocomplete="off"  onclick="UncheckAll(allGt)" /> Gene Trap Construct
            </td>



        </tr>
        <tr>
            <td nowrap="nowrap"><label class="namesearchLabel">Reporter Expression In</label></td>
            <td>
                <style>
                    /* overriding, see case 9050 */
                    input#searchTerm.error { color: black; }
                </style>
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

                <link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
                <script language="javascript"
                        src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js" type=""></script>
                <div id="anatomyTermInput" placeholder="s"></div>


                <div id="anatomyTermList">
                    <a style="display:none; float: right;" id="term-list-remove-all-link"
                       href="javascript:clearTable();decorateTermList();">remove all</a>
                </div>

            </td>
            <td>  </td>
            <td>
                &nbsp;&nbsp;&nbsp;<form:checkbox path="allPt" id="ptConstruct" value="ptConstruct"
                                                 autocomplete="off"  onclick="UncheckAll(allPt)" /> Promoter Trap Construct
            </td>
        </tr>


        <tr>





        </tr>





    </table>

<div class="search-form-bottom-bar" style="text-align:left">
    <input value="Search" onclick="submitForm(1)" type="button">
    <input value="Reset" type="button" onclick="call_reset()">
    <img style="display:none;" id="construct-form-loading-notify" src="/images/ajax-loader1.gif"/>
</div>


</form:form>



<script type="text/javascript">


jQuery(document).ready(function () {

    jQuery('#construct').focus();

    if ((jQuery('input[name=allTg]').attr('checked')==true)||(jQuery('input[name=allGt]').attr('checked')==true)||(jQuery('input[name=allPt]').attr('checked')==true)||(jQuery('input[name=allEt]').attr('checked')==true))       {

        jQuery('input[name=allTypes]').attr('checked', false);

    }

    jQuery('#construct-search-form .auto-submit').change(function () {

        submitForm(1)
    });
    jQuery('#searchTerm').attr('placeholder','heart, kidney, otic vesicle');

    // set the results per page pulldowns on top and bottom, but don't do it if the value is empty because
    // that result in no option being selected at all (i.e. selectedIndex == -1)
    var maxResults = jQuery('#max-display-records-hidden').val();
    if (maxResults) {
        jQuery('.max-results').val(maxResults);
    }

    decorateTermList();
    setTimeout(function () {
        jQuery('#searchTerm').attr('placeholder','heart, kidney, otic vesicle');
        decorateTermList();
    }, 10);
    setTimeout(function () {
        jQuery('#searchTerm').attr('placeholder','heart, kidney, otic vesicle');
        decorateTermList();
    }, 50);

    setTimeout(function () {
        jQuery('#searchTerm').attr('placeholder','heart, kidney, otic vesicle');
        decorateTermList();
    }, 200);
    setTimeout(function () {
        jQuery('#searchTerm').attr('placeholder','heart, kidney, otic vesicle');
        decorateTermList();
    }, 500);
    setTimeout(function () {
        jQuery('#searchTerm').attr('placeholder','heart, kidney, otic vesicle');
        decorateTermList();
    }, 1000);

});






function toggle(id) {
    var advancedSearch = 'true';
    var e = document.getElementById(id);

    if (e.style.display == '')
        e.style.display = 'none';
    else
        e.style.display = '';
}

function checkUncheckAll(){
    if (document.getElementById('allConstructs').checked == true)  {

        document.getElementById('allConstructs').checked = true;
        document.getElementById('gtConstruct').checked = true;
        document.getElementById('etConstruct').checked = true;
        document.getElementById('ptConstruct').checked = true;
        document.getElementById('tgConstruct').checked = true;
    }

    else {

        document.getElementById('allConstructs').checked = true;
        document.getElementById('gtConstruct').checked=false;
        document.getElementById('etConstruct').checked=false;
        document.getElementById('ptConstruct').checked=false;
        document.getElementById('tgConstruct').checked = false;
    }


}

function UncheckAll(type){
        if (document.getElementById('allConstructs').checked == true){
             document.getElementById('allConstructs').checked = false;
        }
        else {
            if ((document.getElementById('gtConstruct').checked == false)&&(document.getElementById('tgConstruct').checked == false)&&(document.getElementById('ptConstruct').checked == false)&&(document.getElementById('etConstruct').checked == false))
            document.getElementById('allConstructs').checked = true;
            else{
                document.getElementById('allConstructs').checked = false;
            }
}


}

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

    jQuery('input[name=construct]').val('');

    jQuery('input[name=promoterOfGene]').val('');

    jQuery('input[name=drivesExpressionOfGene]').val('');
    jQuery('input[name=affectedGene]').val('');
    jQuery('input[name=filter1]').attr('checked', false);
    jQuery('input[name=allTypes]').attr('checked', true);
    jQuery('input[name=allEt]').attr('checked', false);
    jQuery('input[name=allGt]').attr('checked', false);
    jQuery('input[name=allPt]').attr('checked', false);
    jQuery('input[name=allTg]').attr('checked', false);
    clearTable();
    decorateTermList();
    jQuery('#searchTerm').attr('class','');
    jQuery('#max-display-records-hidden').val('20');

}



function submitForm(page) {

    jQuery('#construct-form-loading-notify').show();
    var form = document.getElementById("construct-search-form");
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

//JQuery plugin


</script>



<div id="construct-search-results"/>



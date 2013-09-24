<%@ page import="org.zfin.gwt.root.ui.StandardDivNames" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>
<h1>Phenotype Curation Application</h1>

<div id="show-hide-all-sections"></div>

<div class="error"> Please use only for developmental purposes as this will make changes to the database!!!
</div>
<p></p>

<div title="Hello">
    <jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>
    Publication: <zfin:link entity="${publication}"/> &nbsp; ${publication.zdbID}
    <br>
    Title: ${publication.title}<br>
</div>
<form method="GET">
    Publication ID: <label>
    <input name="publicationID" value=""/>
</label>
    &nbsp;<input type="submit" value="Submit"/>
</form>
<div id="<%=StandardDivNames.directAttributionDiv%>"></div>
<span id="show-hide-experiments"></span>

<div id="display-experiment"></div>
<div id="image-loading"></div>
<div id="display-experiment-errors"></div>

<p></p>
<table width="100%" bgcolor="#33cc99" border="0" cellpadding="0">
    <tbody>
    <tr>
        <td style="font-weight:bold;"> Show:</td>
        <td>
            Only Fig:
            <span id="curation-filter-figure"></span>
        </td>
        <td>
            Only Fish:
            <span id="curation-filter-fish"></span>
        </td>
        <td>
            Only Feature:
            <span id="curation-filter-feature"></span>
        </td>
        <td>
            <span id="curation-filter-reset"></span>
        </td>
    </tr>
    </tbody>
</table>
<p></p>

<div id="show-hide-expressions"></div>

<table width="100%">
    <tr>
        <td colspan="2">
            <div id="check-size-bar"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div id="display-expressions"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div id="display-mutants-construction-zone"></div>
        </td>
    </tr>
</table>
<div id="image-loading-expression-section"></div>
<div id="display-expression-errors"></div>

<p></p>

<div id="show-hide-structures"></div>
<div id="update-experiments"></div>
<table width="100%">
    <tr>
        <td class="right-align-box">
            <div id="structures-check-size-bar"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div id="display-structures"></div>
        </td>
    </tr>
</table>
<div id="update-experiments-bottom"></div>
<div id="image-loading-structure-section"></div>
<div id="display-structure-errors"></div>
<div id="construction-zone"></div>

<script type="text/javascript">
    var curationProperties = {
        zdbID : "${publication.zdbID}",
        moduleType: "PHENOTYPE_CURATION",
        debug: "false"
    };
    var g = "ZDB-PUB-060105-3,ZDB-PUB-090616-53,ZDB-PUB-990507-16,090731-2,ZDB-PUB-970210-18"
</script>

<a name="structure-construction-zone"></a>

<div id="structure-pile-construction-zone" style="display:inline;">
    <table>
        <tbody>
        <tr>
            <td valign="top">
                <table>
                    <tr>
                        <td align="left" width="100" class="bold">Superterm
                        </td>
                        <td class="bold">:</td>
                        <td align="right" width="150" class="bold">Subterm
                        </td>
                    </tr>
                </table>

                <table border="0" cellpadding="5" cellspacing="3">
                    <tr>
                        <td align="center" bgcolor="#006666" width="1">
                        </td>
                        <td align="top">
                            <div id="structure-pile-construction-zone-entity_subterm-info"></div>
                            <p>
                            </p>
                            <span class="indent2"><b><i>within the</i></b></span>
                            <span id="structure-pile-construction-zone-swap-terms" class="indent3"></span>
                        </td>
                    </tr>
                </table>

                <table>
                    <tr>
                        <td>
                            <div id="structure-pile-construction-zone-entity_superterm-info"></div>
                        </td>
                    </tr>
                </table>
                <div class="dots"></div>
                <table>
                    <tr>
                        <td colspan="2">
                            <div id="structure-pile-construction-zone-quality-info"></div>
                        </td>
                    </tr>
                </table>
                <div id="related-terms-panel">
                    <div class="dots"></div>
                    <table>
                        <tr>
                            <td align="left" width="100" class="bold">Superterm
                            </td>
                            <td class="bold">:</td>
                            <td align="right" width="150" class="bold">Subterm
                            </td>
                        </tr>
                    </table>

                    <table border="0" cellpadding="5" cellspacing="3">
                        <tr>
                            <td align="center" bgcolor="#006666" width="1">
                            </td>
                            <td align="top">
                                <div id="structure-pile-construction-zone-related_entity_subterm-info"></div>
                                <p>
                                </p>
                                <span class="indent2"><b><i>within the</i></b></span>
                                <span id="structure-pile-construction-zone-swap-related-terms" class="indent3"></span>
                            </td>
                        </tr>
                    </table>

                    <table>
                        <tr>
                            <td>
                                <div id="structure-pile-construction-zone-related_entity_superterm-info"></div>
                            </td>
                        </tr>
                    </table>
                </div>
                <table>
                    <tr>
                        <td>
                            <div id="tag"></div>
                        </td>
                        <td>
                            <div id="structure-pile-construction-zone-tag-info"></div>
                        </td>
                        <td>
                            <div id="structure-pile-construction-zone-submit-reset"></div>
                        </td>
                    </tr>
                </table>

                <p>

                <div id="structure-pile-construction-zone-errors"></div>
                <br>
            </td>
            <td></td>
            <td>
                <iframe src="javascript:''" id='__gwt_historyFrame'
                        style='position:absolute;width:0;height:0;border:0'></iframe>
                <table style="width:550px; overflow: auto; border: 0.1px; border-style:solid">
                    <tr>
                        <td>
                            <div id="structure-pile-construction-zone-terminfo"></div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<style>
    .indent2 {
        margin-left: 100px;
    }

    .indent3 {
        margin-left: 70px;
    }
</style>

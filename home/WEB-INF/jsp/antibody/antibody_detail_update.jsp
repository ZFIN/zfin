<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.antibody.Antibody" %>
<%@ page import="org.zfin.antibody.presentation.AntibodySearchFormBean" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.antibody.presentation.AntibodyUpdateDetailBean" %>
<%@ page import="org.zfin.framework.presentation.client.LookupComposite" %>
<script type="text/javascript">

var gref = "Def-Pub";
var grefReq = "gref-requirement";
var grefReqMessage = "Enter a publication.";

// set the end stage field to the same value as
// the start field value
// passed in are the ids
function setEndStage(start, end) {
    var startStage = document.getElementById(start).value;
    document.getElementById(end).value = startStage;
    document.getElementById(end).style.color = 'red';
}

// submit a form and check if the global reference is being set.
// If not, focus on the G-Ref and display an note.
function submitFormWithRequiredGRef(form) {
    var zdbID = document.getElementById(gref).value;
    if (zdbID == '') {
        document.getElementById(gref).focus();
        document.getElementById(grefField).style.color = 'red';
        document.getElementById(grefReq).style.visibility = 'visible';
        return;
    }
    form.submit();
}

// submit a form and check if the global reference is being set.
// If not, focus on the G-Ref and display an note.
function submitFormWithRequiredGRef(form, formID) {
    var zdbID = document.getElementById(gref).value;
    if (zdbID == '') {
        document.getElementById(gref).focus();
        document.getElementById(grefField).style.color = 'red';
        document.getElementById(grefReq).style.visibility = 'visible';
        return;
    }
    var val = document.getElementById(formID).value;
    if (val == '') {
        alert("Value required. Please enter a name.");
        document.getElementById(formID).focus();
        return;
    }

    form.submit();
}

function addGRef() {
    document.getElementById(grefReq).style.visibility = 'hidden';
    getPubDetail();
}

var maxCharsForNotes = 8192;

// change the color of entries that were changed from the unchanged state
function changeColorNote(element, button) {
    element.style.color = 'red';
    document.getElementById(button).style.color = 'red';
}

// change the color of entries that were changed from the unchanged state
function changeColor(element, button, saveValue, saveButton, cancelButton) {

    if (element.value != saveValue) {
        element.style.color = 'red';
        document.getElementById(saveButton).disabled = false;
        document.getElementById(cancelButton).disabled = false;

    }
    else {
        element.style.color = 'black';
        document.getElementById(button).style.color = 'black';
    }
}

// only if a valid publication ZDB ID is provided enable buttons to add
// global reference
function enableDisableGRefButtons() {

    var zdbID = document.getElementById(gref).value;
    if (zdbID != '') {
        //document.getElementById(grefField).style.color = 'black';
        //document.getElementById(grefReq).style.visibility = 'hidden';
        grefReqMessage = '';
    }
}

function markAsChanged(element, originalValue) {
    var newvalue = document.getElementById(element).value;
    //window.alert("old: " + originalValue + " New: "+ newvalue);
    if (originalValue != newvalue)
        document.getElementById(element).style.color = 'red';
}

function editNote(elementName, saveButton, cancelButton, editButton, deleteButton) {
    document.getElementById(elementName).disabled = false;
    document.getElementById(saveButton).disabled = false;
    document.getElementById(cancelButton).disabled = false;
    document.getElementById(deleteButton).disabled = true;
    document.getElementById(editButton).disabled = true;
}

function cancelNote(index, originalNote) {
    //window.alert("index: " + index);
    document.getElementById('comment-' + index).value = originalNote;
    document.getElementById('comment-' + index).style.color = 'black';
    document.getElementById('comment-save' + index).style.color = 'black';
    document.getElementById('comment-save' + index).disabled = true;
    document.getElementById('comment-edit' + index).disabled = false;
    document.getElementById('comment-cancel' + index).disabled = true;
    document.getElementById('comment-' + index).disabled = true;
    document.getElementById('comment-delete' + index).disabled = false;
}

function cancelSubmit(immSpec, hostSpec, hcIso, lcIso, clonal, saveButton, cancelButton) {

    document.getElementById("antibody.immunogenSpecies").value = immSpec;
    document.getElementById("antibody.immunogenSpecies").style.color = 'black';
    document.getElementById("antibody.hostSpecies").value = hostSpec;
    document.getElementById("antibody.hostSpecies").style.color = 'black';
    document.getElementById("antibody.heavyChainIsotype").value = hcIso;
    document.getElementById("antibody.heavyChainIsotype").style.color = 'black';
    document.getElementById("antibody.lightChainIsotype").value = lcIso;
    document.getElementById("antibody.lightChainIsotype").style.color = 'black';
    document.getElementById("antibody.clonalType").value = clonal;
    document.getElementById("antibody.clonalType").style.color = 'black';
    document.getElementById(saveButton).disabled = true;
    document.getElementById(cancelButton).disabled = true;
}

function setPub(value) {
    window.alert(value);
    var AliasPub = value;

}

function submitForm(url) {
    form = document.getElementById("Antibody Detail Update");
    form.action = url;
    form.submit();
}

function submitSupplierForm(url) {
    form = document.getElementById("Antibody Detail Update");
    form.action = url;
    document.getElementById("supplierName").value = document.getElementById("supplierNameGWT").value;
    form.submit();
}

function submitNoteForm(url, elementID) {
    form = document.getElementById("Antibody Detail Update");
    extNote = document.getElementById(elementID);
    if (extNote != null && extNote.value != null && extNote.value.length > maxCharsForNotes) {
        alert("Please use fewer than " + maxCharsForNotes + " characters.");
        return;
    }
    form.action = url;
    //alert(form.action);
    form.submit();
}

function submitFormConfirm(url, entity, name) {
    form = document.getElementById("Antibody Detail Update");
    //alert("Form: " + form.name);
    form.action = url;
    //alert(form.action);
    if (window.confirm("Are you sure you want to delete the " + entity + " [" + name + "]?")) {
        form.submit();
    }
}

function subAliasRef(element, urlsubmit) {
    var zdbID = document.getElementById(element).value;
    if (window.confirm("Are you sure you want to disassociate the publication from the alias ?")) {
        var urlalias = urlsubmit + '&aliasRef=' + zdbID;
        submitForm(urlalias);
    }
}

function subAntigenRef(element, urlsubmit) {
    var zdbID = document.getElementById(element).value;
    if (window.confirm("Are you sure you want to disassociate the publication from the marker relationhip ?")) {
        var urlalias = urlsubmit + '&antigenRef=' + zdbID;
        submitForm(urlalias);
    }
}

</script>

<script type="text/javascript">

    function toggleVersion(index, isLong) {
        if (isLong) {
            document.getElementById('notesS_' + index).style.display = 'none';
            document.getElementById('notesL_' + index).style.display = 'inline';
        }
        else {
            document.getElementById('notesS_' + index).style.display = 'inline';
            document.getElementById('notesL_' + index).style.display = 'none';
        }
    }

</script>


<form:form commandName="formBean" name="Antibody Detail Update" id="Antibody Detail Update">
<table bgcolor="#eeeeee" border="0" width="100%">
    <tbody>
        <tr align="center">
            <td><font size="-1"><b>ZFIN ID:</b>
                    ${formBean.antibody.zdbID}
            </font>&nbsp;
            </td>
        </tr>
    </tbody>
</table>

<center><font size=+2>
    <input type=button value=" DONE UPDATING. Back to Viewing! "
           onClick="document.location.replace('detail?antibody.zdbID=${formBean.antibody.zdbID}')">
</font></center>


<table width="100%">
<tr>
    <td>
        <FONT SIZE=+1><STRONG>Antibody Name:</STRONG></FONT>
        <FONT SIZE=+1><STRONG>
                ${formBean.antibody.name}
        </STRONG></FONT>
        &nbsp;&nbsp;
        <input type=button name=s_new value="Update"
               onClick="window.location.replace('updateName?antibody.zdbID=${formBean.antibody.zdbID}&antibodyRenamePubZdbID=${formBean.antibodyDefPubZdbID}')">
    </td>
    <td width="30%" rowspan="7" valign="top">
        <tiles:insert page="def_pub.jsp"/>
    </td>
</tr>

<tr>
    <td valign="top">
        <b>Alias:</b>
    </td>
</tr>
<tr>
    <td style="padding-left:20px" valign="top">
        <table border="0">
            <c:forEach var="markerAlias" items="${formBean.antibody.aliases}" varStatus="status">
                <tr>
                    <td width=140 bgcolor="#dddddd">
                            ${markerAlias.alias}
                    </td>
                    <td>
                        <input type=button id=deleteAlias value="Delete Alias"
                               onClick="submitFormConfirm('delete-alias?antibody.zdbID=${formBean.antibody.zdbID}&antibodyAliaszdbID=${markerAlias.zdbID}', 'alias', '${markerAlias.alias}')"
                               title="Delete Alias '${markerAlias.alias}' including all References.">
                    </td>
                    <td>
                        <c:if test="${markerAlias.publicationCount > 0}">
                            <form:select path="pubAttribData" id="publication${status.index}">
                                <form:options items="${markerAlias.publications}" itemValue="publication.zdbID"
                                              itemLabel="publication.zdbID"/>
                            </form:select>
                            <input type=button id=deleteAliasRef value="Del Ref"
                                   onClick="subAliasRef('publication${status.index}','delete-aliasref?antibody.zdbID=${formBean.antibody.zdbID}&antibodyAliaszdbID=${markerAlias.zdbID}')"
                                   title="Disassociate the selected publication from this alias">
                        </c:if>
                    </td>
                    <td>
                        <input type=button id=addDefPubAttribution value="Attrib. to Def-Pub"
                               onClick="submitForm('add-aliasattrib?antibody.zdbID=${formBean.antibody.zdbID}&antibodyAliaszdbID=${markerAlias.zdbID}')"><br/>
                    </td>
                </tr>
            </c:forEach>
            <tr>
                <td style="text-align:right;" valign=top>
                    <form:input path="newAlias" size="25"></form:input>
                    <form:errors path="newAlias" cssClass="error-inline" /> 
                </td>
                <td style="text-align:left;" colspan="2" valign="top">
                    <input type=button id=addAlias value="Add Alias with Def-Pub"
                           onClick="submitForm('add-alias?antibody.zdbID=${formBean.antibody.zdbID}')"
                           title="Add a new alias with the default publication">
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td>
        <hr width="80%"/>
    </td>
</tr>

<tr>
    <td valign="top">
        <b>Antigen Genes: </b>
    </td>
</tr>
<tr>
    <td style="padding-left:20px">
        <table border="0">
            <c:forEach var="antigenRel" items="${formBean.antibodyStat.sortedAntigenRelationships}" varStatus="status">
                <tr>
                    <td bgcolor="#dddddd" width="140">
                        <zfin:link entity="${antigenRel.firstMarker}"/>
                    </td>
                    <td><input type=button id=deleteAntigen value="Delete Gene"
                               onClick="submitFormConfirm('delete-antigen?antibody.zdbID=${formBean.antibody.zdbID}&antibodyAntigenzdbID=${antigenRel.zdbID}', 'Relation to the antigen gene', '${antigenRel.firstMarker.abbreviation}')"
                               title="Disassociate antigen gene '${antigenRel.firstMarker.abbreviation}' from the antibody including all References.">
                    </td>
                    <td>
                        <c:if test="${antigenRel.publicationCount > 0}">&nbsp;
                            <form:select path="relAttribData" id="relpub${status.index}">
                                <form:options items="${antigenRel.publications}" itemValue="publication.zdbID"
                                              itemLabel="publication.zdbID"/>
                            </form:select>
                            <input type=button id=deleteAntigenRef value="Del Ref"
                                   onClick="subAntigenRef('relpub${status.index}','delete-antigenref?antibody.zdbID=${formBean.antibody.zdbID}&antibodyAntigenzdbID=${antigenRel.zdbID}')"
                                   title="Disassociate the selected publication from this antigen gene">
                        </c:if>
                    </td>
                    <td>
                        &nbsp;
                        <input type=button id=addAntigenRef value="Attrib. to Def-Pub"
                               onClick="submitForm('add-antigenattrib?antibody.zdbID=${formBean.antibody.zdbID}&antibodyAntigenzdbID=${antigenRel.zdbID}')"><br/>
                    </td>
                </tr>
            </c:forEach>
            <tr>
                <td style="text-align:right" valign="top">
                    <script type="text/javascript">
                        var LookupProperties = { NumLookups: "2"} ;
                        var LookupProperties0 = {
                            divName: "newAntigenGene",
                            inputName: "newAntigenGene",
                            width: "25",
                            showError: true,
                            type: "<%= LookupComposite.GENEDOM_AND_EFG %>",
                            wildcard: false
                        };
                    </script>
                    <span id="newAntigenGene"></span>
                    <form:errors path="<%= AntibodyUpdateDetailBean.AB_NEW_ANTIGEN_GENE%>"
                                 cssClass="error-inline"/>
                </td>
                <td style="text-align:left" valign="top" colspan="2">
                    <input type=button value="Add Antigen with Def-Pub"
                           onClick="submitSupplierForm('add-antigen?antibody.zdbID=${formBean.antibody.zdbID}')">
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td>
        <hr width="80%"/>
    </td>
</tr>

<TABLE width=100% bgcolor="#dddddd" border=0>
    <script type="text/javascript">
        // save the original values so we can decide if a value has changed by the user.
        var immunogenSpeciesSaved = '${formBean.antibody.immunogenSpecies}';
        var hostSpeciesSaved = '${formBean.antibody.hostSpecies}';
        var heavyChainIsotypeSaved = '${formBean.antibody.heavyChainIsotype}';
        var lightChainIsotypeSaved = '${formBean.antibody.lightChainIsotype}';
        var clonalTypeSaved = '${formBean.antibody.clonalType}';
    </script>
    <tr>
        <TD>
            <b>Host Organism:</b>
        </td>
        <td width="100%">
            <form:select path="antibody.hostSpecies" multiple="single"
                         onchange="changeColor(this, 'updateHostSpecies', hostSpeciesSaved,'updateProperties','cancelProperties')">
                <form:options items="${formBean.antigenOrganismList}"/>
            </form:select>
            &nbsp;&nbsp;
                <%--<input type=button id=updateHostSpecies value="Save"
                onClick="submitForm('update-hostspecies?antibody.zdbID=${formBean.antibody.zdbID}')">--%>
            &nbsp;&nbsp; against

            &nbsp;&nbsp;
            <b>Immunogen Organism:</b>


            <form:select path="antibody.immunogenSpecies" multiple="single"
                         onchange="changeColor(this, 'updateImmunogen', immunogenSpeciesSaved,'updateProperties','cancelProperties')">
                <form:options items="${formBean.immunogenOrganismList}"/>
            </form:select>
            &nbsp;
        </td>
    </tr>
    <tr>
        <td>
            <nobr><b>Isotype Heavy Chain:</b></nobr>
        </td>
        <td>
            <form:select path="antibody.heavyChainIsotype" multiple="single"
                         onchange="changeColor(this, 'updateHeavyIso', heavyChainIsotypeSaved,'updateProperties','cancelProperties')">
                <form:options items="${formBean.isotypeHeavyChainList}"/>
            </form:select>
            &nbsp;&nbsp;
        </td>
    </tr>
    <tr>
        <td>
            <b>Isotype Light Chain:</b>
        </td>
        <td>
            <form:select path="antibody.lightChainIsotype" multiple="single"
                         onchange="changeColor(this, 'updateLightIso', lightChainIsotypeSaved,'updateProperties','cancelProperties')">
                <form:options items="${formBean.isotypeLightChainList}"/>
            </form:select>
            &nbsp;&nbsp;
        </td>
    </tr>
    <tr>
        <td>
            <b>Type:</b>
        </td>
        <td>
            <form:select path="antibody.clonalType" multiple="single"
                         onchange="changeColor(this, 'updateType', clonalTypeSaved,'updateProperties','cancelProperties')">
                <form:options items="${formBean.typeList}"/>
            </form:select>
            &nbsp;&nbsp;
        </td>
    </tr>
    <tr>
        <td/>
    </tr>
    <tr>
        <td>

        </td>
        <td>
            <input type=button id=updateProperties value="Save" disabled=disabled
                   onClick="submitForm('update-abproperties?antibody.zdbID=${formBean.antibody.zdbID}')">
            &nbsp;&nbsp;
            <input type=button id=cancelProperties value="Cancel" disabled=disabled
                   onclick="cancelSubmit(immunogenSpeciesSaved, hostSpeciesSaved, heavyChainIsotypeSaved, lightChainIsotypeSaved, clonalTypeSaved,'updateProperties','cancelProperties')">
        </td>
    </tr>
</TABLE>
</table>


<hr>
<p>
    <b>SOURCE:</b>
<table width="100%" border="0" bgcolor="#EEEEEE">

    <c:forEach var="supplier" items="${formBean.antibody.suppliers}">
        <tr>
            <td>
                <c:choose>
                    <c:when test="${supplier.organization.url == null}">
                        <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-sourceview.apg&OID=${supplier.organization.zdbID}">
                                ${supplier.organization.name}
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${supplier.organization.url}">${supplier.organization.name}</a>
                    </c:otherwise>
                </c:choose>
                &nbsp;&nbsp;
                <input type=button id=deleteSupplier value="Delete Supplier"
                       onClick="submitFormConfirm('delete-supplier?antibody.zdbID=${formBean.antibody.zdbID}&supplierzdbID=${supplier.organization.zdbID}', 'Supplier', '${supplier.organization.name}')">
            </td>
        </tr>
    </c:forEach>
    <tr>
        <td>
            <script type="text/javascript">
                var LookupProperties1 = {
                    divName: "supplierNameSpan",
                    inputName: "supplierNameGWT",
                    showError: true,
                    type: "<%= LookupComposite.TYPE_SUPPLIER %>",
                    wildcard: false
                };

            </script>

            <link rel="stylesheet" type="text/css" href="/gwt/org.zfin.framework.presentation.Lookup/Lookup.css"/>
            <script language="javascript"
                    src="/gwt/org.zfin.framework.presentation.Lookup/org.zfin.framework.presentation.Lookup.nocache.js"></script>
            <span id="supplierNameSpan"></span>
            <form:errors path="supplierNameErrorString" cssClass="error"/>
            <form:hidden path="supplierName"/>
            <input type=button id=addSupplier value="Add Supplier"
                   onClick="submitSupplierForm('add-supplier?antibody.zdbID=${formBean.antibody.zdbID}')">
        </td>
    </tr>
</table>
<hr width="80%">
<p/>
<b>
    NOTES ON USAGE:
</b>
<table width="100%" border="0" bgcolor="#EEEEEE">
    <tr bgcolor="#cccccc">
        <td width="200"><b>Reference</b></td>

        <td width="450"><b>Comment</b></td>
        <td></td>
    </tr>
    <c:forEach var="extnote" items="${formBean.notesSortedByPubTime}" varStatus="status">

        <script type="text/javascript">
            <%-- save the original values so we can decide if a value has changed by the user.--%>
            var note_${status.index} = '${zfn:escapeForJavaScript(extnote.note)}';
        </script>

        <tr valign="top">

            <td><zfin:link entity="${extnote.singlePubAttribution.publication}"/></td>

            <td>
                <textarea rows="2" cols="50" title="Click to enter a new comment"
                          id="comment-${status.index}" name="usageNote" disabled="disabled"
                          onkeypress="markAsChanged(this.id, note_${status.index})"
                          onchange="changeColorNote(this, 'comment-save${status.index}')">${extnote.note}</textarea>


            </td>
            <td>
                <input value="Edit" type="button"
                       onclick="editNote('comment-${status.index}','comment-save${status.index}','comment-cancel${status.index}', 'comment-edit${status.index}', 'comment-delete${status.index}')"
                       id="comment-edit${status.index}">
                <input value="Save" type="button" id="comment-save${status.index}" disabled=disabled
                       onClick="submitForm('edit-note?antibody.zdbID=${formBean.antibody.zdbID}&antibodyNotezdbID=${extnote.zdbID}&usageNoteIndex=0')">
                <input value="Cancel" type="button" disabled=disabled id="comment-cancel${status.index}"
                       onclick="cancelNote('${status.index}', note_${status.index})">
                <input value="Delete Note" type="button" id="comment-delete${status.index}"
                       onClick="submitNoteForm('delete-note?antibody.zdbID=${formBean.antibody.zdbID}&antibodyNotezdbID=${extnote.zdbID}&usageNoteIndex=${status.index}','comment-${status.index}')">
            </td>

        </tr>

    </c:forEach>
</table>
<table width="100%">
    <tr valign="top">
        <td width="200">Add Note:
        </td>

        <td width="450">
            <form:textarea path="newNote" rows="2" cols="50" title="Click to enter a new comment"
                           id="comment-new" onchange="changeColorNote(this, 'addNote')"></form:textarea>
            <form:errors path="newNote" cssClass="error"/>
        </td>
        <td title="To enable this button please provide a valid global reference. ">
            <div title="jkjh">
                <input value="Add with Def-Pub" type="button" id="addNote"
                       onClick="submitNoteForm('add-note?antibody.zdbID=${formBean.antibody.zdbID}', 'comment-new')">
            </div>
        </td>

    </tr>
</table>
<a href="publication-list?antibody.zdbID=${formBean.antibody.zdbID}&orderBy=author&update=true">CITATIONS</a>&nbsp;&nbsp;(${formBean.numOfPublications})

</form:form>


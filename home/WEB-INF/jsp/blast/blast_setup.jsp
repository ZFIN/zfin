<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script type="text/javascript" src="/javascript/blast.js"></script>

<c:if test="${!empty error}">
    Error happened
</c:if>

<div style="max-width: 750px;">

<form:form commandName="formBean" enctype="multipart/form-data" >


    <div style="float: right">

              <div style="float: right; width:280px; padding: 3px; border:1px solid #ccc; background: #eee">
                <fieldset style="border: none; margin: 0 0 0 3px; padding: 0;"><legend style="font-size: small;">Retrieve Previous Result</legend>
                <span style="background:none;padding-top: 5px;padding-bottom: 10px;">
                <form:input path="ticketNumber" id="ticketNumber" size="30"
                        onkeydown="if (event.keyCode == 13) { window.location='/action/blast/blast-view?resultFile='+document.getElementById('ticketNumber').value; return false;  }"/>
                <form:errors path="ticketNumber"  cssClass="error"/>
                <input type="button"
                       onclick="window.location='/action/blast/blast-view?resultFile='+document.getElementById('ticketNumber').value;" value="GO">
                </span>
                </fieldset>
              </div>

        <div style="margin-top: 1em; float:right; clear: right; width:280px; border: none;">
            <fieldset style="border: none; margin: 0 0 0 0px; padding: 0;"> <legend style="font-size: small;">Selected Database Details:</legend>
            <div id="databaseInfoDiv" style="font-family: monospace; font-size: medium; "> <%-- height: 30px;--%>
            </div>
          </fieldset>

        </div>

    </div>

    <table width="300px;" style="clear: left; padding-top: 2px" border="0">
        <tr>
            <td colspan="2" valign="top">
                <strong>Choose program and database:</strong>
            </td>
        </tr>
        <tr>
            <TD> <a href="/zf_info/blast_info.html">Program</a>:
            <td>
                <form:select path="program" items="${programs}" itemLabel="label" itemValue="value" onchange="setOption();"/>
                <form:errors path="program" cssClass="error"/>
            </td>
        <td width="10%">
        </td>
        <td rowspan="2" width="50%" valign="top" align="left">

        </td>
        </tr>
        <tr>
            <td valign="top">
                <a href="/action/blast/blast-definitions">Database</a>:
            </td>
            <td valign="top">
                <jsp:include page="blast_database_select.jsp"></jsp:include>
                <form:errors path="dataLibraryString" cssClass="error"/>
            </td>
        </TR>
    </TABLE>
    <table>
        <tr>
            <td valign="top">
                <div style="margin-top:.5em;">
                    <strong>Query sequence</strong> <em>(maximum of 50,000 letters)</em> :
                </div>

                    <div style="margin-top: .3em;">
                        <form:radiobutton id="queryTypeFASTA" path="queryType" value="FASTA"/> <label for="queryTypeFASTA">FASTA / free-text format:</label> <form:errors path="querySequence"  cssClass="error"/>
                    </div>
                        <form:textarea  path="querySequence" rows="6" cols="60" onchange="document.getElementById('queryTypeFASTA').checked=true;" onclick="document.getElementById('queryTypeFASTA').checked=true;" onkeypress="document.getElementById('queryTypeFASTA').checked=true;"/>
                    <br>
                        <form:radiobutton id="queryTypeSEQUENCE_ID" path="queryType" value="SEQUENCE_ID" /> or Sequence ID:
                        <form:input path="sequenceID" size="20" onchange="document.getElementById('queryTypeSEQUENCE_ID').checked=true;" onclick="document.getElementById('queryTypeSEQUENCE_ID').checked=true;" onkeypress="document.getElementById('queryTypeSEQUENCE_ID').checked=true;"/>
                    <span style="font-size: small;">(separate multiple with ",")</span>  <form:errors path="sequenceID"  cssClass="error"/>
                    <br>
                        <form:radiobutton id="queryTypeUPLOAD" path="queryType" value="UPLOAD" /> or Upload a free-text file:
                    <input type=file id="sequenceFile" name="sequenceFile"  size=30 onchange="document.getElementById('queryTypeUPLOAD').checked=true;" >
                        <form:errors path="sequenceFile"  cssClass="error"/>
                    <br>
                <p>
                    <input type="hidden" name="isFormSubmission" value="true">
                    <INPUT TYPE=submit VALUE="Begin Search"> &nbsp; &nbsp;
                    <INPUT TYPE=button VALUE="Clear All"
                           onclick="
                            document.getElementById('querySequence').value='' ;
                            document.getElementById('sequenceFile').value='' ;
                            document.getElementById('sequenceID').value='' ;
                            document.getElementById('queryTypeFASTA').checked=true ;
                            "
                            >
                <hr>
            </td>
            <td valign="top">
            </td>
        </tr>
    </table>

    <fieldset style="width: 500px;"><legend>Options</legend>
    <%--<strong>Options: </strong>--%>
    <%--<br> --%>

    <%--<?MICOMMENT> SEQ_TYPE is introduced to identify the query sequence type and distinguish nt or pt--%>
    <%--for input sequence ID when querying NCBI database. Here it is used to distinguish two query type.--%>
    <%--(note, query type is not defined by query sequence type, a nucleotide sequence when participate in--%>
    <%--blastx, tblastx would be requiring protein query prameter) However, it is fine for now to mix use--%>
    <%--these two concepts. I will come back to it after the emergence fix.--%>
    <%--<?/MICOMMENT>--%>
    <form:checkbox path="shortAndNearlyExact" id="SHORT" onclick="setOption()"/><label for="SHORT">&nbsp; Search for short, nearly exact matches</label>
    <p>
        Expect:
            <form:input path="expectValue" size="8"/>
        &nbsp;
        Word Size: <form:input path="wordLength" size="2"/>
        &nbsp;
        Matrix:
        <form:select path = "matrix">
            <form:option value="" label="------" />
            <form:options items="${matrices}" />
        </form:select>
    <p>
    Set subsequence:
    From &nbsp;
    <form:input path="queryFrom" size="10"/>
    &nbsp;&nbsp;&nbsp;
    To &nbsp;
    <form:input path="queryTo" size="15"/>
    <br>
    Filter options for DNA Queries: &nbsp;
    <form:checkbox path="dust"/> Low complexity
    <form:checkbox path="poly_a" /> Poly-A's filter
    <BR>

    Filter options for Protein Queries: <BR>
    &nbsp;&nbsp;&nbsp;<form:checkbox path="seg"/> SEG - filter low compositional complexity regions  <br>

    &nbsp;&nbsp;&nbsp;<form:checkbox path="xnu"/> XNU - filter short-periodicity repeats
    <%--<p>--%>
    <%--<strong>Format: </strong>--%>
    <%--<br>--%>
    <%--<form:checkbox path= "graphDisplay" /> Graphical Overview <em> limit of the first 50 alignments </em>--%>
    </fieldset>
</form:form>
</div>
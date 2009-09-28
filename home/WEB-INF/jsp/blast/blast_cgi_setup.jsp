<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script type="text/javascript" src="/javascript/blast.js"></script>

<form:form commandName="formBean" action="${formBean.action}">
    <B>Choose program and database:</B>
    <TABLE>
        <TR>
            <TD width=40%> <a href="/zf_info/blast_info.html">Program</a>:
                <form:select path = "PROGRAM">

                <option VALUE="blastn" selected> Nucleotide - Nucleotide
                <option VALUE="blastp" > Protein - Protein
                <option VALUE="blastx" > trans. Nucleotide - Protein
                <option VALUE="tblastn" > Protein - trans. Nucleotide
                <option VALUE="tblastx" > trans. Nucleotide - trans. Nucleotide
                    -
                    </form:select>

                    <br>
                <div id="databaseInfoDiv"></div>
            </td>
            <td>&nbsp;</td>
            <td><a href="/action/blast/blast-definitions">Database</a>:
                <br>
                <jsp:include page="blast_database_select.jsp"></jsp:include>
            </td>
        </TR>
    </TABLE>
    <b>Query sequence</b> <i>(maximum of 50,000 letters)</i> :
    <p>
        FASTA or free-text format:
        <br>
            <form:textarea  path="querySequence" rows="6" cols="60"/>
        <br>
        Set subsequence:
        From &nbsp;
        <input TYPE="text" NAME="QUERY_FROM" VALUE="" SIZE="10">
        &nbsp;&nbsp;&nbsp;
        To &nbsp;
        <input TYPE="text" NAME="QUERY_TO" VALUE="" SIZE="15"> <br>
        <input TYPE=checkbox name="SHORT" onclick="setOption()">&nbsp; Search for short, nearly exact matches

    <p>
        Sequence ID:
        <input TYPE=text NAME="SEQ_ID" VALUE="" SIZE="20">
        (one or multiple delimited by ",")

        <BR>
        Sequence Type:
        <form:select path="SEQ_TYPE">
        <option value="nt">Nucleotide </option>
        <option value="pt">Protein </option>
        </form:select>
    <P>
        Upload a free-text file:
        <INPUT TYPE=file NAME="SEQ_FILE"  size=30>
    <P>

    <p>
        <INPUT TYPE=reset VALUE="Clear sequence">
        <INPUT TYPE=submit VALUE="BLAST">


        <B>Options: </B> <br>

            <%--<?MICOMMENT> SEQ_TYPE is introduced to identify the query sequence type and distinguish nt or pt--%>
            <%--for input sequence ID when querying NCBI database. Here it is used to distinguish two query type.--%>
            <%--(note, query type is not defined by query sequence type, a nucleotide sequence when participate in--%>
            <%--blastx, tblastx would be requiring protein query prameter) However, it is fine for now to mix use--%>
            <%--these two concepts. I will come back to it after the emergence fix.--%>
            <%--<?/MICOMMENT>--%>

        Expect:
            <form:input path="EXPECT" />
        &nbsp;

        Word Size: <form:input path="WORD" size="2"/>
        &nbsp;
        Matrix:
        <form:select path = "MATRIX">
        <option value=""> -------
        <option> BLOSUM62 </option>
        <option> BLOSUM45 </option>
        <option> BLOSUM80 </option>
        <option> PAM30 </option>
        <option> PAM70</option>
        </form:select>
    <P>

        Filter options for DNA Queries: &nbsp;

            <form:checkbox path="DUST"/> Low complexity
            <form:checkbox path="POLY_A" /> Poly-A's filter
        <BR>

        Filter options for Protein Queries: <BR>
        &nbsp;&nbsp;&nbsp;<form:checkbox path="SEG"/> SEG - filter low compositional complexity regions  <br>

        &nbsp;&nbsp;&nbsp;<form:checkbox path="XNU"/> XNU - filter short-periodicity repeats
    <p>
        <b>Format: </b><br>
        Show:
            <form:checkbox path= "GRAPH_DISPLAY" /> Graphical Overview <i> limit of the first 50 alignments </i>
    <P>

</form:form>

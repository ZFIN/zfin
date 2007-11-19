<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table bgcolor="#eeeeee" border="0" width="100%">
    <tbody>
        <tr align="center">
            <td><font size="-1"><b>ZFIN ID:</b> <c:out value="${formBean.publication.zdbID}"/></font>
            </td>
        </tr>
    </tbody>
</table>

<center>
    <p>
        <FONT SIZE=+2><c:out value="${formBean.publication.title}"/>
        </FONT>

    <P>
        <B><c:out value="${formBean.publication.authors}"/></B>

    <p>
</center>

<p>

<TABLE width=90%>
    <TR>
        <TD>
            <B>DATE:</B> <fmt:formatDate value="${formBean.publicationDate.time}" type="Date" dateStyle="yyyy"/>
        </TD>
        <TD><B>SOURCE:</B> <c:out value="${formBean.publication.journal.name}"/> <c:out
                value="${formBean.publication.volume}"/>:
            <c:out value="${formBean.publication.pages}"/> (<c:out value="${formBean.publication.type}"/>)
        </TD>
        <%--    ToDo: check when this is being displayed
                <TD><B>STATUS:</B> $pubview2_pub_status
                </TD>
        --%>
    </TR>
</TABLE>

<TABLE width=100%>
    <TR>
        <TD>
            <B>REGISTERED AUTHORS:</B>
            <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-labview.apg&OID=$2">$1</A>
            <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-persview.apg&OID=$2">$1</A>,
        </TD>

        <TD align=right>
            <form method=post action="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->">
                <input type=hidden name=MIval value=aa-pubprintable.apg>
                <input type=hidden name=constraint value="where zdb_id='$OID'">
                <input type=submit name=printable value="Generate reference">
            </form>
        </TD>

    </TR>
</TABLE>

<b>PubMed:</B>
Create Publication accession


<p>
    <B>FILE:</B>
    Create Publication File name from publication pdf file

    Upload a PDF from the
    <a href="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-pubcuration.apg&OID=$OID">Curation</a>

<P>

    <a href="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-fxallfigures.apg&OID=$OID">
        <b>FIGURES</b>
    </a> &nbsp;
    <font size=-1>(<a href="javascript:start_note();">current status</a>)</font>

<p>

    <!-- ============  ABSTRACT  ================= -->
    <B>ABSTRACT:</B> <br>
    <c:out value="${formBean.publication.abstractText}"/>

<p>
    <b>DETAILS:</b>

<p>

<p>
    <!-- =========================================================
 ==         ADDITIONAL INFORMATION
 ==
 == only for non-Unpublished and non-Curation pubs
 =========================================================
    -->
    <B>ADDITIONAL INFORMATION: </B>

    <br><br><a href="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-xpatcuration.apg&OID=$OID">FX Curation</a>





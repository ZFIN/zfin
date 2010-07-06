<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<%--<jsp:useBean id="formBean" --%>

<ul>

    <%--from getShortSeq.pl--%>
    <li><a href="/action/blast/blast-files?action=MORPHOLINO">zfin_mrph</a>

        <%--from getZfinGbAcc.pl--%>
        <%--should be all genbank--%>
    <li><a href="/action/blast/blast-files?action=GENBANK_ALL">zfin_genbank_all</a>
    <li><a href="/action/blast/blast-files?action=GENBANK_CDNA">zfin_genbank_cdna</a>

        <%--these 2 are the same--%>
        <%--<li><a href="">ZfinGenesWithExpression</a>--%>
    <li><a href="/action/blast/blast-files?action=GENBANK_XPAT_CDNA">zfin_genbank_xpat_cdna</a>


        <%--from ZfinGenomicDNAAll--%>
    <li><a href="/action/blast/blast-files?action=GENOMIC_REFSEQ">zfin_genomic_refseq</a> (probably 0)
    <li><a href="/action/blast/blast-files?action=GENOMIC_GENBANK">zfin_genomic_genbank</a> (~17K)
</ul>


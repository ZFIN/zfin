<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="markerBean" type="org.zfin.marker.presentation.SnpMarkerBean"
              rtexprvalue="true" required="true" %>


<table border="0" CELLPADDING="2px" width="100%">
    <tr>
        <td width="80%">
            <table border="0" CELLSPACING="0">
                <tr>
                    <td>
                        ${markerBean.sequence.startToOffset}
                    </td>
                </tr>
                <tr>
                    <td>
                       <span style="color: green;">
                           ${markerBean.sequence.ambiguity}
                       </span>
                    </td>
                </tr>
                <tr>
                    <td>
                        ${markerBean.sequence.offsetToEnd}
                    </td>
                </tr>
                <tr>
                    <td>
                        <span style="font-size:small; font-style:oblique;">
                        &nbsp;(SNP highlighted)
                        </span>
                    </td>
                </tr>
                <tr>
                    <td>

                        ${markerBean.sequence.ambiguity}
                        =
                        ${markerBean.variant}

                    </td>
                </tr>
            </table>
        </td>
        <td rowspan="5">
            <div class="analysis_tools_box" style="min-width: 100px; width: 200px;">
                <c:set var="blastLink" value="blast-popup"/>
                <c:set var="blastLinkPopup" value="blast-links"/>
                <div id="${blastLink}" class="analysis_tools_box_header">
                    Select Sequence Analysis Tool
                </div>
                <div id="${blastLinkPopup}" class="analysis_tools_box_popup_box">
                    <a href="${markerBean.ncbiBlastUrl}${markerBean.sequence.sequence}">NCBI BLAST</a>
                    <br>
                    <a href="${markerBean.snpBlastUrl}${markerBean.sequence.sequence}">SNP BLAST</a>
                    <br>
                    <a href="/action/blast/blast?&program=blastn&sequenceType=nt&queryType=FASTA&expectValue=1e-10&dataLibraryString=RNASequences&querySequence=${markerBean.sequence.sequence}">ZFIN
                        BLAST</a>
                    <br>
                </div>
            </div>
        </td>
    </tr>
</table>

<script>
    jQuery(document).ready(function() {
        jQuery('#${blastLink}').click(function() {
            jQuery("#${blastLinkPopup}").slideToggle(70);
        });
    });
</script>


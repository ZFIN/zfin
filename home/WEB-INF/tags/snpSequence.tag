<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="markerBean" type="org.zfin.marker.presentation.SnpMarkerBean"
              rtexprvalue="true" required="true" %>


<table border="0" CELLSPACING="0">
    <tr >
        <td>
            ${markerBean.sequence.startToOffset}
        </td>
    </tr>
    <tr >
        <td>
                       <span style="color: green;">
                           ${markerBean.sequence.ambiguity}
                       </span>
        </td>
    </tr>
    <tr >
        <td>
            ${markerBean.sequence.offsetToEnd}
        </td>
    </tr>
    <tr >
        <td>
                        <span style="font-size:small; font-style:oblique;">
                        &nbsp;(SNP highlighted)
                        </span>
        </td>
    </tr>
    <tr >
        <td>
            <form>

                ${markerBean.sequence.ambiguity}
                =
                ${markerBean.variant}

                <SELECT NAME="select_tool" onChange="
                        if(0!= this.selectedIndex){
                            var url = this.options[this.selectedIndex].value;
                            window.open(url,parseInt(Math.random()*2000000000));
                        }
                        ">
                    <OPTION VALUE="none" SELECTED>- Select Sequence Analysis Tool -</OPTION>
                    <OPTION VALUE="${markerBean.ncbiBlastUrl}${markerBean.sequence.sequence}">NCBI BLAST</OPTION>
                    <OPTION VALUE="${markerBean.snpBlastUrl}${markerBean.sequence.sequence}">SNP BLAST</OPTION>
                    <OPTION VALUE="/action/blast/blast?&program=blastn&sequenceType=nt&queryType=FASTA&expectValue=1e-10&dataLibraryString=RNASequences&querySequence=${markerBean.sequence.sequence}">ZFIN BLAST</OPTION>
                </SELECT>
            </form>
        </td>
    </tr>
</table>



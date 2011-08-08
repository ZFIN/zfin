<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequences" type="java.util.Collection" rtexprvalue="true" required="true" %>


<c:if test="${!empty sequences}">

    <script>
        function selectSequence(objId) {
		fnDeSelect();
		if (document.selection) {
		var range = document.body.createTextRange();
 	        range.moveToElementText(document.getElementById(objId));
		range.select();
		}
		else if (window.getSelection) {
		var range = document.createRange();
		range.selectNode(document.getElementById(objId));
		window.getSelection().addRange(range);
		}
	}

	function fnDeSelect() {
		if (document.selection) document.selection.empty();
		else if (window.getSelection)
                window.getSelection().removeAllRanges();
	}

    </script>


    <style type="text/css">
        pre.sequence {

        }
        pre.defline {
            white-space: pre-wrap;       /* css-3 */
            white-space: -moz-pre-wrap !important;  /* Mozilla, since 1999 */
            white-space: -pre-wrap;      /* Opera 4-6 */
            white-space: -o-pre-wrap;    /* Opera 7 */
            word-wrap: break-word;       /* Internet Explorer 5.5+ */
            width: 99%;   /* remove horizontal scroll-bar when viewing in IE7 */
        }
    </style>


    <div class="summary">

       <div class="summaryTitle">Sequence</div>
        <div class="sequence-view-table-wrapper">
                <%--todo: this should use an attribute, not a formBean, which is a nucleotideSequence --%>
            <c:forEach var="sequence" items="${sequences}" varStatus="index">
            <table class="summary sequence-view">
                <tr><td width="65%">
                            ${sequence.dbLink.accessionNumber}
                        <zfin:attribution entity="${sequence.dbLink}"/>

                    <span id="showSequenceButton${index.index}" style="display: inline;">
                        <a href="javascript:;"
                           style="text-decoration: none;"
                           onClick="seqdiv = document.getElementById('sequence${index.index}');
                                    showlink = document.getElementById('showSequenceButton${index.index}');
                                    hidelink = document.getElementById('hideSequenceButton${index.index}');
                                    helplink = document.getElementById('sequence${index.index}help');
                                    seqdiv.style.display = 'block';
                                    showlink.style.display = 'none';
                                    hidelink.style.display = 'inline';
                                    helplink.style.display = 'inline';">[ <img border="0" alt="show" src="/images/right_arrow.gif"> Show ]</a></span>

                    <span id="hideSequenceButton${index.index}" style="display: none;">
                        <a href="javascript:;"
                           style="text-decoration: none;"
                           onClick="seqdiv = document.getElementById('sequence${index.index}');
                                    showlink = document.getElementById('showSequenceButton${index.index}');
                                    hidelink = document.getElementById('hideSequenceButton${index.index}');
                                    helplink = document.getElementById('sequence${index.index}help');
                                    seqdiv.style.display = 'none';
                                    hidelink.style.display = 'none';
                                    showlink.style.display = 'inline'
                                    helplink.style.display = 'none';
                           ">[ <img border="0" alt="hide" src="/images/darrow.gif"> Hide ]&nbsp;&nbsp;</a></span>
<span id="sequence${index.index}help" style="display: none; font-size: small ; color: #888;">(double-click sequence to select)</span>
                                    </td>
                                    <td  width="35%">

                        <!--todo: this link needs to be written for a generic CGI and to come out of the databse-->
                        <a href="/action/blast/download-sequence?<%=LookupStrings.ACCESSION%>=${sequence.dbLink.accessionNumber}"
                           style="text-decoration: none; ">[Download]</a>
                           </td>

                           <td>

                        <zfin2:externalBlastDropDown dbLink="${sequence.dbLink}" instructions="Select Analysis Tool" minWidth="150px"/>
                        </td></tr> </table>
                        <div id="sequence${index.index}"
                             style=" background: white; width: 99%; display: none; border: 1px solid #ccc;
                             max-height: 100px; overflow: auto; margin: 4px auto;
                             ">

<%--
                        <textarea style="border: 1px solid #ccc; background: white; color:black "  readonly="readonly" cols="80" rows="5">${sequence.formattedData}</textarea>
--%>

                         <%--<pre class="defline">${sequence.defLine}</pre>
                         <pre class="sequence" onclick="selectSequence('sequence${index.index}');">${sequence.formattedSequence}</pre>--%>

                        <pre ondblclick="selectSequence('sequence${index.index}');">${sequence.formattedData}</pre>
                        </div>

                   </div>
            </c:forEach>

    </div>
</c:if>

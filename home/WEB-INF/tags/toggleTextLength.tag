<%--

  Parameters:
    text: the text to be displayed
    idName:  unique id for each loop member
    shortLength:   the length above which truncation happens in short display mode

    Check out:  http://www.oracle.com/technology/pub/articles/cioroianu_tagfiles.html

 --%>

<%@ tag body-content="scriptless" %>
<%@attribute name="text" type="java.lang.String" %>
<%@attribute name="idName" type="java.lang.String" %>
<%@attribute name="shortLength" type="java.lang.Integer" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<span id="notesS_${idName}">
    <zfin:toggleTextLength text="${text}" shortLength="${shortLength}" shortVersion="true" escape="html" />
    <c:if test="${fn:length(text) > shortLength}">
        <a href="javascript:toggleVersion(${idName}, true)"  title="Show full text">
            &nbsp;...
            <img onclick="toggleVersion(${idName}, true)"
                 src="/images/right_arrow.gif" alt="expand" title="Show full text" border="0">
        </a>
    </c:if>
</span>
<span style="display:none;" id="notesL_${idName}">
    ${zfn:escapeHtml(text)}
&nbsp;
    <img onclick="toggleVersion(${idName}, false)"
         src="/images/left_arrow.gif" alt="collapse" title="Show beginning text">
</span>

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

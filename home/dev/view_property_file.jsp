<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<br>
<B>View File Contents:</B> <bean:write name="viewFileForm" property="fileName" />
<P>
<BR>

<pre>
<bean:write name="viewFileForm" property="fileContent" />
</pre>

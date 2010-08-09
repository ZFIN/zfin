<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>

<b>Gene Name:</b> <zfin:name entity="${gene}"/> <br>
<b>Gene Symbol:</b> <zfin:link entity="${gene}"/> <br>




<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="marker" type="org.zfin.marker.Marker"
              rtexprvalue="true" required="true" %>

<b>Name:</b> <zfin:name entity="${marker}"/> <br>
<b>Symbol:</b> <zfin:abbrev entity="${marker}"/> <br>
<b>Marker Type:</b> ${marker.markerType.name}</br>

<zfin2:previousNames entity="${marker}"/>

<zfin2:notes hasNotes="${formBean.marker}"/>

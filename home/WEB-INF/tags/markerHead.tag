<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="marker" type="org.zfin.marker.Marker"
              rtexprvalue="true" required="true" %>

<table class="primary-entity-attributes">

    <tr>
      <th><span class="name-label">${marker.markerType.name}&nbsp;Name:</span></th>
      <td><span class="name-value"><zfin:name entity="${marker}"/></span></td>
    </tr>

    <tr>
      <th><span class="name-label">${marker.markerType.name}&nbsp;Abbreviation:</span></th>
      <td><span class="name-value"><zfin:abbrev entity="${marker}"/></span></td>
    </tr>

    <zfin2:previousNames entity="${marker}"/>

</table>


<zfin2:notes hasNotes="${formBean.marker}"/>

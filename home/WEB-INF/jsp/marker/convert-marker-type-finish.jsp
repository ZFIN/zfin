<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <p>Marker <b>${formBean.marker.abbreviation}</b> has been converted.</p>
    <p>Old ID: ${formBean.zdbIDToConvert}</p>
    <p>New ID: <a href="/${formBean.newZdbId}">${formBean.newZdbId}</a></p>
</z:page>

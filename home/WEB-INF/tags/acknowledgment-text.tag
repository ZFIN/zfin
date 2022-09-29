<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="publication" type="org.zfin.publication.Publication" %>
<%@attribute name="showElsevierMessage" type="java.lang.Boolean" rtexprvalue="true" required="true" %>
<%@attribute name="hasAcknowledgment" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

This image is the copyrighted work of the attributed author or publisher, and
ZFIN has permission only to display this image to its users.
Additional permissions should be obtained from the applicable author or publisher of the image.

<zfin-figure:additionalAcknowledgment publication="${publication}" hasAcknowledgment="${hasAcknowledgment}"/>

<zfin-figure:elsevierMessage publication="${publication}" showElsevierMessage="${showElsevierMessage}"/>

<zfin-figure:journalAbbrev publication="${publication}"/>

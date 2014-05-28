<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="entity" required="true" type="org.zfin.infrastructure.ZdbID" %>

View Map: <a
        href="/cgi-bin/view_zmapplet.cgi?&userid=GUEST&OID=${entity.zdbID}&view_map=view_map&ZMAP=1&LN54=1&T51=1&MGH=1&HS=1&MOP=1&GAT=1">Merged</a>
<a href="/cgi-bin/view_mapplet.cgi?&userid=GUEST&OID=${entity.zdbID}&view_map=view_map&ZMAP=1&LN54=1&T51=1&MGH=1&HS=1&MOP=1&GAT=1">Individual
    Panels</a>

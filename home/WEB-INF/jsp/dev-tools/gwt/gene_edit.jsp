<link rel="stylesheet" type="text/css" href="/Marker.css"/>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<authz:authorize ifAnyGranted="root">--%>
<%
    String zdbID = request.getParameter("zdbID");
    if(zdbID==null){
        zdbID = "ZDB-GENE-001103-2" ;
    }
    String personID = request.getParameter("personID");
    if(personID==null){
        personID = "ZDB-PERS-960805-676";
    }
%>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "<%= zdbID %>"
    } ;

</script>

<%--Adds the CloneEditController.--%>
<link rel="stylesheet" type="text/css" href="/css/Marker.css"/>
<script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<div id="newProteinSequence"></div>
<br>
<br>
<div id="newStemLoopSequence"></div>

<%--</authz:authorize>--%>




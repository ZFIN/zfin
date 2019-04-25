<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<meta name="marker-go-view-page"/> <%-- this is used by the web testing framework to know which page this is--%>

<head>
    <title>ZFIN GO for <?MIVAR>$mrkr_sym<?/MIVAR></title>
</head>

<form name="newdata" method ="post">
    <INPUT TYPE="hidden" name="MIval" value="aa-markergoentry.apg">
    <div style="text-align: center">
        <b>GO annotations for <a href="/action/marker/marker-go-view/${marker.zdbID}"><zfin:abbrev entity="${marker}"/></a></b>
    </div>

    <hr><!--------------------  the second half is display section --------------------------------->

    <script type="text/javascript">
        var MarkerProperties= {
            state: "go-evidence-display",
            zdbID : "${marker.zdbID}"
        }
    </script>

    <script language="javascript" src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

    <table cellpadding="10">
        <tr align="left">
            <td colspan="1">
                <div id="go-add-link"></div>
                <div id="go-evidence-add"></div>
            </td>
        </tr>
        <tr align="center">
            <td colspan="2">
                <div id="viewName"></div>
            </td>
        </tr>
        <tr align="center">
            <td colspan="2">
                <div id="go-evidence-display"></div>
            </td>
        </tr>
        <tr align="center">
            <td colspan="2">
                <div id="directAttributionName"></div>
            </td>
        </tr>
    </table>

</form>

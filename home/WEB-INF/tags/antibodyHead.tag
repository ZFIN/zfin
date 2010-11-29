<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="antibody" type="org.zfin.antibody.Antibody"
              rtexprvalue="true" required="true" %>

<%@ attribute name="antibodyStat" type="org.zfin.antibody.AntibodyService"
              rtexprvalue="true" required="true" %>


<div style="display: inline;font-size: large; font-weight: bold; margin-top: 1em;">
    Antibody Name:
        <%--<div style="display:inline;vertical-align:middle;font-size:large;">--%>
        <%--<strong>--%>
    <zfin:name entity="${antibody}"/>
    <script type="text/javascript">
    new Ajax.Updater('wikiLink','/action/wiki/wikiLink?zdbID=${antibody.zdbID}&name=${antibody.name}');
    </script>
    <span id="wikiLink" style="display:inline;vertical-align:baseline;font-size:small;"> </span>
</div>

<zfin2:previousNames entity="${antibody}"/>

<br>

<zfin2:notes hasNotes="${antibody}"/>

<br>

<zfin2:antibodyData antibody="${antibody}" antibodyStat="${antibodyStat}"/>


<%--</table>--%>

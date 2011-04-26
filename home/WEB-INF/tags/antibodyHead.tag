<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="antibody" type="org.zfin.antibody.Antibody"
              rtexprvalue="true" required="true" %>

<%@ attribute name="antibodyStat" type="org.zfin.antibody.AntibodyService"
              rtexprvalue="true" required="true" %>


<table class="primary-entity-attributes">
  <tr>
    <th><span class="name-label">Antibody&nbsp;Name:</span></th>
    <td><span class="name-value"><zfin:name entity="${antibody}"/></span></td>
  </tr>
  <zfin2:previousNames entity="${antibody}"/>
  <zfin2:antibodyData antibody="${antibody}" antibodyStat="${antibodyStat}"/>
  <tr>
      <th>Wiki:</th>
      <td>
          <script type="text/javascript">
              jQuery(document).ready(function() {
                  jQuery('#wikiLink').load('/webapp/wiki/wikiLink/${antibody.zdbID}');
              });
          </script>
          <span id="wikiLink"> </span>
      </td>
  </tr>
</table>

<zfin2:notes hasNotes="${antibody}"/>

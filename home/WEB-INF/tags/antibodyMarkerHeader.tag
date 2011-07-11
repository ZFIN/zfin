<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="antibodyBean" type="org.zfin.marker.presentation.AntibodyMarkerBean" rtexprvalue="true" required="true" %>


<table class="primary-entity-attributes">
  <tr>
    <th><span class="name-label">Antibody&nbsp;Name:</span></th>
    <td><span class="name-value"><zfin:name entity="${antibodyBean.marker}"/></span></td>
  </tr>
  <zfin2:previousNamesFast previousNames="${antibodyBean.previousNames}"/>
  <zfin2:antibodyMarkerData antibody="${antibodyBean.marker}" antibodyBean="${antibodyBean}"/>
  <tr>
      <th>Wiki:</th>
      <td>
          <script type="text/javascript">
              jQuery(document).ready(function() {
                  jQuery('#wikiLink').load('/webapp/wiki/wikiLink/${antibodyBean.marker.zdbID}');
              });
          </script>
          <span id="wikiLink"> </span>
      </td>
  </tr>

    <zfin2:notesInDiv hasNotes="${antibodyBean.marker}"/>

</table>


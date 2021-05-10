<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.orthology.presentation.OrthologPublicationListBean" scope="request"/>

<z:page>
  <zfin2:citationList pubListBean="${formBean}" url="citation-list?evidenceCode=${formBean.evidenceCode.code}&">

    <table class="primary-entity-attributes">
      <tr>
        <th>Zebrafish gene</th>
        <td><zfin:link entity="${formBean.ortholog.zebrafishGene}"/></td>
      </tr>
      <tr>
        <th>Ortholog</th>
        <td>${formBean.ortholog.ncbiOtherSpeciesGene.organism.commonName}&nbsp;${formBean.ortholog.ncbiOtherSpeciesGene.abbreviation}</td>
      </tr>
      <tr>
        <th>Evidence</th>
        <td>${formBean.evidenceCode.name}</td>
      </tr>
    </table>

  </zfin2:citationList>
</z:page>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.orthology.presentation.OrthologPublicationListBean" scope="request"/>


<zfin2:citationList pubListBean="${formBean}" url="citation-list?evidenceCode=${formBean.evidenceCode.code}&">

  <div class="name-label">
    Ortholog: <zfin:link entity="${formBean.ortholog.zebrafishGene}"/> with
      ${formBean.ortholog.ncbiOtherSpeciesGene.organism.commonName} ${formBean.ortholog.ncbiOtherSpeciesGene.abbreviation}
  </div>

  <div class="name-label">
    Evidence: ${formBean.evidenceCode.name}
  </div>

</zfin2:citationList>

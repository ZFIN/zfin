<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishPublicationBean" scope="request"/>

<zfin2:citationList pubListBean="${formBean}" url="fish-publication-list?fishID=${formBean.fish.fishID}&">

    <div class="name-label">
        Fish:&nbsp;
        <a href="fish-detail/${formBean.fish.fishID}">${formBean.fish.displayName}</a>
    </div>

</zfin2:citationList>

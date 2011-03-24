<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
     Term: <zfin:name entity="${term}"/>
</div>
<div class="popup-body phenotype-popup-body">
    <div>
        <zfin2:termMiniSummary term="${term}"/>
    </div>
</div>
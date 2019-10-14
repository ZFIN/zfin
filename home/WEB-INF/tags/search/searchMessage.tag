<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<style>
    .search-result-container {
        display: none;
    }
</style>

<div class="search-help-container row">
    <c:if test="${category eq 'Any' || category eq null}">
        <div class="col-md-offset-1 col-md-9 alert"
             style="margin-top: 2em; padding: 2em; border: 1px solid #c0c7c0  ; background-color: #e0e7e0; color: #666">
            <zfin2:messageContent/>
        </div>
    </c:if>

</div>

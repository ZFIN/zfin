<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="search-container">
    <a class="citation" href="/ZDB-FIG-161201-23">Fig. 1 of Monroe et al., 2016</a>
    <div class="search-inner">
        <form class="fs-autocomplete" action="/search" method="get">
            <label>Search expert curated zebrafish data</label>
            <div class="input-group input-group-lg">
                <div class="input-group-prepend category-dropdown">
                    <button type="button" class="btn btn-outline-secondary bg-white text-body dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <span class="category-label">Any</span>
                    </button>
                    <div class="dropdown-menu">
                        <a class="dropdown-item" href="#">Any</a>
                        <div role="separator" class="dropdown-divider"></div>
                        <c:forEach items="${searchCategories}" var="category">
                            <a class="dropdown-item" href="#">${category}</a>
                        </c:forEach>
                    </div>
                </div>
                <input type="hidden" name="category">
                <input type="text" class="form-control" name="q" autocomplete="off" data-placeholders="bmp2a|heart contraction abnormal|tp53 antibody">
                <div class="input-group-append">
                    <button class="btn btn-primary" type="submit">Search</button>
                </div>
            </div>
        </form>
    </div>
</div>
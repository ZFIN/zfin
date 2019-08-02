<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<div class="search-container">
    <div class="search-inner">
        <form class="fs-autocomplete" action="/search" method="get">
            <label>Search expert curated zebrafish data</label>
            <div class="input-group input-group-lg">
                <div class="input-group-btn category-dropdown">
                    <button type="button" class="btn btn-default" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <span class="category-label">Any</span> <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a href="#">Any</a></li>
                        <li role="separator" class="divider"></li>
                        <li><a href="#">Gene / Transcript</a></li>
                        <li><a href="#">Expression</a></li>
                        <li><a href="#">Phenotype</a></li>
                        <li><a href="#">Human Disease</a></li>
                        <li><a href="#">Fish</a></li>
                        <li><a href="#">Reporter Line</a></li>
                        <li><a href="#">Mutation / Tg</a></li>
                        <li><a href="#">Construct</a></li>
                        <li><a href="#">Sequence Targeting Reagent</a></li>
                        <li><a href="#">Antibody</a></li>
                        <li><a href="#">Marker / Clone</a></li>
                        <li><a href="#">Figure</a></li>
                        <li><a href="#">Anatomy / GO</a></li>
                        <li><a href="#">Community</a></li>
                        <li><a href="#">Publication</a></li>
                    </ul>
                </div>
                <input type="hidden" name="category">
                <input type="text" class="form-control" name="q" autocomplete="off">
                <span class="input-group-btn">
                    <button class="btn btn-zfin" type="submit">Search</button>
                </span>
            </div>
        </form>
    </div>
</div>

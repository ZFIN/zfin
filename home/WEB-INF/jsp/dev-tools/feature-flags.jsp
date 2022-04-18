<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Feature Flags">
    <div class="container">
            <div class="__react-root row"
                 id="FeatureToggles"
                 data-url="/action/devtool/feature-flags"
            ></div>
    </div>
    <script src="${zfn:getAssetPath("react.js")}"></script>

</z:devtoolsPage>
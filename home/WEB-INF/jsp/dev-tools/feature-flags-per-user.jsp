<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Feature Flags">
    <div class="container">
        <h3>Per User Flags for ${flagname}</h3>
        <a href="/action/devtool/feature-flags/home">Back to Feature Flags</a>
            <div class="__react-root row"
                 id="FeatureFlagsPerUser"
                 data-url="/action/devtool/feature-flags/people"
                 data-flagname="${flagname}"
            ></div>
    </div>
    <script src="${zfn:getAssetPath("react.js")}"></script>

</z:devtoolsPage>
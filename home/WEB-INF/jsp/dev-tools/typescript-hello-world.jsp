<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Typescript Hello World">
    <div class="container">
            <div class="__react-root row"
                 id="HelloWorld"
                 data-name="World"
            ></div>
    </div>
    <script src="${zfn:getAssetPath("react.js")}"></script>

</z:devtoolsPage>
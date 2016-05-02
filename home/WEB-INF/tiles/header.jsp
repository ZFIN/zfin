<%@ taglib prefix="zfin" uri="http://www.springframework.org/security/tags" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>

<%--
Things needed by just JSP pages go here. If it's needed by both JSP and
static pages it goes in header.js
--%>

<script src="/javascript/popups.js"></script>
<script src="/javascript/your-input-welcome.js" type="text/javascript"></script>

<script>
    $(function() {
        processPopupLinks('body');
        initYIW();
    });
</script>

<body id="body" >

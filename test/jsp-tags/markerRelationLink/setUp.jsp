<%@ page import="org.zfin.properties.impl.ApplicationPropertiesImpl" %>
<%@ page import="org.zfin.properties.ApplicationProperties" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.properties.Path" %>
<%@ page import="org.zfin.properties.impl.PathImpl" %>
<%--
  used to create a ZfinProperties object that can be reused for all tagunit tests
--%>
<%
    ApplicationProperties properties = new ApplicationPropertiesImpl();
    Path path = new PathImpl();
    path.setWebdriver("cgi-bin/webdriver");
    properties.setPath(path);
    ZfinProperties.init(properties);

%>
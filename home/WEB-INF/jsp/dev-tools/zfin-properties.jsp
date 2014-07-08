<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<p>
<div id="errorMessageDiv" style="color: red;"></div>
<form action="/action/dev-tools/zfin-properties" method="post" >


    <%--check error here--%>
        <%
        String errorString = "";
//        errorString += "</ul>\n";
        %>

    <input type="submit" value="Update"/>
    <table border="1">
        <tr>
            <th>Key</th>
            <th>VM Value</th>
        </tr>
            <%
        ZfinPropertiesEnum zfinPropertiesEnum[] = ZfinPropertiesEnum.values() ;
        int i = 0 ;
    %>


<tr id="row<%=i%>">
    <td>
        ALL_FAILURE_REPORTS_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="ALL_FAILURE_REPORTS_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "ALL_FAILURE_REPORTS_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ALL_FAILURE_REPORTS_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ALL_FAILURE_REPORTS_EMAIL")){
                    errorString += "ALL_FAILURE_REPORTS_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@ALL_FAILURE_REPORTS_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ANT_OPTS

        <%--// (do check on key)!--%>
        <%
            if(false=="ANT_OPTS".equals(zfinPropertiesEnum[i].name())){
                errorString += "ANT_OPTS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ANT_OPTS@".equals(zfinPropertiesEnum[i].value()) || false=="-XX:PermSize=256m -XX:MaxPermSize=512m -Xms256m -Xmx1024m".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ANT_OPTS")){
                    errorString += "ANT_OPTS " ;
                    %>
        <font color="red">Deployed value: '@ANT_OPTS@' or '-XX:PermSize=256m -XX:MaxPermSize=512m -Xms256m -Xmx1024m' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        AO_EMAIL_CURATOR

        <%--// (do check on key)!--%>
        <%
            if(false=="AO_EMAIL_CURATOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "AO_EMAIL_CURATOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@AO_EMAIL_CURATOR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("AO_EMAIL_CURATOR")){
                    errorString += "AO_EMAIL_CURATOR " ;
                    %>
        <font color="red">Deployed value: '@AO_EMAIL_CURATOR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        AO_EMAIL_ERR

        <%--// (do check on key)!--%>
        <%
            if(false=="AO_EMAIL_ERR".equals(zfinPropertiesEnum[i].name())){
                errorString += "AO_EMAIL_ERR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@AO_EMAIL_ERR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("AO_EMAIL_ERR")){
                    errorString += "AO_EMAIL_ERR " ;
                    %>
        <font color="red">Deployed value: '@AO_EMAIL_ERR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        APACHE_PORT

        <%--// (do check on key)!--%>
        <%
            if(false=="APACHE_PORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "APACHE_PORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@APACHE_PORT@".equals(zfinPropertiesEnum[i].value()) || false=="80".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("APACHE_PORT")){
                    errorString += "APACHE_PORT " ;
                    %>
        <font color="red">Deployed value: '@APACHE_PORT@' or '80' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        APACHE_PORT_SECURE

        <%--// (do check on key)!--%>
        <%
            if(false=="APACHE_PORT_SECURE".equals(zfinPropertiesEnum[i].name())){
                errorString += "APACHE_PORT_SECURE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@APACHE_PORT_SECURE@".equals(zfinPropertiesEnum[i].value()) || false=="443".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("APACHE_PORT_SECURE")){
                    errorString += "APACHE_PORT_SECURE " ;
                    %>
        <font color="red">Deployed value: '@APACHE_PORT_SECURE@' or '443' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        APACHE_SERVER_NAME

        <%--// (do check on key)!--%>
        <%
            if(false=="APACHE_SERVER_NAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "APACHE_SERVER_NAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@APACHE_SERVER_NAME@".equals(zfinPropertiesEnum[i].value()) || false=="localhost".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("APACHE_SERVER_NAME")){
                    errorString += "APACHE_SERVER_NAME " ;
                    %>
        <font color="red">Deployed value: '@APACHE_SERVER_NAME@' or 'localhost' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        APACHE_USER

        <%--// (do check on key)!--%>
        <%
            if(false=="APACHE_USER".equals(zfinPropertiesEnum[i].name())){
                errorString += "APACHE_USER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@APACHE_USER@".equals(zfinPropertiesEnum[i].value()) || false=="zfishweb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("APACHE_USER")){
                    errorString += "APACHE_USER " ;
                    %>
        <font color="red">Deployed value: '@APACHE_USER@' or 'zfishweb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BACKGROUND_COLOR

        <%--// (do check on key)!--%>
        <%
            if(false=="BACKGROUND_COLOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "BACKGROUND_COLOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BACKGROUND_COLOR@".equals(zfinPropertiesEnum[i].value()) || false=="#FFFFFF".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BACKGROUND_COLOR")){
                    errorString += "BACKGROUND_COLOR " ;
                    %>
        <font color="red">Deployed value: '@BACKGROUND_COLOR@' or '#FFFFFF' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSCRIPT_TARGET_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSCRIPT_TARGET_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSCRIPT_TARGET_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSCRIPT_TARGET_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/zdevblast/dev_scripts/target".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSCRIPT_TARGET_PATH")){
                    errorString += "BLASTSCRIPT_TARGET_PATH " ;
                    %>
        <font color="red">Deployed value: '@BLASTSCRIPT_TARGET_PATH@' or '/zdevblast/dev_scripts/target' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSERVER_BINARY_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSERVER_BINARY_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSERVER_BINARY_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSERVER_BINARY_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/wublast".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSERVER_BINARY_PATH")){
                    errorString += "BLASTSERVER_BINARY_PATH " ;
                    %>
        <font color="red">Deployed value: '@BLASTSERVER_BINARY_PATH@' or '/private/apps/wublast' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSERVER_BLAST_DATABASE_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSERVER_BLAST_DATABASE_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSERVER_BLAST_DATABASE_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSERVER_BLAST_DATABASE_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zblastfiles/zmore/dev_blastdb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSERVER_BLAST_DATABASE_PATH")){
                    errorString += "BLASTSERVER_BLAST_DATABASE_PATH " ;
                    %>
        <font color="red">Deployed value: '@BLASTSERVER_BLAST_DATABASE_PATH@' or '/research/zblastfiles/zmore/dev_blastdb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSERVER_DISTRIBUTED_QUERY_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSERVER_DISTRIBUTED_QUERY_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSERVER_DISTRIBUTED_QUERY_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSERVER_DISTRIBUTED_QUERY_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/tmp/blast_distributed_query".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSERVER_DISTRIBUTED_QUERY_PATH")){
                    errorString += "BLASTSERVER_DISTRIBUTED_QUERY_PATH " ;
                    %>
        <font color="red">Deployed value: '@BLASTSERVER_DISTRIBUTED_QUERY_PATH@' or '/tmp/blast_distributed_query' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSERVER_FASTA_FILE_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSERVER_FASTA_FILE_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSERVER_FASTA_FILE_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSERVER_FASTA_FILE_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/tmp/fasta_file_path".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSERVER_FASTA_FILE_PATH")){
                    errorString += "BLASTSERVER_FASTA_FILE_PATH " ;
                    %>
        <font color="red">Deployed value: '@BLASTSERVER_FASTA_FILE_PATH@' or '/tmp/fasta_file_path' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSERVER_HOSTNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSERVER_HOSTNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSERVER_HOSTNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSERVER_HOSTNAME@".equals(zfinPropertiesEnum[i].value()) || false=="embryonix.zfin.org".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSERVER_HOSTNAME")){
                    errorString += "BLASTSERVER_HOSTNAME " ;
                    %>
        <font color="red">Deployed value: '@BLASTSERVER_HOSTNAME@' or 'embryonix.zfin.org' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSERVER_USER

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSERVER_USER".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSERVER_USER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSERVER_USER@".equals(zfinPropertiesEnum[i].value()) || false=="blast".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSERVER_USER")){
                    errorString += "BLASTSERVER_USER " ;
                    %>
        <font color="red">Deployed value: '@BLASTSERVER_USER@' or 'blast' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSERVER_XDFORMAT

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSERVER_XDFORMAT".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSERVER_XDFORMAT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSERVER_XDFORMAT@".equals(zfinPropertiesEnum[i].value()) || false=="xdformat".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSERVER_XDFORMAT")){
                    errorString += "BLASTSERVER_XDFORMAT " ;
                    %>
        <font color="red">Deployed value: '@BLASTSERVER_XDFORMAT@' or 'xdformat' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLASTSERVER_XDGET

        <%--// (do check on key)!--%>
        <%
            if(false=="BLASTSERVER_XDGET".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLASTSERVER_XDGET " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLASTSERVER_XDGET@".equals(zfinPropertiesEnum[i].value()) || false=="xdget".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLASTSERVER_XDGET")){
                    errorString += "BLASTSERVER_XDGET " ;
                    %>
        <font color="red">Deployed value: '@BLASTSERVER_XDGET@' or 'xdget' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLAST_CACHE_AT_STARTUP

        <%--// (do check on key)!--%>
        <%
            if(false=="BLAST_CACHE_AT_STARTUP".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLAST_CACHE_AT_STARTUP " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLAST_CACHE_AT_STARTUP@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLAST_CACHE_AT_STARTUP")){
                    errorString += "BLAST_CACHE_AT_STARTUP " ;
                    %>
        <font color="red">Deployed value: '@BLAST_CACHE_AT_STARTUP@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLAST_FILE_DIR

        <%--// (do check on key)!--%>
        <%
            if(false=="BLAST_FILE_DIR".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLAST_FILE_DIR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLAST_FILE_DIR@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zmore/dev_fasta".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLAST_FILE_DIR")){
                    errorString += "BLAST_FILE_DIR " ;
                    %>
        <font color="red">Deployed value: '@BLAST_FILE_DIR@' or '/research/zmore/dev_fasta' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BLAST_SCRIPT_DIR

        <%--// (do check on key)!--%>
        <%
            if(false=="BLAST_SCRIPT_DIR".equals(zfinPropertiesEnum[i].name())){
                errorString += "BLAST_SCRIPT_DIR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BLAST_SCRIPT_DIR@".equals(zfinPropertiesEnum[i].value()) || false=="/zdevblast/dev_scripts".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BLAST_SCRIPT_DIR")){
                    errorString += "BLAST_SCRIPT_DIR " ;
                    %>
        <font color="red">Deployed value: '@BLAST_SCRIPT_DIR@' or '/zdevblast/dev_scripts' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        BUILD_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="BUILD_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "BUILD_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@BUILD_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("BUILD_EMAIL")){
                    errorString += "BUILD_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@BUILD_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        CATALINA_BASE

        <%--// (do check on key)!--%>
        <%
            if(false=="CATALINA_BASE".equals(zfinPropertiesEnum[i].name())){
                errorString += "CATALINA_BASE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@CATALINA_BASE@".equals(zfinPropertiesEnum[i].value()) || false=="/private/etc/tomcat/cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("CATALINA_BASE")){
                    errorString += "CATALINA_BASE " ;
                    %>
        <font color="red">Deployed value: '@CATALINA_BASE@' or '/private/etc/tomcat/cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        CATALINA_HOME

        <%--// (do check on key)!--%>
        <%
            if(false=="CATALINA_HOME".equals(zfinPropertiesEnum[i].name())){
                errorString += "CATALINA_HOME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@CATALINA_HOME@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/tomcat".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("CATALINA_HOME")){
                    errorString += "CATALINA_HOME " ;
                    %>
        <font color="red">Deployed value: '@CATALINA_HOME@' or '/private/apps/tomcat' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        CATALINA_PID

        <%--// (do check on key)!--%>
        <%
            if(false=="CATALINA_PID".equals(zfinPropertiesEnum[i].name())){
                errorString += "CATALINA_PID " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@CATALINA_PID@".equals(zfinPropertiesEnum[i].value()) || false=="/private/etc/tomcat/cell/catalina_pid".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("CATALINA_PID")){
                    errorString += "CATALINA_PID " ;
                    %>
        <font color="red">Deployed value: '@CATALINA_PID@' or '/private/etc/tomcat/cell/catalina_pid' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        CGI_BIN_DIR_NAME

        <%--// (do check on key)!--%>
        <%
            if(false=="CGI_BIN_DIR_NAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "CGI_BIN_DIR_NAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@CGI_BIN_DIR_NAME@".equals(zfinPropertiesEnum[i].value()) || false=="cgi-bin".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("CGI_BIN_DIR_NAME")){
                    errorString += "CGI_BIN_DIR_NAME " ;
                    %>
        <font color="red">Deployed value: '@CGI_BIN_DIR_NAME@' or 'cgi-bin' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        CONFIGURATION_DIRECTORY

        <%--// (do check on key)!--%>
        <%
            if(false=="CONFIGURATION_DIRECTORY".equals(zfinPropertiesEnum[i].name())){
                errorString += "CONFIGURATION_DIRECTORY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@CONFIGURATION_DIRECTORY@".equals(zfinPropertiesEnum[i].value()) || false=="conf".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("CONFIGURATION_DIRECTORY")){
                    errorString += "CONFIGURATION_DIRECTORY " ;
                    %>
        <font color="red">Deployed value: '@CONFIGURATION_DIRECTORY@' or 'conf' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COOKIE_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="COOKIE_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "COOKIE_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COOKIE_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COOKIE_PATH")){
                    errorString += "COOKIE_PATH " ;
                    %>
        <font color="red">Deployed value: '@COOKIE_PATH@' or '/' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COUNT_PATO_ERR

        <%--// (do check on key)!--%>
        <%
            if(false=="COUNT_PATO_ERR".equals(zfinPropertiesEnum[i].name())){
                errorString += "COUNT_PATO_ERR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COUNT_PATO_ERR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COUNT_PATO_ERR")){
                    errorString += "COUNT_PATO_ERR " ;
                    %>
        <font color="red">Deployed value: '@COUNT_PATO_ERR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COUNT_PATO_OUT

        <%--// (do check on key)!--%>
        <%
            if(false=="COUNT_PATO_OUT".equals(zfinPropertiesEnum[i].name())){
                errorString += "COUNT_PATO_OUT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COUNT_PATO_OUT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COUNT_PATO_OUT")){
                    errorString += "COUNT_PATO_OUT " ;
                    %>
        <font color="red">Deployed value: '@COUNT_PATO_OUT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COUNT_THISSE_VEGA_ERR

        <%--// (do check on key)!--%>
        <%
            if(false=="COUNT_THISSE_VEGA_ERR".equals(zfinPropertiesEnum[i].name())){
                errorString += "COUNT_THISSE_VEGA_ERR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COUNT_THISSE_VEGA_ERR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COUNT_THISSE_VEGA_ERR")){
                    errorString += "COUNT_THISSE_VEGA_ERR " ;
                    %>
        <font color="red">Deployed value: '@COUNT_THISSE_VEGA_ERR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COUNT_THISSE_VEGA_OUT

        <%--// (do check on key)!--%>
        <%
            if(false=="COUNT_THISSE_VEGA_OUT".equals(zfinPropertiesEnum[i].name())){
                errorString += "COUNT_THISSE_VEGA_OUT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COUNT_THISSE_VEGA_OUT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COUNT_THISSE_VEGA_OUT")){
                    errorString += "COUNT_THISSE_VEGA_OUT " ;
                    %>
        <font color="red">Deployed value: '@COUNT_THISSE_VEGA_OUT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COUNT_VEGA_ERR

        <%--// (do check on key)!--%>
        <%
            if(false=="COUNT_VEGA_ERR".equals(zfinPropertiesEnum[i].name())){
                errorString += "COUNT_VEGA_ERR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COUNT_VEGA_ERR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COUNT_VEGA_ERR")){
                    errorString += "COUNT_VEGA_ERR " ;
                    %>
        <font color="red">Deployed value: '@COUNT_VEGA_ERR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COUNT_VEGA_OUT

        <%--// (do check on key)!--%>
        <%
            if(false=="COUNT_VEGA_OUT".equals(zfinPropertiesEnum[i].name())){
                errorString += "COUNT_VEGA_OUT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COUNT_VEGA_OUT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COUNT_VEGA_OUT")){
                    errorString += "COUNT_VEGA_OUT " ;
                    %>
        <font color="red">Deployed value: '@COUNT_VEGA_OUT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COUNT_ZGC_ERR

        <%--// (do check on key)!--%>
        <%
            if(false=="COUNT_ZGC_ERR".equals(zfinPropertiesEnum[i].name())){
                errorString += "COUNT_ZGC_ERR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COUNT_ZGC_ERR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COUNT_ZGC_ERR")){
                    errorString += "COUNT_ZGC_ERR " ;
                    %>
        <font color="red">Deployed value: '@COUNT_ZGC_ERR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        COUNT_ZGC_OUT

        <%--// (do check on key)!--%>
        <%
            if(false=="COUNT_ZGC_OUT".equals(zfinPropertiesEnum[i].name())){
                errorString += "COUNT_ZGC_OUT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@COUNT_ZGC_OUT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("COUNT_ZGC_OUT")){
                    errorString += "COUNT_ZGC_OUT " ;
                    %>
        <font color="red">Deployed value: '@COUNT_ZGC_OUT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        CURATION_DBNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="CURATION_DBNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "CURATION_DBNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@CURATION_DBNAME@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("CURATION_DBNAME")){
                    errorString += "CURATION_DBNAME " ;
                    %>
        <font color="red">Deployed value: '@CURATION_DBNAME@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        CURATION_INSTANCE

        <%--// (do check on key)!--%>
        <%
            if(false=="CURATION_INSTANCE".equals(zfinPropertiesEnum[i].name())){
                errorString += "CURATION_INSTANCE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@CURATION_INSTANCE@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("CURATION_INSTANCE")){
                    errorString += "CURATION_INSTANCE " ;
                    %>
        <font color="red">Deployed value: '@CURATION_INSTANCE@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        CURATORS_AT_ZFIN

        <%--// (do check on key)!--%>
        <%
            if(false=="CURATORS_AT_ZFIN".equals(zfinPropertiesEnum[i].name())){
                errorString += "CURATORS_AT_ZFIN " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@CURATORS_AT_ZFIN@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("CURATORS_AT_ZFIN")){
                    errorString += "CURATORS_AT_ZFIN " ;
                    %>
        <font color="red">Deployed value: '@CURATORS_AT_ZFIN@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DATABASE_UNLOAD_DIRECTORY

        <%--// (do check on key)!--%>
        <%
            if(false=="DATABASE_UNLOAD_DIRECTORY".equals(zfinPropertiesEnum[i].name())){
                errorString += "DATABASE_UNLOAD_DIRECTORY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DATABASE_UNLOAD_DIRECTORY@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zunloads/databases/production".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DATABASE_UNLOAD_DIRECTORY")){
                    errorString += "DATABASE_UNLOAD_DIRECTORY " ;
                    %>
        <font color="red">Deployed value: '@DATABASE_UNLOAD_DIRECTORY@' or '/research/zunloads/databases/production' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DBNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="DBNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "DBNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DBNAME@".equals(zfinPropertiesEnum[i].value()) || false=="celldb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DBNAME")){
                    errorString += "DBNAME " ;
                    %>
        <font color="red">Deployed value: '@DBNAME@' or 'celldb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DB_NAME

        <%--// (do check on key)!--%>
        <%
            if(false=="DB_NAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "DB_NAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DB_NAME@".equals(zfinPropertiesEnum[i].value()) || false=="celldb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DB_NAME")){
                    errorString += "DB_NAME " ;
                    %>
        <font color="red">Deployed value: '@DB_NAME@' or 'celldb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DB_OWNER

        <%--// (do check on key)!--%>
        <%
            if(false=="DB_OWNER".equals(zfinPropertiesEnum[i].name())){
                errorString += "DB_OWNER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DB_OWNER@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DB_OWNER")){
                    errorString += "DB_OWNER " ;
                    %>
        <font color="red">Deployed value: '@DB_OWNER@' or 'kschaper' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DEBUGPORT

        <%--// (do check on key)!--%>
        <%
            if(false=="DEBUGPORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "DEBUGPORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DEBUGPORT@".equals(zfinPropertiesEnum[i].value()) || false=="9345".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DEBUGPORT")){
                    errorString += "DEBUGPORT " ;
                    %>
        <font color="red">Deployed value: '@DEBUGPORT@' or '9345' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DEFAULT_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="DEFAULT_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "DEFAULT_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DEFAULT_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DEFAULT_EMAIL")){
                    errorString += "DEFAULT_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@DEFAULT_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DISABLE_SECURITY

        <%--// (do check on key)!--%>
        <%
            if(false=="DISABLE_SECURITY".equals(zfinPropertiesEnum[i].name())){
                errorString += "DISABLE_SECURITY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DISABLE_SECURITY@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DISABLE_SECURITY")){
                    errorString += "DISABLE_SECURITY " ;
                    %>
        <font color="red">Deployed value: '@DISABLE_SECURITY@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DOI_EMAIL_REPORT

        <%--// (do check on key)!--%>
        <%
            if(false=="DOI_EMAIL_REPORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "DOI_EMAIL_REPORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DOI_EMAIL_REPORT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DOI_EMAIL_REPORT")){
                    errorString += "DOI_EMAIL_REPORT " ;
                    %>
        <font color="red">Deployed value: '@DOI_EMAIL_REPORT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DOMAIN_NAME

        <%--// (do check on key)!--%>
        <%
            if(false=="DOMAIN_NAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "DOMAIN_NAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DOMAIN_NAME@".equals(zfinPropertiesEnum[i].value()) || false=="cell.zfin.org".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DOMAIN_NAME")){
                    errorString += "DOMAIN_NAME " ;
                    %>
        <font color="red">Deployed value: '@DOMAIN_NAME@' or 'cell.zfin.org' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        DOWNLOAD_DIRECTORY

        <%--// (do check on key)!--%>
        <%
            if(false=="DOWNLOAD_DIRECTORY".equals(zfinPropertiesEnum[i].name())){
                errorString += "DOWNLOAD_DIRECTORY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@DOWNLOAD_DIRECTORY@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zunloads/download-files/celldb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("DOWNLOAD_DIRECTORY")){
                    errorString += "DOWNLOAD_DIRECTORY " ;
                    %>
        <font color="red">Deployed value: '@DOWNLOAD_DIRECTORY@' or '/research/zunloads/download-files/celldb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ELSEVIER_REPORT

        <%--// (do check on key)!--%>
        <%
            if(false=="ELSEVIER_REPORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "ELSEVIER_REPORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ELSEVIER_REPORT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ELSEVIER_REPORT")){
                    errorString += "ELSEVIER_REPORT " ;
                    %>
        <font color="red">Deployed value: '@ELSEVIER_REPORT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        EMAIL_SENDER_CLASS

        <%--// (do check on key)!--%>
        <%
            if(false=="EMAIL_SENDER_CLASS".equals(zfinPropertiesEnum[i].name())){
                errorString += "EMAIL_SENDER_CLASS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@EMAIL_SENDER_CLASS@".equals(zfinPropertiesEnum[i].value()) || false=="org.zfin.framework.mail.IntegratedJavaMailSender".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("EMAIL_SENDER_CLASS")){
                    errorString += "EMAIL_SENDER_CLASS " ;
                    %>
        <font color="red">Deployed value: '@EMAIL_SENDER_CLASS@' or 'org.zfin.framework.mail.IntegratedJavaMailSender' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        FTPROOT_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="FTPROOT_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "FTPROOT_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@FTPROOT_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/ftp/zfin".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("FTPROOT_PATH")){
                    errorString += "FTPROOT_PATH " ;
                    %>
        <font color="red">Deployed value: '@FTPROOT_PATH@' or '/ftp/zfin' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        FTP_ROOT

        <%--// (do check on key)!--%>
        <%
            if(false=="FTP_ROOT".equals(zfinPropertiesEnum[i].name())){
                errorString += "FTP_ROOT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@FTP_ROOT@".equals(zfinPropertiesEnum[i].value()) || false=="/opt/zfin/ftp/test/cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("FTP_ROOT")){
                    errorString += "FTP_ROOT " ;
                    %>
        <font color="red">Deployed value: '@FTP_ROOT@' or '/opt/zfin/ftp/test/cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GBROWSE_DB

        <%--// (do check on key)!--%>
        <%
            if(false=="GBROWSE_DB".equals(zfinPropertiesEnum[i].name())){
                errorString += "GBROWSE_DB " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GBROWSE_DB@".equals(zfinPropertiesEnum[i].value()) || false=="ensembl_current".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GBROWSE_DB")){
                    errorString += "GBROWSE_DB " ;
                    %>
        <font color="red">Deployed value: '@GBROWSE_DB@' or 'ensembl_current' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GBROWSE_DB_HOST

        <%--// (do check on key)!--%>
        <%
            if(false=="GBROWSE_DB_HOST".equals(zfinPropertiesEnum[i].name())){
                errorString += "GBROWSE_DB_HOST " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GBROWSE_DB_HOST@".equals(zfinPropertiesEnum[i].value()) || false=="gbrowse1.zfin.org".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GBROWSE_DB_HOST")){
                    errorString += "GBROWSE_DB_HOST " ;
                    %>
        <font color="red">Deployed value: '@GBROWSE_DB_HOST@' or 'gbrowse1.zfin.org' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GBROWSE_IMG_PATH_FROM_ROOT

        <%--// (do check on key)!--%>
        <%
            if(false=="GBROWSE_IMG_PATH_FROM_ROOT".equals(zfinPropertiesEnum[i].name())){
                errorString += "GBROWSE_IMG_PATH_FROM_ROOT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GBROWSE_IMG_PATH_FROM_ROOT@".equals(zfinPropertiesEnum[i].value()) || false=="gb2/gbrowse_img/zfin_ensembl/".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GBROWSE_IMG_PATH_FROM_ROOT")){
                    errorString += "GBROWSE_IMG_PATH_FROM_ROOT " ;
                    %>
        <font color="red">Deployed value: '@GBROWSE_IMG_PATH_FROM_ROOT@' or 'gb2/gbrowse_img/zfin_ensembl/' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GBROWSE_PATH_FROM_ROOT

        <%--// (do check on key)!--%>
        <%
            if(false=="GBROWSE_PATH_FROM_ROOT".equals(zfinPropertiesEnum[i].name())){
                errorString += "GBROWSE_PATH_FROM_ROOT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GBROWSE_PATH_FROM_ROOT@".equals(zfinPropertiesEnum[i].value()) || false=="gb2/gbrowse/zfin_ensembl/".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GBROWSE_PATH_FROM_ROOT")){
                    errorString += "GBROWSE_PATH_FROM_ROOT " ;
                    %>
        <font color="red">Deployed value: '@GBROWSE_PATH_FROM_ROOT@' or 'gb2/gbrowse/zfin_ensembl/' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GC_LOGGING_OPTS

        <%--// (do check on key)!--%>
        <%
            if(false=="GC_LOGGING_OPTS".equals(zfinPropertiesEnum[i].name())){
                errorString += "GC_LOGGING_OPTS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GC_LOGGING_OPTS@".equals(zfinPropertiesEnum[i].value()) || false=="-verbose:gc -verbose:sizes -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:/private/etc/tomcat/cell/logs/gc.log".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GC_LOGGING_OPTS")){
                    errorString += "GC_LOGGING_OPTS " ;
                    %>
        <font color="red">Deployed value: '@GC_LOGGING_OPTS@' or '-verbose:gc -verbose:sizes -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:/private/etc/tomcat/cell/logs/gc.log' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GENBANK_DAILY_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="GENBANK_DAILY_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "GENBANK_DAILY_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GENBANK_DAILY_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GENBANK_DAILY_EMAIL")){
                    errorString += "GENBANK_DAILY_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@GENBANK_DAILY_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GO_EMAIL_CURATOR

        <%--// (do check on key)!--%>
        <%
            if(false=="GO_EMAIL_CURATOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "GO_EMAIL_CURATOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GO_EMAIL_CURATOR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GO_EMAIL_CURATOR")){
                    errorString += "GO_EMAIL_CURATOR " ;
                    %>
        <font color="red">Deployed value: '@GO_EMAIL_CURATOR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GO_EMAIL_ERR

        <%--// (do check on key)!--%>
        <%
            if(false=="GO_EMAIL_ERR".equals(zfinPropertiesEnum[i].name())){
                errorString += "GO_EMAIL_ERR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GO_EMAIL_ERR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GO_EMAIL_ERR")){
                    errorString += "GO_EMAIL_ERR " ;
                    %>
        <font color="red">Deployed value: '@GO_EMAIL_ERR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        GROOVY_HOME

        <%--// (do check on key)!--%>
        <%
            if(false=="GROOVY_HOME".equals(zfinPropertiesEnum[i].name())){
                errorString += "GROOVY_HOME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@GROOVY_HOME@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/groovy".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("GROOVY_HOME")){
                    errorString += "GROOVY_HOME " ;
                    %>
        <font color="red">Deployed value: '@GROOVY_HOME@' or '/private/apps/groovy' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        HAS_PARTNER

        <%--// (do check on key)!--%>
        <%
            if(false=="HAS_PARTNER".equals(zfinPropertiesEnum[i].name())){
                errorString += "HAS_PARTNER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@HAS_PARTNER@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("HAS_PARTNER")){
                    errorString += "HAS_PARTNER " ;
                    %>
        <font color="red">Deployed value: '@HAS_PARTNER@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        HEADER_BACKGROUND

        <%--// (do check on key)!--%>
        <%
            if(false=="HEADER_BACKGROUND".equals(zfinPropertiesEnum[i].name())){
                errorString += "HEADER_BACKGROUND " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@HEADER_BACKGROUND@".equals(zfinPropertiesEnum[i].value()) || false=="#9AC0CD".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("HEADER_BACKGROUND")){
                    errorString += "HEADER_BACKGROUND " ;
                    %>
        <font color="red">Deployed value: '@HEADER_BACKGROUND@' or '#9AC0CD' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        HEADER_COLOR

        <%--// (do check on key)!--%>
        <%
            if(false=="HEADER_COLOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "HEADER_COLOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@HEADER_COLOR@".equals(zfinPropertiesEnum[i].value()) || false=="#99CCCC".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("HEADER_COLOR")){
                    errorString += "HEADER_COLOR " ;
                    %>
        <font color="red">Deployed value: '@HEADER_COLOR@' or '#99CCCC' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        HIBERNATE_CONFIGURATION_DIRECTORY

        <%--// (do check on key)!--%>
        <%
            if(false=="HIBERNATE_CONFIGURATION_DIRECTORY".equals(zfinPropertiesEnum[i].name())){
                errorString += "HIBERNATE_CONFIGURATION_DIRECTORY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@HIBERNATE_CONFIGURATION_DIRECTORY@".equals(zfinPropertiesEnum[i].value()) || false=="source/org/zfin".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("HIBERNATE_CONFIGURATION_DIRECTORY")){
                    errorString += "HIBERNATE_CONFIGURATION_DIRECTORY " ;
                    %>
        <font color="red">Deployed value: '@HIBERNATE_CONFIGURATION_DIRECTORY@' or 'source/org/zfin' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        HIGHLIGHTER_COLOR

        <%--// (do check on key)!--%>
        <%
            if(false=="HIGHLIGHTER_COLOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "HIGHLIGHTER_COLOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@HIGHLIGHTER_COLOR@".equals(zfinPropertiesEnum[i].value()) || false=="#F4F4F4".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("HIGHLIGHTER_COLOR")){
                    errorString += "HIGHLIGHTER_COLOR " ;
                    %>
        <font color="red">Deployed value: '@HIGHLIGHTER_COLOR@' or '#F4F4F4' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        HIGHLIGHT_COLOR

        <%--// (do check on key)!--%>
        <%
            if(false=="HIGHLIGHT_COLOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "HIGHLIGHT_COLOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@HIGHLIGHT_COLOR@".equals(zfinPropertiesEnum[i].value()) || false=="#EEEEEE".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("HIGHLIGHT_COLOR")){
                    errorString += "HIGHLIGHT_COLOR " ;
                    %>
        <font color="red">Deployed value: '@HIGHLIGHT_COLOR@' or '#EEEEEE' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        HOSTNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="HOSTNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "HOSTNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@HOSTNAME@".equals(zfinPropertiesEnum[i].value()) || false=="bent.zfin.org".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("HOSTNAME")){
                    errorString += "HOSTNAME " ;
                    %>
        <font color="red">Deployed value: '@HOSTNAME@' or 'bent.zfin.org' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        IMAGE_LOAD

        <%--// (do check on key)!--%>
        <%
            if(false=="IMAGE_LOAD".equals(zfinPropertiesEnum[i].name())){
                errorString += "IMAGE_LOAD " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@IMAGE_LOAD@".equals(zfinPropertiesEnum[i].value()) || false=="/imageLoadUp".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("IMAGE_LOAD")){
                    errorString += "IMAGE_LOAD " ;
                    %>
        <font color="red">Deployed value: '@IMAGE_LOAD@' or '/imageLoadUp' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INDEXER_DIRECTORY

        <%--// (do check on key)!--%>
        <%
            if(false=="INDEXER_DIRECTORY".equals(zfinPropertiesEnum[i].name())){
                errorString += "INDEXER_DIRECTORY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INDEXER_DIRECTORY@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zunloads/indexes/production".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INDEXER_DIRECTORY")){
                    errorString += "INDEXER_DIRECTORY " ;
                    %>
        <font color="red">Deployed value: '@INDEXER_DIRECTORY@' or '/research/zunloads/indexes/production' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INDEXER_REPORT_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="INDEXER_REPORT_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "INDEXER_REPORT_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INDEXER_REPORT_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INDEXER_REPORT_EMAIL")){
                    errorString += "INDEXER_REPORT_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@INDEXER_REPORT_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INDEXER_UNLOAD_DIRECTORY

        <%--// (do check on key)!--%>
        <%
            if(false=="INDEXER_UNLOAD_DIRECTORY".equals(zfinPropertiesEnum[i].name())){
                errorString += "INDEXER_UNLOAD_DIRECTORY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INDEXER_UNLOAD_DIRECTORY@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zunloads/indexes/celldb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INDEXER_UNLOAD_DIRECTORY")){
                    errorString += "INDEXER_UNLOAD_DIRECTORY " ;
                    %>
        <font color="red">Deployed value: '@INDEXER_UNLOAD_DIRECTORY@' or '/research/zunloads/indexes/celldb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INDEXER_WIKI_HOSTNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="INDEXER_WIKI_HOSTNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "INDEXER_WIKI_HOSTNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INDEXER_WIKI_HOSTNAME@".equals(zfinPropertiesEnum[i].value()) || false=="devwiki.zfin.org".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INDEXER_WIKI_HOSTNAME")){
                    errorString += "INDEXER_WIKI_HOSTNAME " ;
                    %>
        <font color="red">Deployed value: '@INDEXER_WIKI_HOSTNAME@' or 'devwiki.zfin.org' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INDEXER_WIKI_PASSWORD

        <%--// (do check on key)!--%>
        <%
            if(false=="INDEXER_WIKI_PASSWORD".equals(zfinPropertiesEnum[i].name())){
                errorString += "INDEXER_WIKI_PASSWORD " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INDEXER_WIKI_PASSWORD@".equals(zfinPropertiesEnum[i].value()) || false=="dan1orer1o".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INDEXER_WIKI_PASSWORD")){
                    errorString += "INDEXER_WIKI_PASSWORD " ;
                    %>
        <font color="red">Deployed value: '@INDEXER_WIKI_PASSWORD@' or 'dan1orer1o' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INDEXER_WIKI_USERNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="INDEXER_WIKI_USERNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "INDEXER_WIKI_USERNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INDEXER_WIKI_USERNAME@".equals(zfinPropertiesEnum[i].value()) || false=="webservice".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INDEXER_WIKI_USERNAME")){
                    errorString += "INDEXER_WIKI_USERNAME " ;
                    %>
        <font color="red">Deployed value: '@INDEXER_WIKI_USERNAME@' or 'webservice' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INFORMIXDIR

        <%--// (do check on key)!--%>
        <%
            if(false=="INFORMIXDIR".equals(zfinPropertiesEnum[i].name())){
                errorString += "INFORMIXDIR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INFORMIXDIR@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/Informix/informix".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INFORMIXDIR")){
                    errorString += "INFORMIXDIR " ;
                    %>
        <font color="red">Deployed value: '@INFORMIXDIR@' or '/private/apps/Informix/informix' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INFORMIXSERVER

        <%--// (do check on key)!--%>
        <%
            if(false=="INFORMIXSERVER".equals(zfinPropertiesEnum[i].name())){
                errorString += "INFORMIXSERVER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INFORMIXSERVER@".equals(zfinPropertiesEnum[i].value()) || false=="waffle".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INFORMIXSERVER")){
                    errorString += "INFORMIXSERVER " ;
                    %>
        <font color="red">Deployed value: '@INFORMIXSERVER@' or 'waffle' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INFORMIXSQLHOSTS

        <%--// (do check on key)!--%>
        <%
            if(false=="INFORMIXSQLHOSTS".equals(zfinPropertiesEnum[i].name())){
                errorString += "INFORMIXSQLHOSTS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INFORMIXSQLHOSTS@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/Informix/informix/etc/sqlhosts".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INFORMIXSQLHOSTS")){
                    errorString += "INFORMIXSQLHOSTS " ;
                    %>
        <font color="red">Deployed value: '@INFORMIXSQLHOSTS@' or '/private/apps/Informix/informix/etc/sqlhosts' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INFORMIX_DIR

        <%--// (do check on key)!--%>
        <%
            if(false=="INFORMIX_DIR".equals(zfinPropertiesEnum[i].name())){
                errorString += "INFORMIX_DIR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INFORMIX_DIR@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/Informix/informix".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INFORMIX_DIR")){
                    errorString += "INFORMIX_DIR " ;
                    %>
        <font color="red">Deployed value: '@INFORMIX_DIR@' or '/private/apps/Informix/informix' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INFORMIX_PORT

        <%--// (do check on key)!--%>
        <%
            if(false=="INFORMIX_PORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "INFORMIX_PORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INFORMIX_PORT@".equals(zfinPropertiesEnum[i].value()) || false=="2002".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INFORMIX_PORT")){
                    errorString += "INFORMIX_PORT " ;
                    %>
        <font color="red">Deployed value: '@INFORMIX_PORT@' or '2002' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INFORMIX_SERVER

        <%--// (do check on key)!--%>
        <%
            if(false=="INFORMIX_SERVER".equals(zfinPropertiesEnum[i].name())){
                errorString += "INFORMIX_SERVER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INFORMIX_SERVER@".equals(zfinPropertiesEnum[i].value()) || false=="waffle".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INFORMIX_SERVER")){
                    errorString += "INFORMIX_SERVER " ;
                    %>
        <font color="red">Deployed value: '@INFORMIX_SERVER@' or 'waffle' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INSTANCE

        <%--// (do check on key)!--%>
        <%
            if(false=="INSTANCE".equals(zfinPropertiesEnum[i].name())){
                errorString += "INSTANCE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INSTANCE@".equals(zfinPropertiesEnum[i].value()) || false=="cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INSTANCE")){
                    errorString += "INSTANCE " ;
                    %>
        <font color="red">Deployed value: '@INSTANCE@' or 'cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        INTERNAL_BLAST_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="INTERNAL_BLAST_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "INTERNAL_BLAST_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@INTERNAL_BLAST_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="NEED_TO_SET_A_BLAST_PATH".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("INTERNAL_BLAST_PATH")){
                    errorString += "INTERNAL_BLAST_PATH " ;
                    %>
        <font color="red">Deployed value: '@INTERNAL_BLAST_PATH@' or 'NEED_TO_SET_A_BLAST_PATH' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        JAVA_HOME

        <%--// (do check on key)!--%>
        <%
            if(false=="JAVA_HOME".equals(zfinPropertiesEnum[i].name())){
                errorString += "JAVA_HOME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@JAVA_HOME@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/java".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("JAVA_HOME")){
                    errorString += "JAVA_HOME " ;
                    %>
        <font color="red">Deployed value: '@JAVA_HOME@' or '/private/apps/java' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        JENKINS_HOME

        <%--// (do check on key)!--%>
        <%
            if(false=="JENKINS_HOME".equals(zfinPropertiesEnum[i].name())){
                errorString += "JENKINS_HOME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@JENKINS_HOME@".equals(zfinPropertiesEnum[i].value()) || false=="/opt/zfin/www_homes/cell/server_apps/jenkins/jenkins-home".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("JENKINS_HOME")){
                    errorString += "JENKINS_HOME " ;
                    %>
        <font color="red">Deployed value: '@JENKINS_HOME@' or '/opt/zfin/www_homes/cell/server_apps/jenkins/jenkins-home' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        JENKINS_PORT

        <%--// (do check on key)!--%>
        <%
            if(false=="JENKINS_PORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "JENKINS_PORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@JENKINS_PORT@".equals(zfinPropertiesEnum[i].value()) || false=="9445".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("JENKINS_PORT")){
                    errorString += "JENKINS_PORT " ;
                    %>
        <font color="red">Deployed value: '@JENKINS_PORT@' or '9445' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        JPDA_ADDRESS

        <%--// (do check on key)!--%>
        <%
            if(false=="JPDA_ADDRESS".equals(zfinPropertiesEnum[i].name())){
                errorString += "JPDA_ADDRESS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@JPDA_ADDRESS@".equals(zfinPropertiesEnum[i].value()) || false=="9345".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("JPDA_ADDRESS")){
                    errorString += "JPDA_ADDRESS " ;
                    %>
        <font color="red">Deployed value: '@JPDA_ADDRESS@' or '9345' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LD_LIBRARY_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="LD_LIBRARY_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "LD_LIBRARY_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LD_LIBRARY_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/Informix/informix/lib:/private/apps/Informix/informix/lib/esql".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LD_LIBRARY_PATH")){
                    errorString += "LD_LIBRARY_PATH " ;
                    %>
        <font color="red">Deployed value: '@LD_LIBRARY_PATH@' or '/private/apps/Informix/informix/lib:/private/apps/Informix/informix/lib/esql' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LINKBAR_COLOR

        <%--// (do check on key)!--%>
        <%
            if(false=="LINKBAR_COLOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "LINKBAR_COLOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LINKBAR_COLOR@".equals(zfinPropertiesEnum[i].value()) || false=="#006666".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LINKBAR_COLOR")){
                    errorString += "LINKBAR_COLOR " ;
                    %>
        <font color="red">Deployed value: '@LINKBAR_COLOR@' or '#006666' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LN54_CONTACT_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="LN54_CONTACT_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "LN54_CONTACT_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LN54_CONTACT_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LN54_CONTACT_EMAIL")){
                    errorString += "LN54_CONTACT_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@LN54_CONTACT_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LOADUP_FULL_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="LOADUP_FULL_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "LOADUP_FULL_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LOADUP_FULL_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zcentral/loadUp".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LOADUP_FULL_PATH")){
                    errorString += "LOADUP_FULL_PATH " ;
                    %>
        <font color="red">Deployed value: '@LOADUP_FULL_PATH@' or '/research/zcentral/loadUp' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LOAD_ONTOLOGIES_AT_STARTUP

        <%--// (do check on key)!--%>
        <%
            if(false=="LOAD_ONTOLOGIES_AT_STARTUP".equals(zfinPropertiesEnum[i].name())){
                errorString += "LOAD_ONTOLOGIES_AT_STARTUP " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LOAD_ONTOLOGIES_AT_STARTUP@".equals(zfinPropertiesEnum[i].value()) || false=="true".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LOAD_ONTOLOGIES_AT_STARTUP")){
                    errorString += "LOAD_ONTOLOGIES_AT_STARTUP " ;
                    %>
        <font color="red">Deployed value: '@LOAD_ONTOLOGIES_AT_STARTUP@' or 'true' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LOCUS_REGISTRATION_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="LOCUS_REGISTRATION_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "LOCUS_REGISTRATION_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LOCUS_REGISTRATION_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LOCUS_REGISTRATION_EMAIL")){
                    errorString += "LOCUS_REGISTRATION_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@LOCUS_REGISTRATION_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LOG4J_FILE

        <%--// (do check on key)!--%>
        <%
            if(false=="LOG4J_FILE".equals(zfinPropertiesEnum[i].name())){
                errorString += "LOG4J_FILE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LOG4J_FILE@".equals(zfinPropertiesEnum[i].value()) || false=="coral.log4j.xml".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LOG4J_FILE")){
                    errorString += "LOG4J_FILE " ;
                    %>
        <font color="red">Deployed value: '@LOG4J_FILE@' or 'coral.log4j.xml' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LOG_FILE_MAX

        <%--// (do check on key)!--%>
        <%
            if(false=="LOG_FILE_MAX".equals(zfinPropertiesEnum[i].name())){
                errorString += "LOG_FILE_MAX " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LOG_FILE_MAX@".equals(zfinPropertiesEnum[i].value()) || false=="10".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LOG_FILE_MAX")){
                    errorString += "LOG_FILE_MAX " ;
                    %>
        <font color="red">Deployed value: '@LOG_FILE_MAX@' or '10' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LOG_FILE_PATTERN

        <%--// (do check on key)!--%>
        <%
            if(false=="LOG_FILE_PATTERN".equals(zfinPropertiesEnum[i].name())){
                errorString += "LOG_FILE_PATTERN " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LOG_FILE_PATTERN@".equals(zfinPropertiesEnum[i].value()) || false=="%d [%t] %-5p %c{2} - %m%n".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LOG_FILE_PATTERN")){
                    errorString += "LOG_FILE_PATTERN " ;
                    %>
        <font color="red">Deployed value: '@LOG_FILE_PATTERN@' or '%d [%t] %-5p %c{2} - %m%n' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LOG_FILE_SESSION

        <%--// (do check on key)!--%>
        <%
            if(false=="LOG_FILE_SESSION".equals(zfinPropertiesEnum[i].name())){
                errorString += "LOG_FILE_SESSION " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LOG_FILE_SESSION@".equals(zfinPropertiesEnum[i].value()) || false=="zfin-session.log".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LOG_FILE_SESSION")){
                    errorString += "LOG_FILE_SESSION " ;
                    %>
        <font color="red">Deployed value: '@LOG_FILE_SESSION@' or 'zfin-session.log' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        LOG_FILE_SIZE

        <%--// (do check on key)!--%>
        <%
            if(false=="LOG_FILE_SIZE".equals(zfinPropertiesEnum[i].name())){
                errorString += "LOG_FILE_SIZE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@LOG_FILE_SIZE@".equals(zfinPropertiesEnum[i].value()) || false=="1".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("LOG_FILE_SIZE")){
                    errorString += "LOG_FILE_SIZE " ;
                    %>
        <font color="red">Deployed value: '@LOG_FILE_SIZE@' or '1' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        MACHINE_NAME

        <%--// (do check on key)!--%>
        <%
            if(false=="MACHINE_NAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "MACHINE_NAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@MACHINE_NAME@".equals(zfinPropertiesEnum[i].value()) || false=="bent".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("MACHINE_NAME")){
                    errorString += "MACHINE_NAME " ;
                    %>
        <font color="red">Deployed value: '@MACHINE_NAME@' or 'bent' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        MICROARRAY_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="MICROARRAY_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "MICROARRAY_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@MICROARRAY_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("MICROARRAY_EMAIL")){
                    errorString += "MICROARRAY_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@MICROARRAY_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        MOVE_BLAST_FILES_TO_DEVELOPMENT

        <%--// (do check on key)!--%>
        <%
            if(false=="MOVE_BLAST_FILES_TO_DEVELOPMENT".equals(zfinPropertiesEnum[i].name())){
                errorString += "MOVE_BLAST_FILES_TO_DEVELOPMENT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@MOVE_BLAST_FILES_TO_DEVELOPMENT@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("MOVE_BLAST_FILES_TO_DEVELOPMENT")){
                    errorString += "MOVE_BLAST_FILES_TO_DEVELOPMENT " ;
                    %>
        <font color="red">Deployed value: '@MOVE_BLAST_FILES_TO_DEVELOPMENT@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        MUTANT_NAME

        <%--// (do check on key)!--%>
        <%
            if(false=="MUTANT_NAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "MUTANT_NAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@MUTANT_NAME@".equals(zfinPropertiesEnum[i].value()) || false=="cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("MUTANT_NAME")){
                    errorString += "MUTANT_NAME " ;
                    %>
        <font color="red">Deployed value: '@MUTANT_NAME@' or 'cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        NEW_PUBLICATION_REPORT

        <%--// (do check on key)!--%>
        <%
            if(false=="NEW_PUBLICATION_REPORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "NEW_PUBLICATION_REPORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@NEW_PUBLICATION_REPORT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("NEW_PUBLICATION_REPORT")){
                    errorString += "NEW_PUBLICATION_REPORT " ;
                    %>
        <font color="red">Deployed value: '@NEW_PUBLICATION_REPORT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        NOMEN_COORDINATOR

        <%--// (do check on key)!--%>
        <%
            if(false=="NOMEN_COORDINATOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "NOMEN_COORDINATOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@NOMEN_COORDINATOR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("NOMEN_COORDINATOR")){
                    errorString += "NOMEN_COORDINATOR " ;
                    %>
        <font color="red">Deployed value: '@NOMEN_COORDINATOR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        NON_SECUREPORT

        <%--// (do check on key)!--%>
        <%
            if(false=="NON_SECUREPORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "NON_SECUREPORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@NON_SECUREPORT@".equals(zfinPropertiesEnum[i].value()) || false=="9145".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("NON_SECUREPORT")){
                    errorString += "NON_SECUREPORT " ;
                    %>
        <font color="red">Deployed value: '@NON_SECUREPORT@' or '9145' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        NON_SECURE_HTTP

        <%--// (do check on key)!--%>
        <%
            if(false=="NON_SECURE_HTTP".equals(zfinPropertiesEnum[i].name())){
                errorString += "NON_SECURE_HTTP " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@NON_SECURE_HTTP@".equals(zfinPropertiesEnum[i].value()) || false=="http://".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("NON_SECURE_HTTP")){
                    errorString += "NON_SECURE_HTTP " ;
                    %>
        <font color="red">Deployed value: '@NON_SECURE_HTTP@' or 'http://' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ONCONFIG

        <%--// (do check on key)!--%>
        <%
            if(false=="ONCONFIG".equals(zfinPropertiesEnum[i].name())){
                errorString += "ONCONFIG " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ONCONFIG@".equals(zfinPropertiesEnum[i].value()) || false=="onconfig".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ONCONFIG")){
                    errorString += "ONCONFIG " ;
                    %>
        <font color="red">Deployed value: '@ONCONFIG@' or 'onconfig' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ONCONFIG_FILE

        <%--// (do check on key)!--%>
        <%
            if(false=="ONCONFIG_FILE".equals(zfinPropertiesEnum[i].name())){
                errorString += "ONCONFIG_FILE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ONCONFIG_FILE@".equals(zfinPropertiesEnum[i].value()) || false=="onconfig".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ONCONFIG_FILE")){
                    errorString += "ONCONFIG_FILE " ;
                    %>
        <font color="red">Deployed value: '@ONCONFIG_FILE@' or 'onconfig' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ONTOLOGY_LOADER_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="ONTOLOGY_LOADER_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "ONTOLOGY_LOADER_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ONTOLOGY_LOADER_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ONTOLOGY_LOADER_EMAIL")){
                    errorString += "ONTOLOGY_LOADER_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@ONTOLOGY_LOADER_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        PARTNER_DBNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="PARTNER_DBNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "PARTNER_DBNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@PARTNER_DBNAME@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("PARTNER_DBNAME")){
                    errorString += "PARTNER_DBNAME " ;
                    %>
        <font color="red">Deployed value: '@PARTNER_DBNAME@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        PARTNER_INTERNAL_INSTANCE

        <%--// (do check on key)!--%>
        <%
            if(false=="PARTNER_INTERNAL_INSTANCE".equals(zfinPropertiesEnum[i].name())){
                errorString += "PARTNER_INTERNAL_INSTANCE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@PARTNER_INTERNAL_INSTANCE@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("PARTNER_INTERNAL_INSTANCE")){
                    errorString += "PARTNER_INTERNAL_INSTANCE " ;
                    %>
        <font color="red">Deployed value: '@PARTNER_INTERNAL_INSTANCE@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        PARTNER_SOURCEROOT

        <%--// (do check on key)!--%>
        <%
            if(false=="PARTNER_SOURCEROOT".equals(zfinPropertiesEnum[i].name())){
                errorString += "PARTNER_SOURCEROOT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@PARTNER_SOURCEROOT@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("PARTNER_SOURCEROOT")){
                    errorString += "PARTNER_SOURCEROOT " ;
                    %>
        <font color="red">Deployed value: '@PARTNER_SOURCEROOT@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        PATO_EMAIL_CURATOR

        <%--// (do check on key)!--%>
        <%
            if(false=="PATO_EMAIL_CURATOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "PATO_EMAIL_CURATOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@PATO_EMAIL_CURATOR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("PATO_EMAIL_CURATOR")){
                    errorString += "PATO_EMAIL_CURATOR " ;
                    %>
        <font color="red">Deployed value: '@PATO_EMAIL_CURATOR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        PDF_LOAD

        <%--// (do check on key)!--%>
        <%
            if(false=="PDF_LOAD".equals(zfinPropertiesEnum[i].name())){
                errorString += "PDF_LOAD " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@PDF_LOAD@".equals(zfinPropertiesEnum[i].value()) || false=="/PDFLoadUp".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("PDF_LOAD")){
                    errorString += "PDF_LOAD " ;
                    %>
        <font color="red">Deployed value: '@PDF_LOAD@' or '/PDFLoadUp' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        PDF_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="PDF_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "PDF_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@PDF_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/pdf/".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("PDF_PATH")){
                    errorString += "PDF_PATH " ;
                    %>
        <font color="red">Deployed value: '@PDF_PATH@' or '/pdf/' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        PRODUCTION_NOTIFICATION_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="PRODUCTION_NOTIFICATION_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "PRODUCTION_NOTIFICATION_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@PRODUCTION_NOTIFICATION_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("PRODUCTION_NOTIFICATION_EMAIL")){
                    errorString += "PRODUCTION_NOTIFICATION_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@PRODUCTION_NOTIFICATION_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        REQUEST_NEW_ANATOMY_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="REQUEST_NEW_ANATOMY_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "REQUEST_NEW_ANATOMY_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@REQUEST_NEW_ANATOMY_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("REQUEST_NEW_ANATOMY_EMAIL")){
                    errorString += "REQUEST_NEW_ANATOMY_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@REQUEST_NEW_ANATOMY_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ROOT_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="ROOT_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "ROOT_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ROOT_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/opt/zfin/www_homes/cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ROOT_PATH")){
                    errorString += "ROOT_PATH " ;
                    %>
        <font color="red">Deployed value: '@ROOT_PATH@' or '/opt/zfin/www_homes/cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        RUN_QUARTZ_JOBS

        <%--// (do check on key)!--%>
        <%
            if(false=="RUN_QUARTZ_JOBS".equals(zfinPropertiesEnum[i].name())){
                errorString += "RUN_QUARTZ_JOBS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@RUN_QUARTZ_JOBS@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("RUN_QUARTZ_JOBS")){
                    errorString += "RUN_QUARTZ_JOBS " ;
                    %>
        <font color="red">Deployed value: '@RUN_QUARTZ_JOBS@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SCHEDULE_TRIGGER_FILES

        <%--// (do check on key)!--%>
        <%
            if(false=="SCHEDULE_TRIGGER_FILES".equals(zfinPropertiesEnum[i].name())){
                errorString += "SCHEDULE_TRIGGER_FILES " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SCHEDULE_TRIGGER_FILES@".equals(zfinPropertiesEnum[i].value()) || false=="".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SCHEDULE_TRIGGER_FILES")){
                    errorString += "SCHEDULE_TRIGGER_FILES " ;
                    %>
        <font color="red">Deployed value: '@SCHEDULE_TRIGGER_FILES@' or '' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SECUREPORT

        <%--// (do check on key)!--%>
        <%
            if(false=="SECUREPORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "SECUREPORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SECUREPORT@".equals(zfinPropertiesEnum[i].value()) || false=="9245".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SECUREPORT")){
                    errorString += "SECUREPORT " ;
                    %>
        <font color="red">Deployed value: '@SECUREPORT@' or '9245' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SECURE_HTTP

        <%--// (do check on key)!--%>
        <%
            if(false=="SECURE_HTTP".equals(zfinPropertiesEnum[i].name())){
                errorString += "SECURE_HTTP " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SECURE_HTTP@".equals(zfinPropertiesEnum[i].value()) || false=="https://".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SECURE_HTTP")){
                    errorString += "SECURE_HTTP " ;
                    %>
        <font color="red">Deployed value: '@SECURE_HTTP@' or 'https://' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SECURE_SERVER

        <%--// (do check on key)!--%>
        <%
            if(false=="SECURE_SERVER".equals(zfinPropertiesEnum[i].name())){
                errorString += "SECURE_SERVER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SECURE_SERVER@".equals(zfinPropertiesEnum[i].value()) || false=="true".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SECURE_SERVER")){
                    errorString += "SECURE_SERVER " ;
                    %>
        <font color="red">Deployed value: '@SECURE_SERVER@' or 'true' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SHARED_DOMAIN_NAME

        <%--// (do check on key)!--%>
        <%
            if(false=="SHARED_DOMAIN_NAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "SHARED_DOMAIN_NAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SHARED_DOMAIN_NAME@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SHARED_DOMAIN_NAME")){
                    errorString += "SHARED_DOMAIN_NAME " ;
                    %>
        <font color="red">Deployed value: '@SHARED_DOMAIN_NAME@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SHOW_SQL

        <%--// (do check on key)!--%>
        <%
            if(false=="SHOW_SQL".equals(zfinPropertiesEnum[i].name())){
                errorString += "SHOW_SQL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SHOW_SQL@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SHOW_SQL")){
                    errorString += "SHOW_SQL " ;
                    %>
        <font color="red">Deployed value: '@SHOW_SQL@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SHUTDOWNPORT

        <%--// (do check on key)!--%>
        <%
            if(false=="SHUTDOWNPORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "SHUTDOWNPORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SHUTDOWNPORT@".equals(zfinPropertiesEnum[i].value()) || false=="9045".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SHUTDOWNPORT")){
                    errorString += "SHUTDOWNPORT " ;
                    %>
        <font color="red">Deployed value: '@SHUTDOWNPORT@' or '9045' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SIDEBAR_COLOR

        <%--// (do check on key)!--%>
        <%
            if(false=="SIDEBAR_COLOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "SIDEBAR_COLOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SIDEBAR_COLOR@".equals(zfinPropertiesEnum[i].value()) || false=="#CCCCCC".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SIDEBAR_COLOR")){
                    errorString += "SIDEBAR_COLOR " ;
                    %>
        <font color="red">Deployed value: '@SIDEBAR_COLOR@' or '#CCCCCC' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SITE_BACKGROUND

        <%--// (do check on key)!--%>
        <%
            if(false=="SITE_BACKGROUND".equals(zfinPropertiesEnum[i].name())){
                errorString += "SITE_BACKGROUND " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SITE_BACKGROUND@".equals(zfinPropertiesEnum[i].value()) || false=="#FFFFFF".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SITE_BACKGROUND")){
                    errorString += "SITE_BACKGROUND " ;
                    %>
        <font color="red">Deployed value: '@SITE_BACKGROUND@' or '#FFFFFF' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SOURCEROOT

        <%--// (do check on key)!--%>
        <%
            if(false=="SOURCEROOT".equals(zfinPropertiesEnum[i].name())){
                errorString += "SOURCEROOT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SOURCEROOT@".equals(zfinPropertiesEnum[i].value()) || false=="/opt/zfin/source_roots/cell/ZFIN_WWW".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SOURCEROOT")){
                    errorString += "SOURCEROOT " ;
                    %>
        <font color="red">Deployed value: '@SOURCEROOT@' or '/opt/zfin/source_roots/cell/ZFIN_WWW' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SQLHOSTS_FILE

        <%--// (do check on key)!--%>
        <%
            if(false=="SQLHOSTS_FILE".equals(zfinPropertiesEnum[i].name())){
                errorString += "SQLHOSTS_FILE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SQLHOSTS_FILE@".equals(zfinPropertiesEnum[i].value()) || false=="sqlhosts".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SQLHOSTS_FILE")){
                    errorString += "SQLHOSTS_FILE " ;
                    %>
        <font color="red">Deployed value: '@SQLHOSTS_FILE@' or 'sqlhosts' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SQLHOSTS_HOST

        <%--// (do check on key)!--%>
        <%
            if(false=="SQLHOSTS_HOST".equals(zfinPropertiesEnum[i].name())){
                errorString += "SQLHOSTS_HOST " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SQLHOSTS_HOST@".equals(zfinPropertiesEnum[i].value()) || false=="bent.zfin.org".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SQLHOSTS_HOST")){
                    errorString += "SQLHOSTS_HOST " ;
                    %>
        <font color="red">Deployed value: '@SQLHOSTS_HOST@' or 'bent.zfin.org' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SSH

        <%--// (do check on key)!--%>
        <%
            if(false=="SSH".equals(zfinPropertiesEnum[i].name())){
                errorString += "SSH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SSH@".equals(zfinPropertiesEnum[i].value()) || false=="ssh".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SSH")){
                    errorString += "SSH " ;
                    %>
        <font color="red">Deployed value: '@SSH@' or 'ssh' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SWISSPROT_EMAIL_CURATOR

        <%--// (do check on key)!--%>
        <%
            if(false=="SWISSPROT_EMAIL_CURATOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "SWISSPROT_EMAIL_CURATOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SWISSPROT_EMAIL_CURATOR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SWISSPROT_EMAIL_CURATOR")){
                    errorString += "SWISSPROT_EMAIL_CURATOR " ;
                    %>
        <font color="red">Deployed value: '@SWISSPROT_EMAIL_CURATOR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SWISSPROT_EMAIL_ERR

        <%--// (do check on key)!--%>
        <%
            if(false=="SWISSPROT_EMAIL_ERR".equals(zfinPropertiesEnum[i].name())){
                errorString += "SWISSPROT_EMAIL_ERR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SWISSPROT_EMAIL_ERR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SWISSPROT_EMAIL_ERR")){
                    errorString += "SWISSPROT_EMAIL_ERR " ;
                    %>
        <font color="red">Deployed value: '@SWISSPROT_EMAIL_ERR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        SWISSPROT_EMAIL_REPORT

        <%--// (do check on key)!--%>
        <%
            if(false=="SWISSPROT_EMAIL_REPORT".equals(zfinPropertiesEnum[i].name())){
                errorString += "SWISSPROT_EMAIL_REPORT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@SWISSPROT_EMAIL_REPORT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("SWISSPROT_EMAIL_REPORT")){
                    errorString += "SWISSPROT_EMAIL_REPORT " ;
                    %>
        <font color="red">Deployed value: '@SWISSPROT_EMAIL_REPORT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        TARGETCGIBIN

        <%--// (do check on key)!--%>
        <%
            if(false=="TARGETCGIBIN".equals(zfinPropertiesEnum[i].name())){
                errorString += "TARGETCGIBIN " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@TARGETCGIBIN@".equals(zfinPropertiesEnum[i].value()) || false=="cgi-bin".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("TARGETCGIBIN")){
                    errorString += "TARGETCGIBIN " ;
                    %>
        <font color="red">Deployed value: '@TARGETCGIBIN@' or 'cgi-bin' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        TARGETFTPROOT

        <%--// (do check on key)!--%>
        <%
            if(false=="TARGETFTPROOT".equals(zfinPropertiesEnum[i].name())){
                errorString += "TARGETFTPROOT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@TARGETFTPROOT@".equals(zfinPropertiesEnum[i].value()) || false=="/opt/zfin/ftp/test/cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("TARGETFTPROOT")){
                    errorString += "TARGETFTPROOT " ;
                    %>
        <font color="red">Deployed value: '@TARGETFTPROOT@' or '/opt/zfin/ftp/test/cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        TARGETROOT

        <%--// (do check on key)!--%>
        <%
            if(false=="TARGETROOT".equals(zfinPropertiesEnum[i].name())){
                errorString += "TARGETROOT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@TARGETROOT@".equals(zfinPropertiesEnum[i].value()) || false=="/opt/zfin/www_homes/cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("TARGETROOT")){
                    errorString += "TARGETROOT " ;
                    %>
        <font color="red">Deployed value: '@TARGETROOT@' or '/opt/zfin/www_homes/cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        TARGETROOT_PREFIX

        <%--// (do check on key)!--%>
        <%
            if(false=="TARGETROOT_PREFIX".equals(zfinPropertiesEnum[i].name())){
                errorString += "TARGETROOT_PREFIX " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@TARGETROOT_PREFIX@".equals(zfinPropertiesEnum[i].value()) || false=="/opt/zfin".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("TARGETROOT_PREFIX")){
                    errorString += "TARGETROOT_PREFIX " ;
                    %>
        <font color="red">Deployed value: '@TARGETROOT_PREFIX@' or '/opt/zfin' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        TECHNICAL_AT_ZFIN

        <%--// (do check on key)!--%>
        <%
            if(false=="TECHNICAL_AT_ZFIN".equals(zfinPropertiesEnum[i].name())){
                errorString += "TECHNICAL_AT_ZFIN " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@TECHNICAL_AT_ZFIN@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("TECHNICAL_AT_ZFIN")){
                    errorString += "TECHNICAL_AT_ZFIN " ;
                    %>
        <font color="red">Deployed value: '@TECHNICAL_AT_ZFIN@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        USER

        <%--// (do check on key)!--%>
        <%
            if(false=="USER".equals(zfinPropertiesEnum[i].name())){
                errorString += "USER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@USER@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("USER")){
                    errorString += "USER " ;
                    %>
        <font color="red">Deployed value: '@USER@' or 'kschaper' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        USE_APACHE_FOR_SMOKE_TESTS

        <%--// (do check on key)!--%>
        <%
            if(false=="USE_APACHE_FOR_SMOKE_TESTS".equals(zfinPropertiesEnum[i].name())){
                errorString += "USE_APACHE_FOR_SMOKE_TESTS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@USE_APACHE_FOR_SMOKE_TESTS@".equals(zfinPropertiesEnum[i].value()) || false=="true".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("USE_APACHE_FOR_SMOKE_TESTS")){
                    errorString += "USE_APACHE_FOR_SMOKE_TESTS " ;
                    %>
        <font color="red">Deployed value: '@USE_APACHE_FOR_SMOKE_TESTS@' or 'true' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_AD

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_AD".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_AD " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_AD@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_AD")){
                    errorString += "VALIDATION_EMAIL_AD " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_AD@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_CURATOR

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_CURATOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_CURATOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_CURATOR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_CURATOR")){
                    errorString += "VALIDATION_EMAIL_CURATOR " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_CURATOR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_DBA

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_DBA".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_DBA " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_DBA@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_DBA")){
                    errorString += "VALIDATION_EMAIL_DBA " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_DBA@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_EST

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_EST".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_EST " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_EST@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_EST")){
                    errorString += "VALIDATION_EMAIL_EST " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_EST@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_GENE

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_GENE".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_GENE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_GENE@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_GENE")){
                    errorString += "VALIDATION_EMAIL_GENE " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_GENE@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_GENOCURATOR

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_GENOCURATOR".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_GENOCURATOR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_GENOCURATOR@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_GENOCURATOR")){
                    errorString += "VALIDATION_EMAIL_GENOCURATOR " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_GENOCURATOR@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_LINKAGE

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_LINKAGE".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_LINKAGE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_LINKAGE@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_LINKAGE")){
                    errorString += "VALIDATION_EMAIL_LINKAGE " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_LINKAGE@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_MORPHOLINO

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_MORPHOLINO".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_MORPHOLINO " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_MORPHOLINO@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_MORPHOLINO")){
                    errorString += "VALIDATION_EMAIL_MORPHOLINO " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_MORPHOLINO@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_MUTANT

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_MUTANT".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_MUTANT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_MUTANT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_MUTANT")){
                    errorString += "VALIDATION_EMAIL_MUTANT " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_MUTANT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_OTHER

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_OTHER".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_OTHER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_OTHER@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_OTHER")){
                    errorString += "VALIDATION_EMAIL_OTHER " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_OTHER@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_PUBLICATION

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_PUBLICATION".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_PUBLICATION " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_PUBLICATION@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_PUBLICATION")){
                    errorString += "VALIDATION_EMAIL_PUBLICATION " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_PUBLICATION@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_TRANSCRIPT

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_TRANSCRIPT".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_TRANSCRIPT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_TRANSCRIPT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_TRANSCRIPT")){
                    errorString += "VALIDATION_EMAIL_TRANSCRIPT " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_TRANSCRIPT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALIDATION_EMAIL_XPAT

        <%--// (do check on key)!--%>
        <%
            if(false=="VALIDATION_EMAIL_XPAT".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALIDATION_EMAIL_XPAT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALIDATION_EMAIL_XPAT@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALIDATION_EMAIL_XPAT")){
                    errorString += "VALIDATION_EMAIL_XPAT " ;
                    %>
        <font color="red">Deployed value: '@VALIDATION_EMAIL_XPAT@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VALID_SESSION_TIMEOUT_SECONDS

        <%--// (do check on key)!--%>
        <%
            if(false=="VALID_SESSION_TIMEOUT_SECONDS".equals(zfinPropertiesEnum[i].name())){
                errorString += "VALID_SESSION_TIMEOUT_SECONDS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VALID_SESSION_TIMEOUT_SECONDS@".equals(zfinPropertiesEnum[i].value()) || false=="432000".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VALID_SESSION_TIMEOUT_SECONDS")){
                    errorString += "VALID_SESSION_TIMEOUT_SECONDS " ;
                    %>
        <font color="red">Deployed value: '@VALID_SESSION_TIMEOUT_SECONDS@' or '432000' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        VIDEO_LOAD

        <%--// (do check on key)!--%>
        <%
            if(false=="VIDEO_LOAD".equals(zfinPropertiesEnum[i].name())){
                errorString += "VIDEO_LOAD " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@VIDEO_LOAD@".equals(zfinPropertiesEnum[i].value()) || false=="/videoLoadUp".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("VIDEO_LOAD")){
                    errorString += "VIDEO_LOAD " ;
                    %>
        <font color="red">Deployed value: '@VIDEO_LOAD@' or '/videoLoadUp' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WAREHOUSE_DUMP_DIR

        <%--// (do check on key)!--%>
        <%
            if(false=="WAREHOUSE_DUMP_DIR".equals(zfinPropertiesEnum[i].name())){
                errorString += "WAREHOUSE_DUMP_DIR " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WAREHOUSE_DUMP_DIR@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zunloads/databases/zygotix".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WAREHOUSE_DUMP_DIR")){
                    errorString += "WAREHOUSE_DUMP_DIR " ;
                    %>
        <font color="red">Deployed value: '@WAREHOUSE_DUMP_DIR@' or '/research/zunloads/databases/zygotix' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WAREHOUSE_REGN_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="WAREHOUSE_REGN_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "WAREHOUSE_REGN_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WAREHOUSE_REGN_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WAREHOUSE_REGN_EMAIL")){
                    errorString += "WAREHOUSE_REGN_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@WAREHOUSE_REGN_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBDRIVER_LOC

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBDRIVER_LOC".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBDRIVER_LOC " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBDRIVER_LOC@".equals(zfinPropertiesEnum[i].value()) || false=="cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBDRIVER_LOC")){
                    errorString += "WEBDRIVER_LOC " ;
                    %>
        <font color="red">Deployed value: '@WEBDRIVER_LOC@' or 'cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBDRIVER_PATH_FROM_ROOT

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBDRIVER_PATH_FROM_ROOT".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBDRIVER_PATH_FROM_ROOT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBDRIVER_PATH_FROM_ROOT@".equals(zfinPropertiesEnum[i].value()) || false=="cgi-bin/webdriver".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBDRIVER_PATH_FROM_ROOT")){
                    errorString += "WEBDRIVER_PATH_FROM_ROOT " ;
                    %>
        <font color="red">Deployed value: '@WEBDRIVER_PATH_FROM_ROOT@' or 'cgi-bin/webdriver' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_BINARY_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_BINARY_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_BINARY_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_BINARY_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/private/apps/wublast".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_BINARY_PATH")){
                    errorString += "WEBHOST_BINARY_PATH " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_BINARY_PATH@' or '/private/apps/wublast' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_BLASTALL

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_BLASTALL".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_BLASTALL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_BLASTALL@".equals(zfinPropertiesEnum[i].value()) || false=="wu-blastall".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_BLASTALL")){
                    errorString += "WEBHOST_BLASTALL " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_BLASTALL@' or 'wu-blastall' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_BLASTDB_CURL_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_BLASTDB_CURL_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_BLASTDB_CURL_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_BLASTDB_CURL_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="http://almost.zfin.org/blastdb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_BLASTDB_CURL_PATH")){
                    errorString += "WEBHOST_BLASTDB_CURL_PATH " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_BLASTDB_CURL_PATH@' or 'http://almost.zfin.org/blastdb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_BLASTDB_TO_COPY

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_BLASTDB_TO_COPY".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_BLASTDB_TO_COPY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_BLASTDB_TO_COPY@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zblastfiles/zmore/almdb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_BLASTDB_TO_COPY")){
                    errorString += "WEBHOST_BLASTDB_TO_COPY " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_BLASTDB_TO_COPY@' or '/research/zblastfiles/zmore/almdb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_BLAST_DATABASE_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_BLAST_DATABASE_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_BLAST_DATABASE_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_BLAST_DATABASE_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zblastfiles/zmore/dev_blastdb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_BLAST_DATABASE_PATH")){
                    errorString += "WEBHOST_BLAST_DATABASE_PATH " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_BLAST_DATABASE_PATH@' or '/research/zblastfiles/zmore/dev_blastdb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_DBNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_DBNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_DBNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_DBNAME@".equals(zfinPropertiesEnum[i].value()) || false=="celldb".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_DBNAME")){
                    errorString += "WEBHOST_DBNAME " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_DBNAME@' or 'celldb' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_FASTA_FILE_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_FASTA_FILE_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_FASTA_FILE_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_FASTA_FILE_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zblastfiles/dev_files".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_FASTA_FILE_PATH")){
                    errorString += "WEBHOST_FASTA_FILE_PATH " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_FASTA_FILE_PATH@' or '/research/zblastfiles/dev_files' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_GENBANK_DAILY_FILES

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_GENBANK_DAILY_FILES".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_GENBANK_DAILY_FILES " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_GENBANK_DAILY_FILES@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zblastfiles/dev_files/daily".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_GENBANK_DAILY_FILES")){
                    errorString += "WEBHOST_GENBANK_DAILY_FILES " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_GENBANK_DAILY_FILES@' or '/research/zblastfiles/dev_files/daily' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_HOSTNAME

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_HOSTNAME".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_HOSTNAME " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_HOSTNAME@".equals(zfinPropertiesEnum[i].value()) || false=="bent.zfin.org".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_HOSTNAME")){
                    errorString += "WEBHOST_HOSTNAME " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_HOSTNAME@' or 'bent.zfin.org' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_KEY_PATH

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_KEY_PATH".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_KEY_PATH " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_KEY_PATH@".equals(zfinPropertiesEnum[i].value()) || false=="/research/zcentral/shared_private_keys".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_KEY_PATH")){
                    errorString += "WEBHOST_KEY_PATH " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_KEY_PATH@' or '/research/zcentral/shared_private_keys' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_MUTANT

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_MUTANT".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_MUTANT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_MUTANT@".equals(zfinPropertiesEnum[i].value()) || false=="cell".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_MUTANT")){
                    errorString += "WEBHOST_MUTANT " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_MUTANT@' or 'cell' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_SOURCE

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_SOURCE".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_SOURCE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_SOURCE@".equals(zfinPropertiesEnum[i].value()) || false=="/private/ZfinLinks/Commons/env/waldo".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_SOURCE")){
                    errorString += "WEBHOST_SOURCE " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_SOURCE@' or '/private/ZfinLinks/Commons/env/waldo' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_USER

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_USER".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_USER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_USER@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_USER")){
                    errorString += "WEBHOST_USER " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_USER@' or 'kschaper' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_XDFORMAT

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_XDFORMAT".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_XDFORMAT " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_XDFORMAT@".equals(zfinPropertiesEnum[i].value()) || false=="xdformat".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_XDFORMAT")){
                    errorString += "WEBHOST_XDFORMAT " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_XDFORMAT@' or 'xdformat' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBHOST_XDGET

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBHOST_XDGET".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBHOST_XDGET " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBHOST_XDGET@".equals(zfinPropertiesEnum[i].value()) || false=="xdget".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBHOST_XDGET")){
                    errorString += "WEBHOST_XDGET " ;
                    %>
        <font color="red">Deployed value: '@WEBHOST_XDGET@' or 'xdget' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEBROOT_DIRECTORY

        <%--// (do check on key)!--%>
        <%
            if(false=="WEBROOT_DIRECTORY".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEBROOT_DIRECTORY " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEBROOT_DIRECTORY@".equals(zfinPropertiesEnum[i].value()) || false=="/opt/zfin/www_homes/cell/home".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEBROOT_DIRECTORY")){
                    errorString += "WEBROOT_DIRECTORY " ;
                    %>
        <font color="red">Deployed value: '@WEBROOT_DIRECTORY@' or '/opt/zfin/www_homes/cell/home' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WEB_ADMIN_EMAIL

        <%--// (do check on key)!--%>
        <%
            if(false=="WEB_ADMIN_EMAIL".equals(zfinPropertiesEnum[i].name())){
                errorString += "WEB_ADMIN_EMAIL " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WEB_ADMIN_EMAIL@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WEB_ADMIN_EMAIL")){
                    errorString += "WEB_ADMIN_EMAIL " ;
                    %>
        <font color="red">Deployed value: '@WEB_ADMIN_EMAIL@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WIKI_HOST

        <%--// (do check on key)!--%>
        <%
            if(false=="WIKI_HOST".equals(zfinPropertiesEnum[i].name())){
                errorString += "WIKI_HOST " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WIKI_HOST@".equals(zfinPropertiesEnum[i].value()) || false=="devwiki.zfin.org".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WIKI_HOST")){
                    errorString += "WIKI_HOST " ;
                    %>
        <font color="red">Deployed value: '@WIKI_HOST@' or 'devwiki.zfin.org' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WIKI_PASS

        <%--// (do check on key)!--%>
        <%
            if(false=="WIKI_PASS".equals(zfinPropertiesEnum[i].name())){
                errorString += "WIKI_PASS " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WIKI_PASS@".equals(zfinPropertiesEnum[i].value()) || false=="dan1orer1o".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WIKI_PASS")){
                    errorString += "WIKI_PASS " ;
                    %>
        <font color="red">Deployed value: '@WIKI_PASS@' or 'dan1orer1o' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WIKI_PUSH_TO_WIKI

        <%--// (do check on key)!--%>
        <%
            if(false=="WIKI_PUSH_TO_WIKI".equals(zfinPropertiesEnum[i].name())){
                errorString += "WIKI_PUSH_TO_WIKI " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WIKI_PUSH_TO_WIKI@".equals(zfinPropertiesEnum[i].value()) || false=="false".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WIKI_PUSH_TO_WIKI")){
                    errorString += "WIKI_PUSH_TO_WIKI " ;
                    %>
        <font color="red">Deployed value: '@WIKI_PUSH_TO_WIKI@' or 'false' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        WIKI_USER

        <%--// (do check on key)!--%>
        <%
            if(false=="WIKI_USER".equals(zfinPropertiesEnum[i].name())){
                errorString += "WIKI_USER " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@WIKI_USER@".equals(zfinPropertiesEnum[i].value()) || false=="webservice".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("WIKI_USER")){
                    errorString += "WIKI_USER " ;
                    %>
        <font color="red">Deployed value: '@WIKI_USER@' or 'webservice' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ZDBHOME_BACKGROUND

        <%--// (do check on key)!--%>
        <%
            if(false=="ZDBHOME_BACKGROUND".equals(zfinPropertiesEnum[i].name())){
                errorString += "ZDBHOME_BACKGROUND " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ZDBHOME_BACKGROUND@".equals(zfinPropertiesEnum[i].value()) || false=="url(/images/zdbhome-background.jpg)".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ZDBHOME_BACKGROUND")){
                    errorString += "ZDBHOME_BACKGROUND " ;
                    %>
        <font color="red">Deployed value: '@ZDBHOME_BACKGROUND@' or 'url(/images/zdbhome-background.jpg)' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ZFIN_ADMIN

        <%--// (do check on key)!--%>
        <%
            if(false=="ZFIN_ADMIN".equals(zfinPropertiesEnum[i].name())){
                errorString += "ZFIN_ADMIN " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ZFIN_ADMIN@".equals(zfinPropertiesEnum[i].value()) || false=="kschaper@cs.uoregon.edu".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ZFIN_ADMIN")){
                    errorString += "ZFIN_ADMIN " ;
                    %>
        <font color="red">Deployed value: '@ZFIN_ADMIN@' or 'kschaper@cs.uoregon.edu' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>


<tr id="row<%=i%>">
    <td>
        ZFIN_COOKIE

        <%--// (do check on key)!--%>
        <%
            if(false=="ZFIN_COOKIE".equals(zfinPropertiesEnum[i].name())){
                errorString += "ZFIN_COOKIE " ;
            }
        %>

    </td>
    <td>
        <input name="<%=zfinPropertiesEnum[i].name()%>" value="<%=zfinPropertiesEnum[i].value()%>" size="60"/>
        <%
            if(false=="@ZFIN_COOKIE@".equals(zfinPropertiesEnum[i].value()) || false=="st4mwtR".equals(zfinPropertiesEnum[i].value())){
                if(!errorString.contains("ZFIN_COOKIE")){
                    errorString += "ZFIN_COOKIE " ;
                    %>
        <font color="red">Deployed value: '@ZFIN_COOKIE@' or 'st4mwtR' does not match VM value: '<%=zfinPropertiesEnum[i].value()%>'.</fon
        <%
                }
            }
        %>
        <% ++i ; %>
    </td>
</tr>



</table>

<input type="submit" value="Update"/>
</form>

<%
    if(errorString!=null && errorString.length()>0){
        errorString = "Mismatches: " + errorString.replaceAll("  ",",") ;
        %>
     <script type="text/javascript">
         document.getElementById("errorMessageDiv").innerHTML ="<%=errorString%>" ;
     </script>
<%
    }
%>


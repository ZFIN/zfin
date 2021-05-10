<html>
<head>
    <title>${reportTitle}</title>

    <style>
        body {
            font-family: Helvetica, Arial, sans-serif;
            font-size: 100%;
        }

        table {
            border-collapse: collapse;
        }

        td {
            padding: 0.35em;
        }

        th {
            text-align: left;
            padding: 0.5em 0.35em;
        }

        .result th {
            background-color: rgb(63, 127, 35);
            color: rgb(255, 255, 255);
        }

        .summary {
            min-width: 300px;
        }

        .summary th {
            color: rgb(83, 83, 83);
        }

        .summary tr:first-child {
            border-top: 1px solid rgb(83, 83, 83);
        }

        .summary tr:last-child {
            border-bottom: 1px solid rgb(83, 83, 83);
        }

        .rowstripes tr:nth-child(odd) {
            background-color: #EEEEEE;
        }

        pre.error {
            background-color: #E9CFCF;
            color: rgb(145, 26, 26);
            padding: 1.5em;
        }

        pre.code {
            background-color: rgb(218, 218, 218);
            color: rgb(48, 48, 48);
            padding: 1.5em;
        }
    </style>
</head>

<body>
<#if reportTitle?has_content>
<h1>${reportTitle}</h1>
</#if>
<#if timeStamp??>
<p>Report generated ${timeStamp?string("yyyy-MM-dd HH:mm:ss")}</p>
</#if>
<#list introParagraphs as paragraph>
<p class="intro">${paragraph}</p>
</#list>
<#list summaryTables as table>
    <#if table.caption?has_content>
    <h2>${table.caption}</h2>
    </#if>
    <#if table.data?has_content>
    <table class="summary rowstripes">
        <#list table.data as row>
            <tr>
                <th>${row[0]}</th>
                <td>${row[1]}</td>
            </tr>
        </#list>
    </table>
    </#if>
</#list>
<#list dataTables as table>
    <#if table.caption?has_content>
    <h2>${table.caption}</h2>
    </#if>
    <#if table.data?has_content>
    <table class="result rowstripes">
        <#if table.head?has_content>
            <tr>
                <#list table.head as col>
                    <th>${col}</th>
                </#list>
            </tr>
        </#if>
        <#list table.data as row>
            <tr>
                <#list row as col>
                    <td>
                        <#if col?? && col?starts_with("ZDB")>
                            <a href="http://${domainName}/${col}">${col}</a>
                        <#else>
                        ${col!""}
                        </#if>
                    </td>
                </#list>
            </tr>
        </#list>
    </table>
    </#if>
</#list>
<#list errorMessages as error>
<pre class="error">${error}</pre>
</#list>
<#list codeSnippets as code>
<pre class="code">${code}</pre>
</#list>

</body>
</html>
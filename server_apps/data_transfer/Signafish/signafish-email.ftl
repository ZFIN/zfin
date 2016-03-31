<html>
<head>
    <title>Report for ${jobName}</title>

    <style>
        body {
            font-family: Arial,Helvetica,sans-serif;
            font-size: 100%;
        }

        .result {
            border-collapse: collapse;
        }

        .result td {
            padding: 0.35em;
        }

        .result th {
            text-align: left;
            padding: 0.5em 0.35em;
            background-color: rgb(63, 127, 35);
            color: rgb(255, 255, 255);
        }

        .result tr.alt td {
            color:#000;
            background-color:#EAF2D3;
        }

        .rowstripes tr:nth-child(odd) {
            background-color: #EEEEEE;
        }
    </style>
</head>

<body>
    <h1>Report for ${jobName}</h1>
    <#if dateRun??>
    <p><b>Report generated</b> ${dateRun?string("yyyy-MM-dd HH:mm:ss")}</p>
    </#if>
    <p>With this load there are now <b>${totalLinks}</b> Signafish links in total.</p>
    <h3>${deletedLinks?size} Links Removed</h3>
    <#if deletedLinks?has_content>
    <table class="result rowstripes">
        <thead>
        <tr>
            <th>Gene</th>
            <th>Accession Number</th>
        </tr>
        </thead>
        <tbody>
        <#list deletedLinks as link>
            <tr>
                <td><a href="http://zfin.org/${link.getMarker().getZdbID()}">${link.getMarker().getZdbID()}</a></td>
                <td>${link.getAccessionNumber()}</td>
            </tr>
        </#list>
        </tbody>
    </table>
    </#if>

    <h3>${addedLinks?size} Links Added</h3>
    <#if addedLinks?has_content>
    <table class="result rowstripes">
        <thead>
        <tr>
            <th>Gene</th>
            <th>Accession Number</th>
        </tr>
        </thead>
        <tbody>
        <#list addedLinks as link>
            <tr>
                <td><a href="http://zfin.org/${link.getMarker().getZdbID()}">${link.getMarker().getZdbID()}</a></td>
                <td>${link.getAccessionNumber()}</td>
            </tr>
        </#list>
        </tbody>
    </table>
    </#if>
</body>
</html>

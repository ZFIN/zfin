<#macro reportLayout>
<html>
<head>
    <title>Report for ${jobName}</title>

    <style>
        body {
            font-family: "Trebuchet MS",Arial,Helvetica,sans-serif;
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
<#nested/>
</body>
</html>
</#macro>

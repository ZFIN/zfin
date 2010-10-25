<#include "header-template.ftl"></body>
<table>
    <tr>
        <th colspan="2">${root.dataSectionTitle}: ${root.rows?size}</th>
    </tr>
    <tr>
        <th>Seq</th>
        <th width="300">Annotation ID</th>
        <th width="300">Term ID</th>
        <th width="150">Term Name</th>
        <th width="300">Replacement Term ID</th>
        <th width="150">Replacement Term Name</th>
    </tr>
    <#list root.rows as row>
        <tr>
            <td>${row_index+1}</td>
            <td>${row[0]}</td>
            <td>${row[1]}</td>
            <td>${row[2]}</td>
            <td>${row[3]}</td>
            <td>${row[4]}</td>
        </tr>
    </#list>
</table>
<#include "footer-template.ftl">
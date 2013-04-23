<#include "header-template.ftl">
<span class="section-title">${root.headerInfo[0]}</span>

<table class="rowstripes">
    <tr>
        <th>#</th>
        <th width="150">Parent Term</th>
        <th width="250">Child Term</th>
        <th>${root.headerInfo[1]}: Parent Term</th>
        <th>${root.headerInfo[2]}: Child Term</th>
    </tr>
<#list root.rows as row>
    <#if row_index%2==0>
    <tr class="odd">
        <#else>
        <tr class="even">
    </#if>
    <td>${row_index+1}</td>
    <td>${row[0]}</td>
    <td>${row[1]}</td>
    <td>${row[2]}</td>
    <td>${row[3]}</td>
</tr>
</#list>
</table>
<#include "footer-template.ftl">
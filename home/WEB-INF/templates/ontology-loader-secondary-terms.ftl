<#include "header-template.ftl">
<span class="section-title">Annotations with merged Terms</span>
<table class="rowstripes">
    <tr">
        <th>#</th>
        <th width="150">Now Secondary Term ID</th>
        <th >Primary Term ID</th>
        <th width="250">Term Name</th>
        <th >Ontology</th>
        <th >Genotype Handle</th>
        <th >Experiment Name</th>
        <th >Publication ID</th>
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
    <td>${row[4]}</td>
    <td>${row[5]}</td>
    <td>${row[6]}</td>
</tr>
</#list>
</table>
<#include "footer-template.ftl">
<#include "header-template.ftl">
<span class="section-title">Annotations that use obsoleted Terms</span>

<span>(See also the <a href="http://${domain}/action/ontology/obsolete-term-report">online report</a>)
</span>

<table class="rowstripes">
    <tr>
        <th>#</th>
        <th width="150">Publication ID</th>
        <th>Title</th>
        <th width="250">Phenotype</th>
        <th width="250">Obsoleted Term</th>
        <th>Replaced By</th>
        <th>Consider</th>
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
</tr>
</#list>
</table>
<#include "footer-template.ftl">
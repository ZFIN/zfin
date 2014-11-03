<#if reportTitle?has_content>
${reportTitle}
<#list 1..reportTitle?length as i>=</#list>

</#if>
<#if timeStamp??>
Report generated ${timeStamp?string("yyyy-MM-dd HH:mm:ss")}

</#if>
<#list introParagraphs as paragraph>
${paragraph}

</#list>
<#list summaryTables as table>
<#if table.caption?has_content>
=== ${table.caption} ===

</#if>
<#if table.data?has_content>
+<#list table.colWidths as w><#list 1..w+2 as i>-</#list>+</#list>
<#list table.data as row>
|<#list table.colWidths as w> ${(row[w_index]!'')?right_pad(w)} |</#list>
</#list>
+<#list table.colWidths as w><#list 1..w+2 as i>-</#list>+</#list>

</#if>
</#list>
<#list dataTables as table>
<#if table.caption?has_content>
=== ${table.caption} ===

</#if>
<#if table.data?has_content>
+<#list table.colWidths as w><#list 1..w+2 as i>-</#list>+</#list>
<#if table.head?has_content>
|<#list table.colWidths as w> ${(table.head[w_index]!'')?right_pad(w)} |</#list>
+<#list table.colWidths as w><#list 1..w+2 as i>-</#list>+</#list>
</#if>
<#list table.data as row>
|<#list table.colWidths as w> ${(row[w_index]!'')?right_pad(w)} |</#list>
</#list>
+<#list table.colWidths as w><#list 1..w+2 as i>-</#list>+</#list>

</#if>
</#list>
<#if errorMessages?has_content>
=== Errors ===

<#list errorMessages as error>
${error}

</#list>

</#if>
<#if codeSnippets?has_content>
=== Code being executed ===

<#list codeSnippets as code>
${code}

</#list>
</#if>

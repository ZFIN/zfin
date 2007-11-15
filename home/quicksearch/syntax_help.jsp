<html>
<head>
<title>ZFIN Site Search Tips</title>
<style type="text/css">
.help_body   { margin-left: 40px; margin-right: 40px; }
</style>
</head>

<div class="help_body">
    <h2 align="center">ZFIN Site Search Tips</h2>
    <p><b>Site Search</b> provides a quick and easy glance at data available on the ZFIN web site.  Simply enter your topic of interest and press the enter key.  
     <p>Results from <b>Site Search</b> and advanced ZFIN search forms may vary.  Advanced ZFIN search forms query a particular class of data while <b>Site Search</b> searches snapshots of all ZFIN pages for an occurrence of your query string.  
     <p><b>Site Search</b> does not take advantage of the data relationships and ontologies that are defined in the ZFIN database.  For a comprehensive search of expression or anatomical structures we recommend you use the <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-xpatselect.apg">Expression</a> or  <A HREF="/action/anatomy/search">Anatomy</a> search forms.  
     <p>If you do not find what you are looking for using this search, please be sure to try one of ZFIN's advanced search forms that may be accessed from the links in the top menu bar.</p>
    <br>
    <table width=90%>
      <tr>
	<th> </th>
        <th>Site Search</th> 
        <th>Advanced search forms</th>
      </tr>
      <tr>
        <td>Highly Customizable Searches</td>
        <td align="center">No</td>
        <td align="center">Yes</td>
      </tr>
      <tr>
        <td>Uses established relationships among data in ZFIN</td>
        <td align="center">No</td>
        <td align="center">Yes</td>
      </tr>
      <tr>
        <td>Provides a quick view of all data in ZFIN related to query term</td>
        <td align="center">Yes</td>
        <td align="center">No</td>
      </tr>
    </table>
    <h3>Comparison of <b>Site Search</b> and advanced ZFIN search forms</h3>
      <ul>
      <li>Search for <b>eye</b> using <b>Site Search</b>
      <ul>
	<li> Results will include all occurrences of <b>eye</b> and words for which <b>eye</b> is a root such as:<b>eye</b>less, <b>eye</b>sight, <b>eye</b>cup.  
	<li>Results are organized by categories that reflect the various data types in ZFIN. Categories include:
	  <ul>
	    <li>Mutants/Trangenics
	    <li>Genes/Markers/Clones
	    <li>Expression
	    <li>Anatomy
	    <li>Publications
	    <li>People
	    <li>The Zebrafish Book
	  </ul>
	<li>The number of matches for each category is displayed.  
	<li>Each category links to a complete list of matches for that category.
	<li>Each item in the complete results category will link directly to data for that category type. 
      </ul>
      <li>Search for <b>eye</b> using ZFIN's <b>Gene Expression</b> search form
      <ul>
	<li>Queries may be refined by specifying other parameters such as: Gene/EST, developmental stage or stage range, author or assay name.
	<li>Results will include all expression data that have been annotated to the <b>eye</b> and to any substructures of the <b>eye</b>. (lens, optic vesicle, presumptive retina, etc.)
        <li>Results are limited to expression data.
	<li>Results list will provide hot links to expression data, genes and publications.
	<li>Results are ordered by gene symbol.
      </ul>
      </ul>
      <p>
    <h3>Wildcard Searches</h3>
      <p><b>Site Search</b> does not require use of wildcards.  Wildcard matches are performed automatically. To search for all hox genes, you may use the search: <b>hox</b><br>
      Links to all members of the hox gene family (<b>hox</b>a1a, <b>hox</b>a2b, <b>hox</b>a3a, etc.)</p>

    <h3>Multiple Word Searches</h3>
      <p>Searches may include one or more words separated by spaces.  A search for <b>inner ear</b> will return instances of <b>inner ear</b> as well as matches to root words <b>inner</b> and <b>ear</b> such as <b>inner</b>vating, <b>ear</b>ly, and <b>ear</b>ly <b>ear</b>.</p>

   <h3>Excluded Words</h3>
      <p>Commonly occurring words such as <b>from</b>, <b>any</b>, <b>but</b>, <b>some</b>, <b>were</b>, <b>what</b> and <b>when</b> that are specified in a query are not included in search results.</p>


<p><b>Site Search</b> is powered by <a HREF="http://jakarta.apache.org/lucene/docs/index.html">Lucene</a>.</p>
<p>&nbsp;</p>

<script language="JavaScript" src="/footer.js"></script>

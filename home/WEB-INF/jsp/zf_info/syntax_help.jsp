<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<zfin2:page title="ZFIN Site Search Tips">
<div><div>
<h2 align="center">ZFIN Site Search Tips</h2>
<p><b>Site Search</b> provides a quick and easy glance at data available on the ZFIN web site.  Simply enter your topic of interest and press the enter key.  
     </p><p>Results from <b>Site Search</b> and advanced ZFIN search forms may vary.  Advanced ZFIN search forms query a particular class of data while <b>Site Search</b> searches snapshots of all ZFIN pages for an occurrence of your query string.  
     </p><p>If you do not find what you are looking for using this search, please be sure to try one of ZFIN's advanced search forms that may be accessed from the links in the top menu bar.</p>
<p><b>Site Search</b> does not take advantage of the data relationships and ontologies that are defined in the ZFIN database.  For a comprehensive search of expression or anatomical structures we recommend you use the <a href="/action/expression/search">Expression</a> or  <a href="/action/ontology/search">Anatomy / GO / Human Disease</a> search forms.
    <br>
</p><table width="90%">
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
	</li><li>Results are organized by categories that reflect the various data types in ZFIN. Categories include:
	  <ul>
<li>Mutants/Trangenics
	    </li><li>Genes/Markers/Clones
	    </li><li>Expression
	    </li><li>Anatomy
	    </li><li>Publications
	    </li><li>People
	    </li><li>The Zebrafish Book
	  </li></ul>
</li><li>The number of matches for each category is displayed.  
	</li><li>Each category links to a complete list of matches for that category.
	</li><li>Each item in the complete results category will link directly to data for that category type. 
      </li></ul>
</li><li>Search for <b>eye</b> using ZFIN's <b>Gene Expression</b> search form
      <ul>
<li>Queries may be refined by specifying other parameters such as: Gene/EST, developmental stage or stage range, author or assay name.
	</li><li>Results will include all expression data that have been annotated to the <b>eye</b> and to any substructures of the <b>eye</b>. (lens, optic vesicle, presumptive retina, etc.)
        </li><li>Results are limited to expression data.
	</li><li>Results list will provide hot links to expression data, genes and publications.
	</li><li>Results are ordered by gene symbol.
      </li></ul>
</li></ul>
<p>
</p><h3>Wildcard Searches</h3>
<p><b>Site Search</b> does not require use of wildcards.  Wildcard matches are performed automatically. To search for all hox genes, you may use the search: <b>hox</b><br>
      Links to all members of the hox gene family (<b>hox</b>a1a, <b>hox</b>a2b, <b>hox</b>a3a, etc.)</p>
<h3>Multiple Word Searches</h3>
<p>Searches may include one or more words separated by spaces. Results will only include pages containing all search words.  Matches may be to the complete search string, to individual words in the search string, or to words having words from the search string as their root.  For example, a search for <b>inner ear</b> will return pages containing the string <b>inner ear</b> as well as pages containing  the words <b>inner</b> and <b>ear</b> or words with those roots such as <b>inner</b>vating and <b>ear</b>ly.</p>
<h3>Excluded Words</h3>
<p>Commonly occurring words such as <b>from</b>, <b>any</b>, <b>but</b>, <b>some</b>, <b>were</b>, <b>what</b> and <b>when</b> that are specified in a query are not included in search results.</p>
<p><b>Site Search</b> is powered by <a href="http://jakarta.apache.org/lucene/docs/index.html">Lucene</a>.</p>
<p>&#160;</p>
</div>

</div>
</zfin2:page>

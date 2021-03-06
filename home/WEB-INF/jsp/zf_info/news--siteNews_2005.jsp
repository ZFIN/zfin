<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<z:page title="ZFIN News 2005">
<div><table cellspacing="0" width="100%">
<tr>
<td align="left" width="80%">
<h1>2005 Zebrafish News</h1>
</td>
<td align="right" width="10%">
     View news for
   </td>
<td align="left" width="70%">
<form name="news">
<div>
<select name="year" onchange="document.location.href=
            document.news.year.options[document.news.year.selectedIndex].value">
<option selected="" value="">Select year
            </option><option value="siteNews.html">2007
            </option><option value="siteNews_2006.html">2006 
	    </option><option value="siteNews_2005.html">2005 
          </option></select></div>
</form>
</td>
</tr>
</table>
<p>This page contains news relevant to the zebrafish research community.

If you have any questions, comments or would like to submit an entry, please 
contact <a href="mailto:zfinadmn@zfin.org">Jonathan Knight</a>.
<br><br>
</p><h3>"ZFIN NEWS" is the ZFIN Newsletter, published bi-annually at the University of Oregon.  </h3>
<a href="Newsletter_Summer2005.pdf"> Vol. 2, No. 2 - Summer 2005</a>
<blockquote>
		 Gene Expression Surfs The Literature <br>
		 Dissecting The Zebrafish With the Anatomical Ontology <br>
		 MOrpholino Curation At ZFIN <br>
</blockquote>
<a href="Newsletter_Winter06.pdf"> Vol. 2, No. 1 - Winter 2005</a>
<blockquote>
		 Name That Gene <br>
		 Analysis Tool For Sequences in ZFIN <br>
		 Expression Data in ZFIN <br>
		 ZGC and Zebrafish Annotation Update <br>
		 Get Connected! Subscribe To The Zebrafish Newsgroup <br>
</blockquote>
<p>Click here for our <a href="/zf_info/news/Newsletters.html">ZFIN Newsletter Archive.</a></p>
<br>
<h2>Online Zebrafish Genome Resources Tutorial from Dresden workshop available August 1, 2005</h2>
<p>NCBI, ZFIN and the Sanger Institute presented a joint workshop on genome resources at the 4th European Zebrafish Meeting, held in Dresden, Germany July 14 and 15, 2005. A tutorial is available at:  <a href="http://www.sanger.ac.uk/Projects/D_rerio/workshop_Dresden/">http://www.sanger.ac.uk/Projects/D_rerio/workshop_Dresden/</a>.</p><br>
<h2>Figures and Morpholinos, June 30, 2005</h2>
<p>We've redesigned ZFIN's gene expression pages to display figures from  journal articles.  We've added support for morpholinos, too, so you  can find expression of your favorite genes in morphant and mutant  backgrounds.
<br>
It's still 'early days' - it takes time and effort to annotate  figures and load them into the database, and some journals still  impose copyright restrictions, but we're keeping up with new  publications and plan to gradually add figures from older papers.  <a href="/action/expression/search">Try out the new gene expression search page.</a>
</p></div>
</z:page>

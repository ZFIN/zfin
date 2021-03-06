<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<z:page title="ZFIN Task-centered navigation">
<div><h3></h3>
<h3><center>Position Paper for the CHI 97<br>
Workshop on<br>
Navigation on Electronic Worlds <br>
(March 23-24, 1997, Atlanta, GA) </center>
</h3>
<h2><center>

    Task-centered Navigation in Web-accessible Dataspaces

</center>
</h2>
<p></p><center><i>

    Eckehard Doerry, 
    Sarah A. Douglas, 
    Ted Kirkpatrick, 
    Monte Westerfield<sup>*</sup>
</i><br>

    University of Oregon


<br>

    Dept. of Computer Science, <sup>*</sup>Institute of Neuroscience
<br>
    Eugene, OR 97403
<br>


URL: 

    http://zfish.uoregon.edu

</center>
<h3><br><a name="index"></a>
<hr>Index</h3>
<a href="#abstract">Abstract</a> || <a href="#keywords">Keywords</a> ||
<a href="#content">Content</a> || <a href="#references">References</a>
<h3><hr><a name="abstract"></a>Abstract</h3>

    The increasing popularity of complex, web-accessible dataspaces demands intra-dataspace navigation mechanisms more powerful than the commonly provided history of traversed pages and ubiquitous "return to home page" buttons. Our experience in the design of a database for research geneticists has shown that, while users have little trouble finding specific data, they frequently become disoriented during multi-step data manipulations. For domains with a well-defined task space, we believe that a task-centered model of navigation can provide an effective solution to this problem.

<h3><hr><a name="keywords"></a>Keywords</h3>
Navigation, World Wide Web, Bio-informatics, Data Submission, Genetics
<h3><hr><a name="content"></a>

    Task-centered Navigation in Web-accessible Dataspaces

</h3>

    Navigation in electronic worlds encompasses a variety of wayfinding activities including surveying the data space contents, locating specific information, and maintaining orientation while moving about within the dataspace.  Within the WWW community, attention has been devoted mainly to information finding activities; powerful WWW search engines (e.g. Yahoo!) have been developed to winnow through millions pages in search of potentially relevant information.  
<p>
We are concerned with "intra-site" navigation within a WWW-accessible database for zebrafish geneticists that we are developing.  In this context, we have found that the most severe navigational difficulties experienced by users have to do with maintaining orientation within the data space.  These difficulties arise from several domain characteristics:
</p><ol>
<b><li> Complex, multi-step activities.</li></b>  A central feature of our database is support for submission and updating of experimental data by users; this entails an open-ended, hierarchically-nested sequence of form-filling, browsing, and selection activities.  For example, to submit a new mutation to the database, a user must specify the lineage of the mutation, the lab at which it was discovered, the mutant's phenotypic (observable) characteristics and chromosomal abnormalities, and the publications in which it has been described. Each of these steps may involve one or more subsequences as the user searches the database to locate and select relevant information. Thus, movement through the dataspace involves intertwined sequences of searching and browsing, rather than a simple unidirectional progression from entry to target data.
<b><li> Dynamic, unbounded data space.</li></b>  In a database system, screens are dynamically generated based on user queries; the number of unique "places" within the data space is essentially infinite. Moreover, the information space is continually changing and growing as new data is added; the same sequence of navigational "moves" performed at different times may result in unique displays, depending on what has changed in the interim. This makes it difficult to clearly define notions of "location" and "path" within the data space. 
<b><li> Prevalence of similar displays.</li></b>  While screens vary in (potentially important) details, they are often similar in overall appearance.  For example, every screen that presents a search interface has similar provisions for specifying search criteria and displaying search results. While such interface consistency is highly desirable from the perspective of learnability, we have found it to be a confounding factor for navigation.
</ol>

    In this complex information space, we have found that users have little trouble determining what data and activities are available, but easily become disoriented once engaged in an activity. Specifically, they often become confused about where they are within a multi-step process, how their current activity relates to an overall goal, and how to return to previous steps in the process. 
<p>
Our initial efforts to provide navigational aids focused on simply making a record of previously traversed screens (pages) continually visible to the user; we found this approach completely inadequate because it does not make explicit the relationship of traversed pages to the user's domain level tasks. For example, the domain task of finding a certain set of mutations may involve iterative refinement of the search criteria; this leads to a lengthy sequence of pages (one for each refinement), all related to the same overall domain task. Conversely, similar searches (e.g. searches for a publication) may occur at different times within an interaction, each associated with a different domain task. In both cases, a historical listing of pages fails to emphasize the conceptual relationships that group or differentiate pages.
</p><p>
Motivated by these observations, we have begun exploring a task-centered  model of navigation, which works to characterize the user's movement through the data space in terms of the conceptual structure of the domain level tasks the user is engaged in.  For example, a task-centered navigational aid would draw on knowledge about the subtasks associated with the task of submitting a new mutation to generate a dynamic representation of the user's current position within the conceptual task/subtask hierarchy. 

</p><p>

    While the task-centered approach appears promising, many difficulties remain. A particularly challenging problem is how to gracefully accommodate arbitrary digressions. In the course of entering new data, for example, a user may notice that an existing record is incomplete and digress from the current task to update that record; the navigational aid must somehow recognize and integrate such digressions in its representation of the user's position within the task space. On a more practical level, design of navigational aids is severely constrained by the WWW (HTML/HTTP) environment. Can we design effective navigational aids that do not require sophisticated interface capabilities?  
</p><p>
The increasing popularity of complex, web-accessible dataspaces demands intra-dataspace navigation mechanisms more powerful than histories of traversed pages and ubiquitous "return to home page" buttons; for domains with a well-defined task space, we believe that a task-centered model of navigation can provide an effective framework for maintaining user's orientation within the dataspace. 



</p><h3><hr><a name="references"></a>References</h3>
<ol> Doerry, E.,Douglas S.,Kirkpatrick T.,& Westerfield, M. (1997).
Moving beyond HTML to Create a Multimedia Database with User-Centered Design: A Case Study of a Biological Database. Technical Report CIS-TR-97-02, Department of Computer Science, University of Oregon.
</ol>
<br>
<hr>Return to <a href="#top">Top of Page</a> || <a href="#index">Index</a>.
</div>
</z:page>

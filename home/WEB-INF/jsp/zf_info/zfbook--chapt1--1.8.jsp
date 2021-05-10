<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<z:page title="ZFIN: Zebrafish Book: General Methods">
<div><table align="center" bgcolor="#ffcccc" width="50%"> <tr><td class="small"> This material is from the 4th edition of The Zebrafish Book.  The 5th edition is available <a href="http://zebrafish.org/zirc/orders/buyBookQ.php?item=Book&id=book&detail=The%20Zebrafish%20Book">in print</a> and within the <a href="https://wiki.zfin.org/display/prot/ZFIN+Protocol+Wiki">ZFIN Protocol Wiki</a>.  </td></tr> <table>
<h1>CHAPTER 1<br>
GENERAL METHODS FOR ZEBRAFISH CARE</h1>
<h2>Keeping Track of Stocks</h2>
<i>(Source: S. Russell)</i>
<p>
An easy and efficient way to keep track of zebrafish stocks is to use a computer database.  The following is a description of a database which runs on a Macintosh computer and that is presently being used at the University of Oregon.  It is available on computer disk or by download: 
<a href="http://fish.uoregon.edu/zf/files/zfdB.zip"> http://fish.uoregon.edu/zf/files/zfdB.zip </a>
</p><p>The database was written using FileMaker Pro, an easy to use flat-file database program (Claris Corporation, Customer Relations, 5201 Patrick Henry Drive, Box 58168, Santa Clara CA 95052-8168, Phone (408) 727-8227, FAX (408) 987-3932).
</p><p>
</p><h3><b>The zebrafish database system</b></h3>
The database is composed of three separate databases (files) that interact with each other, the Zebrafish Database, the Tank Status Database, and the Users Database.  The Zebrafish Database contains most of the information about the individual fish stocks.  The Tank Status Database allows users to check easily the status, size, and location of any tank in the facility.  The Users database explains the various user codes used in the main database.
<p>
The Zebrafish Database contains the following fields:
</p><ul>
<li>ID - A few words identifying the fish. A descriptive name
</li><li>Tank # - Tank identification number 
</li><li>Stock # - Stock # of the fish 
</li><li>Status - Status of the fish and tank. Either "Alive", "Dead", or "Empty" 
</li><li>User code - Code identifying the fish user
</li><li>Unsexed fish - Number of unsexed fish in the tank
</li><li># females - Number of female fish in tank 
</li><li># males - Number of male fish in tank 
</li><li>Total # fish - Total of all fish in the tank 
</li><li>Birthday - Date of birth for the fish 
</li><li>Age - Age of the fish in days 
</li><li>Mutant allele - Identified mutant allele carried by the fish 
</li><li>Transgenes - Any genetic material added in transgenic experiments 
</li><li>Update - Date of last change to the record 
</li><li>Priority - Days since last change in the record number (now largely obsolete)
</li><li>Mom's s# - Stock number of mother fish 
</li><li>Mother's id - Mother's descriptive name 
</li><li>Dad's s# - Stock number of father fish 
</li><li>Father's id - Father's descriptive name 
</li><li>Comments - Any additional information goes here 
</li><li>Tank Size - Size of the fish tank, e.g. 5 gallon, 10 gallon, etc.
</li><li>Reason for death - Explanation for death of fish 
</li><li>Original # males - Number of male fish at ~6-8 weeks of age
</li><li>Original # females - Number of female fish at ~6-8 weeks of age 
</li><li>Original # fish - Number of fish at ~6-8 weeks of age
</li><li>Initial sex ratio - Original # females to original # males 
</li><li>Sex ratio comments - Comments on fish at this age
</li></ul><p>
The Users Database contains the following fields:
</p><ul>
<li>User code - Code used to identify user 
</li><li>Name - Full name of user 
</li><li>User status - Status of user, e.g. currently with fish group, or no longer with fish group<p>
</p></li></ul>
The Tank Status Database contains the following fields:
<ul><li>Tank # - Tank identification number 
</li><li>Tank size - Size of the tank 
</li><li>Tank location - Location of tank, e.g. which room
</li><li>User - Current user of the tank 
</li><li>Status - Status of tank
<p>
</p></li></ul><h3><b>Example of Zebrafish Database layout</b></h3>
<img src="1-1.gif">
<h4><b>Startup and Shutdown</b></h4>
Under Macintosh System 7, aliases of the database files can be created. An alias is a small file that points to the location of the original file on the disk. Open the alias, and the original file is opened.  Aliases of the database files are placed in the "Startup Items" folder within the System Folder.  The database is then automatically loaded upon startup of the Macintosh.  The database is kept running continually during normal business hours.  Users can browse and enter data into the database from the main machine, or can use other FileMaker Pro-equipped Macintoshes on a local network.  Upon system shutdown, the database files are automatically backed up to a floppy disk using the CP Backup program in the MacTools package (Central Point Software, 3203 S.W. 154th Terrace, Beaverton OR 97006-9937, Phone (503) 690-8088).
<p>
</p><h4><b>Using the database</b></h4>
<h5>Layouts</h5>
Information about stocks is entered and viewed using different layouts.  To switch between layouts, click on the button in the upper left corner of the screen and select a layout from the pop-up menu.  The Data Entry layout is best for entering data and viewing single records.  The List View layout is good for viewing many records at once.  The Extended List displays a slightly different set of fields.  The Full List is good for viewing complete records as a list.  The Historical Data layout contains fields for recording the original ratio of males and females in a tank, and a field for recording the reason for death when a tank entry is deleted.
<p></p><h5>New Records</h5>
To create a new record for entering data, you will most likely want to use the Data Entry layout.  Use the Layout button (described above) or select the button labeled "Data Entry" on the Welcome screen.  Select "New Record" from the Edit menu.  To duplicate the contents of the current record into a new one, select "Duplicate Record" from the Edit menu.  When using the Data Entry layout, these commands are available as buttons located to the right.
<p>
</p><h5>The Fields</h5>
In most of the fields, data is entered in the usual manner. However, a few of the fields operate a little differently.
<br>The Update field is entered automatically whenever a record is changed.
<br>The Tank Size field is calculated for each tank number in the facility.  This field cannot be selected for data entry.
<br>The &#931;Fish field becomes whatever is entered into it, unless either the female or male fields have values.  If either of these fields are defined, &#931;Fish equals females plus males (see "special note" below).
<br>The Status field is calculated automatically, based on the values in the Tank# and &#931;Fish fields.  The Status field works like this:
<ul> <li>If Tank# is defined and &#931;Fish is undefined, then Status equals "Empty". 
	</li><li>If Tank# and &#931;Fish are both defined, then Status equals "Alive".
	</li><li>If Tank# and &#931;Fish are both undefined, then Status equals "Dead".
</li></ul><br>To change the Status of a record from "Alive" to "Dead", select "Dead this tank" from the Scripts menu.  You will be asked to make an entry in the "Reason for death" field.  When you have done so, press the "Continue" button.  All the numbers in the females, males, and &#931;Fish fields will be deleted.  The tank number will also be deleted.  A new record with the old tank number and user code will be created for you.
<br>The mutant allele field should not contain punctuation of any kind.  Do not use commas, dashes, or any special symbols.  An example follows:
<p>
Correct = b1 b4 b5<br>
Incorrect  = b-1, b-4, b-5<br>
</p><p>A search on the mutant allele field must be carried out in a special way.  If you wish to search on b1, you will get b1, but you will also get any other mutant alleles with the characters "b1", such as b16, b104, etc.  To avoid this, use the "exact" button when you make your Find request.
</p><p>
</p><h5>Find and Sort</h5>
When browsing records, remember first to filter the database for the records you want by using the "Find" command.  The Data Entry layout is best for this.  The Find command is in the Select menu, and is available as a button on the Data Entry layout. When the Find window is open, enter your search criteria in the boxes, and push the Find button located on the left side of the screen.
<br>Next, order the records you have found using the "Sort" command.  The Sort command is also in the select menu, and is available as a button on the Data Entry layout.  The Sort window will appear.  Select the sorting order desired by moving the field you wish to sort into the box on the right.  The data must be resorted each time you filter the records.  The panel on the left has information regarding the current sort status.
<p>
</p><h5>Network Etiquette</h5>
When opening a file over the network, please remember that the performance of the database will diminish.  As a courtesy to other users, please finish your work quickly.  If you receive a message to close the file, comply with the request.  Always close any files you are no longer actively using.
<p>
</p><h5>Special note</h5>
If the "&#931;Fish" field looks funny, it may be because you entered numbers into the "males" and/or "females" fields and then forgot to delete the original number in the &#931;Fish field.  To update the &#931;Fish number, delete the old number.
<hr>
<a href="http://zebrafish.org/zirc/orders/buyBookQ.php?item=Book&id=book&detail=The%2520Zebrafish%2520Book"><b>The Zebrafish Book</b></a>
</table></table></div>
</z:page>

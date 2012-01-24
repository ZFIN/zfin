<TITLE>ZFIN Edit Person/Address</TITLE>

<table width=100% cellspacing=0 border=0 cellpadding=0 class="header">
  <tr>
     <td rowspan=2  bgcolor="#006666" width=80 align=left valign=bottom>

	    <a href="/@cgi-bin_frost@/webdriver?MIval=aa-ZDB_home.apg">
	     <IMG src="/images/zfinlogo.gif" border=0>
	    </a>
     </td>
     <td>
	    <a href="/@cgi-bin_frost@/webdriver?MIval=aa-ZDB_home.apg"> <IMG src="/images/zfintxt.gif" border=0></a>
     </td>
     <td align="center" valign="bottom" bgcolor="#FFFFFF">

           <div class="content" align="right" style="padding-right:10px;">

            <form method="GET" action="/SearchApp/category_search.jsp" name="quicksearch">
                 <nobr><a href="/SearchApp/syntax_help.jsp"><b>Site Search: </b></a><input type="text" name="query" size="25"></nobr>
            </form>
           </div>

	   <DIV class="header">
	    <A HREF="/action/blast/blast"><font color="#006666">BLAST</font></a>&nbsp;&nbsp;
            <A HREF="/action/anatomy/anatomy-search"><font color="#006666">Anatomy</font></a>&nbsp;&nbsp;

            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-pubselect2.apg"><font color="#006666">Publications</font></a>&nbsp;&nbsp;
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-quickfindpers.apg"><font color="#006666">People</font></a> &nbsp;&nbsp;
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-labselect.apg"><font color="#006666">Labs</font></a> &nbsp;&nbsp;
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-companyselect.apg"><font color="#006666">Companies </font></a> &nbsp;&nbsp;
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-accessionselect.apg"><font color="#006666">Acc # </font></a>
            </DIV>

          </td>
    </tr>

  <tr bgcolor=#006666>
    <td colspan=2 width=100%>
      <table width=100% border=0 height=20 cellspacing=0 cellpadding=0>
        <tr>
	  <td align=center>
	   <DIV class="header">
            <A HREF="/index.html">Home</a>

	   </DIV>
          </td>
	  <td align=center>
	   <DIV class="header">
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-fishselect.apg&line_type=mutant">Mutants / Transgenics</a>
	   </DIV>
          </td>
          <td align=center>

	   <DIV class="header">
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-wtlist.apg">Wild-Types</a>
	   </DIV>
          </td>
          <td align=center>
	   <DIV class="header">
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-newmrkrselect.apg">Genes / Markers / Clones</a>
	   </DIV>

          </td>
	  <td align=center>
	   <DIV class="header">
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-xpatselect.apg">Expression</a>
	   </DIV>
          </td>
          <td align=center>
	   <DIV class="header">

            <A HREF="mapper_select.cgi">Maps</a>
	   </DIV>
          </td>

<!--          <td  align=center>
	   <DIV class="header">
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-pubselect2.apg">Publications</a>
	   </DIV>
          </td>
          <td  align=center>
	   <DIV class="header">
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-quickfindpers.apg">People</a>
	   </DIV>
          </td>
          <td  align=center>
	   <DIV class="header">
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-labselect.apg">Labs</a>
	   </DIV>
          </td>
          <td  align=center>
	   <DIV class="header">
            <A HREF="/@cgi-bin_frost@/webdriver?MIval=aa-companyselect.apg">Companies</a>
	   </DIV>
          </td> -->
        </tr>
      </table>
    </td>
  </tr>

</table>
<table cellpadding=5 width=100%><tr><td>







         <FORM NAME="process_address"
                     method=post
     		     encrypt="multipart/form-data
     		     action="/@cgi-bin_frost@/webdriver">

  	<p>

    	 <table border=1  width=100%>
    	 <tr align=center>
    	 <td><font size=-1><b>Person ZDB_ID:</b> ZDB-PERS-000222-51</font></td>

    	 </tr>


  	<td colspan=2 align=center>






































     	  <p><br>
     	  <b>Current Name (old table): </b>Conlin, Tom<br>

	  <b>Current Lab: </b><a href='/@cgi-bin_frost@/webdriver/?MIval=aa-labview.apg&OID=ZDB-LAB-000914-1'>ZFIN Database Team</a>

 	  <p><align=center><b>First Name:</b>
     	      <INPUT TYPE=text name=ua_pers_first_name
			size=40
			value="">
          <p>
     	  <b>Middle Initial:</b>
     	  <INPUT TYPE=text name=ua_pers_middle_name_or_initial
			size=30
			value="">
		 	<b> (Add a '.' please)</b>
     	  <p>

     	  <b>Last Name:</b>
     	  <INPUT TYPE=text name=ua_pers_last_name
			size=40
			value="">
     	  <p>
	  <p><b>Current Address (old table):</b> <br>Zebrafish Information Network<br>5291 University of Oregon<br>Eugene, OR<br>97405-5291<br>USA
     	  <p>
     	  <b>Street:</b>

     	  <INPUT TYPE=text name=ua_pers_street1
			size=85
			value="Zebrafish Information Network">
     	  <p>
	  <b>Street:</b>
     	  <INPUT TYPE=text name=ua_pers_street2
			size=85
			value="5291 University of Oregon">
     	  <p>
	  <b>Street:</b>
     	  <INPUT TYPE=text name=ua_pers_street3
			size=85
			value="">
     	  <p>

	  <b>Street:</b>
     	  <INPUT TYPE=text name=ua_pers_street4
			size=85
			value="">
     	  <p>
	  <b>Street:</b>
     	  <INPUT TYPE=text name=ua_pers_street5
			size=85
			value="">
     	  <p>
	  <b>Street:</b>

     	  <INPUT TYPE=text name=ua_pers_street6
			size=85
			value="">
     	  <p>
     	  <b>City:</b>
     	  <INPUT TYPE=text name=ua_pers_city
			size=40
			value="Eugene">
     	  <p>
     	  <b>State:</b>
          <select name="ua_pers_state_code">
            <option
		value=>unspecified






 	    <option
		value=AL>ALABAMA






 	    <option
		value=AK>ALASKA






 	    <option
		value=AS>AMERICAN SAMOA






 	    <option
		value=AZ>ARIZONA






 	    <option
		value=AR>ARKANSAS






 	    <option
		value=CA>CALIFORNIA






 	    <option
		value=CO>COLORADO






 	    <option
		value=CT>CONNECTICUT






 	    <option
		value=DE>DELAWARE






 	    <option
		value=DC>DISTRICT OF COLUMBIA






 	    <option
		value=FM>FEDERATED STATES OF MICRONESIA






 	    <option
		value=FL>FLORIDA






 	    <option
		value=GA>GEORGIA






 	    <option
		value=GU>GUAM






 	    <option
		value=HI>HAWAII






 	    <option
		value=ID>IDAHO






 	    <option
		value=IL>ILLINOIS






 	    <option
		value=IN>INDIANA






 	    <option
		value=IA>IOWA






 	    <option
		value=KS>KANSAS






 	    <option
		value=KY>KENTUCKY






 	    <option
		value=LA>LOUISIANA






 	    <option
		value=ME>MAINE






 	    <option
		value=MH>MARSHALL ISLANDS






 	    <option
		value=MD>MARYLAND






 	    <option
		value=MA>MASSACHUSETTS






 	    <option
		value=MI>MICHIGAN






 	    <option
		value=MN>MINNESOTA






 	    <option
		value=MS>MISSISSIPPI






 	    <option
		value=MO>MISSOURI






 	    <option
		value=MT>MONTANA






 	    <option
		value=NE>NEBRASKA






 	    <option
		value=NV>NEVADA






 	    <option
		value=NH>NEW HAMPSHIRE






 	    <option
		value=NJ>NEW JERSEY






 	    <option
		value=NM>NEW MEXICO






 	    <option
		value=NY>NEW YORK






 	    <option
		value=NC>NORTH CAROLINA






 	    <option
		value=ND>NORTH DAKOTA






 	    <option
		value=MP>NORTHERN MARIANA ISLANDS






 	    <option
		value=OH>OHIO






 	    <option
		value=OK>OKLAHOMA






 	    <option SELECTED
		value=OR>OREGON






 	    <option
		value=PW>PALAU






 	    <option
		value=PA>PENNSYLVANIA






 	    <option
		value=PR>PUERTO RICO






 	    <option
		value=RI>RHODE ISLAND






 	    <option
		value=SC>SOUTH CAROLINA






 	    <option
		value=SD>SOUTH DAKOTA






 	    <option
		value=TN>TENNESSEE






 	    <option
		value=TX>TEXAS






 	    <option
		value=UT>UTAH






 	    <option
		value=VT>VERMONT






 	    <option
		value=VI>VIRGIN ISLANDS






 	    <option
		value=VA>VIRGINIA






 	    <option
		value=WA>WASHINGTON






 	    <option
		value=WV>WEST VIRGINIA






 	    <option
		value=WI>WISCONSIN






 	    <option
		value=WY>WYOMING


          </select>

     	  <p>
     	  <b>County:</b>
     	  <INPUT TYPE=text name=ua_pers_county
		size=40
		value="">
     	  <p>
     	  <b>Country:</b>
       	  <select name="ua_pers_country_code">
            <option
		value=>unspecified






 	    <option SELECTED
		value=USA>USA






 	    <option
		value=GB>United Kingdom






 	    <option
		value=DE>Germany






 	    <option
		value=JP>Japan






 	    <option
		value=FR>France






 	    <option
		value=CN>China






 	    <option
		value=AF>Afghanistan






 	    <option
		value=AL>Albania






 	    <option
		value=DZ>Algeria






 	    <option
		value=AS>American Samoa






 	    <option
		value=AD>Andorra






 	    <option
		value=AO>Angola






 	    <option
		value=AI>Anguilla






 	    <option
		value=AQ>Antarctica






 	    <option
		value=AG>Antigua and Barbuda






 	    <option
		value=AR>Argentina






 	    <option
		value=AM>Armenia






 	    <option
		value=AW>Aruba






 	    <option
		value=AU>Australia






 	    <option
		value=AT>Austria






 	    <option
		value=AZ>Azerbaijan






 	    <option
		value=BS>Bahamas






 	    <option
		value=BH>Bahrain






 	    <option
		value=BD>Bangladesh






 	    <option
		value=BB>Barbados






 	    <option
		value=BY>Belarus






 	    <option
		value=BE>Belgium






 	    <option
		value=BZ>Belize






 	    <option
		value=BJ>Benin






 	    <option
		value=BM>Bermuda






 	    <option
		value=BT>Bhutan






 	    <option
		value=BO>Bolivia






 	    <option
		value=BA>Bosnia and Herzegovina






 	    <option
		value=BW>Botswana






 	    <option
		value=BV>Bouvet Island






 	    <option
		value=BR>Brazil






 	    <option
		value=IO>British Indian Ocean Territory






 	    <option
		value=BN>Brunei Darussalam






 	    <option
		value=BG>Bulgaria






 	    <option
		value=BF>Burkina Faso






 	    <option
		value=BI>Burundi






 	    <option
		value=KH>Cambodia






 	    <option
		value=CM>Cameroon






 	    <option
		value=CA>Canada






 	    <option
		value=CV>Cape Verde






 	    <option
		value=KY>Cayman Islands






 	    <option
		value=CF>Central African Republic






 	    <option
		value=TD>Chad






 	    <option
		value=CL>Chile






 	    <option
		value=CX>Christmas Island






 	    <option
		value=CC>Cocos (Keeling) Islands






 	    <option
		value=CO>Colombia






 	    <option
		value=KM>Comoros






 	    <option
		value=CG>Congo






 	    <option
		value=CD>Congo, the Democratic Republic of the






 	    <option
		value=CK>Cook Islands






 	    <option
		value=CR>Costa Rica






 	    <option
		value=HR>Croatia






 	    <option
		value=CU>Cuba






 	    <option
		value=CY>Cyprus






 	    <option
		value=CZ>Czech Republic






 	    <option
		value=CI>Côte d'Ivoire






 	    <option
		value=DK>Denmark






 	    <option
		value=DJ>Djibouti






 	    <option
		value=DM>Dominica






 	    <option
		value=DO>Dominican Republic






 	    <option
		value=EC>Ecuador






 	    <option
		value=EG>Egypt






 	    <option
		value=SV>El Salvador






 	    <option
		value=GQ>Equatorial Guinea






 	    <option
		value=ER>Eritrea






 	    <option
		value=EE>Estonia






 	    <option
		value=ET>Ethiopia






 	    <option
		value=FK>Falkland Islands (Malvinas)






 	    <option
		value=FO>Faroe Islands






 	    <option
		value=FJ>Fiji






 	    <option
		value=FI>Finland






 	    <option
		value=GF>French Guiana






 	    <option
		value=PF>French Polynesia including Clipperton Island






 	    <option
		value=TF>French Southern Territories






 	    <option
		value=GA>Gabon






 	    <option
		value=GM>Gambia






 	    <option
		value=GE>Georgia






 	    <option
		value=GH>Ghana






 	    <option
		value=GI>Gibraltar






 	    <option
		value=GR>Greece






 	    <option
		value=GL>Greenland






 	    <option
		value=GD>Grenada






 	    <option
		value=GP>Guadeloupe






 	    <option
		value=GU>Guam






 	    <option
		value=GT>Guatemala






 	    <option
		value=GN>Guinea






 	    <option
		value=GW>Guinea-Bissau






 	    <option
		value=GY>Guyana






 	    <option
		value=HT>Haiti






 	    <option
		value=HM>Heard Island and McDonald Islands






 	    <option
		value=VA>Holy See (Vatican City State)






 	    <option
		value=HN>Honduras






 	    <option
		value=HK>Hong Kong






 	    <option
		value=HU>Hungary






 	    <option
		value=IS>Iceland






 	    <option
		value=IN>India






 	    <option
		value=ID>Indonesia






 	    <option
		value=IR>Iran, Islamic Republic of






 	    <option
		value=IQ>Iraq






 	    <option
		value=IE>Ireland






 	    <option
		value=IL>Israel






 	    <option
		value=IT>Italy






 	    <option
		value=JM>Jamaica






 	    <option
		value=JO>Jordan






 	    <option
		value=KZ>Kazakhstan






 	    <option
		value=KE>Kenya






 	    <option
		value=KI>Kiribati






 	    <option
		value=KP>Korea, Democratic People's Republic of






 	    <option
		value=KR>Korea, Republic of






 	    <option
		value=KW>Kuwait






 	    <option
		value=KG>Kyrgyzstan






 	    <option
		value=LA>Lao People's Democratic Republic






 	    <option
		value=LV>Latvia






 	    <option
		value=LB>Lebanon






 	    <option
		value=LS>Lesotho






 	    <option
		value=LR>Liberia






 	    <option
		value=LY>Libyan Arab Jamahiriya






 	    <option
		value=LI>Liechtenstein






 	    <option
		value=LT>Lithuania






 	    <option
		value=LU>Luxembourg






 	    <option
		value=MO>Macao






 	    <option
		value=MK>Macedonia, the Former Yugoslav Republic of






 	    <option
		value=MG>Madagascar






 	    <option
		value=MW>Malawi






 	    <option
		value=MY>Malaysia






 	    <option
		value=MV>Maldives






 	    <option
		value=ML>Mali






 	    <option
		value=MT>Malta






 	    <option
		value=MH>Marshall Islands






 	    <option
		value=MQ>Martinique






 	    <option
		value=MR>Mauritania






 	    <option
		value=MU>Mauritius






 	    <option
		value=YT>Mayotte






 	    <option
		value=MX>Mexico






 	    <option
		value=FM>Micronesia, Federated States of






 	    <option
		value=MD>Moldova, Republic of






 	    <option
		value=MC>Monaco






 	    <option
		value=MN>Mongolia






 	    <option
		value=MS>Montserrat






 	    <option
		value=MA>Morocco






 	    <option
		value=MZ>Mozambique






 	    <option
		value=MM>Myanmar






 	    <option
		value=NA>Namibia






 	    <option
		value=NR>Nauru






 	    <option
		value=NP>Nepal






 	    <option
		value=NL>Netherlands






 	    <option
		value=AN>Netherlands Antilles






 	    <option
		value=NC>New Caledonia






 	    <option
		value=NZ>New Zealand






 	    <option
		value=NI>Nicaragua






 	    <option
		value=NE>Niger






 	    <option
		value=NG>Nigeria






 	    <option
		value=NU>Niue






 	    <option
		value=NF>Norfolk Island






 	    <option
		value=MP>Northern Mariana Islands






 	    <option
		value=NO>Norway






 	    <option
		value=OM>Oman






 	    <option
		value=PK>Pakistan






 	    <option
		value=PW>Palau






 	    <option
		value=PS>Palestinian Territory, Occupied






 	    <option
		value=PA>Panama






 	    <option
		value=PG>Papua New Guinea






 	    <option
		value=PY>Paraguay






 	    <option
		value=PE>Peru






 	    <option
		value=PH>Philippines






 	    <option
		value=PN>Pitcairn






 	    <option
		value=PL>Poland






 	    <option
		value=PT>Portugal






 	    <option
		value=PR>Puerto Rico






 	    <option
		value=QA>Qatar






 	    <option
		value=RO>Romania






 	    <option
		value=RU>Russian Federation






 	    <option
		value=RW>Rwanda






 	    <option
		value=RE>Réunion






 	    <option
		value=SH>Saint Helena






 	    <option
		value=KN>Saint Kitts and Nevis






 	    <option
		value=LC>Saint Lucia






 	    <option
		value=PM>Saint Pierre and Miquelon






 	    <option
		value=VC>Saint Vincent and the Grenadines






 	    <option
		value=WS>Samoa






 	    <option
		value=SM>San Marino






 	    <option
		value=ST>Sao Tome and Principe






 	    <option
		value=SA>Saudi Arabia






 	    <option
		value=SN>Senegal






 	    <option
		value=CS>Serbia and Montenegro






 	    <option
		value=SC>Seychelles






 	    <option
		value=SL>Sierra Leone






 	    <option
		value=SG>Singapore






 	    <option
		value=SK>Slovakia






 	    <option
		value=SI>Slovenia






 	    <option
		value=SB>Solomon Islands






 	    <option
		value=SO>Somalia






 	    <option
		value=ZA>South Africa






 	    <option
		value=GS>South Georgia and the South Sandwich Islands






 	    <option
		value=ES>Spain including Canary Islands, Ceuta and Melilla






 	    <option
		value=LK>Sri Lanka






 	    <option
		value=SD>Sudan






 	    <option
		value=SR>Suriname






 	    <option
		value=SJ>Svalbard and Jan Mayen






 	    <option
		value=SZ>Swaziland






 	    <option
		value=SE>Sweden






 	    <option
		value=CH>Switzerland






 	    <option
		value=SY>Syrian Arab Republic






 	    <option
		value=TW>Taiwan






 	    <option
		value=TJ>Tajikistan






 	    <option
		value=TZ>Tanzania, United Republic of






 	    <option
		value=TH>Thailand






 	    <option
		value=TL>Timor-Leste






 	    <option
		value=TG>Togo






 	    <option
		value=TK>Tokelau






 	    <option
		value=TO>Tonga






 	    <option
		value=TT>Trinidad and Tobago






 	    <option
		value=TN>Tunisia






 	    <option
		value=TR>Turkey






 	    <option
		value=TM>Turkmenistan






 	    <option
		value=TC>Turks and Caicos Islands






 	    <option
		value=TV>Tuvalu






 	    <option
		value=UG>Uganda






 	    <option
		value=UA>Ukraine






 	    <option
		value=AE>United Arab Emirates






 	    <option
		value=UM>United States Minor Outlying Islands consisting of Baker Isl






 	    <option
		value=UY>Uruguay






 	    <option
		value=UZ>Uzbekistan






 	    <option
		value=VU>Vanuatu






 	    <option
		value=VE>Venezuela






 	    <option
		value=VN>Viet Nam






 	    <option
		value=VG>Virgin Islands, British






 	    <option
		value=VI>Virgin Islands, U.S.






 	    <option
		value=WF>Wallis and Futuna






 	    <option
		value=EH>Western Sahara






 	    <option
		value=YE>Yemen






 	    <option
		value=ZM>Zambia






 	    <option
		value=ZW>Zimbabwe






 	    <option
		value=AX>Åland Islands


      	</select>

     	<p>

     	<b>Postal/Zip Code:</b>
     	<INPUT TYPE=text name=ua_pers_postal_code
			size=20
			value="97403-5291">
	<p>



	<input type=checkbox id=uaUpdateAllAddresses
                      name=ua_updateAllAddresses value=checked
                      <?MIVAR>

		      <?/MIVAR>
               <label for=uaUpdateAllAddresses id=uaUpdateAllAddressesLabel>
			 <b>Update All</b> Addresses for people in the
			  <a href='/@cgi-bin_frost@/webdriver/?MIval=aa-labview.apg&OID=ZDB-LAB-000914-1'>

				ZFIN Database Team
			  </a>
	       </label>
	</b>
     	<p>
    	<table width=100%>
 	<tr align=center><td>

     	<input type=submit
 		name=process_address_update
		value="SUBMIT this update">
     	</td>

        <td>
        <input type=button
	  	name=cancel
	  	value="CANCEL"
 	  	onClick="window.location.replace('/@cgi-bin_frost@/webdriver?MIval=aa-persview.apg&OID=ZDB-PERS-000222-51')">
        </td>
        </tr>
      </table>

     </table>



     	<input type=hidden name=MIval Value=aa-process_address_update.apg>

     	<input type=hidden name=OID Value=ZDB-PERS-000222-51>
     	<input type=hidden name=ua_lab_zdb_id Value=ZDB-LAB-000914-1>



    </form>







</td></tr></table>

<table width=100% class="header">
  <tr>
    <td class="footer">
      <table valign=center width=100%>

        <tr>
          <td align=center class="footer">
	   <DIV class="header">
            <A HREF="/index.html">Home</a>
	   </DIV>
          </td>
          <td align=center class="footer">
	   <DIV class="header">

	     <a href="mailto:zfinadmn@zfin.org">Email ZFIN</a>
	   </DIV>
          </td>
          <td align=center class="footer">
	   <DIV class="header">
            <A HREF="/zf_info/dbase/db.html">About ZFIN</a>
	   </DIV>
          </td>

          <td align=center class="footer">
	   <DIV class="header">
            <A HREF="/ZFIN/misc_html/tips.html">Helpful Hints</a>
	   </DIV>
          </td>
          <td align=center >
	   <DIV class="header" class="footer" >
            <A HREF="/zf_info/citation.html">Citing ZFIN</a>

	   </DIV>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <DIV class="ack">
    <td align=center  class="ack">

      <A HREF="/warranty.html">Copyright</a> &copy
      <A HREF="http://www.uoregon.edu">University of Oregon</a>, 1994-2006,
      <A HREF="http://www.ci.eugene.or.us/">Eugene</a>, Oregon.
    </td>
    </DIV>
  </tr>
  <tr>
    <DIV class="ack">
    <td align=center class="ack">

      <font size="-2">ZFIN logo design by Kari Pape, </font><A HREF="http://www.uoregon.edu"><font size="-2">University of Oregon</font></a>
    </td>
    </DIV>
  </tr>
</table>
</body>
</html>


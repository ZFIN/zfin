<!-- hide from older browsers

function Set_Cookie(name,value,expires,path,domain,secure) {
      document.cookie = name + "=" +escape(value) +
          ( (expires) ? ";expires=" + expires.toGMTString() : "") +
          ( (path) ? ";path=" + path : "") + 
          ( (domain) ? ";domain=" + domain : "") +
          ( (secure) ? ";secure" : "");
  }

function deleteCookie(name, path, domain) {
  if (getCookie(name)) {
    document.cookie = name + "=" + 
    ((path) ? "; path=" + path : "") +
    ((domain) ? "; domain=" + domain : "") +
    "; expires=Thu, 01-Jan-70 00:00:01 GMT";
  }
}

function getCookie(name) {
	var allcookies = document.cookie;
	if (allcookies == "") return false;
	var start = allcookies.indexOf(name + '=');
		if (start == -1) return false;
	start += name.length + 1;
	var end = allcookies.indexOf(';', start);
		if (end == -1) end = allcookies.length;
	return allcookies.substring(start, end); 
}

function cookieExists(name, path, domain) {
  if (getCookie(name) == "") {
    return "no";
  } else {
    return "yes";
  }
}

function cookiesOK(cookieName) {
  if (getCookie(cookieName)) {
  		//var resultCookieExists = cookieExists(cookieName); 
   		//if (resultCookieExists == "yes") {
 	  return "yes";}  // cookies appear to be OK with this user
  else {
      use_cookies = (confirm("Will you allow the contact information and grant number you enter into this form "
                            +"to be stored on your computer so that those values will be pre-filled the next "
                            +"time you return to the Resource Center online forms?") ? "yes":"no");
 	   if (use_cookies == "yes") {
 	     return "yes";}  //cookies are OK for this user
 	   else {  //end of if (use_cookies == "yes")
	     deleteCookie(cookieName); //if the user clicks 'cancel' the cookie (if it can be found) is deleted
 	     return null;}
    }  // end of if/else (resultCookieExists == "yes")
} // end of function cookies OK () 

function emptyFormCheck() {
	var formValues = "";
  		for (i=0;i<document.forms.length;i++) { 
   			for (j=0;j<document.forms[i].elements.length; j++) { 
    			if ((document.forms[i].elements[j].type == "text")
				  || (document.forms[i].elements[j].type == "password")
				  || (document.forms[i].elements[j].type == "textarea")){
					var elementValue = document.forms[i].elements[j].value;
  				} else if (document.forms[i].elements[j].type.indexOf("select") != -1) {
   					var elementValue = "";
   					for(k=0;k<document.forms[i].elements[j].length;k++)
    					if (document.forms[i].elements[j].options[k].selected)
     					elementValue += k+"|";
  				} else if ((document.forms[i].elements[j].type == "checkbox")||(document.forms[i].elements[j].type == "radio")) {
					if (document.forms[i].elements[j].checked != true)
   						elementValue = "";
					else
						elementValue = document.forms[i].elements[j].checked;
  					}
				formValues += elementValue;
			} //end of for (j=0;j<document.forms[i].elements.length; j++) { 	
   		} //end of for (i=0;i<document.forms.length;i++)
//returning the value to saveValues()
  if (formValues == "") {
  		return false;}
	else {
		return true;}

}   //end of function emptyFormCheck

function saveValues(cookieName, days) {
//checking for permission to use cookies...
 	if (cookieName.indexOf("Profile") != -1) {
  		var bakeOK = cookiesOK(cookieName);
 		} 
	else {
  		var bakeOK = "yes";
		}
//checking for data...
var formCheck = emptyFormCheck();
 if (formCheck != true) {
 	alert("You didn't enter anything. Please try again.");
	window.location.href=this.location.href;
	}
 else {
//setting the expiration on the cookie   
   if (days) {
      var expires = new Date((new Date()).getTime() + days *24*60*60*1000);//days*hrs*min*sec*millisec -- this cookie expires in 10 days
   }   
// after checking for data and permission to use cookies, we continue...
 if (bakeOK == "yes") {
  var zircCookie = "";
  for (i=0;i<document.forms.length;i++) { 
   for (j=0;j<document.forms[i].elements.length; j++) { 
    var cookieElement = document.forms[i].elements[j].name; 
  if ((document.forms[i].elements[j].type == "text")
		|| (document.forms[i].elements[j].type == "password")
		|| (document.forms[i].elements[j].type == "textarea")){
	var elementValue = document.forms[i].elements[j].value;
  } else if (document.forms[i].elements[j].type.indexOf("select") != -1) {
		var elementValue = "";
		for(k=0;k<document.forms[i].elements[j].length;k++) {
			if (document.forms[i].elements[j].options[k].selected) {
     			   elementValue += k+"|";}
		}//end of for(k= 
  } else if ((document.forms[i].elements[j].type == "checkbox") || 
			   (document.forms[i].elements[j].type == "radio")) {
					var elementValue = document.forms[i].elements[j].checked;
  } 
  var elementPair = (cookieElement + "=" + elementValue);
	// These next lines loop together the long string that makes the cookie (e[0]=v[0]&e[1]=v[1]&...)
	if (zircCookie == "") {
		zircCookie = elementPair;
		} 
	else {
		zircCookie = zircCookie + "&" + elementPair;
		} //end of if (zircCookie == "")
	} //end of for (j=0;j<document.forms[i].elements.length; j++) { 	
   } //end of for (i=0;i<document.forms.length;i++)
   Set_Cookie(cookieName, zircCookie, expires); //set the cookie
 } //end of if (bakeOK == "yes")
 } // end of else emptyFormCheck
}   //end of function saveValues() 

function storedValues(cookieName) {
  var val = getCookie(cookieName);
	if (val) {
	var a = val.split(escape('&')); //parsing name/value pairs
	for (var x=0; x<a.length; x++){
		a[x] = a[x].split(escape('=')); //breaking pairs into an array
		}
  for (i=0;i<document.forms.length;i++) {
	for (j=0;j<document.forms[i].elements.length;j++) { 
			var elementName = a[j].slice(0,1); //find the name in this pair
			var elementValue = a[j].slice(1,2); //find value in this pair
			var elementValue = unescape(elementValue); //this makes it readable
			if (document.forms[i].elements[j].name == elementName) {
     			if ((document.forms[i].elements[j].type == "text") || 
				    (document.forms[i].elements[j].type == "password") || 
					(document.forms[i].elements[j].type == "textarea"))
      					document.forms[i].elements[j].value = elementValue; 
				else if (document.forms[i].elements[j].type.indexOf("select") != -1) {
      				document.forms[i].elements[j].selectedIndex = -1;
      					while (((pos = elementValue.indexOf("|")) != -1) && (elementValue.length > 1)) {
       						sel = parseInt(elementValue.substring(0,pos));
       						elementValue = elementValue.substring(pos+1,elementValue.length);
       						if (sel < document.forms[i].elements[j].length) {
        						document.forms[i].elements[j].options[sel].selected = true;
								}
      				}
     			}else if ((document.forms[i].elements[j].type == "checkbox")
							||(document.forms[i].elements[j].type == "radio")) {
      					if (elementValue=="true") {					
							document.forms[i].elements[j].checked = true;
							}
				}
			}// end of if (document.forms[i].elements[j].name == elementName) {
		}	//end of for (j=0;j<document.forms[i].elements.length;j++) {
	}   // end of for (i=0;i<document.forms.length;i++) { 
 }  // end of if (val) {
}  //end of function storedValues() 

function saveForm (cookieName, nextPage) {
//checking for data...
var formCheck = emptyFormCheck();
 if (formCheck != true) {
 	alert("You didn't enter anything. Please try again.");
	window.location.href=this.location.href;
	}
 else {   
// after checking for data we continue...
var zircCookie = "";
  for (i=0;i<document.forms.length;i++) { 
   for (j=0;j<document.forms[i].elements.length; j++) { 
    var cookieElement = document.forms[i].elements[j].name; 
  if ((document.forms[i].elements[j].type == "text")
		|| (document.forms[i].elements[j].type == "password")
		|| (document.forms[i].elements[j].type == "textarea")){
	var elementValue = document.forms[i].elements[j].value;
  } else if (document.forms[i].elements[j].type.indexOf("select") != -1) {
		var elementValue = "";
		for(k=0;k<document.forms[i].elements[j].length;k++) {
			if (document.forms[i].elements[j].options[k].selected) {
     			   elementValue = document.forms[i].elements[j].options[k].text;}
		}//end of for(k= 
  } 
//note that these types are not entirely supported 
  else if ((document.forms[i].elements[j].type == "checkbox") || 
			   (document.forms[i].elements[j].type == "radio")) {
					var elementValue = document.forms[i].elements[j].checked;
  } 
//after gathering the name and value we continue...
  var elementPair = (cookieElement + "=" + elementValue);
	// These next lines loop together the long string that makes the cookie (e[0]=v[0]&e[1]=v[1]&...)
	if (zircCookie == "") {
		zircCookie = elementPair;} 
	else {
		zircCookie = zircCookie + "&" + elementPair;} //end of if (zircCookie == "")
	} //end of for (j=0;j<document.forms[i].elements.length; j++) { 	
   } //end of for (i=0;i<document.forms.length;i++)
var expires = new Date((new Date()).getTime() + 1 *24*60*60*1000);//days*hrs*min*sec*millisec -- this cookie expires in 1 day
   Set_Cookie(cookieName, zircCookie, expires); //set the cookie
if (nextPage) {
 window.location.href=nextPage;}
 } // end of else emptyFormCheck
}   //end of function saveForm() 

function formValues(formCookieName) {
	var cookieValue = getCookie(formCookieName);
	if (cookieValue) {
	var a = cookieValue.split(escape('&')); //parsing name/value pairs
		for (x=0; x<a.length; x++) {
			a[x] = a[x].split(escape('=')); //breaking pairs
			if (a[x]) {
				var cookieElementName = a[x].slice(0,1); //find the name in this value pair
				var cookieElementValue = a[x].slice(1,2); //find the value in this value pair
				}
				for (e=0; e<document.forms[i].elements.length; e++) {
			      if ((document.forms[i].elements[e].type != "submit") &&
				  	  (document.forms[i].elements[e].type != "button")) {
					  	if (document.forms[i].elements[e].name == cookieElementName) {
							document.forms[i].elements[e].value = unescape(cookieElementValue);}
						}
				}  //end of for (e=0; e<document.forms[i].elements.length; e++) {
	     } //end of for (x=0; x<a.length; x++) {
	  }   //end of if (cookieValue) {
	  else {alert('no cookie');}
  }   //end of function formValues() 
/************************************************************/

function checkRef() {
	if (document.referrer.indexOf('/zf_info/stckctr/submission/submitTerms.html') != -1) {
	}
	else {window.location.href = 'submitTryAgain.html';}
}

function sendMail(subjectTopic) {
	window.location.href = ('mailto:form_comments@zfin.org?subject=' + subjectTopic);
}

function popup(URL) {
	popupWindow = window.open(URL,"name","width=320,height=240,toolbar=no,menubar=no,scrollbars=no,resizable=no,location=no,directories=no,status=no");
}
	 
//-->

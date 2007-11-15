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
//
// 23-Oct-03 rholland
// No need to prompt if user will allow cookies - just do it
//
//      use_cookies = (confirm("Will you allow the contact information and grant number you enter into this form "
//                            +"to be stored on your computer so that those values will be pre-filled the next "
//                            +"time you return to the Resource Center online forms?") ? "yes":"no");
//
           use_cookies == "yes";
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
   					for(k=0;k<document.forms[i].elements[j].options.length;k++)
    					if (document.forms[i].elements[j].options[k].selected)
     					elementValue += k+"|";
  				} else if ((document.forms[i].elements[j].type == "checkbox")
							||(document.forms[i].elements[j].type == "radio")) {
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

function saveValues(cookieName, days, nextPage) {
//checking for permission to use cookies...
// 	if (cookieName.indexOf("Profile") != -1) {
//  		var bakeOK = cookiesOK(cookieName);
// 		} 
//	else {
  		var bakeOK = "yes";
//		}
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
    var elementName = document.forms[i].elements[j].name; 
	if ((document.forms[i].elements[j].type == "text")
		|| (document.forms[i].elements[j].type == "password")
		|| (document.forms[i].elements[j].type == "hidden")
		|| (document.forms[i].elements[j].type == "textarea")) {
		var elementValue = document.forms[i].elements[j].value;
  } else if (document.forms[i].elements[j].type.indexOf("select") != -1) {
   	elementValue = "";
   		for(k=0;k<document.forms[i].elements[j].options.length;k++) {
    		if (document.forms[i].elements[j].options[k].selected){
       			elementValue += k + "|";
	     		}
	   	} //end of for(k=0;k<document.forms[i].elements[j].options.length;k++) {
  } else if ((document.forms[i].elements[j].type == "checkbox")
         	|| (document.forms[i].elements[j].type == "radio"))  {
				elementValue = document.forms[i].elements[j].checked;
  } else if (document.forms[i].elements[j].type == "button") {
      		var elementValue = "";
  } 
var elementPair = (elementName + "=" + elementValue); 
	// These next lines loop together the long string that makes the cookie (e[0]=v[0]&e[1]=v[1]&...)
	if (zircCookie == "") {
		zircCookie = elementPair;
		} else {
		zircCookie = zircCookie + "&" + elementPair;
		} 
	} //end of for (j=0;j<document.forms[i].elements.length; j++) { 	
   } //end of for (i=0;i<document.forms.length;i++)
   Set_Cookie(cookieName, zircCookie, expires); //set the cookie
 } //end of if (bakeOK == "yes")
if (nextPage) {
   window.location.href = nextPage;
 	}
 } // end of else emptyFormCheck
}   //end of function saveValues() 


function storedValues(cookieName) {
//alert("!storedValues: "+cookieName);
if (getCookie(cookieName)) {
  var cookieValue = getCookie(cookieName);
//alert("!cookieValue: "+cookieValue);
	if (cookieValue) {
	var valueArray = cookieValue.split(escape('&')); //parsing name/value pairs
	for (var x=0; x<valueArray.length; x++){
	   valueArray[x] = valueArray[x].split(escape('=')); //breaking pairs into an array
	}
        for (i=0;i<document.forms.length;i++) {
           for (j=0;j<document.forms[i].elements.length;j++) { 
              var elementName = valueArray[j].slice(0,1); //find the name in this pair
              var elementValue = valueArray[j].slice(1,2); //find value in this pair
              var elementValue = unescape(elementValue); //this makes it readable
              if (document.forms[i].elements[j].name == elementName) {
                 if ((document.forms[i].elements[j].type == "text") || 
                     (document.forms[i].elements[j].type == "password") || 
                     (document.forms[i].elements[j].type == "textarea")) {
                    document.forms[i].elements[j].value = elementValue; 
                 } else if (document.forms[i].elements[j].type.indexOf("select") != -1) {
                    document.forms[i].elements[j].selectedIndex = -1;
                    while (((ourPosition = elementValue.indexOf("|")) != -1) && (elementValue.length > 1)) {
                       var ourSelection = parseInt(elementValue.substring(0,ourPosition));
                       elementValue = elementValue.substring(ourPosition+1,elementValue.length);
                       if (ourSelection < document.forms[i].elements[j].length) {
                          document.forms[i].elements[j].options[ourSelection].selected = true;
                       }
                    }//end of while (((ourPosition...
                 } else if ((document.forms[i].elements[j].type == "checkbox") ||
                            (document.forms[i].elements[j].type == "radio")) {
                    if (elementValue == "true") {					
                       document.forms[i].elements[j].checked = true;
                    }
                 }
              }// end of if (document.forms[i].elements[j].name == elementName) {
           }	//end of for (j=0;j<document.forms[i].elements.length;j++) {
	}   // end of for (i=0;i<document.forms.length;i++) { 
     }  // end of if (cookieValue) {
   }  //end of if(getCookie(cookieName))
}  //end of function storedValues() 

function storedMTAValues(cookieName) {
//alert("!storedMTAValues: "+cookieName);
   if (getCookie(cookieName)) {
     var cookieValue = getCookie(cookieName);
     if (cookieValue) {
	var valueArray = cookieValue.split(escape('&')); //parsing name/value pairs
	for (var x=0; x<valueArray.length; x++){
	   valueArray[x] = valueArray[x].split(escape('=')); //breaking pairs into an array
           var elementName = valueArray[x].slice(0,1); 			//find the name in this pair
           var elementValue = valueArray[x].slice(1,2); 		//find value in this pair
           var elementValue = unescape(elementValue); 			//this makes it readable
           for (i=0; i<document.forms.length;i++) {
              for (j=0; j<document.forms[i].elements.length;j++) {
                 if (document.forms[i].elements[j].name == elementName) {
                    if ((document.forms[i].elements[j].type == "text") || 
                        (document.forms[i].elements[j].type == "password") || 
                        (document.forms[i].elements[j].type == "textarea")) {
                       document.forms[i].elements[j].value = elementValue; 
                    } else if (document.forms[i].elements[j].type.indexOf("select") != -1) {
                       document.forms[i].elements[j].selectedIndex = -1;
                       while (((ourPosition = elementValue.indexOf("|")) != -1) && (elementValue.length > 1)) {
                          var ourSelection = parseInt(elementValue.substring(0,ourPosition));
                          elementValue = elementValue.substring(ourPosition+1,elementValue.length);
                          if (ourSelection < document.forms[i].elements[j].length) {
                             document.forms[i].elements[j].options[ourSelection].selected = true;
                          }
                       }//end of while (((ourPosition...
                    } else if ((document.forms[i].elements[j].type == "checkbox") ||
                               (document.forms[i].elements[j].type == "radio")) {
                       if (elementValue == "true") {					
                          document.forms[i].elements[j].checked = true;
                       }
                    }
                 }		// end of if (document.forms[i]... == elementName) {
              }		     //end of for (j=0;j<document.forms[i]....) {
	   }             // end of for (i=0;i<document.forms...) {
	}	     // for (var x=0; x<valueArray.length; x++) {
      }    	  // end of if (cookieValue) {
   }  	      //end of if(getCookie(cookieName))
}  	   //end of function storedValues() 

function saveForm (cookieName, nextPage) {
//alert("!saveForm: "+cookieName+"  |  "+nextPage);
var zircCookie = "";
//checking for data...
var formCheck = emptyFormCheck();
 if (formCheck != true) {
 	alert("You didn't enter anything. Please try again.");
	window.location.href=this.location.href;
	}
 else {   
// after checking for data we continue...
  for (i=0;i<document.forms.length;i++) { 
   for (j=0;j<document.forms[i].elements.length; j++) { 
    var elementName = document.forms[i].elements[j].name; 
	if ((document.forms[i].elements[j].type == "text")
		|| (document.forms[i].elements[j].type == "password")
		|| (document.forms[i].elements[j].type == "textarea")) {
			var elementValue = document.forms[i].elements[j].value;
  } else if (document.forms[i].elements[j].type.indexOf("select") != -1) {
   		var elementValue = ""; 
		var elementSelected = "";
   			for(k=0;k<document.forms[i].elements[j].options.length;k++){			
				if (document.forms[i].elements[j].options[k].selected == true){
        			elementSelected += k + "|"; //+= is used here to allow for multiple selections
					elementValue += document.forms[i].elements[j].options[k].value + "|";
					} 
				} //end of for(k=0;k<document.forms[i].elements[j].length;k++){
  } else if ((document.forms[i].elements[j].type == "checkbox")
            ||(document.forms[i].elements[j].type == "radio")) {
			   elementValue = "";
				if (document.forms[i].elements[j].checked == true ) {
              		elementValue = document.forms[i].elements[j].value;
			  	}
  } else if (document.forms[i].elements[j].type == "button") {
       			elementValue = "";
  }
  //eliminate items that were not selected and/or entered
  if (elementValue) {
     var elementPair = (elementName + "=" + elementValue); 
	// These next lines loop together the long string that makes the cookie (e[0]=v[0]&e[1]=v[1]&...)
		if (zircCookie == "") {
			zircCookie = elementPair;
			} else {
			zircCookie = zircCookie + "&" + elementPair;
			}
	  } //end of if (elementValue) {
	} //end of for (j=0;j<document.forms[i].elements.length; j++) { 	
   } //end of for (i=0;i<document.forms.length;i++)
//after gathering the name and value we continue...
var expires = new Date((new Date()).getTime() + 1 *24*60*60*1000);//days*hrs*min*sec*millisec -- this cookie expires in 1 day
   Set_Cookie(cookieName, zircCookie, expires); //set the cookie
if (nextPage) {
 	window.location.href=nextPage;
	}
 } // end of else emptyFormCheck
}   //end of function saveForm() 

function formValues(formCookieName) {
	for (i=0; i<document.forms.length; i++) {
	var cookieValue = getCookie(formCookieName);
	if (cookieValue) {
	var cookieArray = cookieValue.split(escape('&')); //parsing name/value pairs
		for (x=0; x<cookieArray.length; x++) {
			cookieArray[x] = cookieArray[x].split(escape('=')); //breaking pairs into an array
			if (cookieArray[x]) {
				var elementName = cookieArray[x].slice(0,1); //find the name in this value pair
				var elementValue = cookieArray[x].slice(1,2); //find the value in this value pair
				}
				for (e=0; e<document.forms[i].elements.length; e++) {
			      if ((document.forms[i].elements[e].type != "submit") &&
				  	  (document.forms[i].elements[e].type != "button")) {
				  		if (elementName == document.forms[i].elements[e].name){
							document.forms[i].elements[e].value = unescape(elementValue);
						}
					}
				}  //end of for (e=0; e<document.forms[i].elements.length; e++) {
	     } //end of for (x=0; x<a.length; x++) {
	  }   //end of if (cookieValue) {
	}   //end of for (i=0; i<document.length; i++) {
  }   //end of function formValues() 
/************************************************************/

// this fills in the PI boxes with the contact person's info
function copyContactInfo(ourForm, ourElement) {
		for (n=0;n<ourForm.length;n++) {
			if (ourForm.elements[n].name == 'required_submitterName') {
					var x = n ;
					}
			} //end of for (n=0
		for (n=0;n<ourForm.length;n++) {
			if (ourForm.elements[n].name == 'PIName') {
					var y = n ;
					}
			} //end of for (n=0
		var z = y - x;
		var f = 9; //number of elements to copy
			
		if (
			(ourElement.type == "checkbox" && ourElement.checked == true) 
			|| (ourElement.type.indexOf('select') != -1 && ourElement.value == "PI / Director")
			) {
				for (n=x;n<f;n++) {	
					ourForm.elements[n+z].value = ourForm.elements[n].value;
					ourForm.copyContact.checked = true;
						} //for (n=x;n<8;n++)
				 } else {
				 	for (n=x;n<f;n++) {
						ourForm.elements[n+z].value = "";
						ourForm.copyContact.checked = false;
						} //for (n=x;n<8;n++)
				} // if (ourCheckbox.checked == true)
}

//this checkRef() function is for the fish line submission process
function checkRef() {
		if (document.referrer.indexOf('/zf_info/stckctr/submission/submitTerms2.html') == -1) {
					window.location.href = 'submitTryAgain.html';
				}	
		}

function sendMail(subjectTopic) {
	window.location.href = ('mailto:form_comments@zfin.org?subject=' + subjectTopic);
}

function popup(URL) {
	popupWindow = window.open(URL,"name","width=320,height=240,toolbar=no,menubar=no,scrollbars=no,resizable=no,location=no,directories=no,status=no");
}
	 
//-->

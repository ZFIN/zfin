/** 

FILE:  sessionstate.js

Uses a single cookie to act as a "session" and store state
information of form elements.



**/


var sessionState =  {
  stateData : {},
  
  storeObjectAttribute : function (id, attribute, value) {
    this.stateData[id] = new Object();
    this.stateData[id][attribute] = { 'value' : value };
    this.saveState();
  },

  getAttribute : function(id, attribute) {
    return this.stateData[id][attribute].value;
  },

  applyAttribute: function(id, attribute) {
    var value = this.getAttribute(id, attribute);
    if (document.getElementById(id) != null) {
      document.getElementById(id)[attribute] = value;

/**  commenting this out because sometimes we
     want the onchange to actually be when the user changed it **/

/*      document.getElementById(id).onchange(); */
    } else {
      /* we'll end up here if element doesn't have an id,
         but it could also happen if an element is deleted  */

      /* to debug: alert('id: ' + id + 'is null, attempting to delete');  */

      /* if the id has stored attributes and doesn't
         exist anymore, delete those attributes */

      this.deleteAttributes(id);
    }
  },

  applyAllAttributes : function() {
    for (var id in this.stateData) {      
      var attributes = this.stateData[id];
      for (var attr in attributes) {
        if (attr != 'attribute') {
          this.applyAttribute(id,attr);
        }
      }
    } 
  },

  deleteAttributes : function(id) {
    this.stateData[id] = null;
    this.saveState();
  },

  loadState: function() {
   this.stateData = new Object();
   var newState = unescape(getCookie('pubcur_c_sessionState')).parseJSON();
   if (newState != null) {
     this.stateData = newState;
   }
  },

  saveState: function() {
    setCookie('sessionState',escape(Object.toJSON(this.stateData)), 'pubcur_c_');
  }

}












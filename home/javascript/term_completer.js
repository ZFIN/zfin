      
      // This is a variable which defines the path to the ajax request processing script
      // This can be changed and should point to the script which process the Ajax queries
      // reference script in source forge is at perl/dichty-ontology.cgi
      var url  = '/db/cgi-bin/ajax_search/term_completer.cgi';
      
      function set_ontology_term(termid){
         if ( !isNaN( termid ) ) {      
            var pars = 'termid=' + termid;

            var myAjax = new Ajax.Updater('terminfo', url, {method: 'get', parameters: pars } );
           // have to have [1] next to form item because the edit subform and
           // the main form both have donorid elements
            if ( document.forms[0].termid.length > 1 ) {
               document.forms[0].termid[1].value = termid;
               document.forms[0].termid[2].value = termid;
            }
            else {
               document.forms[0].termid.value = termid;
            }
         }
      
      
      }

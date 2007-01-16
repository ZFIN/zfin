      
      // This is a variable which defines the path to the ajax request processing script
      // This can be changed and should point to the script which process the Ajax queries
      // reference script in source forge is at perl/dichty-ontology.cgi
      var url  = '/db/cgi-bin/ajax_search/ontology.cgi';
      
      function set_ontology(ontologyid){
         if ( !isNaN( ontologyid ) ) {      
            var pars = 'ontologyid=' + ontologyid;

            var myAjax = new Ajax.Updater('ontologyinfo', url, {method: 'get', parameters: pars, onComplete: document.forms[0].ontologyname.value = '' } );
           // have to have [1] next to form item because the edit subform and
           // the main form both have donorid elements
            if ( document.forms[0].ontologyid.length > 1 ) {
               document.forms[0].ontologyid[1].value = ontologyid;
               document.forms[0].ontologyid[2].value = ontologyid;
            }
            else {
               document.forms[0].ontologyid.value = ontologyid;
            }
         }
      }

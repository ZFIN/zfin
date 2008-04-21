
function rotateRadio(key) {
  if (document.getElementById(key + "ignore").checked == true) {
    document.getElementById(key + "add").click();
  } else if (document.getElementById(key + "remove").checked == true) {
    document.getElementById(key + "ignore").click();
  } else if (document.getElementById(key + "add").checked == true) {
    document.getElementById(key + "remove").click();
  }

}

/* deselect the 'unspecified - quality [abnormal]' phenotype */
function deselectDefault() {
  distinctPhenotypes = patoCuration.distinctPhenotypes;

  for (var dP in distinctPhenotypes) {
    if ((distinctPhenotypes[dP].entity_a == "unspecified") 
         && (distinctPhenotypes[dP].quality == "quality")) {
      distinctPhenotypes[dP].setState('remove');
      document.getElementById(dP+"add").checked = false;
      document.getElementById(dP+"remove").checked = true;
    }  
  }

}

function updateSelectElement(element, value) {
    var i;
    
    for (i=0 ; i < element.options.length ; i++) {
	if (element.options[i].value == value) {
	    element.selectedIndex = i;
	    if (element.onChange != null) { element.onChange(); }
	    break;
	}
    }
}



/** Main 'object' used as a singleton by pato curation app pages **/

function PatoCuration() {
  this.mutants = new Array();
  this.phenotypes = new Array();
  this.distinctPhenotypes = new Array();

  var selectedMutantsString = "";

  this.addMutant = function(m) { 
      this.mutants[this.mutants.length+1] = m; 
  }


  this.addPhenotype = function(p) {
      this.phenotypes[this.phenotypes.length+1] = p;
  }

  this.getPhenotype = function(key) {
      for (var p in this.phenotypes) {
        if (this.phenotypes[p].radio_id == key) {
          return this.phenotypes[p];
        }
      }
  }


  /*
   Uncheck mutants and update the state of the whole system
   */
  this.deselectMutants = function(person,OID) {
      var mutants = this.mutants;
      for (var i = 0 ; i < mutants.length ; i++) {
	  if (document.getElementById(mutants[i].checkbox_id).checked == true) { 
              document.getElementById(mutants[i].checkbox_id).checked = false;
              storeSession(person,OID,mutants[i].checkbox_id, 'false');
	  }
      }
      //once they're all unchecked, update the radio buttons
      this.updateMutants();

  }


  /** it's a little silly that this method needs to take the person 
      and OID, but it's for saving the stage **/

  this.selectAllMutants = function(person,OID) {
      var mutants = this.mutants;
      for (var i = 0 ; i < mutants.length ; i++) {
	  if (document.getElementById(mutants[i].checkbox_id).checked == false) { 
              document.getElementById(mutants[i].checkbox_id).checked = true;
              storeSession(person,OID,mutants[i].checkbox_id, 'true');
	  }
      }
      //because the checkbox values change, we need to update the radio buttons
      this.updateMutants();
  }

  this.updateMutants = function() {
      var infospan = document.getElementById('patobldrSelectedMutants');
      var mutants = this.mutants;
      var distinctPhenotypes = this.distinctPhenotypes;
      var selectedMutants = new Array();
      
      var selectedMutantString = "";
      
      infospan.innerHTML = "";
      
      for (var i = 0 ; i < mutants.length ; i++) {
	  if (document.getElementById(mutants[i].checkbox_id).checked == true) {
	      mutants[i].selected = true;
	      infospan.innerHTML = infospan.innerHTML + mutantToString(mutants[i]) + "<br>"; 
	      selectedMutants.push(mutants[i]);
	      if (selectedMutantString != "") { selectedMutantString = selectedMutantString + "|"; }
	      selectedMutantString = selectedMutantString 
		  + mutants[i].fig_zdb_id
		  + "|" + mutants[i].genox_zdb_id
		  + "|" + mutants[i].apato_start_stg_zdb_id
		  + "|" + mutants[i].apato_end_stg_zdb_id;
	  } else {
	      mutants[i].selected = false;
	  }
	  
      }
      
      this.selectedMutantString = selectedMutantString;
      document.getElementById("patobldrSelectedMutantString").value = selectedMutantString; 
      document.getElementById("patobldrSelectedMutantStringCount").value = selectedMutants.length;
      
      
      var lastState;
      var currentState;
      
      /* reset all phenotypes to have an empty state */
      for (var dP in distinctPhenotypes) {
	  distinctPhenotypes[dP].state = "";
      }
      
      
      /* only process phenotypes if a mutant is selected */
      if (selectedMutants.length > 0) { 
	  
	  
	  for (var dP in distinctPhenotypes) {
	      for (var m=0 ;  m < selectedMutants.length ; m++) {		  
		  currentState = selectedMutants[m].hasPhenotype(dP);
		  
		  /* for the first one, set just make lastState equal to the current state */
		  if (m == 0) { lastState = currentState; }        
		  
		  /* the state gets set to ignore if there are conflicting states, so 
		     once it's been set to ignore, we skip it, because it's never allowed
		     to be set to anything else after that */ 
		  
		  if (distinctPhenotypes[dP].state != "ignore") {
		      
		      /* now, if the state is the same as the last one, that state is valid */
		      if (currentState == lastState) { distinctPhenotypes[dP].state = currentState }
		      else /* if the state isn't the same, we need to set the radio to "ignore", 
			      because it's has a different state in the different mutants */ 
		      { distinctPhenotypes[dP].state = "ignore"; }
		      
		      /* set the lastState for the next go of the loop */
		      lastState = currentState;
		      
		  }
		  
	      } 
	  }
	  
	  /* it turns out that even if all of the mutants agree on a "remove" state
	     for a phenotype, it should still get set to "ignore" */
	  for (var dP in distinctPhenotypes) {
	      if (distinctPhenotypes[dP].state == "remove") {
		  distinctPhenotypes[dP].state = "ignore";
	      }
	  } 
	  
	  
	  for (var dP in distinctPhenotypes) {
	      document.getElementById(dP + distinctPhenotypes[dP].state).checked = true;     
	  }  
	  
      }
      
  }

  this.submitMutants = function() {
      var mutants = this.selectedMutants;
      var input;
      for (var m in mutants) {
	  input = createElement("input");
	  input.type = "hidden";  input.name = "patoup_updatephenos_selectedmutants";
	  input.value = mutants[m].fig_zdb_id
	      + "|" + mutants[i].genox_zdb_id
	      + "|" + mutants[i].apato_start_stg_zdb_id
	      + "|" + mutants[i].apato_end_stg_zdb_id;
	  document.getElementById('patobldrUpdatePhenosForm').appendChild(input);
      }      
  }

  this.updatePhenotypes = function() {
      distinctPhenotypes = this.distinctPhenotypes;
      
      addPhenotypesString = "";
      addPhenotypesStringCount = "0";
      
      removePhenotypesString = "";
      removePhenotypesStringCount = "0";
      
      for (var dP in distinctPhenotypes) {
	  if (distinctPhenotypes[dP].state == "add") {
	      if(addPhenotypesString != "") { addPhenotypesString = addPhenotypesString + "|"; }
	      addPhenotypesString = addPhenotypesString + distinctPhenotypes[dP].api_zdb_id;
	      addPhenotypesStringCount++;
	  } else if (distinctPhenotypes[dP].state == "remove") {
	      if(removePhenotypesString != "") { removePhenotypesString = removePhenotypesString + "|"; }
	      removePhenotypesString = removePhenotypesString + distinctPhenotypes[dP].api_zdb_id;
	      removePhenotypesStringCount++;
	  } 
      }
      
      document.getElementById("patobldrAddPhenotypesString").value = addPhenotypesString;
      document.getElementById("patobldrAddPhenotypesStringCount").value = addPhenotypesStringCount;
      
      document.getElementById("patobldrRemovePhenotypesString").value = removePhenotypesString;
      document.getElementById("patobldrRemovePhenotypesStringCount").value = removePhenotypesStringCount;
      

  }
  
  this.updateMutantForm = function(fig_zdb_id, geno_zdb_id, exp_zdb_id, start_stg_zdb_id, end_stg_zdb_id) {

    updateSelectElement($('patosumFig'), fig_zdb_id); 
    updateSelectElement($('patosumGenotype'), geno_zdb_id);
    updateSelectElement($('patosumEnv'), exp_zdb_id);
    updateSelectElement($('patosumStartStage'), start_stg_zdb_id);
    updateSelectElement($('patosumEndStage'), end_stg_zdb_id);

  }
  

  this.updateSelectElement = function(element, value) {
    var i;

    for (i=0 ; i < element.options.length ; i++) {
      if (element.options[i].value == value) {
        element.selectedIndex = i;
        if (element.onChange != null) { element.onChange(); }
        break;
      }
    }
  }

}




function Mutant(fig_label, fig_zdb_id, geno_handle, geno_zdb_id, exp_name,
                exp_zdb_id, apato_start_stg_zdb_id, start_stg_abbrev, apato_end_stg_zdb_id, end_stg_abbrev,
                genox_zdb_id, selected, checkbox_id) {
  this.fig_label = fig_label;
  this.fig_zdb_id = fig_zdb_id;
  this.geno_handle = geno_handle;
  this.geno_zdb_id = geno_zdb_id;
  this.exp_name = exp_name;
  this.exp_zdb_id = exp_zdb_id; 
  this.apato_start_stg_zdb_id = apato_start_stg_zdb_id;
  this.start_stg_abbrev = start_stg_abbrev;
  this.apato_end_stg_zdb_id = apato_end_stg_zdb_id;
  this.end_stg_abbrev = end_stg_abbrev;
  this.genox_zdb_id = genox_zdb_id;
  this.selected = selected;
  this.checkbox_id = checkbox_id;


  this.phenotypes = new Array();

  this.hasPhenotype = hasPhenotype;


}

function hasPhenotype(key) {
  for (var i=0 ;  i < this.phenotypes.length ; i++ ) {      
	  if (this.phenotypes[i] == key) {
		  return "add";  
	  }	  
  }	
  return "remove";
}



function debugMutants() {
  mutants = patoCuration.mutants;

  j = 0;
  for (i in mutants) {	  
	  j++;
  }  

}

function getMutant(checkbox_id) {
  mutants = patoCuration.mutants;
  for (i = 0 ; i < mutants.length ; i++) {
    if (checkbox_id == mutants[i].checkbox_id) {
      return mutants[i];
    }
  }
}

function mutantToString(m) {

  return m.fig_label + " &nbsp; &nbsp; " 
       + m.geno_handle + " &nbsp; &nbsp;"
       + m.exp_name + "&nbsp; &nbsp;"
       + m.start_stg_abbrev  + " &nbsp; &nbsp;"
       + m.end_stg_abbrev  + " &nbsp; &nbsp;"   ;   

}




function updatePhenotypes() {
  distinctPhenotypes = this.distinctPhenotypes;

  addPhenotypesString = "";
  addPhenotypesStringCount = "0";

  removePhenotypesString = "";
  removePhenotypesStringCount = "0";

  for (var dP in distinctPhenotypes) {
    if (distinctPhenotypes[dP].state == "add") {
      if(addPhenotypesString != "") { addPhenotypesString = addPhenotypesString + "|"; }
      addPhenotypesString = addPhenotypesString + distinctPhenotypes[dP].api_zdb_id;
      addPhenotypesStringCount++;
    } else if (distinctPhenotypes[dP].state == "remove") {
      if(removePhenotypesString != "") { removePhenotypesString = removePhenotypesString + "|"; }
      removePhenotypesString = removePhenotypesString + distinctPhenotypes[dP].api_zdb_id;
      removePhenotypesStringCount++;
    } 
  }

  document.getElementById("patobldrAddPhenotypesString").value = addPhenotypesString;
  document.getElementById("patobldrAddPhenotypesStringCount").value = addPhenotypesStringCount;

  document.getElementById("patobldrRemovePhenotypesString").value = removePhenotypesString;
  document.getElementById("patobldrRemovePhenotypesStringCount").value = removePhenotypesStringCount;

}














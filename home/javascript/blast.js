
function setOption () {
    if (document.getElementById('program').value == "blastn") {

        document.getElementById('xnu1').checked = false;
        document.getElementById('seg1').checked = false;
        document.getElementById('matrix').selectedIndex = 0;

        if (document.getElementById('SHORT').checked) {

            document.getElementById('expectValue').value = 1000;
            document.getElementById('wordLength').value = 7;
            document.getElementById('dust1').checked = false;
            document.getElementById('poly_a1').checked = false;
        }
        else{
            document.getElementById('expectValue').value = 1e-25;
            document.getElementById('wordLength').value = 11;
            document.getElementById('dust1').checked = true;
            document.getElementById('poly_a1').checked = true;
        }
    }
    else {
        document.getElementById('dust1').checked = false;
        document.getElementById('poly_a1').checked = false;
        document.getElementById('matrix').selectedIndex = 1;

        if ( document.getElementById('program').value == "blastp" ) {
            document.getElementById('SHORT').disabled = false;
        }else {
            document.getElementById('SHORT').disabled = true;
        }

        if ( document.getElementById('program').value == "blastp" &&
             document.getElementById('SHORT').checked ) {
            document.getElementById('expectValue').value = 20000;
            document.getElementById('wordLength').value = 2;
            document.getElementById('seg1').checked = false;
            document.getElementById('xnu1').checked = false;
        }
        else {
            document.getElementById('expectValue').value = 10;
            document.getElementById('wordLength').value = 3;
            document.getElementById('seg1').checked = true;
            document.getElementById('xnu1').checked = true;
        }
    }
}

function validateSequence() {

    var input_seq = document.getElementById("querySequence").value;
    var seq_length = input_seq.length;

    // check for any leading 8-bit character
    if (input_seq.charCodeAt(0) > 127 || input_seq.charCodeAt(0) < 1) {
        alert("non-ASCII character is detected at the beginning of the input sequence. ");
        return false;
    }

    //check for any trailing 8-bit character
    if (input_seq.charCodeAt(seq_length-1) > 127 || input_seq.charCodeAt(seq_length-1) < 1) {
        alert("non-ASCII character is detected at the end of the input sequence.");
        return false;
    }

    return true;
}

//   function setFocus() {
//	<?MIVAR COND=$(XST,$SEQ_ID)>
//	  document.formBean.SEQ_ID.focus();
//	<?/MIVAR>
//   }

function setInfo (form_select) {
    var abbrev = form_select.options[form_select.selectedIndex].value
    document.getElementById('databaseInfoDiv').innerHTML = '<img src="/images/ajax-loader.gif"/>';
    new Ajax.Updater('databaseInfoDiv','/action/blast/single-blast-definition?accession='+abbrev);
    if(abbrev.search('MicroRNA')>=0 || abbrev.search('miRNA')>=0 || abbrev.search('zfin_microRNA')>=0 || abbrev.search("zfin_mrph")>=0){
        document.getElementById('SHORT').checked = true ;
    }else{
        document.getElementById('SHORT').checked = false ;
    }

    setOption();
}


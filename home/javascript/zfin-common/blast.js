


function setOption () {
    if (document.getElementById('program').value == "blastn") {

        document.getElementById('xnu1').checked = false;
        document.getElementById('seg1').checked = false;
        document.getElementById('matrix').selectedIndex = 0;
        document.getElementById('filterDNA').style.display = 'inline';
        document.getElementById('filterProteins').style.display = 'none' ;
        document.getElementById('SHORT').disabled = false;

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
        document.getElementById('filterDNA').style.display = 'none';
        document.getElementById('filterProteins').style.display= 'inline' ;

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

function setInfo (form_select) {
    var abbrev = form_select.options[form_select.selectedIndex].value
    document.getElementById('databaseInfoDiv').innerHTML = '<img src="/images/ajax-loader.gif"/>';
    jQuery('#databaseInfoDiv').load('/action/blast/single-blast-definition?accession='+abbrev);
    if(abbrev.search('MicroRNA')>=0 || abbrev.search('miRNA')>=0 || abbrev.search('zfin_microRNA')>=0 || abbrev.search("zfin_mrph")>=0 || abbrev.search("zfin_talen")>=0 || abbrev.search("zfin_crispr")>=0){
        document.getElementById('SHORT').checked = true ;
    }else{
        document.getElementById('SHORT').checked = false ;
    }

    setOption();
}

$(function () {
    $('.update-blast-option').on('change', setOption);
    $('.update-database-info').on('change', function () { setInfo(this); });
});
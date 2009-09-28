function openSequence(index){
    (document.getElementById("sequence"+index)).show()  ;
    (document.getElementById("showSequenceButton"+index)).hide()  ;
    (document.getElementById("hideSequenceButton"+index)).show()  ;
    //                        (document.getElementById("hideSequenceButton"+index)).setStyle("display: inline;")  ;
}

function closeSequence(index){
    (document.getElementById("sequence"+index)).hide()  ;
    (document.getElementById("showSequenceButton"+index)).show()  ;
    //                        (document.getElementById("showSequenceButton"+index)).setStyle("display: inline;")  ;
    (document.getElementById("hideSequenceButton"+index)).hide()  ;
}


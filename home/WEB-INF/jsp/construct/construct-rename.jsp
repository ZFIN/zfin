<h2>Temporary Developer Tool to Rename Construct</h2>

<script>
    function submitForm(constructID, pubZdbID, constructType, constructPrefix, constructStoredName) {
        console.log("submitting form");
        var url = "/action/construct/rename/" + constructID ;

        var data = {
            constructType: constructType,
            constructPrefix: constructPrefix,
            constructStoredName: constructStoredName,
            pubZdbID: pubZdbID
        };
        fetch(url, {
            method: "POST",
            body: JSON.stringify(data),
            headers: {
                "Content-Type": "application/json"
            }
        }).then(function(response) {
            console.log("response", response);
            return response.json();
        }).then(function(json) {
            console.log("json", json);
            if (json.success) {
                alert(json.message);
            } else {
                alert("Error: " + json.message);
            }
        }).catch(function(error) {
            console.log("error", error);
            alert("Error: " + error);
        });
    }

    const changes = [
        {
            "constructID": "ZDB-TGCONSTRCT-161115-2",
            "pubZdbID": "ZDB-PUB-190507-21",
            "constructType": "Tg",
            "constructPrefix": "",
            "constructStoredName": "en.epi#-#Hsa.HBB#:EGFP"
        },
        {
            "constructID": "ZDB-TGCONSTRCT-161115-3",
            "pubZdbID": "ZDB-PUB-190507-21",
            "constructType": "Tg",
            "constructPrefix": "",
            "constructStoredName": "rr.2pand1#:EGFP"
        },
        {
            "constructID": "ZDB-TGCONSTRCT-161115-4",
            "pubZdbID": "ZDB-PUB-190507-21",
            "constructType": "Tg",
            "constructPrefix": "2",
            "constructStoredName": "rr.2pand1#:EGFP"
        },
        {
            "constructID": "ZDB-TGCONSTRCT-220422-1",
            "pubZdbID": "ZDB-PUB-220103-2",
            "constructType": "Tg",
            "constructPrefix": "",
            "constructStoredName": "pth2#:EGFP#Cassette#,#cryaa#:EGFP"
        },
        {
            "constructID": "ZDB-TGCONSTRCT-220422-2",
            "pubZdbID": "ZDB-PUB-220103-2",
            "constructType": "Tg",
            "constructPrefix": "",
            "constructStoredName": "pth2#:TagRFP#Cassette#,#cryaa#:mCherry"
        },
        {
            "constructID": "ZDB-TGCONSTRCT-190812-12",
            "pubZdbID": "ZDB-PUB-190209-17",
            "constructType": "Tg",
            "constructPrefix": "BAC",
            "constructStoredName": "cdh1#:cdh1#-#TagRFP#Cassette#,#cryaa#:Cerulean"
        },
    ];

    changes.forEach(change => {
        document.writeln('<button id="' + change.constructID + '">Rename ' + change.constructID + '</button>  <a target="_blank" href="/' + change.constructID + '">View Page</a><br/><br/>');
        document.getElementById(change.constructID).addEventListener("click",
            () => submitForm(change.constructID, change.pubZdbID, change.constructType, change.constructPrefix, change.constructStoredName));
    });

</script>

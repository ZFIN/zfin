<h2>Temporary Developer Tool to Rename Construct</h2>

<style>
    /* Basic styling for a table */
    table {
        border-collapse: collapse;
        width: 100%;
    }
    /* Add some padding to the cells */
    th, td {
        padding: 8px;
        text-align: left;
        border-bottom: 1px solid #ddd;
    }
    /* Add a background color to the table header */
    th {
        background-color: #d0d0f0;
    }

    /* Give the rows some color */
    tr:nth-child(even) {
        background-color: #e5f0ff;
    }

    /* Style the input */
    input[type=text] {
        width: 100%;
        padding: 12px 20px;
        margin: 8px 0;
        box-sizing: border-box;
    }

</style>
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
        // {
        //     "constructID": "ZDB-TGCONSTRCT-161115-2",
        //     "pubZdbID": "ZDB-PUB-190507-21",
        //     "constructType": "Tg",
        //     "constructPrefix": "",
        //     "constructStoredName": "en.epi#-#Hsa.HBB#:EGFP",
        //     "constructOldName": "Tg(and1-Hsa.HBB:EGFP)",
        //     "issueNumber": "ZFIN-7073"
        // },
        // {
        //     "constructID": "ZDB-TGCONSTRCT-161115-3",
        //     "pubZdbID": "ZDB-PUB-190507-21",
        //     "constructType": "Tg",
        //     "constructPrefix": "",
        //     "constructStoredName": "rr.2pand1#:EGFP",
        //     "constructOldName": "Tg(and1:EGFP)",
        //     "issueNumber": "ZFIN-7073"
        // },
        // {
        //     "constructID": "ZDB-TGCONSTRCT-161115-4",
        //     "pubZdbID": "ZDB-PUB-190507-21",
        //     "constructType": "Tg",
        //     "constructPrefix": "2",
        //     "constructStoredName": "rr.2pand1#:EGFP",
        //     "constructOldName": "Tg2(rr.2pand1:EGFP)",
        //     "issueNumber": "ZFIN-7073"
        // },
        // {
        //     "constructID": "ZDB-TGCONSTRCT-220422-1",
        //     "pubZdbID": "ZDB-PUB-220103-2",
        //     "constructType": "Tg",
        //     "constructPrefix": "",
        //     "constructStoredName": "pth2#:EGFP#Cassette#,#cryaa#:EGFP",
        //     "constructOldName": "Tg(pth2:EGFP)",
        //     "issueNumber": "ZFIN-8967"
        // },
        // {
        //     "constructID": "ZDB-TGCONSTRCT-220422-2",
        //     "pubZdbID": "ZDB-PUB-220103-2",
        //     "constructType": "Tg",
        //     "constructPrefix": "",
        //     "constructStoredName": "pth2#:TagRFP#Cassette#,#cryaa#:mCherry",
        //     "constructOldName": "Tg(pth2:TagRFP)",
        //     "issueNumber": "ZFIN-8967"
        // },
        // {
        //     "constructID": "ZDB-TGCONSTRCT-190812-12",
        //     "pubZdbID": "ZDB-PUB-190209-17",
        //     "constructType": "Tg",
        //     "constructPrefix": "BAC",
        //     "constructStoredName": "cdh1#:cdh1#-#TagRFP#Cassette#,#cryaa#:Cerulean",
        //     "constructOldName": "TgBAC(cdh1:cdh1-TagBFP,cryaa:Cerulean)",
        //     "issueNumber": "ZFIN-8986"
        // },
        // {
        //     "constructID": "ZDB-TGCONSTRCT-220928-1",
        //     "pubZdbID": "ZDB-PUB-210114-8",
        //     "constructType": "Tg",
        //     "constructPrefix": "",
        //     "constructOldName": "Tg(mylpfa:hsa.tpm3,myl7-:egfp)",
        //     "constructStoredName": "mylpfa#:hsa.tpm3#Cassette#,#myl7#:EGFP",
        //     "issueNumber": "ZFIN-9012"
        // },
        // {
        //     "constructID": "ZDB-TGCONSTRCT-230502-2",
        //     "pubZdbID": "ZDB-PUB-210630-8",
        //     "constructType": "Tg",
        //     "constructPrefix": "",
        //     "constructOldName": "Tg(rho:GAP-YFP-2A-NTR2.0)",
        //     "constructStoredName": "rho#:GAP#-#TagYFP#-#2A#-#NTR2.0",
        //     "issueNumber": "ZFIN-9034"
        // },
        // {
        //     "constructID": "ZDB-TGCONSTRCT-211019-1",
        //     "pubZdbID": "ZDB-PUB-210105-1",
        //     "constructType": "Tg",
        //     "constructPrefix": "",
        //     "constructOldName": "Tg(uts2d:GFP-CAAX)",
        //     "constructStoredName": "uts2d#:GFP#-#CAAX#Cassette#,#myl7#:EGFP",
        //     "issueNumber": "ZFIN-9036"
        // },
        {
            "constructID": "ZDB-TGCONSTRCT-230811-2",
            "pubZdbID": "ZDB-PUB-220302-19",
            "constructType": "Tg",
            "constructPrefix": "",
            "constructOldName": "Tg(myl7:ERK KTR-mClover-2A-Hsa.H2B-mScarlet)",
            "constructStoredName": "myl7#:ERK KTR#-#Clover#-#2A#-#Hsa.H2B#-#mScarlet",
            "issueNumber": "ZFIN-9288"
        },
        {
            "constructID": "ZDB-TGCONSTRCT-190211-3",
            "pubZdbID": "ZDB-PUB-181016-11",
            "constructType": "Tg",
            "constructPrefix": "",
            "constructOldName": "Tg(ubb:Mmu.Elk1-KTR-mClover)",
            "constructStoredName": "ubb#:Mmu.Elk1#-#KTR#-#Clover",
            "issueNumber": "ZFIN-9288"
        }
    ];

    document.writeln('<table><thead><tr><th>Construct ID</th><th>Pub ID</th><th>Old Name</th><th>New Name (partial)</th><th>Issue #</th><th>Rename</th></tr></thead><tbody>');
    changes.forEach(change => {
        document.writeln('<tr>' +
            '<td><a target="_blank" href="/' + change.constructID + '">' + change.constructID + '</a></td>' +
            '<td><a target="_blank" href="/' + change.pubZdbID + '">' + change.pubZdbID + '</a></td>' +
            '<td>' + change.constructOldName + '</td>' +
            '<td>' + change.constructStoredName.replaceAll('#Cassette#', '').replaceAll('#', '') + '</td>' +
            '<td><a target="_blank" href="https://zfin.atlassian.net/browse/' + change.issueNumber + '">' + change.issueNumber + '</a></td>' +
            '<td><button id="' + change.constructID + '">Rename</button></td>' +
            '</tr>');
        document.getElementById(change.constructID).addEventListener("click",
            () => submitForm(change.constructID, change.pubZdbID, change.constructType, change.constructPrefix, change.constructStoredName));
    });
    document.writeln('</tbody></table>');

</script>

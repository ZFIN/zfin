<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
    label {
        display: block;
    }
    input[type="text"] {
        width: 70%;
    }
</style>

<h2>Temporary Developer Tool to Rename Construct</h2>

<button id="ZDB-TGCONSTRCT-161115-2">Rename ZDB-TGCONSTRCT-161115-2</button><a target="_blank" href="/ZDB-TGCONSTRCT-161115-2">View Page</a><br/>
<button id="ZDB-TGCONSTRCT-161115-3">Rename ZDB-TGCONSTRCT-161115-3</button><a target="_blank" href="/ZDB-TGCONSTRCT-161115-3">View Page</a><br/>
<button id="ZDB-TGCONSTRCT-161115-4">Rename ZDB-TGCONSTRCT-161115-4</button><a target="_blank" href="/ZDB-TGCONSTRCT-161115-4">View Page</a><br/>

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
    document.getElementById("ZDB-TGCONSTRCT-161115-2").addEventListener("click",
        () => submitForm("ZDB-TGCONSTRCT-161115-2", "ZDB-PUB-190507-21", "Tg", "", "en.epi#-#Hsa.HBB#:EGFP"));

    document.getElementById("ZDB-TGCONSTRCT-161115-3").addEventListener("click",
        () => submitForm("ZDB-TGCONSTRCT-161115-3", "ZDB-PUB-190507-21", "Tg", "", "rr.2pand1#:EGFP"));

    document.getElementById("ZDB-TGCONSTRCT-161115-4").addEventListener("click",
        () => submitForm("ZDB-TGCONSTRCT-161115-4", "ZDB-PUB-190507-21", "Tg", "2", "rr.2pand1#:EGFP"));

</script>

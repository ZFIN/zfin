<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<dl class="row">
    <jsp:doBody />
</dl>

<script>
    function displaySuccessMessage(element) {
        // Create the tooltip
        $(element).attr('title', 'Copied').tooltip();

        // Show the tooltip
        $(element).tooltip('show');

        // hide the tooltip after 1 second
        setTimeout(function() {
            $(element).tooltip('dispose');
        }, 1000);
    }
    function copyElementContentsToClipboard(e) {
        navigator.clipboard.writeText(e.target.innerText.trim()).then(() => {
            displaySuccessMessage(e.target);
        }, (err) => {
            //ignore copy to clipboard error
        });
    }
    document.addEventListener("DOMContentLoaded", (event) => {
        const targets = document.querySelectorAll('.copy-attribute-container');
        targets.forEach(target => {
            target.addEventListener('click', copyElementContentsToClipboard);
        });
    });
</script>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
    <h4 class="modal-title" id="myModalLabel">Welcome to the new ZFIN Search (Beta)!</h4>
</div>
<div class="modal-body">

    <div style="margin-bottom: 2em ;padding-left: 1em;padding-right: 2em;color: #666;
         background-color: #fff;">

        <style>body {
            background: url(/images/zdbhome-background.jpg) -10% -40% repeat;
        }</style>

        <zfin2:messageContent/>

    </div>
</div>
<div class="modal-footer">
    <button class="btn btn-default" data-dismiss="modal" aria-hidden="true">Close</button>
</div>

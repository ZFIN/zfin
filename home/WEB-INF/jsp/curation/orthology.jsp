<script>
    // we want the jquery modal, not the bootstrap one on this page
    $.fn.modal.noConflict();
</script>

<table class="table table-borderless">
    <tr>
        <td>
            <div ortho-edit pub="${publication.zdbID}" edit="true" showDownloadLink="false"></div>
        </td>
    </tr>
</table>

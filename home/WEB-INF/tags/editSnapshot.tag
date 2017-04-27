<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="value" type="org.zfin.profile.HasImage" %>

${empty value.snapshot ? '<div style="color: red;">Please provide a snapshot</div>' : '' }

<form id='image-post' method="post"
      action="/action/profile/image/edit/${value.zdbID}" enctype="multipart/form-data"
      style="border: 2px solid gray; padding:  10px; "
        >
    <zfin2:viewSnapshot value="${value}" className="profile-image"/>
    <br/>
    <input type="file" name="file" onchange="form.submit();"/>
    <br/>
    <div class="error">
          ${imageError}
    </div>

</form>
<form action="/action/profile/image/delete/${value.zdbID}" method="post">
    <input type="submit" value="Delete Picture"/>
</form>

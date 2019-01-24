<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.zebrashare.presentation.LineEditBean" scope="request"/>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">

<div class="container-fluid">
    <c:if test="${!empty publication}">
        <span class="pull-right">
            Edit other line associated with ${publication.shortAuthorList}:
            <select id="other-lines">
                <c:forEach items="${otherFeatures}" var="other">
                    <!-- ${formBean.feature.zdbID} // ${other.zdbID} // ${formBean.feature.zdbID == other.zdbID} -->
                    <option value="${other.zdbID}" ${formBean.feature.zdbID == other.zdbID ? "selected" : ""}>${other.name}</option>
                </c:forEach>
            </select>
        </span>
    </c:if>

    <h1>${formBean.feature.name}</h1>

    <form:form method="POST" commandName="formBean" cssClass="form-horizontal">
        <div class="form-group">
            <label class="col-sm-3 control-label">Functional Consequence</label>
            <div class="col-sm-3">
                <select class="form-control">
                    <option>Null</option>
                    <option>Hypomorph</option>
                    <option>Other</option>
                    <option>Unknown</option>
                </select>
            </div>
        </div>

        <div class="form-group">
            <label class="col-sm-3 control-label">Adult Viable</label>
            <div class="col-sm-3">
                <select class="form-control">
                    <option></option>
                    <option>Yes</option>
                    <option>No</option>
                </select>
            </div>
        </div>

        <div class="form-group">
            <label class="col-sm-3 control-label">Maternal Zygocity Examined</label>
            <div class="col-sm-3">
                <select class="form-control">
                    <option></option>
                    <option>Yes</option>
                    <option>No</option>
                </select>
            </div>
        </div>

        <div class="form-group">
            <label class="col-sm-3 control-label">Currently Available</label>
            <div class="col-sm-3">
                <select class="form-control">
                    <option>Yes</option>
                    <option>No</option>
                </select>
            </div>
        </div>

        <div class="form-group">
            <label class="col-sm-3 control-label">Other Line Information</label>
            <div class="col-sm-9">
                <textarea class="form-control" rows="5"></textarea>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-offset-3 col-sm-8">
                <button type="submit" class="btn btn-primary">Save</button>
            </div>
        </div>
    </form:form>
</div>

<script>
    $(function () {
        $('#other-lines').on('change', function () {
            window.location.href = '/action/zebrashare/line-edit/' + $(this).val();
        });
    });
</script>
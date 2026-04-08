<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Database Properties">
    <div class="container-fluid" style="margin: 20px;">
        <h2>Database Properties (zdb_property)</h2>

        <div id="error-message" class="alert alert-danger" style="display: none"></div>
        <div id="success-message" class="alert alert-success" style="display: none"></div>

        <table class="table table-striped table-bordered" id="properties-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Value</th>
                    <th>Type</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody id="properties-body">
            </tbody>
        </table>

        <h4>Add / Edit Property</h4>
        <form id="property-form" class="form-inline" style="margin-bottom: 20px;">
            <input type="hidden" id="form-id" />
            <div class="form-group" style="margin-right: 10px;">
                <label for="form-name" style="margin-right: 5px;">Name</label>
                <input type="text" class="form-control" id="form-name" required />
            </div>
            <div class="form-group" style="margin-right: 10px;">
                <label for="form-value" style="margin-right: 5px;">Value</label>
                <input type="text" class="form-control" id="form-value" style="width: 400px;" />
            </div>
            <div class="form-group" style="margin-right: 10px;">
                <label for="form-type" style="margin-right: 5px;">Type</label>
                <input type="text" class="form-control" id="form-type" />
            </div>
            <button type="submit" class="btn btn-primary" id="save-btn">Save</button>
            <button type="button" class="btn btn-default" id="cancel-btn" style="display: none; margin-left: 5px;">Cancel</button>
        </form>
    </div>

    <script>
        var apiUrl = '/action/devtool/database-properties';

        function showMessage(type, text) {
            var el = type === 'error' ? jQuery('#error-message') : jQuery('#success-message');
            el.text(text).show();
            setTimeout(function() { el.fadeOut(); }, 3000);
        }

        function loadProperties() {
            jQuery.ajax({
                url: apiUrl,
                method: 'GET',
                success: function(data) {
                    var tbody = jQuery('#properties-body');
                    tbody.empty();
                    data.forEach(function(prop) {
                        tbody.append(
                            '<tr>' +
                            '<td>' + (prop.id || '') + '</td>' +
                            '<td>' + escapeHtml(prop.name || '') + '</td>' +
                            '<td>' + escapeHtml(prop.value || '') + '</td>' +
                            '<td>' + escapeHtml(prop.type || '') + '</td>' +
                            '<td>' +
                            '<button class="btn btn-sm btn-info edit-btn" data-id="' + prop.id + '" data-name="' + escapeAttr(prop.name) + '" data-value="' + escapeAttr(prop.value) + '" data-type="' + escapeAttr(prop.type) + '">Edit</button> ' +
                            '<button class="btn btn-sm btn-danger delete-btn" data-id="' + prop.id + '" data-name="' + escapeAttr(prop.name) + '">Delete</button>' +
                            '</td>' +
                            '</tr>'
                        );
                    });
                },
                error: function() {
                    showMessage('error', 'Failed to load properties');
                }
            });
        }

        function escapeHtml(str) {
            return jQuery('<span>').text(str).html();
        }

        function escapeAttr(str) {
            return (str || '').replace(/&/g, '&amp;').replace(/"/g, '&quot;');
        }

        function resetForm() {
            jQuery('#form-id').val('');
            jQuery('#form-name').val('').prop('disabled', false);
            jQuery('#form-value').val('');
            jQuery('#form-type').val('');
            jQuery('#save-btn').text('Save');
            jQuery('#cancel-btn').hide();
        }

        jQuery(document).on('click', '.edit-btn', function() {
            var btn = jQuery(this);
            jQuery('#form-id').val(btn.data('id'));
            jQuery('#form-name').val(btn.data('name'));
            jQuery('#form-value').val(btn.data('value'));
            jQuery('#form-type').val(btn.data('type'));
            jQuery('#save-btn').text('Update');
            jQuery('#cancel-btn').show();
        });

        jQuery(document).on('click', '.delete-btn', function() {
            var btn = jQuery(this);
            var id = btn.data('id');
            var name = btn.data('name');
            if (!confirm('Delete property "' + name + '"?')) {
                return;
            }
            jQuery.ajax({
                url: apiUrl + '/' + id,
                method: 'DELETE',
                success: function() {
                    showMessage('success', 'Deleted');
                    loadProperties();
                },
                error: function() {
                    showMessage('error', 'Failed to delete');
                }
            });
        });

        jQuery('#cancel-btn').on('click', function() {
            resetForm();
        });

        jQuery('#property-form').on('submit', function(e) {
            e.preventDefault();
            var payload = {
                name: jQuery('#form-name').val(),
                value: jQuery('#form-value').val(),
                type: jQuery('#form-type').val()
            };
            var id = jQuery('#form-id').val();
            if (id) {
                payload.id = parseInt(id);
            }
            jQuery.ajax({
                url: apiUrl,
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(payload),
                success: function() {
                    showMessage('success', id ? 'Updated' : 'Created');
                    resetForm();
                    loadProperties();
                },
                error: function() {
                    showMessage('error', 'Failed to save');
                }
            });
        });

        jQuery(document).ready(function() {
            loadProperties();
        });
    </script>
</z:devtoolsPage>

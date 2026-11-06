<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Assemblies">
    <style>
        #assemblies-page .note { background: #fff8e1; border: 1px solid #e0c36a; padding: .5em .75em; margin: .75em 0; max-width: 60em; }
        #assemblies-page table { border-collapse: collapse; margin: 1em 0; }
        #assemblies-page th, #assemblies-page td { border: 1px solid #ccc; padding: .3em .6em; text-align: left; }
        #assemblies-page th { background: #f0f0f0; }
        #assemblies-page td.center { text-align: center; }
        #assemblies-page .status { margin-left: 1em; }
        #assemblies-page .ok { color: #2a7a2a; }
        #assemblies-page .error { color: #b00020; }
    </style>
    <div class="container" id="assemblies-page">
        <h1>Assemblies</h1>
        <div class="note">
            Mark the assemblies that are <b>current</b>. More than one can be current at once. This is the real
            <code>assembly.a_is_current</code> column and takes effect straight away: a gene on a current assembly
            has the annotation status <i>Current</i>. The order is shown for reference and is not editable here.
            <span class="status" id="status"></span>
        </div>
        <table id="assemblies"></table>
    </div>

    <script>
        (function () {
            var base = '/action/devtool/assemblies';

            function say(text, ok) {
                var s = document.getElementById('status');
                s.textContent = text;
                s.className = 'status ' + (ok ? 'ok' : 'error');
                if (ok) { setTimeout(function () { s.textContent = ''; }, 2000); }
            }

            function cell(text, className) {
                var td = document.createElement('td');
                td.textContent = text;
                if (className) { td.className = className; }
                return td;
            }

            function render(assemblies) {
                var table = document.getElementById('assemblies');
                table.innerHTML = '';
                var head = document.createElement('tr');
                ['Assembly', 'GCF identifier', 'Order', 'Current'].forEach(function (h) {
                    var th = document.createElement('th');
                    th.textContent = h;
                    head.appendChild(th);
                });
                table.appendChild(head);
                assemblies.forEach(function (a) {
                    var tr = document.createElement('tr');
                    tr.appendChild(cell(a.name));
                    tr.appendChild(cell(a.gcfIdentifier || ''));
                    tr.appendChild(cell(a.order, 'center'));
                    var box = document.createElement('input');
                    box.type = 'checkbox';
                    box.checked = a.current;
                    box.addEventListener('change', function () { save(a.id, box.checked); });
                    var td = document.createElement('td');
                    td.className = 'center';
                    td.appendChild(box);
                    tr.appendChild(td);
                    table.appendChild(tr);
                });
            }

            function save(id, current) {
                fetch(base + '/current', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({id: id, current: current})
                }).then(function (r) {
                    if (!r.ok) { throw new Error('Save failed: ' + r.status); }
                    return r.json();
                }).then(function (assemblies) {
                    render(assemblies);
                    say('Saved', true);
                }).catch(function (e) {
                    say(e.message, false);
                    load();
                });
            }

            function load() {
                fetch(base + '/data').then(function (r) { return r.json(); }).then(render);
            }

            load();
        })();
    </script>
</z:devtoolsPage>

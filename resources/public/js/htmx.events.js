// HTMX EVAL LOAD
let HTMX_RESOURCES = {};
function hxEvalLoad() {
    document.querySelectorAll('*[hx-load]').forEach($currentNode => {
        let code = $currentNode.getAttribute('hx-load');
        $currentNode.removeAttribute('hx-load');
        eval(code);
    });
}

function mountMDB(node) {
    htmx.findAll(node, '.form-outline').forEach(x => (new mdb.Input(x)));
}

async function hsPost(url, json) {
    const response = await fetch(url, {
        method: 'POST',
        body: json,
        headers: {'Content-Type': 'application/json'}
    });
    const result = await response.json();
    return result;
}

function hsAlert(body) {
    const toastDiv = htmx.find('#errors-toast');
    htmx.find(toastDiv, '.toast-body').innerHTML = body;
    const inst = new mdb.Toast(toastDiv);
    inst.show();
}

// HTMX RESOURCE LOADER
window.addEventListener('load', () => {
    HTMX_RESOURCES.css_arr = Array.from(document.querySelectorAll('link[href]')).map((x) => x.getAttribute('href'));
    HTMX_RESOURCES.js_arr = Array.from(document.querySelectorAll('js[src]')).map((x) => x.getAttribute('src'));
    let safeParseJSON = (v) => v ? JSON.parse(v) : {};
    document.body.addEventListener("htmx:afterOnLoad", () => {
        let scriptsBucketNode = document.getElementById('scripts-bucket');
        let scriptsBucket = safeParseJSON(scriptsBucketNode.getAttribute('scripts'));
        scriptsBucketNode.removeAttribute('scripts');
        let { css, js } = scriptsBucket;
        // add css
        let new_css = (css || []).filter((x) => HTMX_RESOURCES.css_arr.indexOf(x) == -1);
        if (new_css.length > 0) {
            HTMX_RESOURCES.css_arr.push(...new_css);
            new_css.forEach((x) => {
                let l = document.createElement('link');
                l.setAttribute('href', x);
                document.head.appendChild(l);
            });
        }
        // add js
        let js_new = (js || []).filter((x) => HTMX_RESOURCES.js_arr.indexOf(x) == -1);
        if (js_new.length > 0) {
            HTMX_RESOURCES.js_arr.push(...js_new);
            function hxAddJS() {
                if (js_new.length == 0) {
                    hxEvalLoad();
                    return;
                }
                let l = document.createElement('script');
                l.setAttribute('src', js_new.pop());
                l.addEventListener('load', hxAddJS);
                document.head.appendChild(l);
            }
            hxAddJS();
        } else {
            hxEvalLoad();
        }
    });
    hxEvalLoad();
});
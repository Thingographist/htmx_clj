// HTMX EVAL LOAD
let HTMX_RESOURCES = {};
function hxEvalLoad() {
    document.querySelectorAll('*[hx-load]').forEach(x => {
        let code = x.getAttribute('hx-load');
        x.removeAttribute('hx-load');
        eval(code);
    });
}

// HTMX RESOURCE LOADER
window.addEventListener('load', () => {
    HTMX_RESOURCES.css_arr = Array.from(document.querySelectorAll('link[href]')).map((x) => x.getAttribute('href'));
    HTMX_RESOURCES.js_arr = Array.from(document.querySelectorAll('js[src]')).map((x) => x.getAttribute('src'));
    function evalAfterLoad() {
        hxEvalLoad();
        window.removeEventListener('htmx:afterOnLoad', evalAfterLoad);
    }
    document.body.addEventListener("hx-eval-after-load", () => {
        window.addEventListener('htmx:afterOnLoad', evalAfterLoad);
    });
    document.body.addEventListener("hx-add-resources", (evt) => {
        let { css, js } = evt.detail;
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
            window.addEventListener('htmx:afterOnLoad', evalAfterLoad);
        }
    });
    hxEvalLoad();
});
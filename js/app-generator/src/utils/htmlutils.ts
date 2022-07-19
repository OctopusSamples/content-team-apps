// @ts-nocheck

/**
 * Configure Google Analytics.
 *
 * @param tag The Google Analytics tag.
 */
export function setupGoogleAnalytics(tag: string) {
    if (!tag) return;

    addScript("(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':\n" +
        "new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],\n" +
        "j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=\n" +
        "'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);\n" +
        "})(window,document,'script','dataLayer','" + tag + "');\n")
    addNoScript("https://www.googletagmanager.com/ns.html?id=" + tag);

}

function addNoScript(iframeSrc) {
    const s = document.createElement('noscript');
    const iframe = document.createElement('iframe');
    iframe.setAttribute("src", iframeSrc);
    iframe.setAttribute("height", "0");
    iframe.setAttribute("width", "0");
    iframe.setAttribute("style", "display:none;visibility:hidden");
    s.appendChild(iframe);
    document.body.prepend(s);
}

function addScript(src) {
    const s = document.createElement('script');
    s.textContent = src;
    document.head.appendChild(s);
}
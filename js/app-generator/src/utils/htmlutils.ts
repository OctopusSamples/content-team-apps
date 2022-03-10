// @ts-nocheck

/**
 * Configure Google Analytics.
 *
 * @param tag The Google Analytics tag.
 */
export function setupGoogleAnalytics(tag: string) {
    if (!tag) return;

    addScript("https://www.googletagmanager.com/gtag/js?id=" + tag)
    window.dataLayer = window.dataLayer || [];

    function gtag() {
        dataLayer.push(arguments);
    }

    gtag('js', new Date());
    gtag('config', tag);
}

/**
 * Add a script to the page.
 *
 * @param src The script source.
 */
export function addScript(src: string) {
    const s = document.createElement('script');
    s.setAttribute('src', src);
    s.setAttribute('async', 'async')
    document.body.appendChild(s);
}
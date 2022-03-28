/**
 * Represents the UTM params that may be included with each requets.
 */
export interface UtmParams {
    source: string;
    medium: string;
    campaign: string;
    term: string;
    content: string;
}

/**
 * Save any UTMs to local storage.
 */
export function saveUtms() {
    const urlSearchParams = new URLSearchParams(window.location.search);
    const utmParams = {
        source: urlSearchParams.get("utm_source"),
        medium: urlSearchParams.get("utm_medium"),
        campaign: urlSearchParams.get("utm_campaign"),
        term: urlSearchParams.get("utm_term"),
        content: urlSearchParams.get("utm_content")
    }

    // if any utm params were passed in, save the object to local storage.
    let k: keyof typeof utmParams;
    for (k in utmParams) {
        if (utmParams[k]) {
            window.localStorage.setItem("utmParams", JSON.stringify(utmParams));
            return;
        }
    }

    // if no utm params were passed in, leave the existing utm params in local storage alone.
}
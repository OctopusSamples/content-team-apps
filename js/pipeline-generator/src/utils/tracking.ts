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
    window.localStorage.setItem("utmParams", JSON.stringify(utmParams));
}
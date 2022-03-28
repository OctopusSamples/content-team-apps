import {GET_RETRIES} from "./constants";
import {RedirectRule} from "../pages/Branching";
import {getAccessToken} from "./security";
import {UtmParams} from "./tracking";

export function isBranchingEnabled() {
    return (localStorage.getItem("branchingEnabled") || "").toLowerCase() !== "false";
}

export function getBranchingRules() {
    if (isBranchingEnabled()) {
        const rules: RedirectRule[] = JSON.parse(localStorage.getItem("branching") || "[]")
        return rules
            .filter(r => r.path.trim() && r.destination.trim())
            .map(r => "route[" + r.path + "]=" + r.destination).join(";")
    }

    return "";
}

function responseIsError(status: number) {
    return responseIsServerError(status) || responseIsClientError(status);
}

function responseIsServerError(status: number) {
    return status >= 500 && status <= 599;
}

function responseIsClientError(status: number) {
    return status >= 400 && status <= 499;
}

/**
 * Take the UTM values saved in local storage and append them to the supplied URL.
 * @param url The url with any UTM params appended to it.
 * @param utms The utms supplied directly.
 */
export function appendUtms(url: string, utms?: UtmParams): string {
    try {
        const utmParams = utms || JSON.parse(window.localStorage.getItem("utmParams") || "");

        // if any utm params were passed in, append them to the path or url
        let k: keyof typeof utmParams;
        for (k in utmParams) {
            if (utmParams[k]) {
                url += (url.indexOf("?") !== -1 ? "&" : "?") + "utm_" + k + "=" + encodeURIComponent(utmParams[k]);
            }
        }

        return url;
    } catch {
        // If there were no UTMs and the parse failed, return the original URL
        return url;
    }
}

export function getJson<T>(url: string, retryCount?: number): Promise<T> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/json');
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    const updatedUrl = appendUtms(url);

    return fetch(updatedUrl, {
        method: 'GET',
        headers: requestHeaders
    })
        .then(response => {
            if (!responseIsError(response.status)) {
                return response.json();
            }
            if ((retryCount || 0) <= GET_RETRIES) {
                /*
                 Some lambdas are slow, and initial requests timeout with a 504 response.
                 We automatically retry these requests.
                 */
                return getJson<T>(url, (retryCount || 0) + 1);
            }
            return Promise.reject(response);
        });
}

export function getJsonApi<T>(url: string, partition: string | null, retryCount?: number): Promise<T> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    const updatedUrl = appendUtms(url);

    return fetch(updatedUrl, {
        method: 'GET',
        headers: requestHeaders
    })
        .then(response => {
            if (!responseIsError(response.status)) {
                return response.json();
            }
            if ((retryCount || 0) <= GET_RETRIES) {
                /*
                 Some lambdas are slow, and initial requests timeout with a 504 response.
                 We automatically retry these requests.
                 */
                return getJsonApi<T>(url, partition, (retryCount || 0) + 1);
            }
            return Promise.reject(response);
        });
}

export function patchJsonApi<T>(resource: string, url: string, partition: string | null, retryCount?: number): Promise<T> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    const updatedUrl = appendUtms(url);

    return fetch(updatedUrl, {
        method: 'PATCH',
        headers: requestHeaders,
        body: resource
    })
        .then(response => {
            if (!responseIsError(response.status)) {
                return response.json();
            }
            if ((retryCount || 0) <= GET_RETRIES) {
                /*
                 Some lambdas are slow, and initial requests timeout with a 504 response.
                 We automatically retry these requests.
                 */
                return patchJsonApi<T>(resource, url, partition, (retryCount || 0) + 1);
            }
            return Promise.reject(response);
        });
}

export function postJsonApi<T>(resource: string, url: string, partition: string | null): Promise<T> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    const updatedUrl = appendUtms(url);

    return fetch(updatedUrl, {
        method: 'POST',
        headers: requestHeaders,
        body: resource
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            return Promise.reject(response);
        });
}

export function deleteJsonApi(url: string, partition: string | null, retryCount?: number): Promise<Response> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    const updatedUrl = appendUtms(url);

    return fetch(updatedUrl, {
        method: 'DELETE',
        headers: requestHeaders
    })
        .then(response => {
            if (!responseIsError(response.status)) {
                return Promise.resolve(response);
            }
            if ((retryCount || 0) <= GET_RETRIES) {
                /*
                 Some lambdas are slow, and initial requests timeout with a 504 response.
                 We automatically retry these requests.
                 */
                return deleteJsonApi(url, partition, (retryCount || 0) + 1);
            }
            return Promise.reject(response);
        });
}
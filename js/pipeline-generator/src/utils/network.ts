import {GET_RETRIES} from "./constants";
import {RedirectRule} from "../pages/Branching";
import {getAccessToken} from "./security";
import {UtmParams} from "./tracking";

/**
 * Determines if the dumb reverse proxy rules will be used when making requests.
 */
export function isBranchingEnabled() {
    return (localStorage.getItem("branchingEnabled") || "").toLowerCase() === "true";
}

/**
 * Return the routing rules as a string to be included in the "Routing" header.
 */
export function getBranchingRules() {
    if (isBranchingEnabled()) {
        const rules: RedirectRule[] = JSON.parse(localStorage.getItem("branching") || "[]")
        return rules
            .filter(r => r.path.trim() && r.destination.trim())
            .map(r => "route[" + r.path + "]=" + r.destination).join(";")
    }

    return "";
}

/**
 * Determines if the response code indicates an error of some kind.
 * @param status The HTTP status code.
 */
function responseIsError(status: number) {
    return responseIsServerError(status) || responseIsClientError(status);
}

/**
 * Determines if the status code represents a server side error. This is useful when trying to determine if
 * a request should be retried, as server side errors may be resolved over time by the server.
 * @param status The HTTP status code.
 */
function responseIsServerError(status: number) {
    return status >= 500 && status <= 599;
}

/**
 * Determines if the status code represents a client side error. This is useful when trying to determine if
 * a request should be retried, as client side errors usually mean we've sent the wrong data, and a retry won't fix that.
 * @param status The HTTP status code.
 */
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

/**
 * Make an API call that returns plain JSON.
 * @param url The API url.
 * @param retryCount How many times to retry the request.
 */
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

/**
 * Make an API call that returns JSON API formatted data.
 * @param url The API url.
 * @param partition The data partition to make the call in.
 * @param retryCount How many times to retry the request.
 */
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

/**
 * Make an API call that expects and returns JSON API formatted data.
 * @param resource he request body.
 * @param url The API url.
 * @param partition The data partition to make the call in.
 * @param retryCount How many times to retry the request.
 */
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

/**
 * Make an API call that expects and returns JSON API formatted data.
 * @param resource he request body.
 * @param url The API url.
 * @param partition The data partition to make the call in.
 * @param retryCount How many times to retry the request.
 */
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

/**
 * Make an API call that returns JSON API formatted data.
 * @param url The API url.
 * @param partition The data partition to make the call in.
 * @param retryCount How many times to retry the request.
 */
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
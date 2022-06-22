import {RedirectRule} from "../pages/developer/Branching";
import {getAccessToken} from "./security";
import {RuntimeSettings} from "../config/runtimeConfig";

const GET_RETRIES = 5;
const JSON_TYPES = ["application/vnd.api+json", "application/json"];

export function isBranchingEnabled() {
    return (localStorage.getItem("branchingEnabled") || "").toLowerCase() === "true";
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

function responseIsJson(contentType: string | null) {
    if (contentType) {
        // turn "application/vnd.api+json;charset=UTF-8" into "application/vnd.api+json"
        const cleanContentType = contentType.toLowerCase().split(";")[0].trim();
        return JSON_TYPES.indexOf(cleanContentType) !== -1;
    }
    return false;
}

export function getJson<T>(url: string, settings: RuntimeSettings, retryCount?: number, includeCredentials?: boolean): Promise<T> {
    const accessToken = getAccessToken(settings);
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/json');
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
        method: 'GET',
        headers: requestHeaders,
        credentials: includeCredentials ? 'include' : 'omit'
    })
        .then(response => {
            if (!responseIsError(response.status)) {
                if (response.ok) {
                    return responseIsJson(response.headers.get("Content-Type"))
                        ? response.json()
                        : response.text();
                }
            }
            if ((retryCount || 0) <= GET_RETRIES) {
                /*
                 Some lambdas are slow, and initial requests timeout with a 504 response.
                 We automatically retry these requests.
                 */
                return getJson<T>(url, settings, (retryCount || 0) + 1);
            }
            return Promise.reject(response);
        });
}

export function getJsonApi<T>(url: string, settings: RuntimeSettings, partition?: string | null, retryCount?: number, ignoreReturn?: boolean, includeCredentials?: boolean): Promise<T> {
    const accessToken = getAccessToken(settings);
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
        method: 'GET',
        headers: requestHeaders,
        credentials: includeCredentials ? 'include' : 'omit'
    })
        .then(response => {
            if (!responseIsError(response.status)) {
                if (response.ok) {
                    if (responseIsJson(response.headers.get("Content-Type"))) {
                        if (ignoreReturn) {
                            return Promise.resolve({})
                        }

                        return response.json()
                    }

                    return response.text();
                }
            }
            if ((retryCount || 0) <= GET_RETRIES) {
                /*
                 Some lambdas are slow, and initial requests timeout with a 504 response.
                 We automatically retry these requests.
                 */
                return getJsonApi<T>(url, settings, partition, (retryCount || 0) + 1, ignoreReturn);
            }
            return Promise.reject(response);
        });
}

export function patchJsonApi<T>(resource: string, url: string, settings: RuntimeSettings, partition?: string | null, retryCount?: number, ignoreReturn?: boolean, includeCredentials?: boolean): Promise<T> {
    const accessToken = getAccessToken(settings);
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
        method: 'PATCH',
        headers: requestHeaders,
        body: resource,
        credentials: includeCredentials ? 'include' : 'omit'
    })
        .then(response => {
            if (!responseIsError(response.status)) {
                if (response.ok) {
                    if (responseIsJson(response.headers.get("Content-Type"))) {
                        if (ignoreReturn) {
                            return Promise.resolve({})
                        }

                        return response.json()
                    }

                    return response.text();
                }
            }
            if ((retryCount || 0) <= GET_RETRIES) {
                /*
                 Some lambdas are slow, and initial requests timeout with a 504 response.
                 We automatically retry these requests.
                 */
                return patchJsonApi<T>(resource, url, settings, partition, (retryCount || 0) + 1, ignoreReturn, includeCredentials);
            }
            return Promise.reject(response);
        });
}

export function postJsonApi<T>(
    resource: string, url: string,
    settings: RuntimeSettings,
    partition?: string | null,
    ignoreReturn?: boolean | null,
    getHeaders?: (() => Headers) | null,
    retryServerSideErrors?: boolean | null,
    retryCount?: number | null,
    includeCredentials?: boolean): Promise<T> {

    const accessToken = getAccessToken(settings);
    const requestHeaders: HeadersInit = getHeaders ? getHeaders() : new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
        method: 'POST',
        headers: requestHeaders,
        body: resource,
        credentials: includeCredentials ? 'include' : 'omit'
    })
        .then(response => {
            if (response.ok) {
                if (responseIsJson(response.headers.get("Content-Type"))) {
                    if (ignoreReturn) {
                        return Promise.resolve({})
                    }

                    return response.json()
                }

                return response.text();
            }

            // We optionally allow the request to be retried if the server responded with an error
            if (retryServerSideErrors && responseIsServerError(response.status) && (retryCount || 0) <= GET_RETRIES) {
                return postJsonApi(resource, url, settings, partition, ignoreReturn, getHeaders, retryServerSideErrors, (retryCount || 0) + 1, includeCredentials);
            }

            return Promise.reject(response);
        });
}

export function deleteJsonApi(url: string, settings: RuntimeSettings, partition: string | null, retryCount?: number, ignoreReturn?: boolean, includeCredentials?: boolean): Promise<Response> {
    const accessToken = getAccessToken(settings);
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
        method: 'DELETE',
        headers: requestHeaders,
        credentials: includeCredentials ? 'include' : 'omit'
    })
        .then(response => {
            if (!responseIsError(response.status)) {
                if (responseIsJson(response.headers.get("Content-Type"))) {
                    if (ignoreReturn) {
                        return Promise.resolve({})
                    }

                    return response.json()
                }

                return response.text();
            }
            if ((retryCount || 0) <= GET_RETRIES) {
                /*
                 Some lambdas are slow, and initial requests timeout with a 504 response.
                 We automatically retry these requests.
                 */
                return deleteJsonApi(url, settings, partition, (retryCount || 0) + 1, ignoreReturn, includeCredentials);
            }
            return Promise.reject(response);
        });
}
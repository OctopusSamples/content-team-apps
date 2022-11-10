import {RedirectRule} from "../pages/developer/Branching";
import {RuntimeSettings} from "../config/runtimeConfig";

const GET_RETRIES = 5;
const JSON_TYPES = ["application/vnd.api+json", "application/json"];
/**
 * These are some example branching rules that appear in the UI if no other values are defined.
 */
const DEFAULT_BRANCHES = "[{\"id\":1,\"path\":\"/api/products:GET\",\"destination\":\"_url[https://theupstreamserver]\"},{\"id\":2,\"path\":\"/api/products/*:GET\",\"destination\":\"_path[/api/products:GET]\"}]";

/**
 * We want to expose some default rules to make it more obvious in the UI what kind of rules can be defined.
 * Note we treat an empty string or null as the default state, and a string like "[]" as the empty state.
 */
export function getSavedBranchingRules() {
    return localStorage.getItem("branching") || DEFAULT_BRANCHES;
}

export function isBranchingEnabled() {
    return (localStorage.getItem("branchingEnabled") || "").toLowerCase() === "true";
}

export function getBranchingRules() {
    if (isBranchingEnabled()) {
        const rules: RedirectRule[] = JSON.parse(getSavedBranchingRules() || "[]")
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
        return JSON_TYPES.indexOf(contentType.toLowerCase()) !== -1;
    }
    return false;
}

export function getJson<T>(url: string, settings: RuntimeSettings, retryCount?: number): Promise<T> {
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/json');
    requestHeaders.set('Routing', getBranchingRules());

    return fetch(url, {
        method: 'GET',
        headers: requestHeaders,
        credentials: "omit"
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

export function getJsonApi<T>(url: string, partition?: string | null, retryCount?: number, ignoreReturn?: boolean): Promise<T> {
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());

    return fetch(url, {
        method: 'GET',
        headers: requestHeaders,
        credentials: "omit"
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
                  Some lambdas are slow, usually when they first start and wake up a paused serverless database.
                  This means initial requests timeout with a 504 response. We automatically retry these requests.
                 */
                return getJsonApi<T>(url, partition, (retryCount || 0) + 1, ignoreReturn);
            }
            return Promise.reject(response);
        })
        .catch(error => {
            if ((retryCount || 0) <= GET_RETRIES) {
                return getJsonApi<T>(url, partition, (retryCount || 0) + 1, ignoreReturn);
            }
            return Promise.reject(error);
        });
}

export function patchJsonApi<T>(resource: string, url: string, partition?: string | null, retryCount?: number, ignoreReturn?: boolean): Promise<T> {
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());

    return fetch(url, {
        method: 'PATCH',
        headers: requestHeaders,
        body: resource,
        credentials: "omit"
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
                return patchJsonApi<T>(resource, url, partition, (retryCount || 0) + 1, ignoreReturn);
            }
            return Promise.reject(response);
        })
        .catch(error => {
            if ((retryCount || 0) <= GET_RETRIES) {
                return patchJsonApi<T>(resource, url, partition, (retryCount || 0) + 1, ignoreReturn);
            }
            return Promise.reject(error);
        });
}

export function postJsonApi<T>(resource: string, url: string, partition?: string | null, ignoreReturn?: boolean, getHeaders?: () => Headers): Promise<T> {
    const requestHeaders: HeadersInit = getHeaders ? getHeaders() : new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());

    return fetch(url, {
        method: 'POST',
        headers: requestHeaders,
        body: resource,
        credentials: "omit"
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
            return Promise.reject(response);
        });
}

export function deleteJsonApi(url: string, partition: string | null, retryCount?: number, ignoreReturn?: boolean): Promise<Response> {
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());

    return fetch(url, {
        method: 'DELETE',
        headers: requestHeaders,
        credentials: "omit"
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
                return deleteJsonApi(url, partition, (retryCount || 0) + 1, ignoreReturn);
            }
            return Promise.reject(response);
        })
        .catch(error => {
            if ((retryCount || 0) <= GET_RETRIES) {
                return deleteJsonApi(url, partition, (retryCount || 0) + 1, ignoreReturn);
            }
            return Promise.reject(error);
        });
}
import {GET_RETRIES} from "./constants";
import {RedirectRule} from "../pages/Branching";
import {getAccessToken} from "./security";

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

export function getJson<T>(url: string, retryCount?: number): Promise<T> {
    return fetch(url, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        }
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

export function getJsonApi<T>(url: string, partition: string | null, apiKey?: string | null, retryCount?: number): Promise<T> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
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
                return getJsonApi<T>(url, partition, apiKey, (retryCount || 0) + 1);
            }
            return Promise.reject(response);
        });
}

export function patchJsonApi<T>(resource: string, url: string, partition: string | null, apiKey?: string | null, retryCount?: number): Promise<T> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
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
                return patchJsonApi<T>(resource, url, partition, apiKey, (retryCount || 0) + 1);
            }
            return Promise.reject(response);
        });
}

export function postJsonApi<T>(resource: string, url: string, partition: string | null, apiKey?: string | null): Promise<T> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
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

export function deleteJsonApi(url: string, partition: string | null, apiKey?: string | null, retryCount?: number): Promise<Response> {
    const accessToken = getAccessToken();
    const requestHeaders: HeadersInit = new Headers();
    requestHeaders.set('Accept', 'application/vnd.api+json');
    requestHeaders.set('Content-Type', 'application/vnd.api+json');
    requestHeaders.set('Data-Partition', partition || "");
    requestHeaders.set('Routing', getBranchingRules());
    requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');

    return fetch(url, {
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
                return deleteJsonApi(url, partition, apiKey, (retryCount || 0) + 1);
            }
            return Promise.reject(response);
        });
}
export const DEFAULT_BRANCH = "main";

export function getHashField(field: string) {
    // The returned hash from Cognito splits id and access tokens with ampersand
    return window.location.hash
        // drop the leading hash
        .replace("#", "")
        // split on ampersands
        .split("&")
        // The access token starts with this string
        .filter(h => h.startsWith(field))
        // The tokens are name=value
        .map(h => h.split("="))
        // sanity check to make sure we have 2 values
        .filter(h => h.length === 2)
        // get the value
        .map(h => h.pop())
        // there should only be one element
        .pop();
}

/**
 * Get the path from which to load the config.json file.
 */
export function getBaseUrl() {
    try {
        const url = window.location.pathname;
        if (url.endsWith(".html") || url.endsWith(".htm")) {
            return url.substr(0, url.lastIndexOf('/'));
        } else if (url.endsWith("/")) {
            return url.substring(0, url.length - 1);
        }

        return url;
    } catch {
        return "";
    }
}

/**
 * Determines the branch name from the URL.
 */
export function getBranch() {
    const baseUrl = getBaseUrl();
    if (baseUrl === "") {
        return DEFAULT_BRANCH;
    }

    const pathElements = baseUrl.split("/");

    if (pathElements.length > 1) {
        console.log("The branch detection logic assumes the app is using hash based routing rather than paths.")
        console.log("It looks like you have nested paths in the URL, which likely won't provide the correct result.")
    }

    return baseUrl.split("/").pop() || "";
}

/**
 * Converts a branch name into a path. This takes care of the fact that the main branch is on the root path.
 */
export function getBranchPath(branch: string) {
    if (branch === DEFAULT_BRANCH) {
        return "/";
    }

    return "/" + branch + "/";
}

export function setLoginBranch() {
    return window.localStorage.setItem("loginbranch", getBranch());
}

/**
 * Because of the way Cognito works, you can only be redirected back to pages listed in the Cognito pool redirect URLs.
 * This presents an issue, because we want to redirect users back to the feature branch they logged in from.
 *
 * To support directing users back to their branch, the loginbranch local storage item is set to the name of the branch
 * that initiated the login. When users are returned to the app, they will be redirected back to the feature branch
 * based on the loginbranch local storage item value.
 */
export function getLoginBranch() {
    return window.localStorage.getItem("loginbranch");
}

/**
 * loginbranch is a short-lived value, lasting only as long as is required to complete a login.
 */
export function clearLoginBranch() {
    return window.localStorage.setItem("loginbranch", "");
}

export function removeHash () {
    window.history.pushState(
        "",
        document.title,
        window.location.pathname + window.location.search);
}
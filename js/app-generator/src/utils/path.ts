import {RuntimeSettings} from "../config/runtimeConfig";

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
 * Converts a branch name into a path. This takes care of the fact that the main branch is on the root path.
 */
export function getBranchPath(branch: string) {
    if (branch === DEFAULT_BRANCH) {
        return "";
    }

    return branch + "/";
}

export function setLoginBranch(settings: RuntimeSettings) {
    return window.localStorage.setItem("loginbranch", settings.branch);
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

export function setGitHubLoginBranch(settings: RuntimeSettings) {
    window.localStorage.setItem("githubloginbranch", settings.branch);
}

export function getGitHubLoginBranch() {
    return window.localStorage.getItem("githubloginbranch");
}

export function clearGitHubLoginBranch() {
    return window.localStorage.setItem("githubloginbranch", "");
}

export function removeHash () {
    window.history.pushState(
        "",
        document.title,
        window.location.pathname + window.location.search);
}
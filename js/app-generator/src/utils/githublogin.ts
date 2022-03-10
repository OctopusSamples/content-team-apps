import {setGitHubLoginBranch} from "./path";
import {RuntimeSettings} from "../config/runtimeConfig";

/**
 * Initiate a login to GitHub.
 * @param url The repo that triggered the login.
 * @param loginPath The GitHub login path.
 * @return true if the login process succeeded, false if there was an error (like a login loop).
 */
export function logIntoGitHub(loginPath: string, settings: RuntimeSettings) {
    if (window.localStorage.getItem("loginForRepo") === window.localStorage.getItem("url")) {
        return false;
    }

    setGitHubLoginBranch(settings);
    window.localStorage.setItem("loginForRepo", window.localStorage.getItem("url") || "")
    window.location.href = loginPath;
    return true;
}
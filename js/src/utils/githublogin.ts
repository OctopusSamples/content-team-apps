import {setGitHubLoginBranch} from "./path";

/**
 * Initiate a login to GitHub.
 * @param url The repo that triggered the login.
 * @param loginPath The GitHub login path.
 * @return true if the login process succeeded, false if there was an error (like a login loop).
 */
export function logIntoGitHub(loginPath: string) {
    if (window.localStorage.getItem("loginForRepo") === window.localStorage.getItem("url")) {
        return false;
    }

    setGitHubLoginBranch();
    window.localStorage.setItem("loginForRepo", window.localStorage.getItem("url") || "")
    window.location.href = loginPath;
    return true;
}

export function clearGitHubLogin() {
    window.localStorage.setItem("loginForRepo", "")
}
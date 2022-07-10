import {
    clearGitHubLoginBranch,
    clearLoginBranch,
    getBranch,
    getBranchPath,
    getGitHubLoginBranch,
    getHashField,
    getLoginBranch,
    setLoginBranch
} from "./path";
import jwt from 'jsonwebtoken';
import jwkToPem, {JWK} from 'jwk-to-pem';

/**
 * Store the Cognito access token.
 * @param accessToken The access token.
 */
export function setAccessToken(accessToken: string) {
    window.localStorage.setItem(getLoginBranch() + "-accesstoken", accessToken);
}

/**
 * Store the Cognito ID token.
 * @param accessToken The ID token.
 */
export function setIdToken(idToken: string) {
    window.localStorage.setItem(getLoginBranch() + "-idtoken", idToken);
}

/**
 * Store the token expiry time.
 * @param expiry The token expiry time.
 */
export function setTokenExpiry(expiry: string) {
    const expiryInt = parseInt(expiry);
    if (isNaN(expiryInt)) {
        // bad expiry, so we expire in the past
        window.localStorage.setItem(getLoginBranch() + "-tokenexpiry", new Date().getSeconds().toFixed(0));
    } else {
        const now = new Date();
        window.localStorage.setItem(getLoginBranch() + "-tokenexpiry", now.setSeconds(now.getSeconds() + expiryInt).toFixed(0));
    }
}

/**
 * Gets the saved access token, if it is not expired.
 * @param jwk sourced from https://cognito-idp.<region>.amazonaws.com/<pool id>/.well-known/jwks.json
 */
export function getIdToken(jwk: JWK[]) {
    if (!jwk) {
        return "";
    }

    if (isTokenExpired()) {
        return "";
    }

    const idToken = window.localStorage.getItem(getBranch() + "-idtoken") || "";
    if (idToken) {
        const anyValidate = jwk.map(j => {
            try {
                const pem = jwkToPem(j);
                jwt.verify(idToken, pem, {algorithms: ['RS256']});
                return true;
            } catch (err) {
                return false;
            }
        }).find(j => j);
        if (!anyValidate) {
            return "";
        }
    }

    return idToken;
}

/**
 * Gets the saved access token.
 */
export function getAccessToken() {
    if (isTokenExpired()) {
        return "";
    }

    return window.localStorage.getItem(getBranch() + "-accesstoken") || "";
}

/**
 * Clear all tokens (effectively performing a logout).
 */
export function clearTokens() {
    window.localStorage.setItem(getBranch() + "-accesstoken", "");
    window.localStorage.setItem(getBranch() + "-idtoken", "");
    window.localStorage.setItem(getBranch() + "-tokenexpiry", "");
}

/**
 * Initiate a Cognito login.
 * @param cognitoLogin The URL to open to perform the Cognito login.
 */
export function login(cognitoLogin: string) {
    setLoginBranch();
    window.location.href = cognitoLogin;
}

/**
 * Logout of Cognito.
 */
export function logout() {
    clearTokens();
}

/**
 * Return the remaining lifetime of the Cognito tokens.
 */
export function getTokenTimeLeft() {
    const expiry = parseInt(window.localStorage.getItem(getBranch() + "-tokenexpiry") || "");
    if (!isNaN(expiry)) {
        return Math.round((new Date(expiry).getTime() - new Date().getTime()) / 1000 / 60);
    }

    return 0;
}

/**
 * Determine if the Cognito tokens have expired.
 */
function isTokenExpired() {
    // Check the token expiry
    const expiry = parseInt(window.localStorage.getItem(getBranch() + "-tokenexpiry") || "");
    if (isNaN(expiry) || new Date(expiry) < new Date()) {
        return true;
    }

    return false;
}

/**
 * Deal with a redirection from GitHub, handing a second redirect to the original feature branch
 * that initiated the login.
 * @return true if the app should continue loading normally, false if we're redirecting away from this page
 */
export function handleGitHubLogin() {
    try {
        const loginBranch = getGitHubLoginBranch();
        const loginRedirect = new URLSearchParams(window.location.search).get('action') === "loggedin";

        if (loginBranch && loginRedirect) {
            window.location.href = getBranchPath(loginBranch) + window.location.search;
            return false;
        }

        return true;
    } finally {
        clearGitHubLoginBranch();
    }
}

/**
 * Deal with a redirection from Cognito, handing a second redirect to the original feature branch
 * that initiated the login.
 * @return true if the app should continue loading normally, false if we're redirecting away from this page
 */
export function handleCognitoLogin() {
    try {
        /*
         In the event of a login error, clear the hash and open the main page.
         Without this, the React router may try to match the hash, which won't work.
         */
        const error = getHashField("error");

        if (error) {
            window.location.href = "/";
            return false;
        }

        const loginBranch = getLoginBranch();
        const accessToken = getHashField("access_token");
        const idToken = getHashField("id_token");
        const expiry = getHashField("expires_in");

        // Before we redirect to cognito, the login branch must be set. If not, ignore redirect.
        if (!loginBranch) {
            if (!(accessToken && idToken && expiry)) {
                return true;
            } else {
                // There are values in the hash that will cause issues with routing, so go back to the root.
                window.location.href = "/";
                return false;
            }
        }

        if (accessToken && idToken && expiry) {
            setAccessToken(accessToken);
            setIdToken(idToken);
            setTokenExpiry(expiry);
            window.location.href = getBranchPath(loginBranch) + window.location.search;
            return false;
        }

        return true;
    } finally {
        clearLoginBranch();
    }
}
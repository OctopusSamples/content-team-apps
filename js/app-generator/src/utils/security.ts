import {
    clearGitHubLoginBranch,
    clearLoginBranch,
    getBranchPath,
    getGitHubLoginBranch,
    getHashField,
    getLoginBranch,
    setLoginBranch
} from "./path";
import jwt from 'jsonwebtoken';
import jwkToPem, {JWK} from 'jwk-to-pem';
import {JSEncrypt} from "jsencrypt";
import Cookies from 'js-cookie'
import {RuntimeSettings} from "../config/runtimeConfig";

export function setAccessToken(accessToken: string) {
    window.localStorage.setItem(getLoginBranch() + "-accesstoken", accessToken);
}

export function setIdToken(idToken: string) {
    window.localStorage.setItem(getLoginBranch() + "-idtoken", idToken);
}

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
 * Gets the saved access token.
 * @param jwk sourced from https://cognito-idp.<region>.amazonaws.com/<pool id>/.well-known/jwks.json
 */
export function getIdToken(jwk: JWK[], settings: RuntimeSettings) {
    if (!jwk) {
        return "";
    }

    if (isTokenExpired(settings)) {
        return "";
    }

    const idToken = window.localStorage.getItem(settings.branch + "-idtoken") || "";
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
export function getAccessToken(settings: RuntimeSettings) {
    if (isTokenExpired(settings)) {
        return "";
    }

    return window.localStorage.getItem(settings.branch + "-accesstoken") || "";
}

export function clearTokens(settings: RuntimeSettings) {
    window.localStorage.setItem(settings.branch + "-accesstoken", "");
    window.localStorage.setItem(settings.branch + "-idtoken", "");
    window.localStorage.setItem(settings.branch + "-tokenexpiry", "");
}

export function login(cognitoLogin: string, settings: RuntimeSettings) {
    setLoginBranch(settings);
    window.location.href = cognitoLogin;
}

export function logout(settings: RuntimeSettings) {
    clearTokens(settings);
}

export function getTokenTimeLeft(settings: RuntimeSettings) {
    const expiry = parseInt(window.localStorage.getItem(settings.branch + "-tokenexpiry") || "");
    if (!isNaN(expiry)) {
        return Math.round((new Date(expiry).getTime() - new Date().getTime()) / 1000 / 60);
    }

    return 0;
}

function isTokenExpired(settings: RuntimeSettings) {
    // Check the token expiry
    const expiry = parseInt(window.localStorage.getItem(settings.branch + "-tokenexpiry") || "");
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

export async function encryptAndSaveInCookie(value: string, cookie: string, expires: number) {
    const key = await fetch("public_key.pem", {
        method: 'GET'
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            }
            throw new Error("Failed to load the public key");
        });

    const encrypt = new JSEncrypt();
    encrypt.setPublicKey(key);
    const encrypted = encrypt.encrypt(value) || "";
    Cookies.set(cookie, encrypted, {expires});
}

export function loginRequired(settings: RuntimeSettings, partition: string) {
    const testPartitionRequired = (localStorage.getItem("testPartitionRequired") || "").toLowerCase() === "true";
    const accessToken = getAccessToken(settings);

    if (testPartitionRequired) {
        if (!accessToken) {
            return true;
        }

        if (partition === "" || partition === "main") {
            return true;
        }
    }

    return false;
}
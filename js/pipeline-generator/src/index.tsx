// @ts-nocheck

import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {loadConfig} from "./dynamicConfig";
import {handleCognitoLogin, handleGitHubLogin} from "./utils/security";
import {removeHash} from "./utils/path";
import {saveUtms} from "./utils/tracking";

/*
    This is the entry point to the application. It needs to deal with redirection from multiple login sources, like
    GitHub and Cognito. It also sets up analytics, and captures tracking codes like UTMs.
 */

/*
    Save any UTMs that were passed in to local storage. This allows us to retrieve the values even after we redirect
    from external logins.
 */
saveUtms();

/*
    This app must cater for multiple redirections to the main page from authentication platforms that have no support
    for opening the original login page. Cognito is used to log developers in to gain access to feature branches and
    other advanced features, while GitHub is used to grant access to private repos.

    The logic here caters for the various systems that must redirect back to the main page to ensure users are returned
    to the appropriate branch.

    The page flow is this:
    1. Check to see if we were redirected to this page from a Cognito login. Cognito is used to expose developer
       features, but regular users should not log into Cognito.
         a. If so, tokens will be saved and the page will be redirected to the feature branch that initiated the login.
         b. If not, processing continues.
    2. Check to see if we were redirected to this page from a GitHub login. GitHub is used to grant access to private
       repos.
         a. If so, the user is redirected back to the original branch. Tokens are already persisted as cookies.
         b. If not, processing continues.
    2. Remove any hashes from the URL. This is because we don't support bookmarking nested routes.
    3. Load the app config.
    4. Setup analytics.
    5. Load the main react app.
 */
if (handleCognitoLogin()) {
    if (handleGitHubLogin()) {
        /*
            This app does not support bookmarking nested routes. Every time the page is loaded,
            users are expected to visit the front page.
         */
        removeHash();
        loadConfig().then((config) => {
            setupGoogleAnalytics(config.settings?.google?.tag);

            ReactDOM.render(
                <React.StrictMode>
                    <App settings={config.settings}/>
                </React.StrictMode>,
                document.getElementById('root')
            );
        })
    }
}

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

function setupGoogleAnalytics(tag: string) {
    if (!tag) return;

    addScript("(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':\n" +
        "new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],\n" +
        "j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=\n" +
        "'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);\n" +
        "})(window,document,'script','dataLayer','" + tag + "');\n")
    addNoScript("https://www.googletagmanager.com/ns.html?id=" + tag);

}

function addNoScript(iframeSrc) {
    const s = document.createElement('noscript');
    const iframe = document.createElement('iframe');
    iframe.setAttribute("src", iframeSrc);
    iframe.setAttribute("height", "0");
    iframe.setAttribute("width", "0");
    iframe.setAttribute("style", "display:none;visibility:hidden");
    s.appendChild(iframe);
    document.body.prepend(s);
}

function addScript(src) {
    const s = document.createElement('script');
    s.textContent = src;
    document.head.appendChild(s);
}
// @ts-nocheck

import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {loadConfig} from "./dynamicConfig";

loadConfig().then((config) => {
    setupGoogleAnalytics(config.settings?.google?.tag);

    ReactDOM.render(
        <React.StrictMode>
            <App settings={config.settings}/>
        </React.StrictMode>,
        document.getElementById('root')
    );
})

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();


function setupGoogleAnalytics(tag: string) {
    if (!tag) return;

    addScript("https://www.googletagmanager.com/gtag/js?id=" + tag)
    window.dataLayer = window.dataLayer || [];

    function gtag() {
        dataLayer.push(arguments);
    }

    gtag('js', new Date());
    gtag('config', tag);
}

function addScript(src) {
    const s = document.createElement('script');
    s.setAttribute('src', src);
    s.setAttribute('async', 'async')
    document.body.appendChild(s);
}
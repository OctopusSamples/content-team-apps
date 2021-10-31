import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {DynamicConfig} from "./config/dynamicConfig";

const config = loadConfig();

ReactDOM.render(
  <React.StrictMode>
    <App basename={config.basename}/>
  </React.StrictMode>,
  document.getElementById('root')
);

/**
 * We need for this application to work under a variety of subpaths.
 * This function assumes the path that was used to access the index file
 * represents the base path that other pages will be found from.
 */
function loadConfig(): DynamicConfig {
    try {
        const url = window.location.pathname;
        if (url.endsWith(".html") || url.endsWith(".htm")) {
            return {basename: url.substr(0, url.lastIndexOf('/'))}
        } else if (url.endsWith("/")) {
            return {basename: url.substring(0, url.length - 1)}
        }

        return {basename: url}
    } catch {
        // fall through
    }

    return {basename: ""}
}

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

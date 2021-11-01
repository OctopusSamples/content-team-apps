import {DynamicConfig} from "./config/dynamicConfig";

/**
 * We need for this application to work under a variety of subpaths.
 * This function assumes the path that was used to access the index file
 * represents the base path that other pages will be found from.
 */
export async function loadConfig(): Promise<DynamicConfig> {
    const baseUrl = getBaseUrl();

    try {
        const config = await fetch(baseUrl + "/Development/config.json")
            .then(response => response.json())
            .catch(error => "There was a problem with your request.")
        config.settings.basename = baseUrl;
        return config;
    } catch {
        return {settings: {basename: baseUrl, title: "Pipeline Builder"}}
    }
}

function getBaseUrl() {
    try {
        const url = window.location.pathname;
        if (url.endsWith(".html") || url.endsWith(".htm")) {
            return url.substr(0, url.lastIndexOf('/'));
        } else if (url.endsWith("/")) {
            url.substring(0, url.length - 1);
        }

        return url;
    } catch {
       return "";
    }
}
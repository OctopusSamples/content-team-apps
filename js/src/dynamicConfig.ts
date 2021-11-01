import {DynamicConfig} from "./config/dynamicConfig";

/**
 * We need for this application to work under a variety of subpaths.
 * This function assumes the path that was used to access the index file
 * represents the base path that other pages will be found from.
 */
export async function loadConfig(): Promise<DynamicConfig> {
    const baseUrl = getBaseUrl();

    const config = await fetch(baseUrl + "/config.json")
        .then(response => response.ok ? response.json() : null)
        .catch(error => "There was a problem with your request.") ||
        {settings: {basename: baseUrl}};
    config.settings.basename = baseUrl;
    // Set some default values if the config file was not present or not configured
    config.settings.title = config.settings.title || "Pipeline Builder";
    config.settings.generateApiPath = config.settings.generateApiPath || "/api/pipeline/jenkins/generate";
    return config;
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
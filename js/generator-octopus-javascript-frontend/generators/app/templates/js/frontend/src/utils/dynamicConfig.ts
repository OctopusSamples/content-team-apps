import {RuntimeSettings} from "../config/runtimeConfig";

/**
 * We need for this application to work under a variety of subpaths.
 * This function assumes the path that was used to access the index file
 * represents the base path that other pages will be found from.
 */
export async function loadConfig(): Promise<RuntimeSettings> {
    const baseUrl = getBaseUrl();
    const defaultResponse = {settings: {basename: baseUrl}};

    const settings = await fetch(baseUrl + "/config.json")
        .then(response => response.ok ? response.json() : defaultResponse)
        .catch(() => defaultResponse);
    // Set some default values if the config file was not present or not configured
    settings.basename = baseUrl;
    settings.title = settings.title || "Octopub";
    return settings;
}

function getBaseUrl() {
    try {
        const url = window.location.pathname;
        if (url.endsWith(".html") || url.endsWith(".htm")) {
            return url.substr(0, url.lastIndexOf('/'));
        } else if (url.endsWith("/")) {
            return url.substring(0, url.length - 1);
        }

        return url;
    } catch {
       return "";
    }
}
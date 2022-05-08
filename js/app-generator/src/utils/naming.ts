import {StateContext} from "../statemachine/appBuilder";

/**
 * Create an Octopus space name from the selected fields.
 * @param platform The selected platform.
 * @param framework The selected framework.
 * @param gitHubUser The github username.
 */
export function generateSpaceName(platform: string, framework: string, gitHubUser: string): string {
    return (platform + (framework ? " " + framework : "") + " " + gitHubUser).substring(0, 20);
}

export function getOctopusServer(state: StateContext) {
    if (state.octopusServer) {
        try {
            const url = new URL(state.octopusServer);
            return "https://" + url.hostname;
        } catch {
            return "https://" + state.octopusServer.split("/")[0];
        }
    }
    // Let the service return an error in its response code, and handle the response as usual.
    return "";
}
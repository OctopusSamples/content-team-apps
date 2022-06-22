import {postJsonApi} from "./network";
import {RuntimeSettings} from "../config/runtimeConfig";

export function auditPageVisit(page: string, settings: RuntimeSettings, partition?: string | null) {
    postJsonApi(JSON.stringify({
        data: {
            type: "audits",
            attributes: {
                subject: "GitHubRepoFrontend",
                action: "VisitedPage",
                object: page
            }
        }
    }), settings.auditEndpoint, settings, partition);
}
import {postJsonApi} from "./network";
import {RuntimeSettings} from "../config/runtimeConfig";

export function auditPageVisit(page: string, settings: RuntimeSettings, partition?: string | null) {
    postJsonApi(JSON.stringify({
            data: {
                type: "audits",
                attributes: {
                    subject: "GitHubRepoFrontend",
                    action: "VisitedPage",
                    object: page,
                    dataPartition: partition,
                    encryptedSubject: false,
                    encryptedObject: false,
                    time: new Date().getTime()
                }
            }
        }),
        settings.auditEndpoint,
        settings,
        partition,
        true,
        () => {
            const headers = new Headers();
            headers.set('Invocation-Type', 'Event');
            return headers;
        });
}
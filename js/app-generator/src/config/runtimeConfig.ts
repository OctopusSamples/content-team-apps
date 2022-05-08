import {JWK} from "jwk-to-pem";

export interface RuntimeSettings {
    basename: string;
    branch: string;
    disableExternalCalls: boolean;
    disableOctofrontLogin: boolean;
    octofrontOauthEndpoint: string;
    githubOauthEndpoint: string;
    serviceAccountEndpoint: string;
    githubRepoEndpoint: string;
    githubCommitEndpoint: string;
    octopusSpaceEndpoint: string;
    auditEndpoint: string;
    healthEndpoint: string;
    title: string;
    google: {
        tag: string;
    },
    aws: {
        cognitoLogin: string;
        cognitoDeveloperGroup: string;
        jwk: {
            keys: JWK[]
        };
    }
}
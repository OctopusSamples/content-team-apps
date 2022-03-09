import {JWK} from "jwk-to-pem";
import * as H from "history";

export interface RuntimeSettings {
    basename: string;
    octopusOauthEndpoint: string;
    githubOauthEndpoint: string;
    serviceAccountEndpoint: string;
    githubRepoEndpoint: string;
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
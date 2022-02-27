import {JWK} from "jwk-to-pem";
import * as H from "history";

export interface RuntimeSettings {
    basename: string;
    generateApiPath: string;
    auditEndpoint: string;
    healthEndpoint: string;
    title: string;
    google: {
        tag: string;
    },
    github: {
        enableLogin: boolean;
        loginPath: string;
    },
    aws: {
        cognitoLogin: string;
        cognitoDeveloperGroup: string;
        jwk: {
            keys: JWK[]
        };
    }
}
import {JWK} from "jwk-to-pem";
import * as H from "history";

/**
 * Represents the configuration in the config.json file, which is processed by Octopus for each deployment
 * and environment.
 */
export interface DynamicConfig {
    settings: {
        basename: string;
        generateApiPath: string;
        auditEndpoint: string;
        healthEndpoint: string;
        title: string;
        editorFormat: string;
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
    },
    useDefaultTheme?: boolean;
    generateTemplate: (url: string, history: H.History) => void;
    setCopyText: (copyText: string) => void;
    copyText?: string;
    partition: string | null;
    setPartition: (id: string | null) => void;
}
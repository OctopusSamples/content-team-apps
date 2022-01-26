/**
 * Represents the configuration in the config.json file, which is processed by Octopus for each deployment
 * and environment.
 */
export interface DynamicConfig {
    settings: {
        basename: string,
        generateApiPath: string,
        loginPath: string,
        title: string,
        editorFormat: string
        google: {
            tag: string
        }
    },
    useDefaultTheme?: boolean
}
export function enableNpmInstall() : boolean {
    return process.env.ENABLE_NPM_INSTALL?.toLowerCase() === "true";
}
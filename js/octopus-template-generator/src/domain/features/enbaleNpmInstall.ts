/**
 * Defines whether we allow missing generators to be downloaded when they are requested.
 */
export default function enableNpmInstall() : boolean {
    return process.env.ENABLE_NPM_INSTALL?.toLowerCase() === "true";
}
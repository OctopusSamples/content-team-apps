/**
 * Defines whether we allow missing generators to be downloaded when they are requested.
 * Note that allowing any random generator to be installed and run is unsafe, as
 * generators are just JavaScript code with no safegurads. This option should only be
 * used for testing.
 */
export default function enableNpmInstall() : boolean {
    return process.env.UNSAFE_ENABLE_NPM_INSTALL?.toLowerCase() === "true";
}
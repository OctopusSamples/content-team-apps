/**
 * Returns the list of packages from a safelist that are OK to install, even if
 * enableNpmInstall is false.
 */
export default function safeListModules() : string[] {
    return (process.env.NPM_INSTALL_SAFELIST ?? "")
        .split(",")
        .map(m => m.trim());
}
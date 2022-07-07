// Note the current working directory
import process from "node:process";
import os from "os";

// Note the working dir at initialization
const cwd = process.cwd();

/**
 * The default working dir is where things like NPM package downloads will
 * be placed, as well as where any pre-installed generators will be found.
 */
export function getDefaultWorkingDir() {
    return cwd;
}

/**
 * The temp dir is where the process will sit most of the time. This is
 * because Yeoman generators may not respect the cwd set on the Yeoman
 * environment, and we don't want to save random files in the application
 * working directory.
 */
export function getTempDir() {
    return os.tmpdir();
}
// Note the current working directory
import process from "node:process";

// Note the working dir at initialization
const cwd = process.cwd();

/**
 * The default working dir is where things like NPM package downloads will
 * be placed, as well as where any pre-installed generators will be found.
 */
export default function getDefaultWorkingDir() {
    return cwd;
}
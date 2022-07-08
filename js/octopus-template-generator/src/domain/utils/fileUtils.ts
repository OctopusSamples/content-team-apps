import fs from "fs";
import path from "path";
import {getTempDir} from "../features/defaultWorkingDir";

/**
 * A simple wrapper around fs.existsSync() to make testing easier.
 * @param file The file to check.
 */
export function fileExists(file: string) {
    return fs.existsSync(file);
}

/**
 * Create a temporary directory, pass it to the callback, and then clean the directory up.
 * @param dirname The name of the new temp dir
 * @param cb The function to call with the temp dir
 */
export function createTempDir(dirname: string, cb: (dirname: string) => Promise<unknown>) {
    const tempDir = fs.mkdtempSync(path.join(getTempDir(), "template"));
    return cb(tempDir)
        .finally(() => {
            try {
                fs.rmSync(tempDir, {recursive: true});
            } catch (err) {
                console.error('TemplateGenerator-Template-TempDirCleanupFailed: The temporary directory was not removed because' + err)
            }
        });
}
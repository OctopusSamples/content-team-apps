import fs from "fs";

/**
 * A simple wrapper around fs.existsSync() to make testing easier.
 * @param file The file to check.
 */
export default function fileExists(file: string) {
    return fs.existsSync(file);
}
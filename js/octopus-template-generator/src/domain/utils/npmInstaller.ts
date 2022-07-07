import getDownloadPath from "./downloadPaths";
import process from "node:process";
import {exec, ExecException} from "child_process";
import splitGeneratorName from "./generatorSplitter";

export default function installNpmPackage(generator: string) {
    const generatorId = splitGeneratorName(generator);
    const downloadPath = getDownloadPath(generatorId);
    console.log("Attempting to run npm install --prefix " + downloadPath + " --no-save " + generatorId.namespaceNameAndVersion + " in " + process.cwd());
    return new Promise((resolve, reject) => {
        /*
            Place any newly download generators into a new directory called downloaded.
         */
        exec(
            "npm install --prefix " + downloadPath + " --no-save " + generatorId.namespaceNameAndVersion,
            {},
            (error: ExecException | null, stdout: string | Buffer, stderr: string | Buffer) => {
                if (error) {
                    return reject(error);
                }

                return resolve(downloadPath);
            });
    });
}
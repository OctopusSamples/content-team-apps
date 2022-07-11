import GeneratorId from "../entities/generatorId";
import {getDefaultWorkingDir} from "../features/defaultWorkingDir";
import path from "path";

/**
 * Build a version specific path that hosts a generator package.
 * @param generatorId The npm package to download.
 */
export default function getDownloadPath(generatorId: GeneratorId) {
    const subPaths = generatorId?.version
        ? ["downloaded", generatorId.version]
        : ["downloaded", "latest"];

    return path.join(getDefaultWorkingDir(), ...subPaths);
}
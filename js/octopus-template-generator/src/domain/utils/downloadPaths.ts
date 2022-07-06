import GeneratorId from "../entities/generatorId";

/**
 * Build a version specific path that hosts a generator package.
 * @param generatorId The npm package to download.
 */
export default function getDownloadPath(generatorId: GeneratorId) {
    return generatorId?.version
        ? "downloaded/" + generatorId.version
        : "downloaded";
}
/**
 * Resolve the Yeoman generator, and optionally try to install it if it doesn't exist.
 * @param generator The name of the generator.
 * @param attemptInstall true if we should attempt to install the generator if it doesn't exist. Note the downloading
 * of additional generators is also defined by the enableNpmInstall() feature.
 * @private
 */
import splitGeneratorName from "./generatorSplitter";
import canInstallPackage from "./packageInstaller";
import installNpmPackage from "./npmInstaller";
import GeneratorId from "../entities/generatorId";
import getDownloadPath from "./downloadPaths";
import process from "node:process";
import getDefaultWorkingDir from "../features/defaultWorkingDir";

export default async function resolveGenerator(generator: string, attemptInstall = true): Promise<string> {
    const generatorId = splitGeneratorName(generator);

    try {
        return getSubGenerator(generatorId);
    } catch (e) {
        /*
         If the module was not found, we allow module downloading, and this is the first attempt,
         try downloading the module and return it.
         */
        const failedToFindModule = e.code === "MODULE_NOT_FOUND";

        if (failedToFindModule && canInstallPackage(generatorId.namespaceAndName) && attemptInstall) {
            return installNpmPackage(generator)
                .then(() => resolveGenerator(generator, false));
        }

        throw e;
    }
}

/**
 * Yeoman allows two different directory structures.
 * It’ll look in ./ and in generators/ to register available generators.
 * https://yeoman.io/authoring/index.html
 * @param generatorId The generator id
 * @private
 */
function getSubGenerator(generatorId: GeneratorId) {
    try {
        return require.resolve(
            generatorId.namespaceAndName + "/generators/" + generatorId.subGenerator,
            {paths: [getDownloadPath(generatorId), getDefaultWorkingDir()]});
    } catch (e) {
        /*
            Some generators, like jhipster, don't list the app subgenerator in the
            package.json exports. This leads to ERR_PACKAGE_PATH_NOT_EXPORTED errors.
            Yeoman itself doesn't care about the exports though, so we treat
            ERR_PACKAGE_PATH_NOT_EXPORTED as evidence that the module exists
            and return the path.
         */
        if (e.code === "ERR_PACKAGE_PATH_NOT_EXPORTED") {
            return getDefaultWorkingDir() + "/" + getDownloadPath(generatorId) + "/node_modules/" + generatorId.namespaceAndName + "/generators/" + generatorId.subGenerator;
        }

        console.log(e);
        return getGenerator(generatorId);
    }
}

/**
 * Yeoman allows two different directory structures.
 * It’ll look in ./ and in generators/ to register available generators.
 * https://yeoman.io/authoring/index.html
 * @param generatorId The generator id
 * @private
 */
function getGenerator(generatorId: GeneratorId) {
    try {
        return require.resolve(
            generatorId.namespaceAndName + "/" + generatorId.subGenerator,
            {paths: [getDownloadPath(generatorId), getDefaultWorkingDir()]});
    } catch (e) {
        /*
            Some generators, like jhipster, don't list the app subgenerator in the
            package.json exports. This leads to ERR_PACKAGE_PATH_NOT_EXPORTED errors.
            Yeoman itself doesn't care about the exports though, so we treat
            ERR_PACKAGE_PATH_NOT_EXPORTED as evidence that the module exists
            and return the path.
         */
        if (e.code === "ERR_PACKAGE_PATH_NOT_EXPORTED") {
            return getDefaultWorkingDir() + "/" + getDownloadPath(generatorId) + "/node_modules/" + generatorId.namespaceAndName + "/" + generatorId.subGenerator;
        }

        console.log(e);
        throw e;
    }
}
import splitGeneratorName from "./generatorSplitter";
import canInstallPackage from "./packageInstaller";
import installNpmPackage from "./npmInstaller";
import GeneratorId from "../entities/generatorId";
import getDownloadPath from "./downloadPaths";
import {getDefaultWorkingDir} from "../features/defaultWorkingDir";
import {fileExists} from "./fileUtils";
import path from "path";

/**
 * Resolve the Yeoman generator, and optionally try to install it if it doesn't exist.
 * Where no version is specified (e.g. generator-mytemplate), the local node_module and downloaded/latest directories will be scanned.
 * Where a version is specified (e.g. generator-mytemplate@1.0.0), the downloaded/<version> directory will be scanned.
 * @param generator The name of the generator.
 * @param attemptInstall true if we should attempt to install the generator if it doesn't exist. Note the downloading
 * of additional generators is also defined by the enableNpmInstall() feature.
 * @private
 */
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
    const paths = getPaths(generatorId);

    try {
        return require.resolve(generatorId.namespaceAndName + "/generators/" + generatorId.subGenerator, {paths});
    } catch (e) {
        /*
            Some generators, like jhipster, don't list the app subgenerator in the
            package.json exports. This leads to ERR_PACKAGE_PATH_NOT_EXPORTED errors.
            Yeoman itself doesn't care about the exports though, so we treat
            ERR_PACKAGE_PATH_NOT_EXPORTED as evidence that the module exists
            and return the path.
         */
        if (e.code === "ERR_PACKAGE_PATH_NOT_EXPORTED") {
            const validPath = paths
                .map(p => path.join(p, "node_modules", generatorId.namespaceAndName, "generators", generatorId.subGenerator))
                .find(p => fileExists(p))

            if (validPath) {
                return validPath;
            }
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
    const paths = getPaths(generatorId);

    try {
        return require.resolve(generatorId.namespaceAndName + "/" + generatorId.subGenerator, {paths});
    } catch (e) {
        /*
            Some generators, like jhipster, don't list the app subgenerator in the
            package.json exports. This leads to ERR_PACKAGE_PATH_NOT_EXPORTED errors.
            Yeoman itself doesn't care about the exports though, so we treat
            ERR_PACKAGE_PATH_NOT_EXPORTED as evidence that the module exists
            and return the path.
         */
        if (e.code === "ERR_PACKAGE_PATH_NOT_EXPORTED") {
            const validPath = paths
                .map(p => path.join(p, "node_modules", generatorId.namespaceAndName, generatorId.subGenerator))
                .find(p => fileExists(p))

            if (validPath) {
                return validPath;
            }
        }

        console.log(e);
        throw e;
    }
}

/**
 * Return the list of paths where a module can be found.
 * @param generatorId
 */
function getPaths(generatorId: GeneratorId) {
    // We can always look in the download path, as this path is version specific.
    const paths = [getDownloadPath(generatorId)];

    /*
     If no version has been specified by the client, we treat this as a "server provided version". This means
     any packages bundled with the server, usually because they were included in the package.json
     file, can be used to satisfy the request. The client is agnostic about the version, which
     leaves the server in charge.
     */
    if (!generatorId.version) {
        paths.push(getDefaultWorkingDir());
    }
    return paths;
}
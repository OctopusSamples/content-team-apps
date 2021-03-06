import enableNpmInstall from "../features/enableNpmInstall";
import safeListModules from "../features/safeListModules";
import splitGeneratorName from "./generatorSplitter";

/**
 * Determines if a package can be installed if it isn't found.
 * @param generator The name of the generator
 */
export default function canInstallPackage(
    generator: string,
    myEnableNpmInstall = enableNpmInstall,
    mySafeListModules = safeListModules) {
    const generatorId = splitGeneratorName(generator);
    return myEnableNpmInstall() || mySafeListModules().includes(generatorId.namespaceAndName);
}
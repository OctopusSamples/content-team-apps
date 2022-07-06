/**
 * Represents the Yeoman generator and subgenerator.
 */
export default interface GeneratorId {
    namespaceAndName: string;
    namespaceNameAndVersion: string;
    namespace: string,
    name: string;
    subGenerator: string;
    version: string;
}
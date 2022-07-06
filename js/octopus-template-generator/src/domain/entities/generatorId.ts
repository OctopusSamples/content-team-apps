/**
 * Represents the Yeoman generator and subgenerator.
 */
export default interface GeneratorId {
    namespaceAndName: string;
    namespace: string,
    name: string;
    subGenerator: string;
    version: string;
}
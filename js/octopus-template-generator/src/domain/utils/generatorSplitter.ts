import GeneratorId from "../entities/generatorId";

export default function splitGeneratorName(generator: string): GeneratorId {
    if (!generator) {
        throw new Error("generator can not be empty");
    }

    const splitGenerator = generator.trim().split(":");

    if (splitGenerator.length > 2) {
        throw new Error("generator can only have one colon");
    }

    const namespaceAndName = splitGenerator[0];
    const namespaceAndNameSplit = splitGenerator[0].split("/");
    const namespace = namespaceAndNameSplit.length === 1 ? "" : namespaceAndNameSplit[0];
    const nameAndVersion = namespaceAndNameSplit.length === 1 ? namespaceAndNameSplit[0] : namespaceAndNameSplit[1];
    const nameAndVersionSplit = nameAndVersion.split("@");
    const name = nameAndVersion[0];
    const version = nameAndVersion.length === 1 ? "" : nameAndVersionSplit[1];

    return {
        namespaceAndName: namespaceAndName,
        namespace: namespace,
        name: name,
        subGenerator: splitGenerator[1] || "app",
        version: version
    }
}
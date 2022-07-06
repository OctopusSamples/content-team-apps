import GeneratorId from "../entities/generatorId";

export default function splitGeneratorName(generator: string): GeneratorId {
    if (!generator) {
        throw new Error("generator can not be empty");
    }

    const splitGenerator = generator.trim().split(":");

    if (splitGenerator.length > 2) {
        throw new Error("generator can only have one colon");
    }

    const splitName = splitGenerator[0].split("/");

    return {
        namespaceAndName: splitGenerator[0],
        namespace: splitName.length === 1 ? "" : splitName[0],
        name: splitName.length === 1 ? splitName[0] : splitName[1],
        subGenerator: splitGenerator[1] || "app"
    }
}
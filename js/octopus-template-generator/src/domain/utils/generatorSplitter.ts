import GeneratorId from "../entities/generatorId";

export default function splitGeneratorName(generator: string): GeneratorId {
    if (!generator) {
        throw new Error("generator can not be empty");
    }

    const splitGenerator = generator.trim().split(":");

    if (splitGenerator.length > 2) {
        throw new Error("generator can only have one colon");
    }

    return {
        name: splitGenerator[0],
        subGenerator: splitGenerator[1] || "app"
    }
}
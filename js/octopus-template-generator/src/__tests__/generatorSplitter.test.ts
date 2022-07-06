import assert from "assert";
import splitGeneratorName from "../domain/utils/generatorSplitter";

it('parses npm packages correctly', async () => {
    assert.equal(
        "@octopus-content-team",
        splitGeneratorName("@octopus-content-team/generator-octopus-project@0.1.1").namespace);

    assert.equal(
        "",
        splitGeneratorName("generator-octopus-project@0.1.1").namespace);

    assert.equal(
        "app",
        splitGeneratorName("generator-octopus-project@0.1.1").subGenerator);

    assert.equal(
        "generator-octopus-project",
        splitGeneratorName("@octopus-content-team/generator-octopus-project@0.1.1").name);

    assert.equal(
        "0.1.1",
        splitGeneratorName("@octopus-content-team/generator-octopus-project@0.1.1").version);

    assert.equal(
        "@octopus-content-team/generator-octopus-project",
        splitGeneratorName("@octopus-content-team/generator-octopus-project@0.1.1").namespaceAndName);

    assert.equal(
        "",
        splitGeneratorName("@octopus-content-team/generator-octopus-project").version);

    assert.equal(
        "test",
        splitGeneratorName("@octopus-content-team/generator-octopus-project@0.1.1:test").subGenerator);

    assert.equal(
        "@octopus-content-team/generator-octopus-project@0.1.1",
        splitGeneratorName("@octopus-content-team/generator-octopus-project@0.1.1:test").namespaceNameAndVersion);
});
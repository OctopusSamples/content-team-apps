import assert from "assert";
import canInstallPackage from "../domain/utils/packageInstaller";

it('detects if package can be installed', async () => {
    assert.ok(
        canInstallPackage(
            "@octopus-content-team/generator-octopus-project",
            () => true,
            () => []
        ));

    assert.ok(
        !canInstallPackage(
            "@octopus-content-team/generator-octopus-project",
            () => false,
            () => []
        ));

    assert.ok(
        canInstallPackage(
            "@octopus-content-team/generator-octopus-project",
            () => false,
            () => ["@octopus-content-team/generator-octopus-project"]
        ));


    assert.ok(
        canInstallPackage(
            "@octopus-content-team/generator-octopus-project",
            () => false,
            () => ["something", "@octopus-content-team/generator-octopus-project"]
        ));
});
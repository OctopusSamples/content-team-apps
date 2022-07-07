import getDownloadPath from "../domain/utils/downloadPaths";
import assert from "assert";
import path from "path";
import {getDefaultWorkingDir} from "../domain/features/defaultWorkingDir";

it('parses npm packages correctly', async () => {
    assert.equal(
        path.join(getDefaultWorkingDir(), "downloaded", "1.0.0"),
        getDownloadPath({name: "", namespace: "", namespaceNameAndVersion: "", namespaceAndName: "", subGenerator: "", version: "1.0.0"}));

    assert.equal(
        path.join(getDefaultWorkingDir(), "downloaded"),
        getDownloadPath({name: "", namespace: "", namespaceNameAndVersion: "", namespaceAndName: "", subGenerator: "", version: ""}));
});
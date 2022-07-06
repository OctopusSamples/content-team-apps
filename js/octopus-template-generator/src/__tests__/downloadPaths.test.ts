import getDownloadPath from "../domain/utils/downloadPaths";
import assert from "assert";

it('parses npm packages correctly', async () => {
    assert.equal(
        "downloaded/1.0.0",
        getDownloadPath({name: "", namespace: "", namespaceAndName: "", subGenerator: "", version: "1.0.0"}));

    assert.equal(
        "downloaded",
        getDownloadPath({name: "", namespace: "", namespaceAndName: "", subGenerator: "", version: ""}));
});
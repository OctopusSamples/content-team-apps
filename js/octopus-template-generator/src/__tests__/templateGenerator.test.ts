import assert from "assert";
import {TemplateGenerator} from "../domain/hanlders/templateGenerator";
import * as fileLockProcess from "../domain/utils/fileLockProcess";
import * as fileUtils from "../domain/utils/fileUtils";
import * as generatorExecutor from "../domain/utils/generatorExecutor";
import * as generatorResolver from "../domain/utils/generatorResolver";
import * as npmInstaller from "../domain/utils/npmInstaller";
import {sinon} from "@loopback/testlab";

it('can generate a template with mocked services', async () => {
    /*
        These are all the "services" that interact with external resources like files,
        processes, or the network. They are all faked to remove any external access
        while performing the test.
     */
    sinon.stub(fileLockProcess, 'default').callsFake((...props) => new Promise(resolve => {resolve(props[2]())}));
    sinon.stub(fileUtils, 'fileExists').callsFake((...props) => true);
    sinon.stub(generatorExecutor, 'default').callsFake((...props) => new Promise(resolve => resolve()));
    sinon.stub(generatorResolver, 'default').callsFake((...props) => new Promise(resolve => resolve("fake package")));
    sinon.stub(npmInstaller, 'default').callsFake((...props) => new Promise(resolve => resolve("fake path")));

    const templateGenerator = new TemplateGenerator();

    // Generate the template in a sync operation
    assert.ok(
        (await templateGenerator.generateTemplateSync(
            "generator-test",
            {},
            {},
            [])).endsWith(".zip")
    );

    // Deal with nulls gracefully
    assert.ok(
        (await templateGenerator.generateTemplateSync(
            "generator-test",
            null!,
            null!,
            null!)).endsWith(".zip")
    );

    // Generate the template, then get the resulting zip file.
    assert.ok(
        (await templateGenerator.getTemplate(await templateGenerator.generateTemplate(
            "generator-test",
            {},
            {},
            []))).endsWith(".zip")
    );

    // Deal with nulls gracefully
    assert.ok(
        (await templateGenerator.getTemplate(await templateGenerator.generateTemplate(
            "generator-test",
            null!,
            null!,
            null!))).endsWith(".zip")
    );
});
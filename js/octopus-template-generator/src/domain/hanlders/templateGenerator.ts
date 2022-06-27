import {enableNpmInstall} from "../features/enbaleNpmInstall";

const yeoman = require('yeoman-environment');
const fs = require('fs');
const os = require('os');
const path = require('path');
const AdmZip = require("adm-zip");
const argon2 = require('argon2');
const lockFile = require('lockfile');
const {execSync} = require('child_process')

export class TemplateGenerator {
    constructor() {
    }

    private async getTemplateId(generator: string, options: { [key: string]: string; }): Promise<string> {
        const hash = await argon2.hash(generator + Object.keys(options).sort().map(k => k + options[k]).join(""));
        return new Buffer(hash).toString('base64');
    }

    async getTemplate(id: string): Promise<string> {
        // This is where the template is created
        const zipPath = path.join(os.tmpdir(), id + '.zip');

        // If the template does nopt exist, build it
        if (fs.existsSync(zipPath)) {
            return zipPath;
        }

        return "";
    }

    async generateTemplateSync(generator: string, options: { [key: string]: string; }): Promise<string> {

        // Create a hash based on the generator and the options
        const hash = await this.getTemplateId(generator, options);
        // This is where the template is created
        const zipPath = path.join(os.tmpdir(), hash + '.zip');

        await this.buildNewTemplate(generator, options, zipPath);

        return zipPath;
    }

    async generateTemplate(generator: string, options: { [key: string]: string; },): Promise<string> {

        // Create a hash based on the generator and the options
        const hash = await this.getTemplateId(generator, options);
        // This is where the template is created
        const zipPath = path.join(os.tmpdir(), hash + '.zip');

        // trigger the build, but don't wait for it
        this.buildNewTemplate(generator, options, zipPath)
            .catch(e => console.log(e));

        return hash;
    }

    async buildNewTemplate(generator: string, options: { [key: string]: string; }, zipPath: string) {
        try {
            lockFile.lockSync(zipPath + ".lock");

            // If two requests were queued up, just process one of them
            if (!fs.existsSync(zipPath)) {
                await this.writeTemplate(generator, options, zipPath);
            }

            return zipPath;

        } finally {
            lockFile.unlock(zipPath + ".lock", (err: never) => {
                if (err) {
                    console.error('Failed to unlock the file: ' + err)
                }
            });
        }
    }

    private async writeTemplate(generator: string, options: { [key: string]: string; }, zipPath: string) {
        const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "template"));

        try {
            const env = yeoman.createEnv({cwd: tempDir});
            env.register(this.resolveGenerator(generator + "/generators/app"), 'octopus-generator:app');

            await env.run('octopus-generator:app', options);

            const zip = new AdmZip();
            zip.addLocalFolder(tempDir);
            zip.writeZip(zipPath);
        } finally {
            try {
                fs.rmSync(tempDir, {recursive: true});
            } catch {
                console.error('The temporary directory was not removed.')
            }
        }
    }

    private resolveGenerator(generator: string, attemptInstall = true): string {
        try {
            return require.resolve(generator + "/generators/app")
        } catch (e) {
            /*
             If the module was not found, we allow module downloading, and this is the first attempt,
             try downloading the module and return it.
             */
            if (e.code === "MODULE_NOT_FOUND" && enableNpmInstall() && attemptInstall) {
                execSync("npm install " + generator);
                return this.resolveGenerator(generator, false)
            } else {
                throw e;
            }
        }
    }
}

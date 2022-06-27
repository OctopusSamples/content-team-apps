const yeoman = require('yeoman-environment');
const fs = require('fs');
const os = require('os');
const path = require('path');
const AdmZip = require("adm-zip");
const argon2 = require('argon2');

const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "template"));

export class TemplateGenerator {
  constructor() {}

  private async getTemplateId(generator: string, options: { [key: string]: string; }): Promise<string> {
    return argon2.hash(generator + Object.keys(options).sort().map(k => k + options[k]).join(""));
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

  async generateTemplate(generator: string, options: { [key: string]: string; }, sync = true): Promise<string> {

    // Create a hash based on the generator and the options
    const hash = await this.getTemplateId(generator, options);
    // This is where the template is created
    const zipPath = path.join(os.tmpdir(), hash + '.zip');

    // If the template does nopt exist, build it
    if (!fs.existsSync(zipPath)) {
      if (sync) {
        await this.buildNewTemplate(generator, options, zipPath);
      } else {
        // trigger the build, but don't wait for it
        this.buildNewTemplate(generator, options, zipPath)
            .catch(e => console.log(e));
      }
    }

    return zipPath;
  }

  async buildNewTemplate(generator: string, options: { [key: string]: string; }, zipPath: string) {
    // If two requests were queued up, just process one of them
    if (fs.existsSync(zipPath)) {
      return zipPath;
    }

    try {
      const env = yeoman.createEnv({cwd: tempDir});
      env.register(require.resolve(generator + "/generators/app"), 'octopus-generator:app');

      await env.run('octopus-generator:app', options);

      const zip = new AdmZip();
      zip.addLocalFolder(tempDir);
      zip.writeZip(zipPath);

      return zipPath;

    } finally {
      try {
        fs.rmSync(tempDir, { recursive: true });
      } catch {
        console.error('The temporary directory was not removed.')
      }
    }
  }
}

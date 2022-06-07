import * as crypto from 'crypto';

const yeoman = require('yeoman-environment');
const fs = require('fs');
const os = require('os');
const path = require('path');
const AdmZip = require("adm-zip");

export class TemplateGenerator {
  constructor() {}

  async generateTemplate(generator: string, options: { [key: string]: string; }): Promise<string> {

    const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "template"));

    try {
      const env = yeoman.createEnv({cwd: tempDir});
      env.register(require.resolve(generator + "/generators/app"), 'octopus-generator:app');

      await env.run('octopus-generator:app', options);

      const zipPath = path.join(os.tmpdir(), crypto.randomUUID() + '.zip');
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

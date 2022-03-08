import * as crypto from 'crypto';

const yeoman = require('yeoman-environment');
const fs = require('fs');
const os = require('os');
const path = require('path');
const archiver = require('archiver');

export class TemplateGenerator {
  constructor() {}

  async generateTemplate(generator: string, options: { [key: string]: string; }): Promise<string> {

    const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), crypto.randomUUID()));

    try {
      const env = yeoman.createEnv({cwd: tempDir});
      env.register(require.resolve(generator), 'octopus-generator:app');

      await env.run('octopus-generator:app', options);

      const output = fs.createWriteStream(path.join(os.tmpdir(), crypto.randomUUID(), 'target.zip'));
      const archive = archiver('zip');

      archive.pipe(output)
      archive.directory(tempDir, false);
      archive.finalize();

      return output;

    } finally {
      try {
        fs.rmSync(tempDir, { recursive: true });
      } catch {
        console.error('The temporary directory was not removed.')
      }
    }
  }
}

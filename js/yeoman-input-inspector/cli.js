#!/usr/bin/env node

import yeoman from 'yeoman-environment';
import * as fs from "fs";
import path from "path";
import os from "os";
import LoggingAdapter from "./adapter.js";

const args = process.argv.splice(2);

if (args.length === 0) {
    console.log("Pass the generator name as the first argument, for example:")
    console.log("yeoman-inspector springboot")
    process.exit(1);
}

const env = yeoman.createEnv(
    {cwd: fs.mkdtempSync(path.join(os.tmpdir(), "template"))},
    {},
    new LoggingAdapter());
env.lookup();
env.run(args[0], {skipInstall: true}).catch(() => {});

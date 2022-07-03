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

/*
    Yeoman does not make it easy to inspect a generator to find the inputs it requires.
    Options and arguments are displayed by the "--help" command, but the questions,
    which typically make up the bulk of any generator inputs, are not listed or
    exposed in any convenient way. It is simply expected that the end user will
    run through the questions one by one as they are asked.

    This doesn't help us when trying to incorporate Yeoman into a more automated
    fail-into-the-pit-of-success workflow where many questions many only have one
    acceptable answer, or where all answers need to be passed in an automated fashion.

    So we need to find hooks into the Yeoman workflow that allow us to reliably
    extract the options, arguments, and questions so they can be extracted and used
    in automated workflows.

    This is necessarily a little hacky given the lack of nice API support for this
    process.
 */

const env = yeoman.createEnv(
    {cwd: fs.mkdtempSync(path.join(os.tmpdir(), "template"))},
    {},
    new LoggingAdapter());
env.lookup();

/*
    We can get access to the options and arguments by creating an instance of the
    generator and dumping the private properties "_options" and "_prompts".
 */
const generator = env.create(args[0], [], {initialGenerator: true});
console.log("OPTIONS")
console.log(JSON.stringify(generator._options, null, 2));
console.log("ARGUMENTS")
console.log(JSON.stringify(generator._prompts, null, 2));

/*
    Getting access to the questions is a little trickier. We use the LoggingAdapter
    to get access to the questions, and then exit the application before generating
    any files.
 */
env.run(args[0], {skipInstall: true}).catch(() => {});
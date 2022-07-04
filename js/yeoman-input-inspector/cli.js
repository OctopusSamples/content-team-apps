#!/usr/bin/env node

import yeoman from 'yeoman-environment';
import * as fs from "fs";
import path from "path";
import os from "os";
import LoggingAdapter from "./adapter.js";
import buildAdaptiveCard from "./adaptiveCardBuilder.js";

/*
    Missing files and other errors will kill the node process by default. This is
    undesirable for a long-running web server, so we catch the exception here.
    https://nodejs.org/api/process.html#event-uncaughtexception
 */
process.on('uncaughtException', (err, origin) => {
    console.log(err);
});

const args = process.argv.splice(2);

if (args.length === 0) {
    console.log("Pass the generator name as the first argument, for example:")
    console.log("yeoman-inspector springboot")
    process.exit(1);
}

const generatorName = args[0];

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

const allQuestions = []

function questionsCallBack(questions) {
    const fixedQuestions = Array.isArray(questions) ? questions : [questions];
    fixedQuestions.forEach(q => allQuestions.push(q));
}

function dumpInputs(options, args, questions) {
    /*
        Dump the options, arguments, and questions.
     */
    console.log("OPTIONS");
    console.log(JSON.stringify(options, null, 2));
    console.log("ARGUMENTS");
    console.log(JSON.stringify(args, null, 2));
    console.log("QUESTIONS");
    console.log(JSON.stringify(questions, null, 2));
    console.log("ADAPTIVE CARD EXAMPLE");
    console.log(JSON.stringify(buildAdaptiveCard(allQuestions, generatorName), null, 2));
}

/*
    This tool shouldn't write any files, but if it does for some reason,
    they should be in the temp dir.
 */
const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "template"));
process.chdir(tempDir);

const env = yeoman.createEnv(
    {cwd: tempDir},
    {},
    new LoggingAdapter(questionsCallBack));
env.lookup();

/*
    We don't want to generate the template or write any files. Unfortunately there is
    no such thing as a dry run mode in Yeoman (see https://github.com/yeoman/environment/issues/110),
    so the next best thing we can do that should work generically across templates is
    to effectively disable any runloop queues (which relate to method priorities described
    at https://yeoman.io/authoring/running-context.html#the-run-loop) that don't relate to the prompting
    stage.

    Note some generators don't work without later priorities like "writing". As a fallback for these
    use cases, the ALLOW_FULL_INSTALL environment variable can be set to true to allow the
    generator to perform a full installation.
 */
if ((process.env.ALLOW_FULL_INSTALL || "").trim().toLowerCase() !== "true") {
    Object.keys(env.runLoop.__queues__).forEach(k => {
        if (!(k === "environment:run"
            || k === "initializing"
            || k === "prompting"
            || k === "default")) {
                env.runLoop.__queues__[k].push = function () {}
            }
    });
}

/*
    We can get access to the options and arguments by creating an instance of the
    generator and dumping the private properties "_options" and "_prompts".
 */
const generator = env.create(generatorName, args.splice(1), {'skip-install': true});

/*
    Getting access to the questions is a little trickier. We use the LoggingAdapter
    to get access to the questions.
 */
env.run(generatorName, {})
    .finally(() => {
        dumpInputs(generator._options, generator._arguments, allQuestions);
        try {
            fs.rmSync(tempDir, {recursive: true});
        } catch (err) {
            console.error('The temporary directory was not removed because' + err)
        }
    });


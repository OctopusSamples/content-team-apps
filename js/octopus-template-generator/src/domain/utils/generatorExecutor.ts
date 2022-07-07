import process from "node:process";
import NonInteractiveAdapter from "../yeoman/adapter";
import resolveGenerator from "./generatorResolver";
const yeoman = require('yeoman-environment');

export default async function executeGenerator(
    generator: string,
    tempDir: string,
    fixedArgs: string[],
    fixedOptions: { [key: string]: string },
    fixedAnswers: { [key: string]: string }) {

    /*
        Catch issues like missing files and save the uncaught exception, so we can
        handle it gracefully rather than killing the node process.
     */
    let uncaughtException = null;
    const handleException = (err: Error) => {
        uncaughtException = err;
    };
    process.on('uncaughtException', handleException);

    try {
        const env = yeoman.createEnv({cwd: tempDir}, {}, new NonInteractiveAdapter(fixedAnswers));
        env.register(await resolveGenerator(generator), generator);

        /*
            Not all generators respect the cwd option passed into createEnv. Setting the
            working directory means we keep our application directory clean.
         */
        process.chdir(tempDir);

        /*
         The docs at https://yeoman.io/authoring/integrating-yeoman.html indicate we should set the
         "skip-install" option. This is incorrect, and should be "skipInstall". The loadSharedOptions()
         method on the environment shows the actual options to be passed in.
         */
        await env.run([generator, ...fixedArgs], {...fixedOptions, skipInstall: true});

        // If there were any exceptions, rethrow them so the caller receives the appropriate
        // error code.
        if (uncaughtException) {
            throw uncaughtException;
        }
    } catch (err) {
        console.log(err);
        throw err;
    }
}
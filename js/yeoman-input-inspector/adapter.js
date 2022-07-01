import TerminalAdapter from "yeoman-environment/adapter";

/**
 * An adapter that reads the questions and then exits.
 */
export default class LoggingAdapter extends TerminalAdapter {
    constructor() {
        super();
    }

    /**
     * Dump the questions to the console and exit.
     */
    prompt(questions, answers, cb) {
        console.log("The JSON below represents the list of questions asked by the generator.")
        console.log(JSON.stringify(questions, null, 2));
        process.exit(1);
    }
}
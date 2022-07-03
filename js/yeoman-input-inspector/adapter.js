import TerminalAdapter from "yeoman-environment/adapter";

/**
 * An adapter that reads the questions and then exits. This allows developers to
 * see the questions that they must provide answers to when generating templates
 * non-interactively, without having to actually generate any files.
 */
export default class LoggingAdapter extends TerminalAdapter {
    constructor() {
        super();
    }

    /**
     * Dump the questions to the console and exit.
     */
    prompt(questions, answers, cb) {
        console.log("QUESTIONS")
        console.log(JSON.stringify(questions, null, 2));
        process.exit(1);
    }
}
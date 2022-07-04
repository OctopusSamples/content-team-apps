import TerminalAdapter from "yeoman-environment/adapter";

/**
 * An adapter that reads the questions and then exits. This allows developers to
 * see the questions that they must provide answers to when generating templates
 * non-interactively, without having to actually generate any files.
 */
export default class LoggingAdapter extends TerminalAdapter {
    questionsCb;

    constructor(questionsCb) {
        super();
        this.questionsCb = questionsCb;
    }

    /**
     * Dump the questions to the console and exit.
     */
    prompt(questions, answers, cb) {
        // Capture the questions so we can inspect them later
        this.questionsCb(questions);
        // Defer back to the terminal adapter
        return super.prompt(...arguments);
    }
}
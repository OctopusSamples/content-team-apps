import TerminalAdapter from "yeoman-environment/adapter";

/**
 * An adapter that extracts the questions and answers so other processes, like
 * the adaptive card sample JSON generator, can access them. This allows developers to
 * see the questions that they must provide answers to when generating templates
 * non-interactively.
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
        this.questionsCb(questions, answers);
        // Defer back to the terminal adapter
        return super.prompt(...arguments);
    }
}
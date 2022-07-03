import TerminalAdapter from "yeoman-environment/adapter";
import buildAdaptiveCard from "./adaptiveCardBuilder.js";

/**
 * An adapter that reads the questions and then exits. This allows developers to
 * see the questions that they must provide answers to when generating templates
 * non-interactively, without having to actually generate any files.
 */
export default class LoggingAdapter extends TerminalAdapter {
    generator;

    constructor(generator) {
        super();
        this.generator = generator;
    }

    /**
     * Dump the questions to the console and exit.
     */
    prompt(questions, answers, cb) {
        console.log("QUESTIONS")
        console.log(JSON.stringify(questions, null, 2));
        console.log("ADAPTIVE CARD EXAMPLE")
        console.log(JSON.stringify(buildAdaptiveCard(questions, this.generator), null, 2));
        process.exit(1);
    }
}
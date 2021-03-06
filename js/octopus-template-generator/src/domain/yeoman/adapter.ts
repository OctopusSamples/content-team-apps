/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/ban-ts-comment */

import inquirer, {Answers, PromptModule} from "inquirer";
import {Interface as ReadlineInterface} from "readline";
// @ts-ignore
import log from "yeoman-environment/log";
import diff, {Change} from 'diff';
import PromptState = inquirer.prompts.PromptState;

class DummyPrompt {
    question: any;
    suppliedAnswers: { [key: string]: string; };
    status: PromptState;

    constructor(suppliedAnswers: { [key: string]: string; }, question: any, readLine: ReadlineInterface, answers: Answers) {
        this.status = "answered";
        this.suppliedAnswers = suppliedAnswers || {};
        this.question = question;
    }

    run(): Promise<any> {
        console.log("Question details:");
        console.log(JSON.stringify(this.question));

        const answer = this.suppliedAnswers[this.question.name];

        if (answer !== null && answer !== undefined) {
            return Promise.resolve(this.suppliedAnswers[this.question.name]);
        }

        console.log("TemplateGenerator-GenerateTemplate-MissingAnswer: Answer to \"" + this.question.name +
            "\" was not provided. A default response is provided instead, but you should include an answer to question \""
            + this.question.name + "\" in the request.");

        // checkboxes require an array of responses
        if (this.question.type === "checkbox") {
            return Promise.resolve(this.question.default ?? []);
        }

        // everything else is a string
        return Promise.resolve(this.question.default ?? "");
    }
}

/**
 * An adapter that responds with predetermined answers to questions.
 */
export default class NonInteractiveAdapter {
    promptModule: PromptModule;
    log: any = log({});

    constructor(suppliedAnswers: { [key: string]: string; }) {
        this.promptModule = inquirer.createPromptModule();
        Object.keys(this.promptModule.prompts).forEach((promptName) =>
            this.promptModule.registerPrompt(promptName, DummyPrompt.bind(DummyPrompt, suppliedAnswers)));
    }

    /**
     * Attempt to respond with a predetermined answer.
     */
    prompt(questions: Object | Object[], answers: never, cb: never) {
        return this.promptModule(questions).then(cb || undefined);
    }

    /**
     * Shows a color-based diff of two strings. See https://github.com/yeoman/environment/blob/main/lib/adapter.js
     *
     * @param {string} actual
     * @param {string} expected
     * @param {Array} changes returned by diff.
     */
    diff(actual:string|[], expected:string, changes:Change[]) {

        changes = Array.isArray(actual)
            ? actual
            : diff.diffLines(actual, expected);

        let message = changes.map(string => {
            if (string.added) {
                return 'Added';
            }

            if (string.removed) {
                return 'Removed';
            }

            return string.value;
        }).join('');

        // Legend
        message = '\n' +
            'removed' +
            ' ' +
            'added' +
            '\n\n' +
            message +
            '\n';

        console.log(message);
        return message;
    }
}
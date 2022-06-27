/* eslint-disable  @typescript-eslint/no-explicit-any */

import inquirer, {Answers, PromptModule} from "inquirer";
import {Interface as ReadlineInterface} from "readline";
import PromptState = inquirer.prompts.PromptState;

class DummyPrompt {
    question: any;
    suppliedAnswers: { [key: string]: string; };
    status: PromptState;

    constructor(suppliedAnswers:{ [key: string]: string; }, question: any, readLine: ReadlineInterface, answers: Answers) {
        this.status = "answered";
        this.suppliedAnswers = suppliedAnswers;
        this.question = question;
    }

    run(): Promise<any> {
        if (this.suppliedAnswers[this.question.name]) {
            return Promise.resolve(this.suppliedAnswers[this.question.name]);
        }
        console.log("TemplateGenerator-GenerateTemplate-MissingAnswer: Answer to " + this.question.name + " was not provided");
        return Promise.reject("Answer to " + this.question.name + " was not provided");
    }
}

/**
 * An adapter that responds with predetermined answers to questions.
 */
export class NonInteractiveAdapter {
    promptModule: PromptModule;

    constructor(suppliedAnswers:{ [key: string]: string; }) {
        this.promptModule = inquirer.createPromptModule();
        Object.keys(this.promptModule.prompts).forEach((promptName) =>
            this.promptModule.registerPrompt(promptName, DummyPrompt.bind(DummyPrompt, suppliedAnswers)));
    }

    /**
     * Attempt to respond with a predetermined answer.
     */
    prompt(questions: Object|Object[], answers: never, cb: never) {
        return this.promptModule(questions).then(cb || undefined);
    }

    /**
     * Each time the generator is run, it is done in an empty temporary directory. So there should never be a reason
     * to diff any files.
     */
    diff() {
        console.log("TemplateGenerator-GenerateTemplate-DiffRequested: Was prompted to display a diff")
        throw new Error("Was asked to diff files");
    }

    log() {
        // no op
    }
}
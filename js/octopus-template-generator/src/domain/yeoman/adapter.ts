/**
 * A no-op adapter that fails if any questions are asked, as there is no way to respond from a REST API.
 */
export class NonInteractiveAdapter {
    prompt(questions: Object|Object[], answers: never, cb: never) {
        if (Array.isArray(questions)) {
            questions.forEach(q => console.log("TemplateGenerator-GenerateTemplate-IncompleteOptions: Was prompted for " + JSON.stringify(q)));
        } else {
            console.log("TemplateGenerator-GenerateTemplate-IncompleteOptions: Was prompted for " + JSON.stringify(questions))
        }
        throw new Error("Was prompted for questions");
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
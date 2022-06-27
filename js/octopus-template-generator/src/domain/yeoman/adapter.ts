/**
 * A no-op adapter that fails if any questions are asked, as there is no way to respond from a REST API.
 */
export class NonInteractiveAdapter {
    prompt(questions: Object|Object[], answers: never, cb: never) {
        if (Array.isArray(questions)) {
            questions.forEach(q => console.log("Was prompted for " + q));
        } else {
            console.log("Was prompted for " + questions)
        }
        throw new Error("Was prompted for questions");
    }

    diff() {
        // no op
    }

    log() {
        // no op
    }
}
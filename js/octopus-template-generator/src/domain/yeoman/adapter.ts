export class NonInteractiveAdapter {
    prompt(questions: Object|Object[], answers: never, cb: never) {
        if (Array.isArray(questions)) {
            questions.forEach(q => console.log("Was promted for " + q));
        } else {
            console.log("Was promted for " + questions)
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
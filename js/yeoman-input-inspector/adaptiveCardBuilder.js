import stripAnsi from 'strip-ansi';

/**
 * Converts the questions exposed by a template into an adaptive card template.
 * @param questions The Yeoman generator questions.
 */
export default function buildAdaptiveCard(questions, answers, generator) {
    /*
     The template generator service uses the full npm package name, so we need
     to append the "generator" prefix to the package name, taking into account
     any namespaces.
     */
    const splitGenerator = generator.split("/");
    const fixedGenerator = splitGenerator.length === 1
        ? "generator-" + splitGenerator[0]
        : splitGenerator[0] + "/generator-" + splitGenerator[1];

    const card = {
        "type": "AdaptiveCard",
        "version": "1.0",
        "body": [
            {
                "type": "Input.Text",
                "id": "generator",
                "value": fixedGenerator,
                "isVisible": false
            }
        ],
        "actions": [
            {
                "type": "Action.Execute",
                "title": "< Back",
                "verb": "openCard",
                "data": {
                    "filename": "index.json"
                }
            },
            {
                "type": "Action.Execute",
                "title": "Download",
                "verb": "downloadTemplate"
            }
        ]
    };

    const fixedQuestions = Array.isArray(questions) ? questions : [questions];

    for (const fixedQuestion of fixedQuestions) {
        card.body.push({
            "type": "TextBlock",
            "text": stripAnsi(fixedQuestion["message"]),
            "wrap": true
        });

        if (fixedQuestion["type"] === "number") {
            const input = {
                type: "Input.Text",
                id: "answer.number." + fixedQuestion["name"],
                value: fixedQuestion["default"],
            };

            card.body.push(input);
        } else if (fixedQuestion["type"] === "confirm") {
            const input = {
                type: "Input.Toggle",
                id: "answer.boolean." + fixedQuestion["name"],
                value: fixedQuestion["default"],
            };

            card.body.push(input);
        } else if (fixedQuestion["type"] === "expand") {
            const input = {
                type: "Input.Text",
                id: "answer.char." + fixedQuestion["name"],
                value: fixedQuestion["default"],
            };

            card.body.push(input);
        } else if (fixedQuestion["type"] === "checkbox") {
            const choiceSet = {
                type: "Input.ChoiceSet",
                id: "answer.list." + fixedQuestion["name"],
                isMultiSelect: true,
                value: getChoices(fixedQuestion["choices"], answers)
                    .filter(c => !!c)
                    .filter(c => c["checked"])
                    .map(c => {
                        if (typeof c === 'string' || c instanceof String) {
                            return c;
                        }
                        return c["value"];
                    })
                    .filter(c => !!c)
                    .join(","),
                choices: (getChoices(fixedQuestion["choices"], answers))
                    .filter(c => !!c)
                    .map(c => {
                        if (typeof c === 'string' || c instanceof String) {
                            return {
                                title: c,
                                value: c
                            }
                        }

                        return {
                            title: c["name"],
                            value: c["value"]
                        }
                    })
                    .filter(c => c.title && c.value)
            };

            card.body.push(choiceSet);
        } else if (fixedQuestion["type"] === "list" || fixedQuestion["type"] === "rawlist") {
            const choiceSet = {
                type: "Input.ChoiceSet",
                id: "answer.string." + fixedQuestion["name"],
                isMultiSelect: false,
                /*
                 The default value from yeoman is the choice name, whereas adaptive cards
                 requires the default value. So we have to so a transformation between
                 the two.
                 */
                value: getChoices(fixedQuestion["choices"], answers)
                    .filter(c => !!c)
                    .map(c => {
                        if (typeof c === 'string' || c instanceof String) {
                            return {
                                title: c,
                                value: c
                            }
                        }

                        return {
                            title: c["name"],
                            value: c["value"]
                        }
                    })
                    .filter(c => c.title && c.value)
                    .filter(c => c.title === fixedQuestion["default"] || c.value === fixedQuestion["default"])
                    .map(c => c.value)
                    .pop(),
                choices: (getChoices(fixedQuestion["choices"], answers))
                    .filter(c => !!c)
                    .map(c => {
                        if (typeof c === 'string' || c instanceof String) {
                            return {
                                title: c,
                                value: c
                            }
                        }

                        return {
                            title: c["name"],
                            value: c["value"]
                        }
                    })
                    .filter(c => c.title && c.value)
            };

            card.body.push(choiceSet);
        } else {
            const input = {
                type: "Input.Text",
                id: "answer.string." + fixedQuestion["name"],
                value: fixedQuestion["default"],
            };

            card.body.push(input);
        }
    }

    return card;
}

function getChoices(choices, answers) {
    const fixedChoices = choices || [];

    if (typeof choices === 'function') {
        try {
            return choices(answers);
        } catch (err) {
            console.log(err);
            return [];
        }
    }

    return fixedChoices;
}
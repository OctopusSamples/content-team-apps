import stripAnsi from 'strip-ansi';

/**
 * Converts the questions exposed by a template into an adaptive card template.
 * @param questions The Yeoman generator questions.
 */
export default function buildAdaptiveCard(questions, generator) {
    const card = {
        "type": "AdaptiveCard",
        "version": "1.0",
        "body": [
            {
                "type": "Input.Text",
                "id": "generator",
                "value": "generator-" + generator,
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
            "text": stripAnsi(fixedQuestion["message"])
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
                value: getChoices(fixedQuestion["choices"])
                    .filter(c => c["checked"])
                    .map(c => {
                        if (typeof c === 'string' || c instanceof String) {
                            return c;
                        }
                        return c["value"];
                    })
                    .join(","),
                choices: (getChoices(fixedQuestion["choices"])).map(c => {
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
                value: getChoices(fixedQuestion["choices"])
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
                    .filter(c => c.title === fixedQuestion["default"] || c.value === fixedQuestion["default"])
                    .map(c => c.value)
                    .pop(),
                choices: (getChoices(fixedQuestion["choices"])).map(c => {
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

function getChoices(choices) {
    const fixedChoices = choices || [];

    if (typeof choices === 'function' ) {
        try {
            return choices();
        } catch {
            // There is not much we can do here
            return [];
        }
    }

    return fixedChoices;
}
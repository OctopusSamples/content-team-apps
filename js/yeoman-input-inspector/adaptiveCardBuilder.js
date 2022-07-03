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
            "text": fixedQuestion["message"]
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
                type: "Input.Text",
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
                value: fixedQuestion["default"],
                choices: (fixedQuestion["choices"] || []).map(c => {
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
                value: fixedQuestion["default"],
                choices: (fixedQuestion["choices"] || []).map(c => {
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
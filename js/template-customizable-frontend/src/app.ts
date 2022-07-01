// import the module
import * as AdaptiveCards from "adaptivecards";
import {ExecuteAction, OpenUrlAction, parseBool, parseNumber} from "adaptivecards";
import * as download from "downloadjs";
import pRetry from 'p-retry';

/**
 * This is the verb used by ExecuteAction buttons to generate and download the template.
 */
const DOWNLOAD_TEMPLATE = "downloadTemplate";
/**
 * This is the verb used by ExecuteAction buttons to load a new card.
 */
const OPEN_CARD = "openCard";

// Create an AdaptiveCard instance
const adaptiveCard = new AdaptiveCards.AdaptiveCard();

// Set its hostConfig property unless you want to use the default Host Config
// Host Config defines the style and behavior of a card
adaptiveCard.hostConfig = new AdaptiveCards.HostConfig({
    fontFamily: "Segoe UI, Helvetica Neue, sans-serif"
    // More host config options
});

/*
    This is where we respond to actions from the cards. The specialized logic regards
    building templates and navigating to different cards is defined here.
 */
adaptiveCard.onExecuteAction = (action) => {
    if (action instanceof OpenUrlAction) {
        window.open(action.url, "_blank");
    }

    if (action instanceof ExecuteAction) {
        if (action.verb == DOWNLOAD_TEMPLATE) {
            downloadTemplate(action);
        }

        if (action.verb == OPEN_CARD) {
            loadCard(action.data["filename"]);
        }
    }
}

/**
 * Displays a loading screen that blocks interaction with the main UI.
 */
function displaySpinner() {
    document.getElementById("spinner").style.display = "block";
}

/**
 * Removes the loading screen that blocks interaction with the main UI.
 */
function hideSpinner() {
    document.getElementById("spinner").style.display = "none";
}

/**
 * Loads the application configuration.
 */
function loadConfig(): Promise<any> {
    return fetch("config.json")
        .then(data => data.json());
}

/**
 * Generate and download a template.
 * @param action The action that triggered the download.
 */
function downloadTemplate(action: ExecuteAction) {
    displaySpinner();
    const request = generateTemplateBody(action.data);

    loadConfig().then(config => {
        /*
            We start by requesting the template be built. The actual process of building
            the template is done asynchronously, and what is returned here is the ID
            of the template that we can eventually download.
         */
        fetch(config.templateGeneratorHost + "/api/template", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/vnd.api+json'
                },
                body: JSON.stringify(request)
            }
        )
            .then(data => data.json())
            /*
                We then poll the template endpoint with the ID from the previous API call. We expect
                to receive a few 404 errors as the template is built, but eventually the template
                is available for download.
             */
            .then((data: any) => pRetry(async () => {
                const response = await fetch(config.templateGeneratorHost + '/download/template/' + data.data.id);

                if (response.status === 404) {
                    throw new Error(response.statusText);
                }

                return response.blob();
            }, {retries: 20, minTimeout: 3000}))
            // This is where we trigger the download in the browser
            .then(blob => {
                console.log(blob);
                download(blob, "template.zip", "application/zip")
            })
            .catch(err => console.log(err))
            // The last step is to remove the loading screen
            .finally(() => {
                hideSpinner();
            })
    })
    .catch(err => console.log(err));
}

/**
 * Generate the API body that is sent to the template generator. It is here that the
 * conventions around element ids is used to map to template specific values like the
 * generator name, options, and questions.
 * @param data The data associated with a execute action.
 */
function generateTemplateBody(data: object) {
    /*
        This is the skeleton of the request body used when generating templates.
        Note the template generator uses the JSON API (https://jsonapi.org/)
        specification.
     */
    const generateTemplate = {
        "data": {
            "type": "generatetemplate",
            "attributes": {
                generator: "",
                options: {},
                questions: {}
            }
        }
    }

    // The name of the generator is always from the "generator" field
    generateTemplate.data.attributes.generator = data["generator"];

    // Options all start with "option.whatever"
    Object.keys(data)
        .filter(k => /^option\./.test(k))
        .forEach(k => generateTemplate.data.attributes.options[k.replace(/^option\./, "")] = data[k]);

    // Answers all start with "question.type.whatever"
    // Here we save the plain string answers
    Object.keys(data)
        .filter(k => /^question\.string/.test(k))
        .forEach(k => generateTemplate.data.attributes.questions[k.replace(/^question\.string\./, "")] = data[k]);

    // Here we split strings on a comma to generate arrays
    Object.keys(data)
        .filter(k => /^question\.list/.test(k))
        .forEach(k => generateTemplate.data.attributes.questions[k.replace(/^question\.list\./, "")] = data[k].split(","));

    // Here we parse a boolean value
    Object.keys(data)
        .filter(k => /^question\.boolean/.test(k))
        .forEach(k => generateTemplate.data.attributes.questions[k.replace(/^question\.boolean\./, "")] = parseBool(data[k]));

    // Here we parse a number value
    Object.keys(data)
        .filter(k => /^question\.number/.test(k))
        .forEach(k => generateTemplate.data.attributes.questions[k.replace(/^question\.number\./, "")] = parseNumber(data[k]));

    return generateTemplate;
}

/**
 * Load and display a new card.
 * @param filename The JSON file to load the card from.
 */
function loadCard(filename) {
    fetch("cards/" + filename)
        .then(data => data.json())
        .then(data => {
            // Parse the card payload
            adaptiveCard.parse(data);

            // Render the card to an HTML element:
            const renderedCard = adaptiveCard.render();

            const adaptiveCardParent = document.getElementById("content");

            // Clear any existing elements
            adaptiveCardParent.innerHTML = "";

            // And finally insert it somewhere in your page:
            adaptiveCardParent.appendChild(renderedCard);
        })
        .catch(err => console.log(err));
}

// We start the app by loading the card defined in index.json
loadCard("index.json");




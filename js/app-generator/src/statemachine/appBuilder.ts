/*
    This page defines the wizard style interface users interact with to define the application they wish to construct.

    The path users can take is defined as a state machine. Each state has an accompanying React component that is
    displayed.

    The components are located in the src/components/journey directory.
 */

import {AnyEventObject, InterpreterFrom} from "xstate/lib/types";
import {FC} from "react";
import {assign, createMachine} from "xstate";
import DoYouHaveCloudOctopus from "../components/journey/DoYouHaveCloudOctopus";
import TargetSelection from "../components/journey/TargetSelection";
import SignUpForCloudOctopus from "../components/journey/SignUpForCloudOctopus";
import LogIntoOctopus from "../components/journey/LogIntoOctopus";
import LoggedIntoOctopus from "../components/journey/LoggedIntoOctopus";
import LogIntoGitHub from "../components/journey/LogIntoGitHub";
import LoggedIntoGithub from "../components/journey/LoggedIntoGitHub";
import PushPackage from "../components/journey/PushPackage";
import Done from "../components/journey/Done";
import EnterAwsCredentials from "../components/journey/EnterAwsCredentials";
import Error from "../components/journey/Error";
import EnterOctopusCredentials from "../components/journey/EnterOctopusCredentials";
import {RuntimeSettings} from "../config/runtimeConfig";
import {auditPageVisit} from "../utils/audit";
import {loginRequired} from "../utils/security";
import Welcome from "../components/journey/Welcome";
import Cookies from "js-cookie";

/**
 * Return the state context from local storage.
 */
function getInitialStateContext() {
    const stateString = localStorage.getItem("appBuilderStateContext");
    if (stateString) {
        const state = JSON.parse(stateString);
        return {
            form: TargetSelection,              // This doesn't matter too much, as the state entry functions override it anyway.
            standAlone: !!state.standAlone,
            awsAccessKey: state.awsAccessKey || "",
            awsRegion: state.awsRegion || "",
            targetPlatform: state.targetPlatform || "",
            generator: state.generator || "",
            // We may go so far as to offer different framework examples, but for now this is always blank.
            developmentFramework: "",
            // developmentFramework: state.developmentFramework || "",
            octopusServer: state.octopusServer || "",
            githubRepo: state.githubRepo || "",
            browsableRepoUrl: state.browsableRepoUrl || "",
            apiRepoUrl: state.apiRepoUrl || "",
            owner: state.owner || "",
            repoName: state.repoName || "",
        }
    }

    return {
        form: TargetSelection,
        standAlone: false,
        awsAccessKey: "",
        awsRegion: "",
        targetPlatform: "",
        generator: "",
        developmentFramework: "",
        octopusServer: "",
        githubRepo: "",
        browsableRepoUrl: "",
        apiRepoUrl: "",
        owner: "",
        repoName: ""
    }
}

const VALID_STATES = [
    "welcome",
    "selectTarget",
    "selectedTargetNotAvailable",
    "doYouHaveCloudOctopus",
    "signUpForCloudOctopus",
    "logIntoOctopus",
    "enterOctopusCredentials",
    "loggedIntoOctopus",
    "logIntoGitHub",
    "loggedIntoGithub",
    "enterAwsCredentials",
    "pushPackage",
    "done",
    "error"];

/**
 * The name of the cookie containing the encrypted GitHub session token.
 */
const GITHUB_SESSION_TOKEN = "GitHubUserSession";

/**
 * Return the name of the state that we should start the form at.
 */
function getInitialState(settings: RuntimeSettings, partition: string) {

    /*
        If the user has selected the requirement to be logged in (usually because they
        are testing the app), and they are not logged in, drop back to the initial
        state. This prevents the stats from being skewed by tester opening the wizard
        at their last location.
     */
    if (loginRequired(settings, partition)) {
        return "welcome";
    }

    const initialState = localStorage.getItem("appBuilderState") || "";
    if (VALID_STATES.indexOf(initialState) !== -1) {
        const fixedState = checkForValidGitHubLogin(initialState);
        return fixedState;
    }

    return "welcome";
}

/**
 * The app sets the state machine state to return to "loggedIntoGithub" after leaving the
 * "logIntoGitHub" state. But it is only valid to enter the "loggedIntoGithub" state if
 * the user actually completed the GitHub login.
 *
 * So here we check to see if we are trying to enter the "loggedIntoGithub" state, and
 * only allow it if we have a valid session token.
 */
function checkForValidGitHubLogin(state: string): string {
    if (state === "loggedIntoGithub" && !Cookies.get(GITHUB_SESSION_TOKEN)) {
        // We don't have a valid token, so return to the login state
        return "logIntoGitHub"
    }

    return state;
}

/**
 * Persist the state machine context to local storage. This allows the end user to reload the app and
 * drop back into the form at any point.
 *
 * @param stateName The name of the current state.
 */
export function saveCurrentState(stateName: string) {
    return (context: StateContext, event: AnyEventObject) => {
        if (event.type !== "xstate.init") {
            localStorage.setItem("appBuilderStateContext", JSON.stringify({...context, form: null}))
            localStorage.setItem("appBuilderState", stateName);
        }
    }
}

function auditState(stateName: string, settings: RuntimeSettings, partition: string) {
    return (context: StateContext, event: AnyEventObject) => {
        /*
         Don't audit page loads under the main partition for test users.
         So check the status of the login before auditing.
         */
        if (!loginRequired(settings, partition)) {
            auditPageVisit(stateName, settings, partition);
        }
    }
}

/**
 * Assign a function component to the state context.
 * @param component The component to save to the state context.
 */
function assignForm(component: FC<JourneyProps>) {
    return assign<StateContext>({
        form: (context: StateContext, event: AnyEventObject) => component
    })
}

/**
 * Reset the persisted state context.
 */
export function startAtBeginning() {
    localStorage.setItem("appBuilderState", "");
}

/**
 * The properties associated with each journey component.
 */
export interface JourneyProps {
    machine: InterpreterFrom<typeof appBuilderMachine>
}

/**
 * The context associated with each state.
 */
export interface StateContext {
    /**
     * True if the user has selected to build a standalone application with no CI/CD configuration.
     */
    standAlone: boolean,
    /**
     * The React component displayed with the current state.
     */
    form: FC<JourneyProps> | null,
    /**
     * The AWS access key
     */
    awsAccessKey: string,
    /**
     * The AWS region
     */
    awsRegion: string,
    /**
     * The target platform to deploy to
     */
    targetPlatform: string,
    /**
     * The generator associated with the platform
     */
    generator: string,
    /**
     * The development framework
     */
    developmentFramework: string,
    /**
     * The Octopus Server we will be populating
     */
    octopusServer: string,
    /**
     * The github repository that was just populated
     */
    githubRepo: string,
    /**
     * The location of the repo created by the backend service
     */
    browsableRepoUrl: string,
    /**
     * The API location of the repo created by the backend service
     */
    apiRepoUrl: string,
    /**
     * The name of the GitHub repo owner
     */
    owner: string,
    /**
     * The name of the github repo
     */
    repoName: string
}

/**
 * Defines the condition where the user has selected to construct a standalone application.
 */
const isStandalone = (context: StateContext, event: AnyEventObject) => {
    return context.standAlone;
};

/**
 * Defines the condition where the user has selected to construct, build, and deploy an application with a CI tool
 * and Octopus.
 */
const isNotStandalone = (context: StateContext, event: AnyEventObject) => {
    return !isStandalone(context, event)
};

/**
 * The state machine defining the journey.
 */
export const appBuilderMachine = (settings: RuntimeSettings, partition: string) => createMachine<StateContext>({
            id: 'appBuilder',
            context: getInitialStateContext(),
            initial: getInitialState(settings, partition),
            states: {
                welcome: {
                    on: {
                        NEXT: {target: 'selectTarget'}
                    },
                    entry: [
                        auditState("welcome", settings, partition),
                        saveCurrentState("welcome"),
                        assignForm(Welcome)
                    ]
                },
                selectTarget: {
                    on: {
                        ECS: {target: 'doYouHaveCloudOctopus'},
                        EKS: {target: 'doYouHaveCloudOctopus'},
                        LAMBDA: {target: 'doYouHaveCloudOctopus'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("selectTarget", settings, partition),
                        saveCurrentState("selectTarget"),
                        assignForm(TargetSelection)
                    ]
                },
                selectedTargetNotAvailable: {
                    on: {
                        BACK: {target: 'selectTarget'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("selectedTargetNotAvailable", settings, partition),
                        saveCurrentState("selectedTargetNotAvailable"),
                    ]
                },
                doYouHaveCloudOctopus: {
                    on: {
                        YES: {target: 'logIntoOctopus'},
                        NO: {target: 'signUpForCloudOctopus'},
                        BACK: {target: 'selectTarget'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("doYouHaveCloudOctopus", settings, partition),
                        saveCurrentState("doYouHaveCloudOctopus"),
                        assignForm(DoYouHaveCloudOctopus)
                    ]
                },
                signUpForCloudOctopus: {
                    on: {
                        NEXT: {target: 'logIntoOctopus'},
                        BACK: {target: 'doYouHaveCloudOctopus'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("signUpForCloudOctopus", settings, partition),
                        saveCurrentState("signUpForCloudOctopus"),
                        assignForm(SignUpForCloudOctopus)
                    ]
                },
                logIntoOctopus: {
                    on: {
                        BACK: {target: 'doYouHaveCloudOctopus'},
                        APIKEY: {target: 'enterOctopusCredentials'},
                        MOCK: {target: 'loggedIntoOctopus'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("logIntoOctopus", settings, partition),
                        saveCurrentState("logIntoOctopus"),
                        assignForm(LogIntoOctopus)
                    ]
                },
                enterOctopusCredentials: {
                    on: {
                        BACK: {target: 'doYouHaveCloudOctopus'},
                        NEXT: {target: 'logIntoGitHub'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("enterOctopusCredentials", settings, partition),
                        saveCurrentState("enterOctopusCredentials"),
                        assignForm(EnterOctopusCredentials)
                    ]
                },
                loggedIntoOctopus: {
                    on: {
                        NEXT: {target: 'logIntoGitHub'},
                        ALREADY_LOGGED_INTO_GITHUB: {target: 'enterAwsCredentials'},
                        BACK: {target: 'doYouHaveCloudOctopus'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("loggedIntoOctopus", settings, partition),
                        saveCurrentState("loggedIntoOctopus"),
                        assignForm(LoggedIntoOctopus)
                    ]
                },
                logIntoGitHub: {
                    on: {
                        BACK: {target: 'doYouHaveCloudOctopus'},
                        MOCK: {target: 'loggedIntoGithub'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("logIntoGitHub", settings, partition),
                        saveCurrentState("logIntoGitHub"),
                        assignForm(LogIntoGitHub)
                    ]
                },
                loggedIntoGithub: {
                    on: {
                        BACK: {target: 'logIntoGitHub'},
                        NEXT: {target: 'enterAwsCredentials'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("loggedIntoGithub", settings, partition),
                        saveCurrentState("loggedIntoGithub"),
                        assignForm(LoggedIntoGithub)
                    ]
                },
                enterAwsCredentials: {
                    on: {
                        BACK: {target: 'logIntoGitHub'},
                        NEXT: {target: 'pushPackage'},
                        ERROR: {target: 'error'},
                    },
                    entry: [
                        auditState("enterAwsCredentials", settings, partition),
                        saveCurrentState("enterAwsCredentials"),
                        assignForm(EnterAwsCredentials)
                    ]
                },
                pushPackage: {
                    on: {
                        BACK: {target: 'logIntoGitHub'},
                        NEXT: {target: 'done'},
                        ERROR: {target: 'error'}
                    },
                    entry: [
                        auditState("pushPackage", settings, partition),
                        saveCurrentState("pushPackage"),
                        assignForm(PushPackage)
                    ]
                },
                done: {
                    type: 'final',
                    entry: [
                        auditState("done", settings, partition),
                        saveCurrentState(""),
                        assignForm(Done)
                    ]
                },
                error: {
                    type: 'final',
                    entry: [
                        auditState("error", settings, partition),
                        saveCurrentState(""),
                        assignForm(Error)
                    ]
                },
            }
        },
        {
            guards: {
                isStandalone,
                isNotStandalone
            }
        }
    )
;
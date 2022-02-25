/*
    This page defines the wizard style interface users interact with to define the application they wish to construct.

    The path users can take is defined as a state machine. Each state has an accompanying React component that is
    displayed. The components are located in the src/components/journey directory.
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

const LoginActions = ["octopusLoginSucceeded", "githubLoginSucceeded"];

function getInitialStateContext() {
    if (LoginActions.indexOf(new URLSearchParams(window.location.search).get('action') || "") !== -1) {
        const stateString = localStorage.getItem("appBuilderStateContext");
        if (stateString) {
            const state = JSON.parse(stateString);
            return {
                form: null,
                standAlone: state.standAlone
            }
        }
    }

    return {
        form: null,
        standAlone: false
    }
}

function getInitialState() {
    if (LoginActions.indexOf(new URLSearchParams(window.location.search).get('action') || "") !== -1) {
        return localStorage.getItem("appBuilderState") || "selectTarget";
    }

    return "selectTarget";
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
    form: FC<JourneyProps> | null
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
export const appBuilderMachine = createMachine<StateContext>({
            id: 'appBuilder',
            initial: getInitialState(),
            context: getInitialStateContext(),
            states: {
                selectTarget: {
                    on: {
                        ECS: {target: 'doYouHaveCloudOctopus'},
                        EKS: {target: 'doYouHaveCloudOctopus'},
                        LAMBDA: {target: 'doYouHaveCloudOctopus'},
                        STANDALONE: {target: 'selectFramework'},
                    },
                    entry: assign<StateContext>({
                        form: (context:StateContext, event: AnyEventObject) => TargetSelection
                    })
                },
                selectedTargetNotAvailable: {
                    on: {
                        BACK: {target: 'selectTarget'}
                    }
                },
                doYouHaveCloudOctopus: {
                    on: {
                        YES: {target: 'logIntoOctopus'},
                        NO: {target: 'signUpForCloudOctopus'},
                        BACK: {target: 'selectTarget'},
                    },
                    entry: assign<StateContext>({
                        form: (context:StateContext, event: AnyEventObject) => DoYouHaveCloudOctopus
                    })
                },
                signUpForCloudOctopus: {
                    on: {
                        NEXT: {target: 'logIntoOctopus'},
                        BACK: {target: 'doYouHaveCloudOctopus'},
                    },
                    entry: assign<StateContext>({
                        form: (context:StateContext, event: AnyEventObject) => SignUpForCloudOctopus
                    })
                },
                logIntoOctopus: {
                    on: {
                        BACK: {target: 'doYouHaveCloudOctopus'},
                    },
                    entry: assign<StateContext>({
                        form: (context:StateContext, event: AnyEventObject) => LogIntoOctopus
                    })
                },
                loggedIntoOctopus: {
                    on: {
                        NEXT: {target: 'logIntoGitHub'},
                        BACK: {target: 'doYouHaveCloudOctopus'},
                    },
                    entry: assign<StateContext>({
                        form: (context:StateContext, event: AnyEventObject) => LoggedIntoOctopus
                    })
                },
                logIntoGitHub: {
                    on: {
                        BACK: {target: 'doYouHaveCloudOctopus'},
                    },
                    entry: assign<StateContext>({
                        form: (context:StateContext, event: AnyEventObject) => LogIntoGitHub
                    })
                },
                loggedIntoGithub: {
                    on: {
                        NEXT: {target: 'selectFramework'}
                    }
                },
                selectFramework: {
                    on: {
                        QUARKUS: {target: 'defineEntity'},
                        SPRING: {target: 'selectedFrameworkNotAvailable'},
                        DOTNETCORE: {target: 'selectedFrameworkNotAvailable'},
                        PYTHON: {target: 'selectedFrameworkNotAvailable'},
                        GO: {target: 'selectedFrameworkNotAvailable'}
                    }
                },
                selectedFrameworkNotAvailable: {
                    on: {
                        BACK: {target: 'selectFramework'}
                    }
                },
                defineEntity: {
                    on: {
                        DONE: [
                            {target: 'pushPackage', cond: isNotStandalone},
                            {target: 'downloadPackage', cond: isStandalone},
                        ]
                    }
                },
                pushPackage: {
                    type: 'final'
                },
                downloadPackage: {
                    type: 'final'
                }
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
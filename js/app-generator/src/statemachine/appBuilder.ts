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
import LoggedIntoGithub from "../components/journey/LoggedIntoGitHub";
import SelectFramework from "../components/journey/SelectFramework";
import PushPackage from "../components/journey/PushPackage";
import Done from "../components/journey/Done";
import EnterAwsCredentials from "../components/journey/EnterAwsCredentials";

function getInitialStateContext() {
    const stateString = localStorage.getItem("appBuilderStateContext");
    if (stateString) {
        const state = JSON.parse(stateString);
        return {
            form: TargetSelection,
            standAlone: !!state.standAlone
        }
    }

    return {
        form: TargetSelection,
        standAlone: false
    }
}

function getInitialState() {
    return localStorage.getItem("appBuilderState") || "selectTarget";
}

function saveCurrentState(stateName: string) {
    return (context: StateContext, event: AnyEventObject) => {
        if (event.type !== "xstate.init") {
            localStorage.setItem("appBuilderStateContext", JSON.stringify({...context, form: null}))
            localStorage.setItem("appBuilderState", stateName);
        }
    }
}

function assignForm(component: FC<JourneyProps>) {
    return assign<StateContext>({
        form: (context: StateContext, event: AnyEventObject) => component
    })
}

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
                    entry: [
                        saveCurrentState("selectTarget"),
                        assignForm(TargetSelection)
                    ]
                },
                selectedTargetNotAvailable: {
                    on: {
                        BACK: {target: 'selectTarget'}
                    },
                    entry: [
                        saveCurrentState("selectedTargetNotAvailable"),
                    ]
                },
                doYouHaveCloudOctopus: {
                    on: {
                        YES: {target: 'logIntoOctopus'},
                        NO: {target: 'signUpForCloudOctopus'},
                        BACK: {target: 'selectTarget'},
                    },
                    entry: [
                        saveCurrentState("doYouHaveCloudOctopus"),
                        assignForm(DoYouHaveCloudOctopus)
                    ]
                },
                signUpForCloudOctopus: {
                    on: {
                        NEXT: {target: 'logIntoOctopus'},
                        BACK: {target: 'doYouHaveCloudOctopus'},
                    },
                    entry: [
                        saveCurrentState("signUpForCloudOctopus"),
                        assignForm(SignUpForCloudOctopus)
                    ]
                },
                logIntoOctopus: {
                    on: {
                        BACK: {target: 'doYouHaveCloudOctopus'},
                    },
                    entry: [
                        saveCurrentState("logIntoOctopus"),
                        assignForm(LogIntoOctopus)
                    ]
                },
                loggedIntoOctopus: {
                    on: {
                        NEXT: {target: 'logIntoGitHub'},
                        BACK: {target: 'doYouHaveCloudOctopus'},
                    },
                    entry: [
                        saveCurrentState("loggedIntoOctopus"),
                        assignForm(LoggedIntoOctopus)
                    ]
                },
                logIntoGitHub: {
                    on: {
                        BACK: {target: 'doYouHaveCloudOctopus'},
                    },
                    entry: [
                        saveCurrentState("logIntoGitHub"),
                        assignForm(LogIntoGitHub)
                    ]
                },
                loggedIntoGithub: {
                    on: {
                        NEXT: {target: 'enterAwsCredentials'}
                    },
                    entry: [
                        saveCurrentState("loggedIntoGithub"),
                        assignForm(LoggedIntoGithub)
                    ]
                },
                enterAwsCredentials: {
                    on: {
                        BACK: {target: 'logIntoGitHub'},
                        NEXT: {target: 'selectFramework'},
                    },
                    entry: [
                        saveCurrentState("enterAwsCredentials"),
                        assignForm(EnterAwsCredentials)
                    ]
                },
                selectFramework: {
                    on: {
                        BACK: {target: 'enterAwsCredentials'},
                        QUARKUS: {target: 'pushPackage'},
                        SPRING: {target: 'selectedFrameworkNotAvailable'},
                        DOTNETCORE: {target: 'selectedFrameworkNotAvailable'},
                        PYTHON: {target: 'selectedFrameworkNotAvailable'},
                        GO: {target: 'selectedFrameworkNotAvailable'}
                    },
                    entry: [
                        saveCurrentState("selectFramework"),
                        assignForm(SelectFramework)
                    ]
                },
                selectedFrameworkNotAvailable: {
                    on: {
                        BACK: {target: 'selectFramework'}
                    },
                    entry: [
                        saveCurrentState("selectedFrameworkNotAvailable"),
                    ]
                },
                pushPackage: {
                    on: {
                        NEXT: {target: 'done'}
                    },
                    entry: [
                        saveCurrentState("pushPackage"),
                        assignForm(PushPackage)
                    ]
                },
                downloadPackage: {
                    type: 'final'
                },
                done: {
                    type: 'final',
                    entry: [
                        saveCurrentState(""),
                        assignForm(Done)
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
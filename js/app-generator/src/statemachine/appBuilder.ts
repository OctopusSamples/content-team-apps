/*
    This page defines the wizard style interface users interact with to define the application they wish to construct.

    The path users can take is defined as a state machine. Each state has an accompanying React component that is
    displayed. The components are located in the src/components/journey directory.
 */

import {AnyEventObject, InterpreterFrom} from "xstate/lib/types";
import {FC} from "react";
import {assign, createMachine} from "xstate";
import LogIntoOctopus from "../components/journey/LogIntoOctopus";
import TargetSelection from "../components/journey/TargetSelection";

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
    form: FC<JourneyProps>
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
            initial: 'selectTarget',
            context: {
                standAlone: false,
                form: TargetSelection
            },
            states: {
                selectTarget: {
                    on: {
                        ECS: {target: 'logIntoOctopus'},
                        EKS: {target: 'logIntoOctopus'},
                        LAMBDA: {target: 'logIntoOctopus'},
                        STANDALONE: {target: 'selectFramework'},
                    },
                },
                selectedTargetNotAvailable: {
                    on: {
                        BACK: {target: 'selectTarget'}
                    }
                },
                logIntoOctopus: {
                    on: {
                        SUCCESS: {target: 'logIntoGitHub'},
                        FAILURE: {target: 'logIntoOctopusFailed'},
                    },
                    entry: assign<StateContext>({
                        form: (context:StateContext, event: AnyEventObject) => {
                            console.log("updating form");
                            return LogIntoOctopus
                        }
                    })
                },
                logIntoOctopusFailed: {
                    on: {
                        BACK: {target: 'logIntoOctopus'}
                    }
                },
                logIntoGitHub: {
                    on: {
                        SUCCESS: {target: 'selectFramework'},
                        FAILURE: {target: 'logIntoGitHubFailed'},
                    }
                },
                logIntoGitHubFailed: {
                    on: {
                        BACK: {target: 'logIntoGitHub'}
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
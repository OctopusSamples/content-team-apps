import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {AnyEventObject} from "xstate/lib/types";
import {createMachine} from "xstate";
import TargetSelection from "../components/TargetSelection";
import {useActor, useInterpret} from "@xstate/react";

interface StateContext {
    standAlone: boolean,
    form: FC
}

const isStandalone = (context: StateContext, event: AnyEventObject) => {
    return context.standAlone;
};

const isNotStandalone = (context: StateContext, event: AnyEventObject) => {
    return !isStandalone(context, event)
};

const appBuilderMachine = createMachine<StateContext>({
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
                }
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
    });

const Home: FC = (): ReactElement => {
    const {settings} = useContext(AppContext)
    const appBuilder = useInterpret(appBuilderMachine);
    const [state] = useActor(appBuilder);

    return (
        <>
            <Helmet>
                <title>
                    {settings.title}
                </title>
            </Helmet>
            {(state.context as StateContext).form({})}
        </>
    );
};

export default Home;
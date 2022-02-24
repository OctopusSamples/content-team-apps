import React, {createContext, FC, useEffect, useReducer, useState} from "react";
import {createTheme, responsiveFontSizes, Theme, ThemeProvider,} from "@material-ui/core/styles";
import {Helmet} from "react-helmet";
import jwt_decode from 'jwt-decode';
import {createMachine, InterpreterFrom} from 'xstate';
import {darkTheme, lightTheme} from "./theme/appTheme";
import {RuntimeSettings} from "./config/dynamicConfig";
import {DEFAULT_BRANCH, getBranch} from "./utils/path";
import {getIdToken} from "./utils/security";
import Login from "./pages/Login";
import {AnyEventObject} from "xstate/lib/types";
import {useActor, useInterpret} from "@xstate/react";
import Layout from "./components/Layout";
import TargetSelection from "./pages/TargetSelection";

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

// define app context
export const AppContext = createContext({
    settings: {} as RuntimeSettings,
    appBuilder: {} as InterpreterFrom<typeof appBuilderMachine>,
    setDeveloperMode: (mode: boolean) => {
    },
    developerMode: false,
    useDefaultTheme: true,
    partition: "",
    setPartition: (mode: string) => {
    },
});

function App(settings: RuntimeSettings) {

    const appBuilder = useInterpret(appBuilderMachine);
    const [state] = useActor(appBuilder);

    const [useDefaultTheme, toggle] = useReducer(
        (theme) => {
            localStorage.setItem('defaultTheme', String(!theme));
            return !theme;
        },
        localStorage.getItem('defaultTheme') !== "false");

    // define custom theme
    let theme: Theme = createTheme(useDefaultTheme ? lightTheme : darkTheme);
    theme = responsiveFontSizes(theme);

    const [requireLogin, setRequireLogin] = useState<boolean>(false);
    const [developerMode, setDeveloperMode] = useState<boolean>(false);
    const [partition, setPartition] = useState<string>("");

    const keys = settings.aws?.jwk?.keys;
    const developerGroup = settings.aws?.cognitoDeveloperGroup;
    const branch = getBranch();
    const idToken = getIdToken(keys);

    useEffect(() => {
        if (branch !== DEFAULT_BRANCH) {
            if (idToken) {
                const decoded: any = jwt_decode(idToken);
                setRequireLogin(decoded["cognito:groups"].indexOf(developerGroup) === -1);
            } else {
                setRequireLogin(true)
            }
        } else {
            setRequireLogin(false);
        }
    }, [developerGroup, branch, idToken]);


    const returningFromLogin = new URLSearchParams(window.location.search).get('action') === "loggedin";

    useEffect(() => {
        /*
            If we are returning for being logged in, jump straight to the template generation page.
            It is the github oauth proxy
            (https://github.com/OctopusSamples/content-team-apps/tree/main/java/github-oauth-backend) that sets the
            action query parameter.
         */
        if (returningFromLogin) {

        }
    }, [returningFromLogin])

    return (
        <>
            <Helmet>
                <title>{settings.title}</title>
            </Helmet>
            <AppContext.Provider value={{
                settings,
                appBuilder,
                useDefaultTheme,
                developerMode,
                setDeveloperMode,
                partition,
                setPartition
            }}>
                <ThemeProvider theme={theme}>
                    <Layout toggleTheme={toggle}>
                        {requireLogin && <Login/>}
                        {!requireLogin && (state.context as StateContext).form({})}
                    </Layout>
                </ThemeProvider>
            </AppContext.Provider>
        </>
    );
}

export default App;
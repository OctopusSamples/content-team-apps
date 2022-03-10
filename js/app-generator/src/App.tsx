import React, {createContext, useEffect, useReducer, useState} from "react";
import {createTheme, responsiveFontSizes, StyledEngineProvider, Theme, ThemeProvider,} from "@mui/material/styles";
import {Helmet} from "react-helmet";
import jwt_decode from 'jwt-decode';
import {darkTheme, lightTheme} from "./theme/appTheme";
import {RuntimeSettings} from "./config/runtimeConfig";
import {DEFAULT_BRANCH, getBranch} from "./utils/path";
import {getIdToken} from "./utils/security";
import Login from "./pages/developer/Login";
import Layout from "./components/scaffold/Layout";
import {HashRouter, Route, Switch} from "react-router-dom";
import {routes} from "./config";
import RouteItem from "./model/RouteItem.model";
import {useInterpret} from "@xstate/react";
import {appBuilderMachine} from "./statemachine/appBuilder";
import {InterpreterFrom} from "xstate/lib/types";

declare module '@mui/styles/defaultTheme' {
    // eslint-disable-next-line @typescript-eslint/no-empty-interface
    interface DefaultTheme extends Theme {
    }
}

// define app context
export const AppContext = createContext({
    settings: {} as RuntimeSettings,
    setDeveloperMode: (mode: boolean) => {
    },
    developerMode: false,
    useDefaultTheme: true,
    partition: "",
    setPartition: (mode: string) => {
    },
    machine: {} as InterpreterFrom<typeof appBuilderMachine>
});

const DefaultComponent = () => <div>No Component Defined.</div>;

function App(settings: RuntimeSettings) {
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

    const machine = useInterpret(appBuilderMachine);

    return <>
        <Helmet>
            <title>{settings.title}</title>
        </Helmet>
        <AppContext.Provider value={{
            settings,
            useDefaultTheme,
            developerMode,
            setDeveloperMode,
            partition,
            setPartition,
            machine
        }}>
            <StyledEngineProvider injectFirst>
                <ThemeProvider theme={theme}>

                    {requireLogin && <Login/>}
                    {!requireLogin && <HashRouter>
                        <Switch>
                            <Layout toggleTheme={toggle}>
                                {/* for each route config, a react route is created */}
                                {routes.map((route: RouteItem) =>
                                    route.subRoutes ? (
                                        route.subRoutes.map((item: RouteItem) => (
                                            <Route
                                                key={`${item.key}`}
                                                path={`${item.path}`}
                                                component={(item.component && item.component()) || DefaultComponent}
                                                exact
                                            />
                                        ))
                                    ) : (
                                        <Route
                                            key={`${route.key}`}
                                            path={`${route.path}`}
                                            component={(route.component && route.component()) || DefaultComponent}
                                            exact
                                        />
                                    )
                                )}
                            </Layout>
                        </Switch>
                    </HashRouter>}
                </ThemeProvider>
            </StyledEngineProvider>
        </AppContext.Provider>
    </>;
}

export default App;
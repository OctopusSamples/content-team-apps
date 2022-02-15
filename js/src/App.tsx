import React, {useEffect, useReducer, useState} from "react";
import {createTheme, responsiveFontSizes, Theme, ThemeProvider,} from "@material-ui/core/styles";
import {HashRouter, Route, Switch} from "react-router-dom";
import {Helmet} from "react-helmet";
// app routes
import {routes} from "./config";
import jwt_decode from 'jwt-decode';

// components
import Layout from "./components/Layout";

// theme
import {darkTheme, lightTheme} from "./theme/appTheme";

// interfaces
import RouteItem from "./model/RouteItem.model";
import {DynamicConfig} from "./config/dynamicConfig";
import {DEFAULT_BRANCH, getBranch} from "./utils/path";
import {getAccessToken, getIdToken} from "./utils/security";
import Login from "./pages/Login";
import * as H from "history";

// define app context
export const AppContext = React.createContext<DynamicConfig>({
    settings: {
        basename: "",
        title: "",
        auditEndpoint: "",
        healthEndpoint: "",
        generateApiPath: "",
        editorFormat: "",
        google: {tag: ""},
        github: {enableLogin: false, loginPath: ""},
        aws: {jwk: {keys: []}, cognitoLogin: "", cognitoDeveloperGroup: ""}
    },
    useDefaultTheme: true,
    partition: "",
    setPartition: () => {
    },
    generateTemplate: () => {
    },
    setCopyText: () => {
    },
    copyText: "",
    developerMode: false,
    setDeveloperMode: () => {
    },
});

// default component
const DefaultComponent = () => <div>No Component Defined.</div>;

function App(config: DynamicConfig) {

    const [useDefaultTheme, toggle] = useReducer(
        (theme) => {
            localStorage.setItem('defaultTheme', String(!theme));
            return !theme;
        },
        localStorage.getItem('defaultTheme') !== "false");

    // define custom theme
    let theme: Theme = createTheme(useDefaultTheme ? lightTheme : darkTheme);
    theme = responsiveFontSizes(theme);

    const [copyText, setCopyText] = useState("");
    const [requireLogin, setRequireLogin] = useState<boolean>(false);
    const [partition, setPartition] = useState<string | null>(localStorage.getItem("partition") || "main");
    const [developerMode, setDeveloperMode] = useState<boolean>((localStorage.getItem("developerMode") || "false") === "true");

    const keys = config.settings.aws?.jwk?.keys;
    const developerGroup = config.settings.aws?.cognitoDeveloperGroup;
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

    // Generates the template and stores the result in the copyText state variable
    const generateTemplate = (url: string, history: H.History) => {
        async function getTemplate() {
            const accessToken = getAccessToken();
            const requestHeaders: HeadersInit = new Headers();
            if (getAccessToken()) {
                requestHeaders.set('Authorization', accessToken ? 'Bearer ' + accessToken : '');
            }
            const template =
                await fetch(config.settings.generateApiPath + '?repo=' + url, {redirect: "error", headers: requestHeaders})
                    .then(response => {
                        /*
                            The /generate endpoint will return unauthorized if it detects that it can not read the repo.
                            We then redirect to the login page to give the user a chance to login.
                        */
                        if (response.status === 401 && config.settings.github.enableLogin) {
                            history.push("/githublogin");
                            return "";
                        }

                        window.localStorage.setItem("loginForRepo", "");
                        return response.text();
                    })
                    .catch(error => {
                        console.log(error);
                        return "There was a problem with your request.";
                    })

            setCopyText(template);
        }

        getTemplate();
    }

    return (
        <>
            <Helmet>
                <title>{config.settings.title}</title>
            </Helmet>
            <AppContext.Provider value={{
                ...config,
                generateTemplate,
                useDefaultTheme,
                setCopyText,
                copyText,
                partition,
                setPartition,
                developerMode,
                setDeveloperMode
            }}>
                <ThemeProvider theme={theme}>
                    {requireLogin && <Login/>}
                    {!requireLogin &&
                        <HashRouter>
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
                        </HashRouter>
                    }
                </ThemeProvider>
            </AppContext.Provider>
        </>
    );
}

export default App;
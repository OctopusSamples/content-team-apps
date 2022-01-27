import React, {useReducer, useState} from "react";
import {createTheme, responsiveFontSizes, Theme, ThemeProvider,} from "@material-ui/core/styles";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {Helmet} from "react-helmet";
// app routes
import {routes} from "./config";

// components
import Layout from "./components/Layout";

// theme
import {darkTheme, lightTheme} from "./theme/appTheme";

// interfaces
import RouteItem from "./model/RouteItem.model";
import {DynamicConfig} from "./config/dynamicConfig";

// define app context
export const AppContext = React.createContext<DynamicConfig>({
    settings: {basename: "", title: "", generateApiPath: "", editorFormat: "", google: {tag: ""}, github: {enableLogin: false, loginPath: ""}},
    useDefaultTheme: true,
    generateTemplate: () => {},
    setCopyText: () => {},
    copyText: ""
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

    // Generates the template and stores the result in the copyText state variable
    const generateTemplate = (url: string) => {
        async function getTemplate() {
            const template =
                await fetch(config.settings.generateApiPath + '?repo=' + url, {redirect: "error"})
                    .then(response => {
                        /*
                            The /generate endpoint will return unauthorized if it detects that it can not read the repo.
                            We then redirect to the login page to give the user a chance to login.
                        */
                        if (response.status === 401 && config.settings.github.enableLogin) {
                            /*
                                Catch infinite loops where we continually try to login. The template generator
                                "should" only respond once with a 401, but be defensive here just in case.
                             */
                            if (window.localStorage.getItem("loginForRepo") === url) {
                                return "Unable to access the repo " + url;
                            }
                            window.localStorage.setItem("loginForRepo", url)

                            window.location.href = config.settings.github.loginPath;
                            return "Redirecting to login page to access private Github repo.";
                        }

                        window.localStorage.setItem("loginForRepo", "");
                        return response.text();
                    })
                    .catch(error => "There was a problem with your request.")

            setCopyText(template);
        }

        getTemplate();
    }

    return (
        <>
            <Helmet>
                <title>{config.settings.title}</title>
            </Helmet>
            <AppContext.Provider value={{...config, generateTemplate, useDefaultTheme, setCopyText, copyText}}>
                <ThemeProvider theme={theme}>
                    <Router basename={config.settings.basename}>
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
                    </Router>
                </ThemeProvider>
            </AppContext.Provider>
        </>
    );
}

export default App;
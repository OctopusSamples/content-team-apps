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
    settings: {basename: "", title: "", generateApiPath: "", editorFormat: "", google: {tag: ""}},
    useDefaultTheme: true
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

    //
    const [copyText, setCopyText] = useState("");


    return (
        <>
            <Helmet>
                <title>{config.settings.title}</title>
            </Helmet>
            <AppContext.Provider value={{...config, useDefaultTheme}}>
                <ThemeProvider theme={theme}>
                    <Router basename={config.settings.basename}>
                        <Switch>
                            <Layout toggleTheme={toggle} useDefaultTheme={useDefaultTheme} copyText={copyText}>
                                {/* for each route config, a react route is created */}
                                {routes.map((route: RouteItem) =>
                                    route.subRoutes ? (
                                        route.subRoutes.map((item: RouteItem) => (
                                            <Route
                                                key={`${item.key}`}
                                                path={`${item.path}`}
                                                component={(item.component && item.component({setCopyText, copyText})) || DefaultComponent}
                                                exact
                                            />
                                        ))
                                    ) : (
                                        <Route
                                            key={`${route.key}`}
                                            path={`${route.path}`}
                                            component={(route.component && route.component({setCopyText, copyText})) || DefaultComponent}
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
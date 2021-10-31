import React, {useReducer} from "react";
import {createMuiTheme, responsiveFontSizes, Theme, ThemeProvider,} from "@material-ui/core/styles";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {Helmet} from "react-helmet";

// components
import Layout from "./components/Layout";

// theme
import {darkTheme, lightTheme} from "./theme/appTheme";

// app routes
import {routes} from "./config";

// constants
import {APP_TITLE} from "./utils/constants";

// interfaces
import RouteItem from "./model/RouteItem.model";
import {DynamicConfig} from "./config/dynamicConfig";

// define app context
export const AppContext = React.createContext<DynamicConfig>({basename: "", useDefaultTheme: true});

// default component
const DefaultComponent = () => <div>No Component Defined.</div>;

function App(config: DynamicConfig) {

    const [useDefaultTheme, toggle] = useReducer(
        (theme) => {
            localStorage.setItem('defaultTheme',  String(!theme));
            return !theme;
        },
        localStorage.getItem('defaultTheme') !== "false");

    // define custom theme
    let theme: Theme = createMuiTheme(useDefaultTheme ? lightTheme : darkTheme);
    theme = responsiveFontSizes(theme);

    return (
        <>
            <Helmet>
                <title>{APP_TITLE}</title>
            </Helmet>
            <AppContext.Provider value={{...config, useDefaultTheme}}>
                <ThemeProvider theme={theme}>
                    <Router basename={config.basename}>
                        <Switch>
                            <Layout toggleTheme={toggle} useDefaultTheme={useDefaultTheme}>
                                {/* for each route config, a react route is created */}
                                {routes.map((route: RouteItem) =>
                                    route.subRoutes ? (
                                        route.subRoutes.map((item: RouteItem) => (
                                            <Route
                                                key={`${item.key}`}
                                                path={`${item.path}`}
                                                component={item.component || DefaultComponent}
                                                exact
                                            />
                                        ))
                                    ) : (
                                        <Route
                                            key={`${route.key}`}
                                            path={`${route.path}`}
                                            component={route.component || DefaultComponent}
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
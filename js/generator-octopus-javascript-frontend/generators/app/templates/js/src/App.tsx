import React, {createContext, useReducer, useState} from "react";
import {createTheme, responsiveFontSizes, StyledEngineProvider, Theme, ThemeProvider,} from "@mui/material/styles";
import {Helmet} from "react-helmet";
import {darkTheme, lightTheme} from "./theme/appTheme";
import {RuntimeSettings} from "./config/runtimeConfig";
import Layout from "./components/Layout";
import {HashRouter, Route, Switch} from "react-router-dom";
import {routes} from "./config";
import RouteItem from "./model/RouteItem.model";

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
    allBookId: "",
    setAllBookId: (bookId: string) => {
    }
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

    const [developerMode, setDeveloperMode] = useState<boolean>(false);
    const [partition, setPartition] = useState<string>("");
    const [allBookId, setAllBookId] = useState<string>("");

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
            allBookId,
            setAllBookId
        }}>
            <StyledEngineProvider injectFirst>
                <ThemeProvider theme={theme}>
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
                </ThemeProvider>
            </StyledEngineProvider>
        </AppContext.Provider>
    </>;
}

export default App;
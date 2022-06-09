import React, {createContext, useReducer, useState} from "react";
import {createTheme, responsiveFontSizes, StyledEngineProvider, Theme, ThemeProvider,} from "@mui/material/styles";
import {Helmet} from "react-helmet";
import {darkTheme, lightTheme} from "./theme/appTheme";
import {RuntimeSettings} from "./config/runtimeConfig";
import Layout from "./components/Layout";
import {HashRouter, Route, Routes} from "react-router-dom";
import Home from "./pages/Home";
import Book from "./pages/Book";
import Settings from "./pages/developer/Settings";
import Branching from "./pages/developer/Branching";
import Health from "./pages/developer/Health";

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

    const [developerMode, setDeveloperMode] = useState<boolean>(localStorage.getItem("developerMode") === "true");
    const [partition, setPartition] = useState<string>(localStorage.getItem("partition") || "main");
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
                        <Routes>
                            <Route element={ <Layout toggleTheme={toggle}/>}>
                                <Route path={"/"} element={<Home/>}/>
                                <Route path={"/settings"} element={<Settings/>}/>
                                <Route path={"/book/:bookId"} element={<Book/>}/>
                                <Route path={"/branching"} element={<Branching/>}/>
                                <Route path={"/health"} element={<Health/>}/>
                            </Route>
                        </Routes>
                    </HashRouter>
                </ThemeProvider>
            </StyledEngineProvider>
        </AppContext.Provider>
    </>;
}

export default App;
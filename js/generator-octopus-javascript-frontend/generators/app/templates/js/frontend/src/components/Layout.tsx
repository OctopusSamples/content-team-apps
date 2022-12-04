import React, {FC, useContext} from "react";
import clsx from "clsx";
import {CssBaseline, Theme} from "@mui/material";
import {makeStyles} from '@mui/styles';

// components
import Header from "./Header";
import Footer from "./Footer";
import {AppContext} from "../App";
import {Outlet} from "react-router-dom";

// define css-in-js
const useStyles = makeStyles((theme: Theme) => {
        return {
            root: {
                flex: 1,
                display: "flex",
                flexDirection: "column",
                flexGrow: 1,
                height: "100%"
            },
            content: {
                display: "flex",
                flexGrow: 1,
                padding: theme.spacing(3),
                overflowY: "scroll"
            },
            toolbar: {
                ...theme.mixins.toolbar,
            },
        }
    }
);

// define interface to represent component props
interface LayoutProps {
    toggleTheme: () => void;
    enableToggle: boolean;
}

// functional component
const Layout: FC<LayoutProps> = ({toggleTheme, enableToggle}: LayoutProps) => {
    const classes = useStyles();
    const {useDefaultTheme} = useContext(AppContext);
    return (
        <div className={classes.root}>
            <CssBaseline/>
            <Header
                toggleTheme={toggleTheme}
                useDefaultTheme={useDefaultTheme === undefined ? true : useDefaultTheme}
                enableToggle={enableToggle}
            />
            <main
                className={clsx(classes.content)}
            >
                <Outlet />
            </main>
            <footer>
                <Footer/>
            </footer>
        </div>
    );
};

export default Layout;
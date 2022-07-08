import {createTheme, Theme} from "@mui/material";

// define light theme colors
export const lightTheme: Theme = createTheme({
    palette: {
        primary: {
            main: "#0d80d8",
        },
        text: {
            primary: "#000000",
            secondary: "#ffffff"
        },
        background: {
            default: "#fafafa",
            paper: "#0a67b1"
        },
    },
});

// define dark theme colors
export const darkTheme: Theme = createTheme({
    palette: {
        primary: {
            main: "#0f2535",
        },
        text: {
            primary: "#ffffff",
            secondary: "#ffffff"
        },
        background: {
            default: "#112e44",
            paper: "#07121a"
        },
    },
});
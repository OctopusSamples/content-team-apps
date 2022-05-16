import {createTheme, Theme} from "@mui/material";

// define light theme colors
export const lightTheme: Theme = createTheme({
    palette: {
        mode: "light",
        text: {
            primary: "#000000",
            secondary: "#262626"
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
        mode: "dark",
        text: {
            primary: "#ffffff",
            secondary: "#afafaf",
        },
        background: {
            default: "#112e44",
            paper: "#07121a"
        },
    },
});
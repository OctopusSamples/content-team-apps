import {createTheme, Theme} from "@mui/material";
import {blue, indigo, green, lightGreen, pink, purple, deepOrange, orange} from '@mui/material/colors';

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

// general colour themes
export const colouredThemes: { [key: string]: Theme; } = {
    "blue": createTheme({
        palette: {
            primary: indigo,
            secondary: blue,
        },
    }),
    "green": createTheme({
        palette: {
            primary: green,
            secondary: lightGreen,
        },
    }),
    "pink": createTheme({
        palette: {
            primary: pink,
            secondary: purple,
        },
    }),
    "orange": createTheme({
        palette: {
            primary: deepOrange,
            secondary: orange,
        },
    }),
};
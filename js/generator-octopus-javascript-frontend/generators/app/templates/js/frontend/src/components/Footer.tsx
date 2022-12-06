import { IconButton, Link, Theme } from "@mui/material";
import {makeStyles} from '@mui/styles';
import DeveloperModeIcon from '@mui/icons-material/DeveloperMode';
import {useContext} from "react";
import {AppContext} from "../App";

const FOOTER_TEXT = `Built by Octopus. Happy Deployments!`
const FOOTER_HEIGHT = 30

// define css-in-js
const useStyles = makeStyles((theme: Theme) => {
        return {
            root: {
                flex: 1,
                display: "flex",
                justifyContent: "center",
                background: theme.palette.primary.main,
                minHeight: FOOTER_HEIGHT,
            },
            footer: {
                textTransform: "uppercase",
                color: "white",
                lineHeight: "30px"
            },
            icon: {
                paddingTop: "0",
                paddingBottom: "0",
            }
        }
    }
);

// functional component
const Footer = () => {
    const classes = useStyles();
    const context = useContext(AppContext);

    return (
        <div className={classes.root}>
            <Link
                href={"http://octopus.com"}
                target="_blank"
                className={classes.footer}
            >
                {FOOTER_TEXT}
            </Link>
            <IconButton
                className={classes.icon}
                onClick={() => {
                    localStorage.setItem("developerMode", `${!context.developerMode}`);
                    context.setDeveloperMode(!context.developerMode)
                }}
                size="large">
                <DeveloperModeIcon/>
            </IconButton>
        </div>
    );
};

export default Footer;
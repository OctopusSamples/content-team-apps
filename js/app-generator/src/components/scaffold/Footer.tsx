import { IconButton, Link, Theme } from "@mui/material";
import {makeStyles} from '@mui/styles';
import DeveloperModeIcon from '@mui/icons-material/DeveloperMode';

// constants
import {FOOTER_HEIGHT, FOOTER_TEXT} from "../../utils/constants";
import {useContext} from "react";
import {AppContext} from "../../App";

// define css-in-js
const useStyles = makeStyles((theme: Theme) => {
        return {
            root: {
                flex: 1,
                display: "flex",
                justifyContent: "center",
                background: theme.palette.background.paper,
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
                onClick={() => context.setDeveloperMode(!context.developerMode)}
                size="large">
                <DeveloperModeIcon/>
            </IconButton>
        </div>
    );
};

export default Footer;
import {createStyles, IconButton, Link, makeStyles, Theme} from "@material-ui/core";
import DeveloperModeIcon from '@mui/icons-material/DeveloperMode';

// constants
import {FOOTER_HEIGHT, FOOTER_TEXT} from "../utils/constants";
import {useContext} from "react";
import {AppContext} from "../App";

// define css-in-js
const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flex: 1,
            display: "flex",
            justifyContent: "center",
            background: theme.palette.background.paper,
            minHeight: FOOTER_HEIGHT,
        },
        footer: {
            textTransform: "uppercase",
            color: theme.palette.text.secondary,
            lineHeight: "30px"
        },
        icon: {
            paddingTop: "0",
            paddingBottom: "0",
        }
    })
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
            <IconButton className={classes.icon}
                        onClick={() => {
                            localStorage.setItem("developerMode", `${!context.developerMode}`);
                            context.setDeveloperMode(!context.developerMode);
                        }}>
                <DeveloperModeIcon/>
            </IconButton>
        </div>
    );
};

export default Footer;
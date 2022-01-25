import {createStyles, Link, makeStyles, Theme} from "@material-ui/core";

// constants
import {FOOTER_HEIGHT, FOOTER_TEXT} from "../utils/constants";

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
            color: theme.palette.text.secondary
        },
    })
);

// functional component
const Footer = () => {
    const classes = useStyles();
    return (
        <div className={classes.root}>
            <Link
                href={"http://octopus.com"}
                target="_blank"
                className={classes.footer}
            >
                {FOOTER_TEXT}
            </Link>
        </div>
    );
};

export default Footer;
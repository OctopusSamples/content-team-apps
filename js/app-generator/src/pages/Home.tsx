import {FC, ReactElement, useContext, useEffect} from "react";
import {Helmet} from "react-helmet";
import {createStyles, makeStyles} from "@material-ui/core/styles";
import {Grid, Theme} from "@material-ui/core";
import {AppContext} from "../App";

// define css-in-js
const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flex: 1,
            display: "flex",
            flexDirection: "row",
            minWidth: "100%",
            minHeight: "100%",
            height: "80vh",
            justifyContent: "center"
        },
        imageButton: {
            width: "64px",
            height: "64px",
            cursor: "pointer"
        },
        sampleLabelContainer: {
            textAlign: "center",
            margin: "1em",
        },
        sampleLabel: {
            color: theme.palette.text.primary
        },
        leftFormLabel: {
            marginRight: "1em",
            color: theme.palette.text.primary
        },
        error: {
            marginTop: "1em"
        }
    })
);

const Home: FC = (): ReactElement => {
    const classes = useStyles();
    const {settings} = useContext(AppContext);

    const returningFromLogin = new URLSearchParams(window.location.search).get('action') === "loggedin";

    useEffect(() => {
        /*
            If we are returning for being logged in, jump straight to the template generation page.
            It is the github oauth proxy
            (https://github.com/OctopusSamples/content-team-apps/tree/main/java/github-oauth-backend) that sets the
            action query parameter.
         */
        if (returningFromLogin) {

        }
    }, [returningFromLogin])

    return (
        <>
            <Helmet>
                <title>
                    {settings.title}
                </title>
            </Helmet>
            <Grid
                container={true}
                className={classes.root}
                spacing={2}
            >

            </Grid>
        </>
    );
};

export default Home;
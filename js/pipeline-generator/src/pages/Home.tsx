import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {Helmet} from "react-helmet";
import {createStyles, makeStyles} from "@material-ui/core/styles";
import {Button, FormLabel, Grid, Link, TextField, Theme} from "@material-ui/core";
import maven from './logos/maven.png';
import gradle from './logos/gradle.png';
import nodejs from './logos/nodejs.png';
import php from './logos/php.png';
import python from './logos/python.png';
import golang from './logos/golang.png';
import ruby from './logos/ruby.png';
import dotnetcore from './logos/dotnetcore.png';

// constants
import {useHistory} from "react-router-dom";
import {AppContext} from "../App";
import {getAccessToken} from "../utils/security";

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

const Home: FC<{}> = (): ReactElement => {
    const history = useHistory();
    const classes = useStyles();
    const {setCopyText, settings, generateTemplate, partition} = useContext(AppContext);

    setCopyText("");

    if (!localStorage.getItem("url")) {
        localStorage.setItem("url", "https://github.com/OctopusSamples/RandomQuotes-Java");
    }

    const [url, setUrl] = useState(
        localStorage.getItem("url")
    );

    const [error, setError] = useState("");
    const [testPartitionRequired] = useState<boolean>((localStorage.getItem("testPartitionRequired") || "").toLowerCase() === "true");
    const [accessToken] = useState<string | null>(getAccessToken());

    const returningFromLogin = new URLSearchParams(window.location.search).get('action') === "loggedin";


    useEffect(() => {
        /*
            If we are returning for being logged in, jump straight to the template generation page.
            It is the github oauth proxy
            (https://github.com/OctopusSamples/content-team-apps/tree/main/java/github-oauth-backend) that sets the
            action query parameter.
         */
        if (returningFromLogin) {
            if (!url || !url.trim().startsWith("https://github.com")) {
                setError("URL must point to a GitHub repo!");
            } else {
                setError("");
                history.push("/template");
                generateTemplate(url, history);
            }
        }
    }, [returningFromLogin, setError, url, generateTemplate, history])


    function handleUrlUpdate(url: string) {
        localStorage.setItem("url", url);
        setUrl(url);
    }

    function handleClick() {
        // Manually clicking the "Go" button restarts the login cycle, so clear the last url from local storage.
        window.localStorage.setItem("loginForRepo", "");

        if (!url || !url.trim().startsWith("https://github.com")) {
            setError("URL must point to a GitHub repo!");
        } else {
            setError("");
            history.push("/template");
            generateTemplate(url, history);
        }
    }

    function handleExampleClick(url: string) {
        // Manually clicking a sample repo button restarts the login cycle, so clear the last url from local storage.
        window.localStorage.setItem("loginForRepo", "");
        handleUrlUpdate(url);
        setError("");
        history.push("/template");
        generateTemplate(url, history);
    }

    function loginRequired() {
        if (testPartitionRequired) {
            if (!accessToken) {
                return true;
            }

            if (partition === "" || partition === "main") {
                return true;
            }
        }

        return false;
    }

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
                <Grid
                    container={true}
                    alignItems="center"
                    justifyContent="center"
                >
                    <Grid
                        container={true}
                        alignItems="center"
                        justifyContent="center"
                    >
                        <Grid item xs={2}
                              justifyContent="flex-end"
                              container={true}>
                            <FormLabel className={classes.leftFormLabel}>{"GitHub URL"}</FormLabel>
                        </Grid>
                        <Grid item xs={8}>
                            <TextField id={"url"} fullWidth={true} value={url}
                                       onChange={event => handleUrlUpdate(event.target.value)}
                                       variant={"outlined"}/>
                        </Grid>
                        <Grid item xs={2}>
                            {loginRequired()
                                ? <Button style={{marginLeft: "1em"}} variant="contained" onClick={() => history.push('/settings')}>{"Login"}</Button>
                                : <Button style={{marginLeft: "1em"}} variant="contained" onClick={handleClick}>{"Go"}</Button>}
                        </Grid>
                        {error !== "" &&
                        <Grid item xs={12} container={true} justifyContent="center">
                            <FormLabel className={classes.error}>{error}</FormLabel>
                        </Grid>}
                    </Grid>
                </Grid>
                <Grid
                    container={true}
                    justifyContent="center"
                    alignContent={"flex-start"}
                >
                    <Grid item md={1} sm={2} xs={3}>
                        <Link
                            onClick={() => handleExampleClick("https://github.com/mcasperson/SampleMavenProject-SpringBoot")}>
                            <img alt="Maven" className={classes.imageButton} src={maven}/>
                        </Link>
                    </Grid>
                    <Grid item md={1} sm={2} xs={3}>
                        <Link
                            onClick={() => handleExampleClick("https://github.com/mcasperson/SampleGradleProject-SpringBoot")}>
                            <img alt="Gradle" className={classes.imageButton} src={gradle}/>
                        </Link>
                    </Grid>
                    <Grid item md={1} sm={2} xs={3}>
                        <Link
                            onClick={() => handleExampleClick("https://github.com/OctopusSamples/RandomQuotes-JS")}>
                            <img alt="Node.js" className={classes.imageButton} src={nodejs}/>
                        </Link>
                    </Grid>
                    <Grid item md={1} sm={2} xs={3}>
                        <Link
                            onClick={() => handleExampleClick("https://github.com/OctopusSamples/RandomQuotes-PHP")}>
                            <img alt="PHP" className={classes.imageButton} src={php}/>
                        </Link>
                    </Grid>
                    <Grid item md={1} sm={2} xs={3}>
                        <Link
                            onClick={() => handleExampleClick("https://github.com/OctopusSamples/RandomQuotes-Python")}>
                            <img alt="Python" className={classes.imageButton} src={python}/>
                        </Link>
                    </Grid>
                    <Grid item md={1} sm={2} xs={3}>
                        <Link
                            onClick={() => handleExampleClick("https://github.com/OctopusSamples/RandomQuotes-Go")}>
                            <img alt="Go" className={classes.imageButton} src={golang}/>
                        </Link>
                    </Grid>
                    <Grid item md={1} sm={2} xs={3}>
                        <Link
                            onClick={() => handleExampleClick("https://github.com/OctopusSamples/RandomQuotes-Ruby")}>
                            <img alt="Ruby" className={classes.imageButton} src={ruby}/>
                        </Link>
                    </Grid>
                    <Grid item md={1} sm={2} xs={3}>
                        <Link
                            onClick={() => handleExampleClick("https://github.com/OctopusSamples/RandomQuotes")}>
                            <img alt="DotNetCore" className={classes.imageButton} src={dotnetcore}/>
                        </Link>
                    </Grid>
                </Grid>
            </Grid>
        </>
    );
};

export default Home;
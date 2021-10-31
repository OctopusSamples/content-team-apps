import {FC, ReactElement, useState} from "react";
import {Helmet} from "react-helmet";
import {createStyles, makeStyles} from "@material-ui/core/styles";
import {Button, FormLabel, Grid, Link, TextField} from "@material-ui/core";
import maven from './logos/maven.png';
import gradle from './logos/gradle.png';
import nodejs from './logos/nodejs.png';
import php from './logos/php.png';
import python from './logos/python.png';
import golang from './logos/golang.png';
import ruby from './logos/ruby.png';
import dotnetcore from './logos/dotnetcore.png';

// constants
import {APP_TITLE} from "../utils/constants";
import {useHistory} from "react-router-dom";

// define css-in-js
const useStyles = makeStyles(() =>
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
        sampleLabel: {
            textAlign: "center",
            margin: "1em"
        },
        error: {
            marginTop: "1em"
        }
    })
);

const Home: FC<{}> = (): ReactElement => {
    const history = useHistory();

    function handleClick() {
        if (!url.trim().startsWith("https://github.com")) {
            setError("URL must point to a GitHub repo!");
        } else {
            setError("");
            history.push("/template", {url});
        }
    }

    function handleExampleClick(url: string) {
        history.push("/template", {url});
    }

    const classes = useStyles();
    const [url, setUrl] = useState(
        "https://github.com/OctopusSamples/RandomQuotes-Java"
    );
    const [error, setError] = useState("");

    return (
        <>
            <Helmet>
                <title>
                    {APP_TITLE}
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
                    justify="center"
                >
                    <Grid
                        container={true}
                        alignItems="center"
                        justify="center"
                    >
                        <Grid item xs={2}
                              justify="flex-end"
                              container={true}>
                            <FormLabel style={{marginRight: "1em"}}>{"GitHub URL"}</FormLabel>
                        </Grid>
                        <Grid item xs={8}>
                            <TextField id={"url"} fullWidth={true} value={url}
                                       onChange={event => setUrl(event.target.value)}
                                       variant={"outlined"}/>
                        </Grid>
                        <Grid item xs={2}>
                            <Button style={{marginLeft: "1em"}} variant="contained"
                                    onClick={handleClick}>{"Go"}</Button>
                        </Grid>
                        {error !== "" &&
                        <Grid item xs={12} container={true} justify="center">
                            <FormLabel className={classes.error}>{error}</FormLabel>
                        </Grid>}
                    </Grid>
                </Grid>
                <Grid
                    container={true}
                    justify="center"
                    alignContent={"flex-start"}
                    justifyContent={"center"}
                    xs={12}
                >
                    <Grid item xs={12} className={classes.sampleLabel}>
                        <FormLabel>Sample Projects</FormLabel>
                    </Grid>
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
import {FC, ReactElement, useContext, useEffect} from "react";
import {Helmet} from "react-helmet";
import {createStyles, makeStyles} from "@material-ui/core/styles";
import {Grid} from "@material-ui/core";
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/neo.css';
import 'codemirror/theme/rubyblue.css';
import {UnControlled as CodeMirror} from 'react-codemirror2';
import lightSpinner from './images/spinnerLight.gif'
import lightDark from './images/spinnerDark.gif'

import {useLocation} from "react-router-dom";
import {AppContext} from "../App";

require('codemirror/mode/groovy/groovy');
require('codemirror/mode/yaml/yaml');
require('codemirror/mode/javascript/javascript');

// define css-in-js
const useStyles = makeStyles(() =>
    createStyles({
        flexGrowColumn: {
            flex: 1,
            display: "flex",
            flexDirection: "column",
            width: "100%"
        }
    })
);

const Template: FC<{}> = (): ReactElement => {
    const classes = useStyles();
    const {state} = useLocation<{ url: string }>();

    /*
        Need to destructure this to consume the values in useEffect:
        https://github.com/facebook/react/issues/16265
     */
    const {setCopyText, copyText, settings, useDefaultTheme} = useContext(AppContext);

    /*
        This needs to be rethought.

        The main issue here is that we want to propagate the template text out to parents and siblings, mostly
        to allow other elements to provide features like copying to clipboard. So we find ourselves in a situation
        where this element generates the text that it, and other elements, display. This in turn means this element
        triggers its own rerendering, which causes useEffect to be called twice.

        Checking for the presence of the template text before making the network call solves the double calls to
        the API, but this logic to call the API is likely better moved "up the chain" to a point where updating the
        template text doesn't render the element generating the text.
     */
    useEffect(() => {
        async function getTemplate() {
            const template =
                await fetch(settings.generateApiPath + '?repo=' + state.url, {redirect: "error"})
                    .then(response => {
                        /*
                            The /generate endpoint will return unauthorized if it detects that it can not read the repo.
                            We then redirect to the login page to give the user a chance to login.
                        */
                        if (response.status === 401 && settings.github.enableLogin) {
                            /*
                                Catch infinite loops where we continually try to login. The template generator
                                "should" only respond once with a 401, but be defensive here just in case.
                             */
                            if (window.localStorage.getItem("loginForRepo") === state.url) {
                                return "Unable to access the repo " + state.url;
                            }
                            window.localStorage.setItem("loginForRepo", state.url)

                            window.location.href = settings.github.loginPath;
                            return "Redirecting to login page to access private Github repo.";
                        }

                        window.localStorage.setItem("loginForRepo", "");
                        return response.text();
                    })
                    .catch(error => "There was a problem with your request.")

            setCopyText(template);
        }

        if (!copyText) {
            getTemplate();
        }
    }, [copyText, setCopyText, settings.generateApiPath, settings.github.enableLogin, settings.github.loginPath, state.url])

    const theme = !useDefaultTheme ? 'rubyblue' : 'neo';
    const mode = settings?.editorFormat || 'javascript';

    return (
        <>
            <Helmet>
                <title>
                    {settings.title}
                </title>
            </Helmet>
            <Grid
                container={true}
                className={classes.flexGrowColumn}
                spacing={2}
                alignItems="center"
                justify="center"
            >
                <Grid item xs={12} className={classes.flexGrowColumn}>
                    <CodeMirror
                        value={copyText}
                        className={classes.flexGrowColumn}
                        options={{
                            mode: mode,
                            theme: theme,
                            lineNumbers: true,
                            readOnly: true
                        }}
                    />
                </Grid>
            </Grid>
            {!copyText &&
                <img alt="loading" id="spinner" src={useDefaultTheme ? lightSpinner : lightDark} style={{
                    position: "fixed",
                    top: "50%",
                    left: "50%",
                    marginTop: "-64px",
                    marginLeft: "-64px",
                    zIndex: 1000
                }}/>
            }
        </>
    );
};

export default Template;
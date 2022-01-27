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
import {CommonProps} from "../model/RouteItem.model";

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

const Template: FC<CommonProps> = (props: CommonProps): ReactElement => {
    const classes = useStyles();
    const {state} = useLocation<{ url: string }>();

    const context = useContext(AppContext);

    useEffect(() => {
        async function getTemplate() {
            const template =
                await fetch(context.settings.generateApiPath + '?repo=' + state.url, {redirect: "error"})
                    .then(response => {
                        /*
                            The /generate endpoint will return unauthorized if it detects that it can not read the repo.
                            We then redirect to the login page to give the user a chance to login.
                        */
                        if (response.status === 401 && context.settings.github.enableLogin) {
                            /*
                                Catch infinite loops where we continually try to login. The template generator
                                "should" only respond once with a 401, but be defensive here just in case.
                             */
                            if (window.localStorage.getItem("loginForRepo") === state.url) {
                                return "Unable to access the repo " + state.url;
                            }
                            window.localStorage.setItem("loginForRepo", state.url)

                            window.location.href = context.settings.loginPath;
                            return "Redirecting to login page to access private Github repo.";
                        }

                        window.localStorage.setItem("loginForRepo", "");
                        return response.text();
                    })
                    .catch(error => "There was a problem with your request.")

            props.setCopyText(template);
        }

        getTemplate();
    }, [props, context.settings.basename, context.settings.generateApiPath, context.settings.github.enableLogin, context.settings.loginPath, state.url])

    const theme = context && !context.useDefaultTheme ? 'rubyblue' : 'neo';
    const mode = context?.settings?.editorFormat || 'javascript';

    return (
        <>
            <Helmet>
                <title>
                    {context.settings.title}
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
                        value={props.copyText}
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
            {!props.copyText &&
                <img alt="loading" id="spinner" src={context.useDefaultTheme ? lightSpinner : lightDark} style={{
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
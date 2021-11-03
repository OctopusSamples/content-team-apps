import {FC, ReactElement, useContext, useEffect, useState} from "react";
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

    const [template, setTemplate] = useState("Loading...");

    const context = useContext(AppContext);

    useEffect(() => {
        async function getTemplate() {
            const template =
                await fetch(context.settings.basename + '/' + context.settings.generateApiPath + '?repo=' + state.url)
                    .then(response => response.text())
                    .catch(error => "There was a problem with your request.")

            const spinner = document.getElementById("spinner");
            if (spinner !== null && spinner.parentElement !== null) {
                spinner.parentElement.removeChild(spinner);
            }

            setTemplate(template);
        }

        getTemplate();
    })

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
                        value={template}
                        className={classes.flexGrowColumn}
                        options={{
                            mode: mode,
                            theme: theme,
                            lineNumbers: true
                        }}
                    />
                </Grid>
            </Grid>
            <img alt="loading" id="spinner" src={context.useDefaultTheme ? lightSpinner : lightDark} style={{
                position: "fixed",
                top: "50%",
                left: "50%",
                marginTop: "-64px",
                marginLeft: "-64px",
                zIndex: 1000
            }}/>
        </>
    );
};

export default Template;
import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {createStyles, makeStyles} from "@material-ui/core/styles";
import {Grid} from "@material-ui/core";
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/neo.css';
import 'codemirror/theme/rubyblue.css';
import {UnControlled as CodeMirror} from 'react-codemirror2';
import lightSpinner from './images/spinnerLight.gif'
import lightDark from './images/spinnerDark.gif'
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

    /*
        Need to destructure this to consume the values in useEffect:
        https://github.com/facebook/react/issues/16265
     */
    const {copyText, settings, useDefaultTheme} = useContext(AppContext);

    const theme = !useDefaultTheme ? 'neo' : 'rubyblue';
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
                justifyContent="center"
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
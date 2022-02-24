import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {Button, Grid, Theme} from "@mui/material";
import {AppContext} from "../../App";
import { makeStyles } from '@mui/styles';
import {logIntoGitHub} from "../../utils/githublogin";

const useStyles = makeStyles((theme: Theme) => {
        return {
            content: {
                "& a": {
                    color: theme.palette.text.primary
                },
                padding: "28px",
                alignContent: "flex-start"
            },
            loginButtonContainer: {
                textAlign: "center",
                padding: "24px"
            },
            loginButton: {
                fontSize: "1.5em"
            }
        }
    }
);

const GitHubLogin: FC = (): ReactElement => {

    const classes = useStyles();
    const context = useContext(AppContext);

    function login() {
        if (!logIntoGitHub(context.settings.github.loginPath)) {

        }
    }

    return (
        <>
            <Helmet>
                <title>
                    {context.settings.title}
                </title>
            </Helmet>
            <Grid container={true} className={classes.content}>
                <Grid item md={2} sm={false} xs={false}/>
                <Grid item md={8} sm={12} xs={12}>
                    <h1>Log into GitHub to continue.</h1>
                    <h3>Logging into GitHub provides access to private repositories and increases rate limits.</h3>
                    <h3>By logging in, you agree to your GitHub email address being collected for marketing or advertising purposes.</h3>
                    <h3>View the <a target={"_blank"} rel={"noreferrer"} href={"https://octopus.com/legal/privacy"}>Octopus privacy policy</a> for more information.</h3>
                </Grid>
                <Grid item md={2} sm={false} xs={false}/>
                <Grid item md={2} sm={false} xs={false}/>
                <Grid item xs={8} className={classes.loginButtonContainer}>
                    <Button className={classes.loginButton} variant={"contained"} onClick={login}>Login to GitHub</Button>
                </Grid>
            </Grid>

        </>
    );
}

export default GitHubLogin;
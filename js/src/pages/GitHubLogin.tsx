import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {Button, Grid, Theme} from "@material-ui/core";
import {AppContext} from "../App";
import {createStyles, makeStyles} from "@material-ui/core/styles";
import {logIntoGitHub} from "../utils/githublogin";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        content: {
            "& a": {
                color: theme.palette.text.primary
            },
            padding: "28px"
        }
    })
);

const GitHubLogin: FC<{}> = (): ReactElement => {

    const classes = useStyles();
    const context = useContext(AppContext);

    function login() {
        if (!logIntoGitHub(context.settings.github.loginPath)) {
            context.setCopyText("Unfortunately we were not able to generate a template for your repo.")
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
                    <h2>You must log into GitHub to continue.</h2>
                    <h4>Logging into GitHub provides access to private repositories and increases rate limits.</h4>
                    <h4>By logging in, you agree that your GitHub email address may be used for marketing purposes.</h4>
                    <h4>View the <a target={"_blank"} rel={"noreferrer"} href={"https://octopus.com/legal/privacy"}>Octopus privacy policy</a> for more information.</h4>
                </Grid>
                <Grid item md={2} sm={false} xs={false}/>
                <Grid item md={2} sm={false} xs={false}/>
                <Grid item xs={10}>
                    <Button variant={"outlined"} onClick={login}>Login</Button>
                </Grid>
            </Grid>

        </>
    );
}

export default GitHubLogin;
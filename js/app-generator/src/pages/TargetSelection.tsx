import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {createStyles, makeStyles} from "@material-ui/core/styles";
import {Grid, Theme} from "@material-ui/core";
import {AppContext} from "../App";
import {Button} from "@mui/material";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flex: 1,
            display: "flex",
            flexDirection: "row",
            minWidth: "100%",
            minHeight: "100%",
            justifyContent: "center"
        },
        button: {
            width: "100%",
            minHeight: "64px"
        }
    })
);

const TargetSelection: FC = (): ReactElement => {
    const classes = useStyles();
    const {settings} = useContext(AppContext);

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
                <Grid item md={4} xs={false}/>
                <Grid item md={4} xs={12}>
                    <Button variant="outlined" className={classes.button}>
                        {"EKS with GitHub and Octopus"}
                    </Button>
                </Grid>
                <Grid item md={4} xs={false}/>
            </Grid>
        </>
    );
};

export default TargetSelection;
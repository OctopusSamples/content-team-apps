import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import { makeStyles } from '@mui/styles';
import {Grid} from "@mui/material";
import {AppContext} from "../App";
import {Button} from "@mui/material";
import {SxProps} from "@mui/system";
import {Theme} from "@mui/material/styles";

const useStyles = makeStyles((theme) => {
    return {
        root: {
            flex: 1,
            display: "flex",
            flexDirection: "row",
            minWidth: "100%",
            minHeight: "100%",
            justifyContent: "center"
        }
    }
});

const buttonStyle:SxProps<Theme> = {
    width: '100%',
    minHeight: '64px',
}

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
                    <Button sx={buttonStyle} variant="outlined">
                        {"EKS with GitHub and Octopus"}
                    </Button>
                </Grid>
                <Grid item md={4} xs={false}/>
            </Grid>
        </>
    );
};

export default TargetSelection;
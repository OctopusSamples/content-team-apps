import {createStyles, makeStyles} from "@material-ui/core";

export const styles = makeStyles((theme) =>
    createStyles({
        container: {
            height: "fit-content"
        },
        cell: {
            padding: "8px",
            alignItems: "center"
        },
        label: {
            color: theme.palette.text.primary
        },
        helpText: {
            color: theme.palette.text.hint
        }
    })
);
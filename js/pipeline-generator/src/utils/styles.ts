import {createStyles, makeStyles} from "@material-ui/core";

export const styles = makeStyles((theme) =>
    createStyles({
        container: {
            height: "fit-content",
            backgroundColor: theme.palette.background.default
        },
        fullHeightContainer: {
            height: "100%",
            backgroundColor: theme.palette.background.default
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
        },
        mainContent: {
            height: "calc(100% - 40px)"
        },
        buttonRow: {
            height: "40px",
            paddingTop: "16px"
        },
        table: {
            height: "fit-content",
            marginRight: "auto",
            marginLeft: "auto"
        }
    })
);
import { makeStyles } from '@mui/styles';

export const styles = makeStyles((theme) => {
        return {
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
                color: theme.palette.text.primary
            }
        }
    }
);
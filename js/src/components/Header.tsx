import clsx from "clsx";
import {
    AppBar,
    createStyles,
    IconButton,
    Link,
    makeStyles,
    Theme,
    Toolbar,
    Tooltip,
    Typography,
} from "@material-ui/core";
import Brightness7Icon from "@material-ui/icons/Brightness7";
import Brightness3Icon from "@material-ui/icons/Brightness3";
import ContentCopy from '@material-ui/icons/ContentCopy';
import {FC, useContext} from "react";
import {AppContext} from "../App";

// define css-in-js
const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        appBar: {
            zIndex: theme.zIndex.drawer + 1,
            transition: theme.transitions.create(["width", "margin"], {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.leavingScreen,
            }),
        },
        toolbar: {
            flex: 1,
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
        },
        title: {
            flex: 1,
            display: "flex",
            flexDirection: "row",
            alignItems: "center"
        },
        menuButton: {
            marginRight: 36,
        },
        hide: {
            display: "none",
        },
        heading: {
            color: "white"
        }
    })
);

// define interface to represent component props
interface HeaderProps {
    toggleTheme: () => void;
    useDefaultTheme: boolean;
    copyText?: string;
}

const Header: FC<HeaderProps> = ({
                                     toggleTheme,
                                     useDefaultTheme,
                                     copyText
                                 }: HeaderProps) => {
    const classes = useStyles();
    const context = useContext(AppContext);
    return (
        <AppBar
            position="relative"
            elevation={0}
            className={clsx(classes.appBar)}
        >
            <Toolbar className={classes.toolbar}>
                <div className={classes.title}>
                    <Link href={`${process.env.PUBLIC_URL}/index.html`} className={classes.heading}>
                        <Typography variant="h6" noWrap>
                            {context.settings.title}
                        </Typography>
                    </Link>
                </div>
                {copyText &&
                <IconButton onClick={() => navigator.clipboard.writeText(copyText)}>
                    <Tooltip title={"Copy to clipboard"} placement={"bottom"}>
                        <ContentCopy/>
                    </Tooltip>
                </IconButton>
                }
                <IconButton onClick={toggleTheme}>
                    {useDefaultTheme ? (
                        <Tooltip title="Switch to dark mode" placement="bottom">
                            <Brightness3Icon/>
                        </Tooltip>
                    ) : (
                        <Tooltip title="Switch to light mode" placement="bottom">
                            <Brightness7Icon/>
                        </Tooltip>
                    )}
                </IconButton>
            </Toolbar>
        </AppBar>
    );
};

export default Header;
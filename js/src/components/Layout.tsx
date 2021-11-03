import {FC, ReactNode} from "react";
import clsx from "clsx";
import {createStyles, CssBaseline, makeStyles, Theme,} from "@material-ui/core";

// components
import Header from "./Header";
import Footer from "./Footer";

// define css-in-js
const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flex: 1,
            display: "flex",
            flexDirection: "column",
            flexGrow: 1
        },
        content: {
            display: "flex",
            flexGrow: 1,
            padding: theme.spacing(3),
        },
        toolbar: {
            ...theme.mixins.toolbar,
        },
    })
);

// define interface to represent component props
interface LayoutProps {
    toggleTheme: () => void;
    useDefaultTheme: boolean;
    children: ReactNode;
    copyText?: string;
}

// functional component
const Layout: FC<LayoutProps> = ({
                                     toggleTheme,
                                     useDefaultTheme,
                                     children,
                                     copyText
                                 }: LayoutProps) => {
    const classes = useStyles();
    return (
        <div className={classes.root}>
            <CssBaseline/>
            <Header
                toggleTheme={toggleTheme}
                useDefaultTheme={useDefaultTheme}
                copyText={copyText}
            />
            <main
                className={clsx(classes.content)}
            >
                {children}
            </main>
            <footer>
                <Footer/>
            </footer>
        </div>
    );
};

export default Layout;
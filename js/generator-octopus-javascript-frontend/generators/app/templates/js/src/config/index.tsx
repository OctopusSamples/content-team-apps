// components
import Branching from "../pages/developer/Branching";
import Health from "../pages/developer/Health";

// interface
import RouteItem from '../model/RouteItem.model';
import Settings from "../pages/developer/Settings";
import Home from "../pages/Home";
import Book from "../pages/Book";

// define app routes
export const routes: Array<RouteItem> = [
    {
        key: "router-home",
        title: "Home",
        path: "/",
        enabled: true,
        component: () => () => <Home/>,
    },
    {
        key: "router-home-index",
        title: "Home",
        path: "/index.html",
        enabled: true,
        component: () => () => <Home/>,
    },
    {
        key: "router-settings",
        title: "Settings",
        path: "/settings",
        enabled: true,
        component: () => () => <Settings/>,
    },
    {
        key: "router-health",
        title: "Health",
        path: "/health",
        enabled: true,
        component: () => () => <Health/>,
    },
    {
        key: "router-branching",
        title: "Branching",
        path: "/branching",
        enabled: true,
        component: () => () => <Branching/>,
    },
    {
        key: "router-book",
        title: "Home",
        path: "/book/:bookId",
        enabled: true,
        component: () => () => <Book/>,
    },
]
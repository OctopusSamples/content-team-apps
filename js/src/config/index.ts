// icons
import HomeIcon from '@material-ui/icons/Home';

// components
import Home from '../pages/Home';

// interface
import RouteItem from '../model/RouteItem.model';
import Template from "../pages/Template";

// define app routes
export const routes: Array<RouteItem> = [
    {
        key: "router-home",
        title: "Home",
        tooltip: "Home",
        path: "/",
        enabled: true,
        component: Home,
        icon: HomeIcon,
        appendDivider: true
    },
    {
        key: "router-home-index",
        title: "Home",
        tooltip: "Home",
        path: "/index.html",
        enabled: true,
        component: Home,
        icon: HomeIcon,
        appendDivider: true
    },
    {
        key: "router-template",
        title: "Template",
        tooltip: "Template",
        path: "/template",
        enabled: true,
        component: Template,
        icon: HomeIcon,
        appendDivider: true
    }
]
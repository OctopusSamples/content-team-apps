import {FC} from "react";

// RouteItem is an interface for defining the application routes and navigation menu items
interface RouteItem {
    key: string;
    title: string;
    tooltip?: string;
    path?: string;
    component?: () => FC;
    enabled: boolean;
    subRoutes?: Array<RouteItem>;
    appendDivider?: boolean;
}

export default RouteItem;
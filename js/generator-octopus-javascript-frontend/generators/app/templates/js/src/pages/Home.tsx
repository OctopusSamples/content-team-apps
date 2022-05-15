import React, {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";

const Home: FC = (): ReactElement => {
    const {settings} = useContext(AppContext)

    return (
        <>
            <Helmet>
                <title>
                    {settings.title}
                </title>
            </Helmet>
        </>
    );
};

export default Home;
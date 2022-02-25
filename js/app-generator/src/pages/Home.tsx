import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {useActor} from "@xstate/react";

/**
 * The React component that displays the components associated with the states in the state machine. Each state is
 * responsible for setting the "form" context property, which is rendered by this component.
 * @constructor
 */
const Home: FC = (): ReactElement => {
    const {settings, machine} = useContext(AppContext)
    const [state] = useActor(machine);

    return (
        <>
            <Helmet>
                <title>
                    {settings.title}
                </title>
            </Helmet>
            {state.context.form && state.context.form({machine})}
        </>
    );
};

export default Home;
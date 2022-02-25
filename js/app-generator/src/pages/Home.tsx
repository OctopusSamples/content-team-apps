import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {useActor, useInterpret} from "@xstate/react";
import {appBuilderMachine} from "../statemachine/appBuilder";

/**
 * The React component that displays the components associated with the states in the state machine.
 * @constructor
 */
const Home: FC = (): ReactElement => {
    const {settings} = useContext(AppContext)
    const machine = useInterpret(appBuilderMachine);
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
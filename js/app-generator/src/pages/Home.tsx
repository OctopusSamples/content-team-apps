import React, {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {useActor} from "@xstate/react";
import TargetSelection from "../components/journey/TargetSelection";
import { JourneyProps } from "../statemachine/appBuilder";

/**
 * The React component that displays the components associated with the states in the state machine. Each state is
 * responsible for setting the "form" context property, which is rendered by this component.
 * @constructor
 */
const Home: FC = (): ReactElement => {
    const {settings, machine} = useContext(AppContext)
    const [state] = useActor(machine);

    const Child = (props: React.Attributes & JourneyProps) => React.createElement(state.context.form || TargetSelection, {...props})

    return (
        <>
            <Helmet>
                <title>
                    {settings.title}
                </title>
            </Helmet>
            <Child machine={machine}/>
        </>
    );
};

export default Home;
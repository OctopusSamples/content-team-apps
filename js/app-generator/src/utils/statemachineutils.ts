import {InterpreterFrom} from "xstate/lib/types";
import {appBuilderMachine} from "../statemachine/appBuilder";

export function saveStateMachineStateContext(machine: InterpreterFrom<typeof appBuilderMachine>) {
    localStorage.setItem("appBuilderStateContext", JSON.stringify({standalone: machine.state.context.standAlone}))
}
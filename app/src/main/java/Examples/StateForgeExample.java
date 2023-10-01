package Examples;

import com.whitneyrobotics.whslib.StateForge.State;
import com.whitneyrobotics.whslib.StateForge.StateForge;
import com.whitneyrobotics.whslib.StateForge.StateMachine;
import com.whitneyrobotics.whslib.StateForge.SubstateMachine;

import java.util.ArrayList;

import Examples.ExampleStates;

public class StateForgeExample {
    static boolean visited, secret  = false;
    public static void main(String[] args){


        StateMachine<ExampleStates> autonomous = StateForge.StateMachine()
            .state(ExampleStates.INTAKING_PIXELS)
                .onExit(() -> System.out.println("Exiting initial state"))
                .transition(() -> secret, ExampleStates.SECRET_STATE)
                .transition(() -> visited, ExampleStates.NAVIGATING_UNDER_STAGE_BARRIERS_TO_BACKRDROP)
                .timedTransitionLinear(1)
                .fin()
            .state(ExampleStates.NAVIGATING_TO_BACKRROP)
                .onEntry(() -> System.out.println("Entering state 1"))
                .timedTransitionLinear(1)
                .fin()
            .state(ExampleStates.NAVIGATING_TO_STACKS)
                .onEntry(() -> {
                    System.out.println("Entering state 2");
                    visited = true;
                })
                .timedTransition(1, ExampleStates.INTAKING_PIXELS) //You can jump back to a previous state by specifying a pointer
                .fin()
            .state(ExampleStates.NAVIGATING_UNDER_STAGE_BARRIERS_TO_BACKRDROP)
                .onEntry(() -> {
                    System.out.println("Entering state 3");
                    secret = true;
                })
                .timedTransitionLinear(1) // should go to initial state as secret state is nonlinear
                .fin()
            .substate(ExampleStates.TEST_SUBSTATE)
                .buildEmbeddedStateMachine(s -> s
                    .state(ExampleStates2.INITIAL)
                        .onEntry(() -> System.out.println("Entering substate 1"))
                        .timedTransitionLinear(1)
                        .fin()
                    .state(ExampleStates2.INTERMEDIATE)
                        .onEntry(() -> System.out.println("Entering substate 2"))
                        .timedTransitionLinear(1)
                        .fin()
                    .state(ExampleStates2.FINAL)
                        .fin())
                .transitionWithEmbeddedStateMachine(machine -> machine.getMachineState() == ExampleStates2.FINAL, ExampleStates.SECRET_STATE)
                .onEntry(() -> System.out.println("Entering substate machine"))
                .fin()
            .nonLinearState(ExampleStates.SECRET_STATE) //excluded from linear order - the transition call from the previous state will start from the beginning of the linear states again
                .onEntry(() -> System.out.println("You found me!"))
                .fin()
                .build();

        autonomous.start();
        System.out.println(autonomous.linearStates.get(0).getTransitions());
        while (true){
            autonomous.update();
        }
    }

}

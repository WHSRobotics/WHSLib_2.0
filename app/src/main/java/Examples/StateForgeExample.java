package Examples;

import com.whitneyrobotics.whslib.StateForge.State;
import com.whitneyrobotics.whslib.StateForge.StateForge;
import com.whitneyrobotics.whslib.StateForge.StateMachine;

import java.util.ArrayList;

import Examples.ExampleStates;

public class StateForgeExample {
    static int random;
    public static void main(String[] args){


        StateMachine<ExampleStates> autonomous = StateForge.StateMachine()
            .state(ExampleStates.INTAKING_PIXELS)
                .onEntry(StateForgeExample::selectRandom)
                .onExit(() -> System.out.println("Exiting initial state"))
                .transition(() -> random == 0, ExampleStates.NAVIGATING_TO_BACKRROP)
                .transition(() -> random<=5, ExampleStates.NAVIGATING_TO_STACKS)
                .transition(() -> random>5, ExampleStates.NAVIGATING_TO_BACKRROP)
                .fin()
            .state(ExampleStates.NAVIGATING_TO_BACKRROP).fin()
                .build();

        autonomous.start();
        while (true){
            autonomous.update();
        }
    }

    static void selectRandom(){
        random = (int)(Math.random() * 11);
    }




}

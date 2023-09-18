package com.whitneyrobotics.whslib.StateForge;

import com.whitneyrobotics.whslib.Util.Action;
import com.whitneyrobotics.whslib.Util.Triple;

import java.util.HashMap;
import java.util.List;

/**
 * StateForge is a modified version of StateMachine for FIRST Tech Challenge
 * Learn more here: https://github.com/StateFactory-Dev/StateFactory
 */
public class StateMachine<E extends Enum<E>> {
    public List<State<E>> linearStates;
    public List<State<E>> nonLinearStates;

    /**
     * Hashmaps to improve lookup time for states
     */
    public HashMap<E, Integer> linearStateMap = new HashMap<>();
    public HashMap<E, Integer> nonLinearStateMap = new HashMap<>();

    private State<E> currentState;

    private boolean active = false;
    public boolean ranEnterCallback = false;

    public StateMachine(List<State<E>> states){
        for (State s: states){
            if (s.nonLinear){
                nonLinearStates.add(s);
                nonLinearStateMap.put((E)s.getState(), nonLinearStates.size() - 1);
            } else {
                linearStates.add(s);
                linearStateMap.put((E)s.getState(), linearStates.size() - 1);
            }
        }
        currentState = linearStates.get(0);
    }

    public void synchronizeAllSubStateMachines(){
        for(State s : linearStates){
            if (s instanceof SubstateMachine) ((SubstateMachine)s).synchronize(active);
        }
    }

    public void start(){
        if(currentState == null){
            active = false;
        } else active = true;
        currentState.getOnEntryAction().call();
        ranEnterCallback = true;
        synchronizeAllSubStateMachines();
    }

    public void stop(){
        active = false;
        synchronizeAllSubStateMachines();
    }

    public E getMachineState(){
        return currentState.getState();
    }

    public void reset(){
        currentState = linearStates.get(0);
        active = true;
        for(State s : linearStates){
            if (s instanceof SubstateMachine) ((SubstateMachine)s).reset();
        }
        synchronizeAllSubStateMachines();
    }

    public void update(){
        if(!active) return;
        Action exitAction = null;
        State nextState = null;
        boolean willTransition = false;
        for (Triple<TransitionCondition, E, Action> transitionInfo : currentState.getTransitions()){
            if(transitionInfo.a instanceof TimedTransition && !((TimedTransition) transitionInfo.a).timerStarted()){
                ((TimedTransition) transitionInfo.a).startTimer();
            }
            int index = linearStateMap.get(currentState.getState());
            if(transitionInfo.a.shouldTransition()){
                exitAction = transitionInfo.c;

                if(transitionInfo.b != null){
                    try {
                        nextState = linearStates.get(linearStateMap.get(transitionInfo.b));
                    } catch (NullPointerException e){ //if no linear state is specified, it must be a fallback transiion
                        nextState = nonLinearStates.get(nonLinearStateMap.get(transitionInfo.b));
                    }
                } else {
                    nextState = linearStates.get(index+1 % linearStates.size());
                }
                willTransition = true;
                break;
            }
        }

        if(!ranEnterCallback && currentState.getOnEntryAction() != null){
            currentState.getOnEntryAction().call();
            ranEnterCallback = true;
        }

        if(willTransition && nextState != null){
            if(exitAction != null){ //an exit action paired with a transition OVERRIDES the default exit action of the current state
                exitAction.call();
            } else if (currentState.getOnExitAction() != null) {
                currentState.getOnExitAction().call();
            }

            currentState = nextState;
            ranEnterCallback = false;
            //reset all timers of the new state
            for (Triple<TransitionCondition, E, Action> transitionInfo : currentState.getTransitions()) {
                if (transitionInfo.a instanceof TimedTransition && ((TimedTransition) transitionInfo.a).timerStarted()) {
                    ((TimedTransition) transitionInfo.a).reset();
                }
            }
        }
    }
}

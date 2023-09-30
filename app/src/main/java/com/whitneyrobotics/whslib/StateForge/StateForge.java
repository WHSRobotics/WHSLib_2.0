package com.whitneyrobotics.whslib.StateForge;

import com.whitneyrobotics.whslib.Util.Action;
import com.whitneyrobotics.whslib.Util.Triple;

import java.util.ArrayList;
import java.util.List;

public class StateForge {

    public static class StateMachineBuilder<E extends Enum<E>> {
        private List<State<E>> states = new ArrayList<>();

        public StateBuilder<E> state(E stateEnum){
            return new StateBuilder<>(this, stateEnum);
        }

        public StateBuilder<E> nonLinearState(E stateEnum) {
            return new StateBuilder<>(this, stateEnum, true);
        }

        public StateMachine<E> build(){
            return new StateMachine<>(states);
        }

        public StateMachine<E> build(State<E> initialState){
            states.add(0,initialState);
            StateMachine<E> sm = new StateMachine<>(states);
            return sm;
        }

    }
    public static class StateBuilder<E extends Enum<E>> {
        private E stateEnum;
        private boolean nonLinear;

        private Action onEntryAction, onExitAction = null;


        private StateMachineBuilder<E> host;
        private List<Triple<TransitionCondition, E, Action>> transitions = new ArrayList<>();

        private StateBuilder(StateMachineBuilder<E> host, E stateEnum){
            this.host = host;
            this.stateEnum = stateEnum;
        }

        private StateBuilder(StateMachineBuilder<E> host, E stateEnum, boolean nonLinear){
            this.host = host;
            this.stateEnum = stateEnum;
            this.nonLinear = nonLinear;
        }

        public StateBuilder<E> setNonLinear(boolean nonLinear){
            this.nonLinear = nonLinear;
            return this;
        }

        public StateBuilder<E> onEntry(Action onEntryAction){
            this.onEntryAction = onEntryAction;
            return this;
        }

        public StateBuilder<E> onExit(Action onExitAction){
            this.onExitAction = onExitAction;
            return this;
        }

        public StateBuilder<E> transitionLinear(TransitionCondition condition){
            transitions.add(new Triple<>(condition, null, null));
            return this;
        }

        public StateBuilder<E> transition(TransitionCondition condition, E nextState){
            transitions.add(new Triple<>(condition, stateEnum, null));
            return this;
        }

        public StateBuilder<E> transitionWithAction(TransitionCondition condition, E nextState, Action action){
            transitions.add(new Triple<>(condition, stateEnum, action));
            return this;
        }

        public StateBuilder<E> timedTransitionLinear(double timeSeconds){
            return transitionLinear(new TimedTransition(timeSeconds));
        }

        public StateBuilder<E> timedTransition(double timeSeconds, E nextState){
            return transition(new TimedTransition(timeSeconds), nextState);
        }

        public StateBuilder<E> timedTransitionWithAction(double timeSeconds, E nextState, Action action){
            return transitionWithAction(new TimedTransition(timeSeconds), nextState, action);
        }

        public StateMachineBuilder<E> fin(){
            host.states.add(new State(stateEnum, onEntryAction, onExitAction, transitions, nonLinear));
            return host;
        }


    }

    public static <E> StateMachineBuilder StateMachine(){
        return new StateMachineBuilder();
    }
}

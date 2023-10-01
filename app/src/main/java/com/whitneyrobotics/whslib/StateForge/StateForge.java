package com.whitneyrobotics.whslib.StateForge;

import com.whitneyrobotics.whslib.Util.Action;
import com.whitneyrobotics.whslib.Util.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class StateForge {

    public static class StateMachineBuilder<E extends Enum<E>> {
        private List<State<E>> states = new ArrayList<>();

        public StateBuilder<E> state(E stateEnum){
            return new StateBuilder<>(this, stateEnum);
        }

        public <R extends Enum<R>> SubStateMachineBuilder<E, R> substate(E stateEnum){
            return new SubStateMachineBuilder<E,R>(this, stateEnum);
        }

        public <R extends Enum<R>> SubStateMachineBuilder<E, R> nonLinearSubstate(E stateEnum){
            return new SubStateMachineBuilder<E,R>(this, stateEnum, true);
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
        protected E stateEnum;
        protected boolean nonLinear;

        protected Action onEntryAction, onExitAction = null;


        protected StateMachineBuilder<E> host;
        protected List<Triple<TransitionCondition, E, Action>> transitions = new ArrayList<>();

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
            transitions.add(new Triple<>(condition, nextState, null));
            return this;
        }

        public StateBuilder<E> transitionWithAction(TransitionCondition condition, E nextState, Action action){
            transitions.add(new Triple<>(condition, nextState, action));
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

    public static class SubStateMachineBuilder<E extends Enum<E>, R extends Enum<R>> extends StateBuilder<E> {
        private SubstateMachine.TRANSITION_BEHAVIOR transitionBehavior = SubstateMachine.TRANSITION_BEHAVIOR.PERSIST_INTERNAL_STATE;
        private StateMachine<R> embeddedMachine;
        private SubStateMachineBuilder(StateMachineBuilder<E> host, E stateEnum) {
            super(host, stateEnum);
        }

        private SubStateMachineBuilder(StateMachineBuilder<E> host, E stateEnum, boolean nonLinear) {
            super(host, stateEnum, nonLinear);
        }

        public SubStateMachineBuilder<E,R> transitionBehavior(SubstateMachine.TRANSITION_BEHAVIOR behavior) {
            this.transitionBehavior = transitionBehavior;
            return this;
        }

        /**
         * @param builderCommands A lambda function that accepts a sequence of commands to build the embedded state machine
         * @return the instance
         */
        public SubStateMachineBuilder<E,R> buildEmbeddedStateMachine(SubstateBuilder<E,R> builderCommands) {
            embeddedMachine = builderCommands.useCommands(new StateMachineBuilder<>()).build();
            return this;
        }

        public SubStateMachineBuilder<E,R> transitionWithEmbeddedStateMachine(SubstateTester<R> conditionProvider, E nextState, Action action) {
            transitionWithAction(() -> conditionProvider.test(embeddedMachine), nextState, action);
            return this;
        }
        public SubStateMachineBuilder<E,R> transitionWithEmbeddedStateMachine(SubstateTester<R> conditionProvider, E nextState) {
            transition(() -> conditionProvider.test(embeddedMachine), nextState);
            return this;
        }
        public SubStateMachineBuilder<E,R> transitionWithEmbeddedStateMachine(SubstateTester<R> conditionProvider) {
            transitionLinear(() -> conditionProvider.test(embeddedMachine));
            return this;
        }

        @Override
        public StateMachineBuilder<E> fin() {
            if(embeddedMachine == null) throw new IllegalStateException("Embedded state machine not built. Use .buildEmbeddedStateMachine() to configure.");
            host.states.add(new SubstateMachine<>(embeddedMachine, stateEnum, onEntryAction, onExitAction, transitions, nonLinear)._setTransitionBehavior(transitionBehavior));
            return host;
        }
    }

    public static <E> StateMachineBuilder StateMachine(){
        return new StateMachineBuilder();
    }
}

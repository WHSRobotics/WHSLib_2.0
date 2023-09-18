package com.whitneyrobotics.whslib.StateForge;

import com.whitneyrobotics.whslib.Util.Action;
import com.whitneyrobotics.whslib.Util.Triple;
import static com.whitneyrobotics.whslib.Util.Functions.requireNotNull;

import java.util.List;

public abstract class State<E extends Enum<E>> {
    private E state;
    private List<Triple<TransitionCondition, E, Action>> transitions;
    private Action onEntryAction = () -> {};
    private Action onExitAction = () -> {};
    public final boolean nonLinear;

    public State(E state, List<Triple<TransitionCondition, E, Action>> transitions) {
        this.state = state;
        this.transitions = transitions;
        this.nonLinear = false;
    }

    public State(E state, Action onEntryAction, Action onExitAction, List<Triple<TransitionCondition, E, Action>> transitions) {
        this.state = state;
        this.transitions = transitions;
        this.onEntryAction = onEntryAction;
        this.onExitAction = onExitAction;
        this.nonLinear = false;
    }

    public State(E state, Action onEntryAction, Action onExitAction, List<Triple<TransitionCondition, E, Action>> transitions, boolean nonLinear) {
        this.state = state;
        this.transitions = transitions;
        this.onEntryAction = onEntryAction;
        this.onExitAction = onExitAction;
        this.nonLinear = nonLinear;
    }

    public List<Triple<TransitionCondition, E, Action>> getTransitions() {
        return transitions;
    }

    public E getState() {
        return state;
    }

    @Override
    public String toString() {
        return state.toString();
    }

    public Action getOnEntryAction() {
        return onEntryAction;
    }

    public void setOnEntryAction(Action onEntryAction) {
        this.onEntryAction = requireNotNull(onEntryAction);
    }

    public Action getOnExitAction() {
        return onExitAction;
    }

    public void setOnExitAction(Action onExitAction) {
        this.onExitAction = requireNotNull(onExitAction);
    }

    public <R extends Enum<R>> R getSubstate(){
        return null;
    }
}


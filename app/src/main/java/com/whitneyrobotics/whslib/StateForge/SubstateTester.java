package com.whitneyrobotics.whslib.StateForge;

@FunctionalInterface
public interface SubstateTester<R extends  Enum<R>> {
    boolean test(StateMachine<R> machine);
}

package com.whitneyrobotics.whslib.StateForge;
@FunctionalInterface
public interface TransitionCondition {
    boolean shouldTransition();
}

package com.ridelink.ridemanagement.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the lifecycle state of a ride in the RideLink platform.
 * Enforces deterministic state machine transition rules:
 *
 * Valid Forward Path:
 * REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
 *
 * Valid Cancellation Paths:
 * REQUESTED -> CANCELLED
 * ASSIGNED  -> CANCELLED
 * ACCEPTED  -> CANCELLED
 *
 * Invalid Transitions:
 * IN_PROGRESS -> CANCELLED (cannot cancel once trip has started)
 * COMPLETED   -> ANY       (terminal state)
 * CANCELLED   -> ANY       (terminal state)
 */
public enum RideStatus {

    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    /**
     * Returns the set of valid next states from the current state.
     */
    public Set<RideStatus> getAllowedNextStatuses() {
        return switch (this) {
            case REQUESTED -> EnumSet.of(ASSIGNED, CANCELLED);
            case ASSIGNED -> EnumSet.of(ACCEPTED, CANCELLED);
            case ACCEPTED -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(COMPLETED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(RideStatus.class);
        };
    }

    /**
     * Checks whether transitioning from the current state to targetStatus is valid.
     *
     * @param targetStatus the desired next state
     * @return true if the transition is permitted; false otherwise
     */
    public boolean canTransitionTo(RideStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        return getAllowedNextStatuses().contains(targetStatus);
    }
}

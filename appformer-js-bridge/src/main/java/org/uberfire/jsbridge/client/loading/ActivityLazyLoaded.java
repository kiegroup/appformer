package org.uberfire.jsbridge.client.loading;

import java.util.Objects;

import org.uberfire.client.mvp.Activity;
import org.uberfire.workbench.events.UberFireEvent;

public class ActivityLazyLoaded implements UberFireEvent {

    private final String identifier;
    private final Activity activity;

    public ActivityLazyLoaded(final String identifier, final Activity activity) {
        this.identifier = identifier;
        this.activity = activity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Activity getActivity() {
        return activity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActivityLazyLoaded that = (ActivityLazyLoaded) o;
        return Objects.equals(identifier, that.identifier) &&
                Objects.equals(activity, that.activity);
    }

    @Override
    public int hashCode() {

        return Objects.hash(identifier, activity);
    }

    @Override
    public String toString() {
        return "ActivityLazyLoaded{" +
                "identifier='" + identifier + '\'' +
                ", activity=" + activity +
                '}';
    }
}

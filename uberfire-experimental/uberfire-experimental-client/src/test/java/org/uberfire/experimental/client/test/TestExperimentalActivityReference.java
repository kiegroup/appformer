package org.uberfire.experimental.client.test;

import org.uberfire.experimental.client.mvp.ExperimentalActivityReference;

public class TestExperimentalActivityReference implements ExperimentalActivityReference {

    private String activityTypeName;
    private String activityId;
    private String experimentalFeatureId;

    public TestExperimentalActivityReference(String activityTypeName, String activityId, String experimentalFeatureId) {
        this.activityTypeName = activityTypeName;
        this.activityId = activityId;
        this.experimentalFeatureId = experimentalFeatureId;
    }

    @Override
    public String getActivityTypeName() {
        return activityTypeName;
    }

    @Override
    public String getActivityId() {
        return activityId;
    }

    @Override
    public String getExperimentalFeatureId() {
        return experimentalFeatureId;
    }
}

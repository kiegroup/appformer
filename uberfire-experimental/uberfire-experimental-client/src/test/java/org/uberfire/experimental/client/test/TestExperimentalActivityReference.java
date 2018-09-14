package org.uberfire.experimental.client.test;

import org.uberfire.experimental.client.service.auth.ExperimentalActivityReference;
import org.uberfire.workbench.model.ActivityResourceType;

public class TestExperimentalActivityReference implements ExperimentalActivityReference {

    private String activityTypeName;
    private String activityId;
    private String experimentalFeatureId;
    private ActivityResourceType activityType;

    public TestExperimentalActivityReference(String activityTypeName, String activityId, String experimentalFeatureId, ActivityResourceType activityType) {
        this.activityTypeName = activityTypeName;
        this.activityId = activityId;
        this.experimentalFeatureId = experimentalFeatureId;
        this.activityType = activityType;
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

    @Override
    public ActivityResourceType getActivityType() {
        return activityType;
    }
}

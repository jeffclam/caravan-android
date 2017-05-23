package com.caravan.senior_project.caravan_android;

import com.mapbox.services.api.directions.v5.models.IntersectionLanes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohin_000 on 5/17/2017.
 */

public class MyIntersectionLanes {
    private boolean valid;
    private List<String> indications = new ArrayList<String>();

    /**
     * Empty constructor
     *
     * @since 2.1.0
     */
    public MyIntersectionLanes() {
    }

    /**
     * Constructor taking in a boolean true if the lane can validly be used for the user to complete the maneuver.
     *
     * @param valid boolean true if the lane can validly be used for the user to complete the maneuver.
     * @since 2.1.0
     */
    public MyIntersectionLanes(boolean valid) {
        this.valid = valid;
    }

    public MyIntersectionLanes(IntersectionLanes lane) {
        this.valid = lane.getValid();
        for (int i = 0; i < lane.getIndications().length; i++)
            this.indications.add(lane.getIndications()[i]);
    }

    /**
     * Provides a boolean value you can use to determine if the given lane is valid for the user to complete the maneuver.
     *
     * @return Boolean value for whether this lane can be taken to complete the maneuver. For
     * instance, if the lane array has four objects and the first two are marked as valid, then the
     * driver can take either of the left lanes and stay on the route.
     * @since 2.0.0
     */

    public boolean getValid() {
        return valid;
    }

    /**
     * Sets Boolean value for whether this lane can be taken to complete the maneuver. For
     * instance, if the lane array has four objects and the first two are marked as valid, then the
     * driver can take either of the left lanes and stay on the route.
     *
     * @param valid boolean true if the user can validly complete the maneuver using this lane, otherwise false.
     * @since 2.1.0
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Array that can be made up of multiple signs such as {@code left}, {@code right}, etc.
     *
     * @return Array of signs for each turn lane. There can be multiple signs. For example, a turning
     * lane can have a sign with an arrow pointing left and another sign with an arrow pointing
     * straight.
     * @since 2.0.0
     */
    public List<String> getIndications() {
        return indications;
    }

    /**
     * Sets Array of signs for each turn lane. There can be multiple signs. For example, a turning
     * lane can have a sign with an arrow pointing left and another sign with an arrow pointing
     * straight.
     *
     * @param indications a {@code String} array made up of one or more signs such as {@code left}, {@code right}, etc.
     * @since 2.1.0
     */
    public void setIndications(List<String> indications) {
        this.indications = indications;
    }
}

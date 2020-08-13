/**
 * 
 */
package com.hortonsoft.foodtruck;

import org.glassfish.jersey.server.ResourceConfig;

public class JerseyConfiguration extends ResourceConfig {

    public JerseyConfiguration() {
        packages("com.hortonsoft.foodtruck");
    }
}
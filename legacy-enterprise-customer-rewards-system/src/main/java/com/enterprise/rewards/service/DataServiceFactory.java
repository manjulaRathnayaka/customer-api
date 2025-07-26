
package com.enterprise.rewards.service;

import javax.servlet.ServletContext;

/**
 * Factory for creating data service instances
 * Supports configuration via web.xml context parameters
 * Legacy 2015 factory pattern implementation
 */
public class DataServiceFactory {
    /**
     * Overload for JSPs using the implicit 'application' object
     */
    public static DataServiceInterface getInstance() {
        // Use the singleton instance if already created
        if (instance != null) return instance;
        throw new IllegalStateException("DataServiceFactory.getInstance() called before initialization. Use getInstance(ServletContext) at least once.");
    }
    // Singleton instance
    private static DataServiceInterface instance;

    /**
     * Returns a singleton DataServiceInterface instance based on context-param
     * "dataService.implementation" (DATABASE or IN_MEMORY)
     */
    public static DataServiceInterface getInstance(ServletContext context) {
        if (instance == null) {
            String impl = context.getInitParameter("dataService.implementation");
            if ("DATABASE".equalsIgnoreCase(impl)) {
                instance = new DatabaseDataService(context);
            } else {
                throw new UnsupportedOperationException("Only DATABASE implementation is supported.");
            }
        }
        return instance;
    }
}

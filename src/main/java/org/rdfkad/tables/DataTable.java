package org.rdfkad.tables;

import java.util.concurrent.ConcurrentHashMap;

public class DataTable {
    // Static variable single_instance of type DataTable
    private static DataTable single_instance = null;

    // The ConcurrentHashMap that will be used across the application
    private ConcurrentHashMap<String, Object> dataTable;

    // Private constructor to restrict instantiation of the class from other classes
    private DataTable() {
        dataTable = new ConcurrentHashMap<>();
    }

    // Static method to create instance of DataTable class
    public static DataTable getInstance() {
        if (single_instance == null)
            single_instance = new DataTable();

        return single_instance;
    }

    // Method to get the concurrent hash map
    public ConcurrentHashMap<String, Object> getMap() {
        return dataTable;
    }

    // Methods to manipulate the map can be added here to control access and provide additional functionality
}

package com.paisatrack.entity;

/**
 * Represents the two supported financial entry types in PaisaTrack.
 */
public enum CategoryType {
    INCOME("Income"),
    EXPENSE("Expense");

    private final String label;

    CategoryType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

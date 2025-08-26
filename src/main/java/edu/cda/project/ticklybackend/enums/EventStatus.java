package edu.cda.project.ticklybackend.enums;

public enum EventStatus {
    DRAFT, // Event in preparation, not publicly visible
    PUBLISHED, // Event published and visible
    CANCELLED, // Event cancelled
    COMPLETED, // Event completed
    ARCHIVED // Event archived, not visible in active listings
}
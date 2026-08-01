/*
* Used to reference status of businesses, users, */
package model.enums;

public enum EntityStatus {
    ACTIVE,             //default on creation
    CHANGES_REQUESTED,  //email sent, user notified of required corrections
    PENDING,            //user has submitted corrections, awaiting admin/dev review
    TAKEN_DOWN,         //admin permanently removes entity, separate api call to correct back to being available
    REMOVED,            //soft delete from any view, for obsolete or no longer existing listings or businesses
    DELETED             //user action, chose to take down entity
}
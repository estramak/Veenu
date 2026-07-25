/*
* Used to reference status of businesses, users, */
package model.enums;

public enum EntityStatus {
    ACTIVE,             //default on creation
    SUSPENDED,          //admin/dev action, temporary
    CHANGES_REQUESTED,  //email sent, user notified of required corrections
    PENDING,            //user has submitted corrections, awaiting admin/dev review
    BANNED              //permanent, no correction path
}
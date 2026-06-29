package model.enums;

public enum EntityStatus {
    ACTIVE,             //default on creation
    SUSPENDED,          //temporary, correctable
    CHANGES_REQUESTED,  //email sent, user notified of required corrections
    PENDING,            //user has submitted corrections, awaiting admin/dev review
    BANNED              //permanent, no correction path
}
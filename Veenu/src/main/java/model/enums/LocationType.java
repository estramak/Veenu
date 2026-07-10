/*
* Referenced in Listing class, acts as type label for each location on the map
*/
package model.enums;

public enum LocationType {
    BUSINESS,   //has an attached business entity
    PARK,       //public green space
    LANDMARK,   //notable location(building, statue, etc)
    TRAIL,      //hiking, biking, walking path
    VENUE,      //recurring event space without a permanent business
    GENERAL     //unclassified or temporary placeholder
}

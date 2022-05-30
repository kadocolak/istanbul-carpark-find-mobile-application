package com.kadircolak.parket;

public class Post {

    private int parkID;

    private String parkName;

    private double lat;

    private double lng;

    private int capacity;

    private int emptyCapacity;

    private String workHours;

    private int isOpen;

    public int getParkID() {
        return parkID;
    }

    public String getParkName() {
        return parkName;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEmptyCapacity() {
        return emptyCapacity;
    }

    public String getWorkHours() {
        return workHours;
    }

    public int getIsOpen() {
        return isOpen;
    }
}

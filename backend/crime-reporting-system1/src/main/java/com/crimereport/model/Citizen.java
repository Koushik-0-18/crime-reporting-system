package com.crimereport.model;

public class Citizen {
    private int citizenId;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String address;
    private String password;

    public Citizen(int citizenId, String fullName, String mobileNumber, String email, String address, String password) {
        this.citizenId = citizenId;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.address = address;
        this.password = password;
    }

    public int getCitizenId() { return citizenId; }
    public String getFullName() { return fullName; }
    public String getMobileNumber() { return mobileNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getPassword() { return password; }
}
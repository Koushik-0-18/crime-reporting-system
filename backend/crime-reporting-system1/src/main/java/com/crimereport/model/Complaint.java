package com.crimereport.model;

public class Complaint {
    private int complaintId;
    private int citizenId;
    private String description;
    private String incidentDate;
    private String incidentTime;
    private String location;
    private String status;

    public Complaint(int complaintId, int citizenId, String description, String incidentDate, String incidentTime, String location, String status) {
        this.complaintId = complaintId;
        this.citizenId = citizenId;
        this.description = description;
        this.incidentDate = incidentDate;
        this.incidentTime = incidentTime;
        this.location = location;
        this.status = status;
    }

    public int getComplaintId() { return complaintId; }
    public int getCitizenId() { return citizenId; }
    public String getDescription() { return description; }
    public String getIncidentDate() { return incidentDate; }
    public String getIncidentTime() { return incidentTime; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
}
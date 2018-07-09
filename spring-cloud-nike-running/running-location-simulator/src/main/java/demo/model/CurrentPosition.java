package demo.model;

import lombok.Data;

@Data
public class CurrentPosition {

    private String runningId;
    private Point location;
    private RunnerStatus runnerStatus =RunnerStatus.NONE;
    private double speed;
    private double heading;
    private MedicalInfo medicalInfo;

    public CurrentPosition(){

    }

    public CurrentPosition(String runningId, Point location, RunnerStatus runnerStatus, double speed, double heading, MedicalInfo medicalInfo){
        this.runningId = runningId;
        this.location = location;
        this.runnerStatus = runnerStatus;
        this.speed = speed;
        this.heading = heading;
        this.medicalInfo = medicalInfo;
    }
}

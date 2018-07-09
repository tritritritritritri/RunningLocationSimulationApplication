package demo.task;


import demo.model.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocationSimulator implements Runnable {

    private long id;

    private AtomicBoolean cancel =  new AtomicBoolean();

    private  double speedInMps;

    private boolean shouldMove;
    private boolean exportPositionsToMessaging = true;
    private Integer reportInterval = 500;

    private PositionInfo currentPosition = null;

    private List<Leg> legs;
    private RunnerStatus runnerStatus = RunnerStatus.NONE;
    private String runningId;

    private Point startPoint;
    private Date executionStartTime;

    private MedicalInfo medicalInfo;

    public LocationSimulator(GpsSimulatorRequest gpsSimulatorRequest) {
        this.shouldMove = gpsSimulatorRequest.isMove();
        this.exportPositionsToMessaging = gpsSimulatorRequest.isExportPositionsToMessaging();
        this.setSpeed(gpsSimulatorRequest.getSpeed());
        this.reportInterval = gpsSimulatorRequest.getReportInterval();

        this.runningId = gpsSimulatorRequest.getRunningId();
        this.runnerStatus = gpsSimulatorRequest.getRunnerStatus();
        this.medicalInfo = gpsSimulatorRequest.getMedicalInfo();
    }

    private void setSpeed(double speed) {
        this.speedInMps = speed;
    }

    @Override
    public void run() {
        try {
            executionStartTime = new Date();
            if (cancel.get()) {
                destroy();
                return;
            }
            while (currentPosition != null) {
                long startTIme = new Date().getTime();

                if (currentPosition != null) {
                    if (shouldMove) {
                        moveRunningLocation();
                        currentPosition.setSpeed(speedInMps);
                    } else {
                        currentPosition.setSpeed(0.0);
                    }

                    currentPosition.setRunnerStatus(this.runnerStatus);

                    final MedicalInfo medicalInfoToUse;

                    switch (this.runnerStatus) {

                        case SUPPLY_NOW:
                        case SUPPLY_SOON:
                        case STOP_NOW:
                            medicalInfoToUse = this.medicalInfo;
                            break;
                        default:
                            medicalInfoToUse = null;
                            break;
                    }

                    final CurrentPosition currentPosition = new CurrentPosition(this.currentPosition.getRunningId(),
                            new Point(this.currentPosition.getPosition().getLatitude(), this.currentPosition.getPosition().getLongitude()),
                            this.currentPosition.getRunnerStatus(),
                            this.currentPosition.getSpeed(),
                            this.currentPosition.getLeg().getHeading(),
                            medicalInfoToUse);

                    // send current position to distribution service by rest API
                    //@TODO implement positionInfoService

                }
                //wait until next position report
                sleep(startTIme);
            }

        } catch (InterruptedException ie) {
            destroy();
            return;
        }
        destroy();
    }

    private void destroy () {
        currentPosition = null;
    }

    private void sleep (long startTime) throws InterruptedException {
        long endTime = new Date().getTime();
        long elapsedTime = endTime - startTime;
        long sleepTime = reportInterval - elapsedTime > 0 ? reportInterval - elapsedTime : 0;
        sleep(sleepTime);
    }

    private void moveRunningLocation() {

        double distance = speedInMps * reportInterval / 1000.0;
        double distanceFromStart = currentPosition.getDistanceFromStart() + distance;
        double excess = 0.0;


        for (int i = currentPosition.getLeg().getId(); i < legs.size(); i++) {
            Leg currentLeg = legs.get(i);
            excess = distanceFromStart > currentLeg.getLength() ? distanceFromStart - currentLeg.getLength() : 0;
            if (Double.doubleToLongBits(excess) == 0) {
                currentPosition.setDistanceFromStart(distanceFromStart);
                currentPosition.setLeg(currentLeg);
                //@TODO implement a new position calculation method in NavUtils
                Point newPosition = null;
                currentPosition.setPosition(newPosition);
                return;
            }
            distanceFromStart = excess;
        }

        setStartPosition();

    }

    private void setStartPosition() {
        currentPosition = new PositionInfo();
        currentPosition.setRunningId(this.runningId);
        Leg leg = legs.get(0);
        currentPosition.setLeg(leg);
        currentPosition.setPosition(leg.getStartPosition());
        currentPosition.setDistanceFromStart(0.0);
    }

}

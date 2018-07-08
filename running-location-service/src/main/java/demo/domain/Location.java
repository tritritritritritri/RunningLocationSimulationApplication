package demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

// Object module 1 filed - DB 1 column :ORM
//               1       -    many
//               many    -    1

// Json <-> Object model <- (ORM) -> Relational Model

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Data
@Table(name = "LOCATIONS")
public class Location {

    public enum GpsStatus {
        EXCELLENT, OK, UNRELIABLE, BAD, NOFIX, UNKNOWN;
    }

    public enum RunnerMovementType{
        STOPPED, IN_MOTION;
    }

    @Id
    @GeneratedValue
    private long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fmi", column = @Column(name = "medical_fmi")),
            @AttributeOverride(name = "bfr", column = @Column(name = "medical_bfr"))
    })
    private MedicalInfo medicalInfo;

    @Embedded
    @AttributeOverride(name = "bandMake", column = @Column(name = "unit_band_make"))
    private UnitInfo unitInfo;

    private double latitude;
    private double longitude;
    private String heading;
    private double gpsSpeed;
    private GpsStatus gpsStatus;
    private double odometer;
    private double totalRunningTime;
    private double totalIdleTime;
    private double totalCalorieBurnt;
    private String address;
    private Date timestamp = new Date();
    private String gearProvider;
    private RunnerMovementType runnerMovementType;
    private String serviceType;

    @JsonCreator
    public Location(@JsonProperty("runningId") String runningId) {

        this.unitInfo = new UnitInfo(runningId);

    }

    public Location () {

    }

    public Location(UnitInfo unitInfo) {
        this.unitInfo = unitInfo;
    }

    public String getRunningId() {
        return this.unitInfo == null ? null : this.unitInfo.getRunningId();
    }



}

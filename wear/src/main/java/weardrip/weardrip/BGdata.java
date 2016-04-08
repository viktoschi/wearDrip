package weardrip.weardrip;
 import com.google.gson.annotations.Expose;
 import com.google.gson.annotations.SerializedName;

 import io.realm.RealmObject;

public class BGdata extends RealmObject{

    @SerializedName("a")
    @Expose
    private Double a;
    @SerializedName("age_adjusted_raw_value")
    @Expose
    private Double ageAdjustedRawValue;
    @SerializedName("b")
    @Expose
    private Double b;
    @SerializedName("c")
    @Expose
    private Double c;
    @SerializedName("calculated_value")
    @Expose
    private Double calculatedValue;
    @SerializedName("calculated_value_slope")
    @Expose
    private Double calculatedValueSlope;
    @SerializedName("calibration_flag")
    @Expose
    private Boolean calibrationFlag;
    @SerializedName("calibration_uuid")
    @Expose
    private String calibrationUuid;
    @SerializedName("filtered_data")
    @Expose
    private Double filteredData;
    @SerializedName("ra")
    @Expose
    private Double ra;
    @SerializedName("raw_data")
    @Expose
    private Double rawData;
    @SerializedName("rb")
    @Expose
    private Double rb;
    @SerializedName("rc")
    @Expose
    private Double rc;
    @SerializedName("sensor_uuid")
    @Expose
    private String sensorUuid;
    @SerializedName("time_since_sensor_started")
    @Expose
    private Double timeSinceSensorStarted;
    @SerializedName("timestamp")
    @Expose
    private double timestamp;
    @SerializedName("uuid")
    @Expose
    private String uuid;

    /**
     *
     * @return
     * The a
     */
    public Double getA() {
        return a;
    }

    /**
     *
     * @param a
     * The a
     */
    public void setA(Double a) {
        this.a = a;
    }

    /**
     *
     * @return
     * The ageAdjustedRawValue
     */
    public Double getAgeAdjustedRawValue() {
        return ageAdjustedRawValue;
    }

    /**
     *
     * @param ageAdjustedRawValue
     * The age_adjusted_raw_value
     */
    public void setAgeAdjustedRawValue(Double ageAdjustedRawValue) {
        this.ageAdjustedRawValue = ageAdjustedRawValue;
    }

    /**
     *
     * @return
     * The b
     */
    public Double getB() {
        return b;
    }

    /**
     *
     * @param b
     * The b
     */
    public void setB(Double b) {
        this.b = b;
    }

    /**
     *
     * @return
     * The c
     */
    public Double getC() {
        return c;
    }

    /**
     *
     * @param c
     * The c
     */
    public void setC(Double c) {
        this.c = c;
    }

    /**
     *
     * @return
     * The calculatedValue
     */
    public Double getCalculatedValue() {
        return calculatedValue;
    }

    /**
     *
     * @param calculatedValue
     * The calculated_value
     */
    public void setCalculatedValue(Double calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    /**
     *
     * @return
     * The calculatedValueSlope
     */
    public Double getCalculatedValueSlope() {
        return calculatedValueSlope;
    }

    /**
     *
     * @param calculatedValueSlope
     * The calculated_value_slope
     */
    public void setCalculatedValueSlope(Double calculatedValueSlope) {
        this.calculatedValueSlope = calculatedValueSlope;
    }

    /**
     *
     * @return
     * The calibrationFlag
     */
    public Boolean getCalibrationFlag() {
        return calibrationFlag;
    }

    /**
     *
     * @param calibrationFlag
     * The calibration_flag
     */
    public void setCalibrationFlag(Boolean calibrationFlag) {
        this.calibrationFlag = calibrationFlag;
    }

    /**
     *
     * @return
     * The calibrationUuid
     */
    public String getCalibrationUuid() {
        return calibrationUuid;
    }

    /**
     *
     * @param calibrationUuid
     * The calibration_uuid
     */
    public void setCalibrationUuid(String calibrationUuid) {
        this.calibrationUuid = calibrationUuid;
    }

    /**
     *
     * @return
     * The filteredData
     */
    public Double getFilteredData() {
        return filteredData;
    }

    /**
     *
     * @param filteredData
     * The filtered_data
     */
    public void setFilteredData(Double filteredData) {
        this.filteredData = filteredData;
    }

    /**
     *
     * @return
     * The ra
     */
    public Double getRa() {
        return ra;
    }

    /**
     *
     * @param ra
     * The ra
     */
    public void setRa(Double ra) {
        this.ra = ra;
    }

    /**
     *
     * @return
     * The rawData
     */
    public Double getRawData() {
        return rawData;
    }

    /**
     *
     * @param rawData
     * The raw_data
     */
    public void setRawData(Double rawData) {
        this.rawData = rawData;
    }

    /**
     *
     * @return
     * The rb
     */
    public Double getRb() {
        return rb;
    }

    /**
     *
     * @param rb
     * The rb
     */
    public void setRb(Double rb) {
        this.rb = rb;
    }

    /**
     *
     * @return
     * The rc
     */
    public Double getRc() {
        return rc;
    }

    /**
     *
     * @param rc
     * The rc
     */
    public void setRc(Double rc) {
        this.rc = rc;
    }

    /**
     *
     * @return
     * The sensorUuid
     */
    public String getSensorUuid() {
        return sensorUuid;
    }

    /**
     *
     * @param sensorUuid
     * The sensor_uuid
     */
    public void setSensorUuid(String sensorUuid) {
        this.sensorUuid = sensorUuid;
    }

    /**
     *
     * @return
     * The timeSinceSensorStarted
     */
    public Double getTimeSinceSensorStarted() {
        return timeSinceSensorStarted;
    }

    /**
     *
     * @param timeSinceSensorStarted
     * The time_since_sensor_started
     */
    public void setTimeSinceSensorStarted(Double timeSinceSensorStarted) {
        this.timeSinceSensorStarted = timeSinceSensorStarted;
    }

    /**
     *
     * @return
     * The timestamp
     */
    public double getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     * The timestamp
     */
    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    /**
     *
     * @return
     * The uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     *
     * @param uuid
     * The uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
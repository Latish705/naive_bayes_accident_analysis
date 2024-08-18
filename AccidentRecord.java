public class AccidentRecord {
    String junctionControl;
    String junctionDetail;
    String lightConditions;
    String roadSurfaceConditions;
    String weatherConditions;
    String vehicleType;
    String accidentSeverity; // This is the target

    public AccidentRecord(String junctionControl, String junctionDetail, String lightConditions,
                          String roadSurfaceConditions, String weatherConditions, String vehicleType, String accidentSeverity) {
        this.junctionControl = junctionControl;
        this.junctionDetail = junctionDetail;
        this.lightConditions = lightConditions;
        this.roadSurfaceConditions = roadSurfaceConditions;
        this.weatherConditions = weatherConditions;
        this.vehicleType = vehicleType;
        this.accidentSeverity = accidentSeverity;
    }
}

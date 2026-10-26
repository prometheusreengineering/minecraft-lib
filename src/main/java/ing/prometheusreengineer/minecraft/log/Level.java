package ing.prometheusreengineer.minecraft.log;

public enum Level {
    /** Off unless {@code -Dprometheus.debug=true} or {@code -Dprometheus.<project>.debug=true}. */
    DEBUG,
    INFO,
    WARN,
    ERROR
}

package raidzero.robot;

/**
 * Some robot-wide settings.
 *
 * <p>Other classes can use the constants defined in this class to have different behavior based on
 * different settings of the robot. This way the settings can be easily changed in one location.
 */

public class Settings {

    /**
     * The version of the robot.
     */
    public static final Version VERSION = Version.PRAC;

    /**
     * The possible versions of the robot.
     */
    public static enum Version { PRAC, COMP }
}

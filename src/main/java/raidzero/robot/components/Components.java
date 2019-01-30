package raidzero.robot.components;

/**
 * The components of the robot.
 * 
 * <p>Do not construct an instance of this class.
 * 
 * <p>Make sure the {@link #initialize()} method has been called before accessing any components.
 */
public class Components {

    private static Intake intake;
    private static Lift lift;

    /**
     * Initializes each component by calling its constructor.
     * 
     * <p>Make sure this method has been called before accessing any components, as they will be
     * null before this method is called.
     */

    public static void initialize() {
        intake = new Intake(14, 2, 3); 
        lift = new Lift(10, 11);
    }

    /**
     * Returns the intake component.
     * 
     * @return the intake component
     */
    public static Intake getIntake() {
        return intake;
    }

    /**
     * Returns the lift component.
     * 
     * @return the lift component
     */

    public static Lift getLift() {
        return lift;
    }

}
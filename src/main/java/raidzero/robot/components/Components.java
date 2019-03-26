package raidzero.robot.components;

/**
 * The components of the robot.
 *
 * <p>Do not construct an instance of this class.
 *
 * <p>Make sure the {@link #initialize()} method has been called before accessing any components.
 */
public class Components {

    private static Base base;
    private static Lift lift;
    private static Arm arm;
    private static Intake intake;
    private static Climb climb;

    /**
     * Initializes each component by calling its constructor.
     *
     * <p>Make sure this method has been called before accessing any components, as they will be
     * null before this method is called.
     */
    public static void initialize() {
        base = new Base(3, 4, 5, 11, 12, 13, 0);
        lift = new Lift(2, 1);
        arm = new Arm(14, 15);
        intake = new Intake(2, 1);
        climb = new Climb(6, 8, 7, 11, 0, 1);
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

    /**
     * Returns the arm component.
     *
     * @return the arm component
     */
    public static Arm getArm() {
        return arm;
    }

    /**
     * Returns the climb component.
     *
     * @return the climb component
     */
    public static Climb getClimb() {
        return climb;
    }

    /**
     * Returns the base component.
     *
     * @return the base component
     */
    public static Base getBase() {
        return base;
    }

}

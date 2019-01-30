package raidzero.robot.teleop;

import edu.wpi.first.wpilibj.XboxController;

import static edu.wpi.first.wpilibj.GenericHID.Hand.kLeft;
import static edu.wpi.first.wpilibj.GenericHID.Hand.kRight;

import raidzero.robot.components.Components;

public class Teleop {

    private static XboxController controller1;
    private static XboxController controller2;

    /**
     * Initializes the teleop-specific components.
     * 
     * <p>This should be called when the robot starts up.
     */
    public static void initialize() {
        controller1 = new XboxController(0);
        controller2 = new XboxController(1);
    }

    /**
     * Configures the components for use in teleop mode.
     * 
     * <p>This should be called once every time the robot is switched to teleop mode, before calling
     * {@link #run()}.
     */
    public static void setup() {
        
    }

    /**
     * Runs the teleop code.
     * 
     * <p>This should be called repeatedly during teleop mode.
     */
    public static void run() {
        
        // Player 1

        // Lift
        double rightTriggerAxis1 = controller1.getTriggerAxis(kRight);
        double leftTriggerAxis1 = controller1.getTriggerAxis(kLeft);

        Components.getLift().movePercent(rightTriggerAxis1 - (leftTriggerAxis1 * 0.5));
        
        // Player 2

        // Intake
        double rightTriggerAxis2 = controller2.getTriggerAxis(kRight);
        double leftTriggerAxis2 = controller2.getTriggerAxis(kLeft);
        if (rightTriggerAxis2 > 0.1) {
            Components.getIntake().runWheelsIn(rightTriggerAxis2);
        } else if (leftTriggerAxis2 > 0.1) {
            Components.getIntake().runWheelsOut(leftTriggerAxis2);
        } else {
            Components.getIntake().stopWheels();
        }

    }

}
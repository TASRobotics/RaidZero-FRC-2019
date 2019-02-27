package raidzero.robot.teleop;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.XboxController;

import static edu.wpi.first.wpilibj.GenericHID.Hand.kLeft;
import static edu.wpi.first.wpilibj.GenericHID.Hand.kRight;

import com.ctre.phoenix.motorcontrol.ControlMode;

import raidzero.robot.components.Components;
import raidzero.robot.components.Arm.Position;

public class Teleop {

    private static XboxController controller1;
    private static XboxController controller2;

    private static int armPos;
    private static final int ARM_MAX = 2140;
    private static final int ARM_MIN = 0;

    private static boolean climbing = false;

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
        climbing = false;
        armPos = Components.getArm().getEncoderPos();
    }

    /**
     * Runs the teleop code.
     *
     * <p>This should be called repeatedly during teleop mode.
     */
    public static void run() {

        // Buttons to toggle the climb
        if (controller1.getStartButton() && controller2.getStartButton()) {
            climbing = true;
        }
        if (controller1.getBackButton() || controller2.getBackButton()) {
            climbing = false;
        }

        // Player 1

        // Drive
        if (controller1.getBumper(kRight)) {
            Components.getBase().getRightMotor().set(ControlMode.PercentOutput,
                controller1.getY(kRight));
            Components.getBase().getLeftMotor().set(ControlMode.PercentOutput,
                controller1.getY(kLeft));
        } else {
            Components.getBase().getRightMotor().set(ControlMode.PercentOutput,
                -controller1.getY(kRight));
            Components.getBase().getLeftMotor().set(ControlMode.PercentOutput,
                -controller1.getY(kLeft));
        }

        // Lift
        double rightTriggerAxis1 = controller1.getTriggerAxis(kRight);
        double leftTriggerAxis1 = controller1.getTriggerAxis(kLeft);

        if (rightTriggerAxis1 > 0.1) {
            Components.getLift().movePercent(rightTriggerAxis1);
        } else if (leftTriggerAxis1 > 0.1) {
            Components.getLift().movePercent(-leftTriggerAxis1 * 0.5);
        } else {
            Components.getLift().movePercent(0);
        }

        // Player 2

        // Arm
        Components.getArm().movePercentOutput(-controller2.getY(kRight));

        // Components.getArm().move(armPos);
        // armPos = (int) (armPos - (controller2.getY(kRight) * 80));

        // // Prevent overrotation
        // if (armPos >= ARM_MAX) {
        //     armPos = ARM_MAX;
        // } else if (armPos <= ARM_MIN) {
        //     armPos = ARM_MIN;
        // }

        // Intake Wheels
        double rightTriggerAxis2 = controller2.getTriggerAxis(kRight);
        double leftTriggerAxis2 = controller2.getTriggerAxis(kLeft);
        if (rightTriggerAxis2 > 0.1) {
            Components.getIntake().runWheelsIn(rightTriggerAxis2);
        } else if (leftTriggerAxis2 > 0.1) {
            Components.getIntake().runWheelsOut(leftTriggerAxis2);
        } else {
            Components.getIntake().stopWheels();
        }

        // Hook
        if (controller2.getBumper(kRight)) {
            Components.getIntake().grab();
        } else if (controller2.getBumper(kLeft)) {
            Components.getIntake().release();
        } else {
            Components.getIntake().stopHook();
        }

        //Components.getClimb().lockClimb();
        // Climb

        if (controller2.getPOV() == 0) {
            Components.getClimb().lockClimb();
        } else if (controller2.getPOV() == 180) {
            Components.getClimb().unlockClimb();
        }

        if (climbing) {
           // Components.getClimb().unlockClimb();
            Components.getClimb().climbPWM(controller2.getY(kLeft));
        }

    }

}

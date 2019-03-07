package raidzero.robot.teleop;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.PowerDistributionPanel;

import static edu.wpi.first.wpilibj.GenericHID.Hand.kLeft;
import static edu.wpi.first.wpilibj.GenericHID.Hand.kRight;

import com.ctre.phoenix.motorcontrol.ControlMode;

import raidzero.robot.components.Components;
import raidzero.robot.components.Arm;
import raidzero.robot.components.Lift;

public class Teleop {

    private static XboxController controller1;
    private static XboxController controller2;
    private static PowerDistributionPanel pdp;

    private static int armSetpoint;

    private static boolean climbing = false;

    /**
     * Initializes the teleop-specific components.
     *
     * <p>This should be called when the robot starts up.
     */
    public static void initialize() {
        controller1 = new XboxController(0);
        controller2 = new XboxController(1);

        UsbCamera cam = CameraServer.getInstance().startAutomaticCapture(0);
        cam.setResolution(480, 320);
        cam.setFPS(30);

        pdp = new PowerDistributionPanel(0);
    }

    /**
     * Configures the components for use in teleop mode.
     *
     * <p>This should be called once every time the robot is switched to teleop mode, before calling
     * {@link #run()}.
     */
    public static void setup() {
        climbing = false;

        // Set starting setpoint as the current position
        armSetpoint = Components.getArm().getEncoderPos();
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
        // Tank
        if (controller1.getBumper(kRight)) {
            Components.getBase().getRightMotor().set(ControlMode.PercentOutput,
                controller1.getY(kLeft) * 0.8);
            Components.getBase().getLeftMotor().set(ControlMode.PercentOutput,
                controller1.getY(kRight) * 0.8);
        } else {
            Components.getBase().getRightMotor().set(ControlMode.PercentOutput,
                -controller1.getY(kRight) * 0.8);
            Components.getBase().getLeftMotor().set(ControlMode.PercentOutput,
                -controller1.getY(kLeft) * 0.8);
        }

        // Arcade
        // if (controller1.getBumper(kRight)) {
        //     Components.getBase().getRightMotor().set(ControlMode.PercentOutput,
        //         controller1.getY(kLeft) + controller1.getX(kRight));
        //     Components.getBase().getLeftMotor().set(ControlMode.PercentOutput,
        //         controller1.getY(kLeft) - controller1.getX(kRight));
        // } else {
        //     Components.getBase().getRightMotor().set(ControlMode.PercentOutput,
        //         -controller1.getY(kLeft) + controller1.getX(kRight));
        //     Components.getBase().getLeftMotor().set(ControlMode.PercentOutput,
        //         -controller1.getY(kLeft) - controller1.getX(kRight));
        // }

        // Lift
        Components.getLift().limitReset();
        // Presets
        if (controller1.getYButton()) {
            Components.getLift().movePosition(Lift.THIRD_ROCKET);
        } else if (controller1.getBButton()) {
            Components.getLift().movePosition(Lift.SECOND_ROCKET);
        } else if (controller1.getAButton()) {
            Components.getLift().movePosition(Lift.CARGO_SHIP);
        } else if (controller1.getXButton()) {
            Components.getLift().movePosition(Lift.FIRST_ROCKET_BALL);
        } else { // Manual control
            double rightTriggerAxis1 = controller1.getTriggerAxis(kRight);
            double leftTriggerAxis1 = controller1.getTriggerAxis(kLeft);
            if (rightTriggerAxis1 > 0.1) {
                Components.getLift().movePercent(rightTriggerAxis1 * 0.6);
            } else if (leftTriggerAxis1 > 0.1) {
                Components.getLift().movePercent(-leftTriggerAxis1 * 0.2);
            } else {
                Components.getLift().movePercent(0.0);
            }
        }
        if (controller1.getBumper(kLeft)) {
            Components.getLift().resetEncoderPos();
        }
        System.out.println("Lift encoder = " + Components.getLift().getEncoderPos());

        // Player 2

        // Arm
        // Components.getArm().movePercentOutput(-controller2.getY(kRight));

        Components.getArm().move(armSetpoint);

        // Reset setpoint when limit is reached
        if (Components.getArm().getReverseLimit() && Components.getArm().getEncoderPos() < 100) {
            Components.getArm().setEncoderPos(0);
            armSetpoint = 0;
            System.out.println("Arm limit switch reached!");
        }
        // Change the setpoint for the arm
        if (Math.abs(controller2.getY(kRight)) > 0.1) {
            armSetpoint = (int) (armSetpoint - (controller2.getY(kRight) * 80));
        } else if (controller2.getXButton()) {
            armSetpoint = Arm.BALL_INTAKE;
        } else if (controller2.getYButton()) {
            armSetpoint = Arm.STARTING_POS;
        } else if (controller2.getBButton()) {
            armSetpoint = Arm.CARGO;
        } else if (controller2.getAButton()) {
            armSetpoint = Arm.ROCKET_BALL;
        }
        // System.out.println("Arm SP = " + armSetpoint + " | Encoder = " + Components.getArm().getEncoderPos());

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

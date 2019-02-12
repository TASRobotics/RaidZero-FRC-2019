package raidzero.robot.components;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;

public class SparkMaxPrime extends CANSparkMax {

    private static final double MAX_OUTPUT = 1.0;
    private static final double MIN_OUTPUT = -1.0;

    /*
     * These objects are stored as instance variables so that
     * there is no overhead when calling PID controller and
     * encoder methods.
     *
     * @see com.revrobotics.CANSparkMax#getEncoder()
     */
    private CANPIDController controller;
    private CANEncoder encoder;

    /**
     * Creates the SparkMaxPrime object.
     *
     * @param deviceID the ID of the motor controller
     * @param MotorType the type of the motor, Brushless or Brushed
     */
    public SparkMaxPrime(int deviceID, MotorType type) {
        super(deviceID, type);
        controller = getPIDController();
        encoder = getEncoder();
    }

    /**
     * Sets up the FPID and IZone.
     *
     * @param kF the feed forward value to set
     * @param kP the P gain value to set
     * @param kI the I gain value to set
     * @param kD the D gain value to set
     * @param iZone the IZone value to set
     * @param pidSlot the PID slot for this PID
     */
    public void setPID(double kF, double kP, double kI, double kD, double iZone, int pidSlot) {
        controller.setFF(kF, pidSlot);
        controller.setP(kP, pidSlot);
        controller.setI(kI, pidSlot);
        controller.setD(kD, pidSlot);
        controller.setIZone(iZone, pidSlot);
        controller.setOutputRange(MIN_OUTPUT, MAX_OUTPUT);
    }

    /**
     * Configure the parameters for SmartMotion
     *
     * @param minVel the mimimum velocity in RPM
     * @param maxVel the maxiumum velocity in RPM
     * @param maxAccel the maximum acceleration in RPM per second
     * @param allowedErr the allowed deviation for the setpoint vs. current rotations
     * @param smSlot the gain slot, ranging from 0 to 3
     */
    public void configureSmartMotion(double minVel, double maxVel, double maxAccel,
    double allowedErr, int smSlot) {
        controller.setSmartMotionMinOutputVelocity(minVel, smSlot);
        controller.setSmartMotionMaxVelocity(maxVel, smSlot);
        controller.setSmartMotionMaxAccel(maxAccel, smSlot);
        controller.setSmartMotionAllowedClosedLoopError(allowedErr, smSlot);
    }

    /**
     * Sets the motor to run. Changes behavior based off ControlType.
     *
     * <p> For DutyCycle, args is from -1 to 1 (the percent output of the motor).
     * <p> For Position, args is the position you want.
     * <p> For SmartMotion, args is the position you want.
     * <p> For Velocity, args is the velocity you want.
     * <p> For Voltage, args is the voltage you want.
     *
     * @param args the argument
     * @param type the control type
     * @param pidSlot the PID Slot
     */
    public void set(double args, ControlType type, int pidSlot) {
        controller.setReference(args, type, pidSlot);
    }

    /**
     * Sets the encoder to the desired position.
     *
     * @param pos the position desired
     */
    public void setPosition(double pos) {
        encoder.setPosition(pos);
    }

    /**
     * Gets the position of the encoder.
     *
     * @return the encoder position
     */
    public double getPosition() {
        return encoder.getPosition();
    }

}

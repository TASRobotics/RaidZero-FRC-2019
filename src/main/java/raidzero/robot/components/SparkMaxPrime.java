package raidzero.robot.components;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;

public class SparkMaxPrime extends CANSparkMax {

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

        // Reset conversion factors
        encoder.setPositionConversionFactor(1.0);
        encoder.setVelocityConversionFactor(1.0);
    }

    /**
     * Sets up the FPID and IZone.
     *
     * @param kF the feed forward value to set
     * @param kP the P gain value to set
     * @param kI the I gain value to set
     * @param kD the D gain value to set
     * @param iZone the IZone value to set
     * @param minOutput the minimum output of the motor
     * @param maxOutput the maximum output of the motor
     * @param pidSlot the PID slot for this PID
     */
    public void setPID(double kF, double kP, double kI, double kD, double iZone, double minOutput,
    double maxOutput, int pidSlot) {
        controller.setFF(kF, pidSlot);
        controller.setP(kP, pidSlot);
        controller.setI(kI, pidSlot);
        controller.setD(kD, pidSlot);
        controller.setIZone(iZone, pidSlot);
        controller.setOutputRange(minOutput, maxOutput, pidSlot);
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
     * Sets the encoder to the desired position in rotations.
     *
     * @param pos the position desired in rotations
     */
    public void setPosition(double pos) {
        encoder.setPosition(pos);
    }

    /**
     * Gets the position in rotations of the encoder.
     *
     * @return the encoder position in rotations
     */
    public double getPosition() {
        return encoder.getPosition();
    }

}

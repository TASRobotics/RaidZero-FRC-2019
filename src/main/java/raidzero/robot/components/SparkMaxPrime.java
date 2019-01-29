package raidzero.robot.components;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;

public class SparkMaxPrime extends CANSparkMax {

    private CANPIDController controller;
    private CANEncoder encoder;

    /**
     * zeroPos exists to deal with the inability to reset encoders
     * It basically gets the current position and becomes the relative position you want 
     */
    private double zeroPos = 0;

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
     * Sets up the PID.
     * 
     * @param FPID the FPID values and integral zone in that order
     * @param pidSlot the PID slot for this PID
     */
    public void setPID(double kF, double kP, double kI, double kD, double iZone, int pidSlot) {
        controller.setFF(kF, pidSlot);
        controller.setP(kP, pidSlot);
        controller.setI(kI, pidSlot);
        controller.setD(kD, pidSlot);
        controller.setIZone(iZone, pidSlot);
    }
    
    /**
     * Sets the motor to run. Changes behavior based off ControlType.
     * 
     * <p> For DutyCycle, args is from -1 to 1 (the percent output of the motor).
     * <p> For Position, args is the position you want.
     * <p> For Velocity, args is the velocity you want.
     * <p> For Voltage, args is the voltage you want.
     * 
     * @param args the argument
     * @param type the control type
     * @param pidSlot the PID Slot
     */
    public void set(double args, ControlType type, int pidSlot) {
        if (type == ControlType.kPosition) {
            controller.setReference(args + zeroPos, type, pidSlot);
        } else {
            controller.setReference(args, type, pidSlot);
        }
    }

    /**
     * Sets the encoder to the desired position.
     * 
     * @param pos the position desired
     */
    public void setPosition(double pos) {
        zeroPos = encoder.getPosition() - pos;
    }

    /**
     * Gets the position of the encoder.
     * 
     * @return the encoder position
     */
    public double getPosition() {
        return encoder.getPosition() - zeroPos;
    }

}

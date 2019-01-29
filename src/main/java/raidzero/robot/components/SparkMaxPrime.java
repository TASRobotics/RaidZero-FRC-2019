package raidzero.robot.components;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;

public class SparkMaxPrime extends CANSparkMax {
    private CANPIDController controller;
    private CANEncoder encoder;
    private double relZero = 0;

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
     * @param PIDSlot the PID slot for this PID
     */
    public void setPID(double[] FPID, int PIDSlot) {
        controller.setFF(FPID[0], PIDSlot);
        controller.setP(FPID[1], PIDSlot);
        controller.setI(FPID[2], PIDSlot);
        controller.setD(FPID[3], PIDSlot);
        controller.setIZone(FPID[4], PIDSlot);
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
     * @param PIDSlot the PID Slot
     */
    public void set(double args, ControlType type, int PIDSlot) {
        if (type == ControlType.kPosition){
            controller.setReference(args + relZero, type, PIDSlot);
        } else {
            controller.setReference(args, type, PIDSlot);
        }
    }

    /**
     * Sets the encoder to the desired position.
     * 
     * @param pos the position desired
     */
    public void setEncoder(double pos) {
        relZero = pos + encoder.getPosition();
    }

    /**
     * Gets the position of the encoder.
     * 
     * @return the encoder position
     */
    public double getPosition() {
        return encoder.getPosition() - relZero;
    }
}
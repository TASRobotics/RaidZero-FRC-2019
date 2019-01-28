package raidzero.robot.components;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity;

public class SparkSensors {
    private CANDigitalInput limitSwitch;
    private CANPIDController controller;
    private CANEncoder encoder;
    private double relZero = 0;

    /**
     * Creates the sparksensors object that corresponds to the input SparkMax.
     * 
     * @param leader the motor the sensors are on
     */
    public SparkSensors(CANSparkMax leader) {
        limitSwitch = leader.getForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed);
        controller = leader.getPIDController();
        encoder = leader.getEncoder();
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

    /**
     * Gets the velocity of the encoder in native units of 'RPM'.
     * 
     * @return the velocity of the encoder
     */
    public double getVel() {
        return encoder.getVelocity();
    }

    /**
     * Gets the PID Controller.
     * 
     * @return the PID Controller
     */
    public CANPIDController getPIDController() {
        return controller;
    }

    /**
     * Gets the limit switch.
     * 
     * @return the limit switch
     */
    public CANDigitalInput getLimitSwitch() {
        return limitSwitch;
    }
}
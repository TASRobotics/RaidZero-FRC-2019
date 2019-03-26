package raidzero.robot.components;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.PWM;

public class Climb {

    private CANSparkMax leader;
    private CANSparkMax follower1;
    private CANSparkMax follower2;
    private CANSparkMax follower3;

    private PWM servo1;
    private PWM servo2;

    // Overall gearing: 192:1
    // Banebots gearbox: 64:1
    // Physical outside gearbox: 54:18
    private static final double GEAR_RATIO = 192;
    // Rotations = angle/360
    private static final double ROTATIONS = 150 / 360;
    // Do not change the maxRotations for safety
    private static final double MAX_ROTATIONS = 160 / 360;
    private static final boolean ROTATIONS_SAFE = ROTATIONS < MAX_ROTATIONS;

    /**
     * Constructs a climb object, intializes the motors
     *
     * @param leaderID ID of the leader motor
     * @param follower1ID ID of the first follower motor
     * @param follower2ID ID of the second follower motor
     * @param follower3ID ID of the third follower motor
     * @param servoChannel
     */
    public Climb(int leaderID, int follower1ID, int follower2ID, int follower3ID,
    int servoChannel1, int servoChannel2) {

        leader = new CANSparkMax(leaderID, MotorType.kBrushless);
        follower1 = new CANSparkMax(follower1ID, MotorType.kBrushless);
        follower2 = new CANSparkMax(follower2ID, MotorType.kBrushless);
        follower3 = new CANSparkMax(follower3ID, MotorType.kBrushless);

        servo1 = new PWM(servoChannel1);
        servo2 = new PWM(servoChannel2);

        leader.restoreFactoryDefaults();
        follower1.restoreFactoryDefaults();
        follower2.restoreFactoryDefaults();
        follower3.restoreFactoryDefaults();

        // Disable direct inverts, motors are inverted in the follow section
        leader.setInverted(false);
        follower1.setInverted(false);
        follower2.setInverted(false);
        follower3.setInverted(false);


        // Set to brakemode
        leader.setIdleMode(IdleMode.kBrake);
        follower1.setIdleMode(IdleMode.kBrake);
        follower2.setIdleMode(IdleMode.kBrake);
        follower3.setIdleMode(IdleMode.kBrake);

        // Set follow(with inverts)
        follower1.follow(leader, true);
        follower3.follow(leader, true);
        follower2.follow(leader, false);

        leader.set(0);
    }

    /**
     * Gets the encoder position of any CANSparkMax
     *
     * @param sparkMax The Spark Max to find the position of
     * @return The encoder position
     */
    private static double getEncoderPos(CANSparkMax sparkMax) {
        return sparkMax.getEncoder().getPosition();
    }

    /**
     * Locks the climb by moving the servo
     */
    public void lockClimb() {
        servo1.setPosition(-1);
        servo2.setPosition(-1);
    }

    /**
     * Unlocks the climb by moving the servo
     */
    public void unlockClimb() {
        servo1.setPosition(1);
        servo2.setPosition(1);
    }

    /**
     * Climbs while the input is true
     *
     * @param input The variable that controls the climb (must be held true during climb)
     */
    public void climb(boolean input) {
        // PLEASE ENSURE THAT THE MECHANICAL LEADER MOTOR DIRECTION MATCHES
        //  WITH THE LEADER MOTOR DIRECTION IN CODE
        //      (motors spinning in a positive direction will move
        //      clockwise when looking at the ROTOR side of the motor)
        // the planetary gearbox will NOT reverse the motor direction,
        //  but every additional gear after the first one will reverse the direction

        if (ROTATIONS * GEAR_RATIO > Math.abs(getEncoderPos(leader)) && input) {
            moveLeapFrog();
        } else {
            stopLeapFrog();
        }
    }

    /**
     * Moves the climb by PWM
     * @param speed
     */
    public void climbPWM(double speed) {
        leader.set(speed);
    }

    /**
     * Moves the climb, while also checking that the number of rotations set is not too high
     */
    private void moveLeapFrog() {
        if (ROTATIONS_SAFE) {
            leader.set(1);
        } else {
            stopLeapFrog();
        }
    }

    /**
     * Stops the climb
     */
    private void stopLeapFrog() {
        leader.set(0);
    }

}

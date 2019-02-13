package raidzero.robot.components;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Climb{

    private CANSparkMax leader;
    private CANSparkMax follower1;
    private CANSparkMax follower2;
    private CANSparkMax follower3;

    //overall gearing: 192:1
            //banebots gearbox: 64:1
            //physical outside gearbox: 54:18
    private final double GEAR_RATIO = 192;
    //rotations = angle/360
    private static final double ROTATIONS = 150 / 360;
    //do not change the maxRotations for safety
    private final double MAX_ROTATIONS = 160/360;
    private static boolean rotationsSafe = true;

    /**
     * Gets the encoder position
     *
     * @return the encoder position
     */
    private double getEncoderPos(CANSparkMax sparkMax) {
        return sparkMax.getEncoder().getPosition();
    }

    /**
     * Constructs a climb object, intializes the motors
     *
     * @param leaderID ID of the leader motor
     * @param follower1ID ID of the first follower motor
     * @param follower2ID ID of the second follower motor
     * @param follower3ID ID of the third follower motor
     */
    public Climb(int leaderID, int follower1ID, int follower2ID, int follower3ID) {

        leader = new CANSparkMax(leaderID, MotorType.kBrushless);
        follower1 = new CANSparkMax(follower1ID, MotorType.kBrushless);
        follower2 = new CANSparkMax(follower2ID, MotorType.kBrushless);
        follower3 = new CANSparkMax(follower3ID, MotorType.kBrushless);

        //disable direct inverts, motors are inverted in the follow section
        follower1.setInverted(false);
        follower3.setInverted(false);
        leader.setInverted(false);
        follower2.setInverted(false);

        //Set to brakemode
        leader.setIdleMode(IdleMode.kBrake);
        follower1.setIdleMode(IdleMode.kBrake);
        follower2.setIdleMode(IdleMode.kBrake);
        follower3.setIdleMode(IdleMode.kBrake);

        //set follow(with inverts)
        follower1.follow(leader, true);
        follower3.follow(leader, true);
        follower2.follow(leader, false);

        resetEncoder(leader);
        leader.set(0);

        //makes sure the climb won't overrotate
        rotationsSafe = (ROTATIONS <= MAX_ROTATIONS);
    }

    /**
     * Resets the encoder value by calling the constructor
     *
     * @param sparkMax The sparkMax object that needs to reset its encoder value
     */
    private void resetEncoder(CANSparkMax sparkMax) {
        CANSparkMax temp;
        temp = sparkMax;
        sparkMax = new CANSparkMax(sparkMax.getDeviceId(), MotorType.kBrushless);
        sparkMax.close();
        sparkMax = temp;
    }

    /**
     * Climbs while the input is true
     *
     * @param input the variable that controls the climb(must be held true during climb)
     */
    public void climb(boolean input) {
        //PLEASE ENSURE THAT THE MECHANICAL LEADER MOTOR DIRECTION MATCHES
        //  WITH THE LEADER MOTOR DIRECTION IN CODE
        //      (motors spinning in a positive direction will move
        //      clockwise when looking at the ROTOR side of the motor)
        //the planetary gearbox will NOT reverse the motor direction,
        //  but every additional gear after the first one will reverse the direction

        if ((ROTATIONS * GEAR_RATIO) > Math.abs(getEncoderPos(leader)) && input == true) {
            moveLeapFrog();
        } else {
            stopLeapFrog();
        }
    }

    /**
     * Moves the climb, while also checking that the number of
     * rotations set is not too high
     */
    private void moveLeapFrog() {
        if (rotationsSafe) {
            leader.set(1);
        } else {
            stopLeapFrog();
        }
    }

    /**
     * stops the climb
     */
    private void stopLeapFrog() {
        leader.set(0);
    }

}

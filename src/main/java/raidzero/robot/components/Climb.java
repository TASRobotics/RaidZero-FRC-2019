package raidzero.robot.components;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Climb{

    private CANSparkMax leader;
    private CANSparkMax follower1;
    private CANSparkMax follower2;
    private CANSparkMax follower3;

    private final double GEAR_RATIO = 256;
    //rotations = angle/360
    private final double ROTATIONS = 150/360;
    //do not change the maxRotations for safety
    private final double MAX_ROTATIONS = 160/360;

    /**
     * Constructs a climb object, intializes the motors
     *
     * @param leaderID ID of the leader motor
     * @param follower1ID ID of the first follower motor
     * @param follower2ID ID of the second follower motor
     * @param follower3ID ID of the third follower motor
     */
    public Climb(int leaderID, int follower1ID, int follower2ID, int follower3ID){

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
    }

    /**
     * Resets the encoder value by calling the constructor
     *
     * @param sparkMax The sparkMax object that needs to reset its encoder value
     */
    private void resetEncoder(CANSparkMax sparkMax){
        CANSparkMax temp;
        temp = sparkMax;
        sparkMax = new CANSparkMax(sparkMax.getDeviceId(), MotorType.kBrushless);
        sparkMax.close();
        sparkMax = temp;
    }

    /**
     * Climbs while the button is held down
     *
     * @param button the button that controls the climb(must be held down during climb)
     */
    public void climb(boolean button){
/*
        //first PID base backwards a bit
            //DO NOT UNCOMMENT CODE until base PID is added
            //PLEASE ENSURE THAT THE MECHANICAL LEADER MOTOR DIRECTION MATCHES
            //  WITH THE LEADER MOTOR DIRECTION IN CODE
            //      (motors spinning in a positive direction will move
            //      clockwise when looking at the rotor side of the motor)
            //REMEMBER THAT THE GEARBOX MAY REVERSE THE MOTOR DIRECTION

            //base PID

            //overall gearing: 256:1
                //banebots gearbox: 64:1
                //physical outside gearbox: 72:18\
        if((ROTATIONS * GEAR_RATIO) > Math.abs(leader.getEncoder().getPosition()) && button == true){
            moveLeapFrog();
        }
        else{
            stopLeapFrog();
        }
*/
    }

    /**
     * Moves the climb
     */
    private void moveLeapFrog(){
        leader.set(1);
    }

    /**
     * stops the climb
     */
    private void stopLeapFrog(){
        leader.set(0);
    }
}
package raidzero.robot.components;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Climb{

    private SparkMaxPrime leader;
    private CANSparkMax follower1;
    private CANSparkMax follower2;
    private CANSparkMax follower3;

    private final double GEAR_RATIO = 256;
    //rotations = angle/360
    private final double ROTATIONS = 150/360;
    //do not change the maxRotations for safety
    private final double MAX_ROTATIONS = 160/360;

    public Climb(int leaderID, int follower1ID, int follower2ID. int follower3ID){

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

    private double resetEncoder(CANSparkMax neo)){
        return neo.getEncoder().setPosition(0);
    }

    public void climb(){
        if(ROTATIONS < MAX_ROTATIONS){
        /*//first PID base backwards a bit
            //DO NOT UNCOMMENT CODE until base PID is added
            //PLEASE ENSURE THAT THE MECHANICAL LEADER MOTOR DIRECTION MATCHES
            //  WITH THE LEADER MOTOR DIRECTION IN CODE
            //      (motors spinning in a positive direction will move
            //      clockwise when looking at the rotor side of the motor)
            //REMEMBER THAT THE PLANETARY GEARBOX WILL NOT REVERSE THE
            //  DIRECTION, BUT THE SINGLE 72:18 WILL REVERSE THE DIRECTION ONCE

            //base PID

            //overall gearing: 256:1
                //banebots gearbox: 64:1
                //physical outside gearbox: 72:18

            try(leader.getEncoder()){
                if((ROTATIONS * GEAR_RATIO) > Math.abs(leader.getEncoder)){
                    moveLeapFrog();
                } else{
                    stopLeapFrog();
                }
            }
            catch(Exception e){
                stopLeapFrog();
            }
        }
        */
    }

    private void moveLeapFrog(){
        leader.set(1);
    }

    private void stopLeapFrog(){
        leader.set(0);
    }
}
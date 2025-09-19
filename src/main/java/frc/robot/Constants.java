/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

public final class Constants {

    public static final boolean ENABLEMOCKDS = true;
    public static final boolean DEBUG_CONTROLPANEL = true;

    public static final int TITAN_ID = 42;

    public static final int M0 = 0;
    public static final int M1 = 1;
    public static final int M2 = 2;
    public static final int M3 = 3;
    public static final int GARSERVO = 0;
    public static final int LINEARGARRA = 2;
    public static final int ROTGARRA = 1;
    public static final double wheelRadius = 55;
    public static final double pulsePerRevolution = 1440;
    public static final double gearRatio = 1 / 1;
    public static final double wheelPulseRatio = pulsePerRevolution * gearRatio;
    public static final double WHEEL_DIST_PER_TICK = (Math.PI * 2 * wheelRadius) / wheelPulseRatio;

    public static final class controlPanel {

        public static final int STARTBUTTON = 8;
        public static final int RESETBUTTON = 9;
        public static final int STOPBUTTON = 10;
        public static final int RUNNING_LED = 15;
        public static final int STOPPED_LED = 16;

    }

    public static final class Elevator {
        public static final double POS_LOW  = 0.0;   // “linear baixo”
        public static final double POS_HIGH = 10.0;  // “linear alto” (ajuste)
      }
      
      public static final class LinearArm {
        public static final double IN_DEG  = 160.0; // retrai
        public static final double OUT_DEG = 0.0;   // estende
      }
      
      public static final class Claw {
        public static final double OPEN_DEG   = 220.0;
        public static final double CLOSED_DEG = 60.0;
      }
      
      public static final class Rot {
        public static final double STOW  = 0.0;
        public static final double GRIP1 = 90.0;
        public static final double GRIP2 = 210.0;
      }

      public static final double pulleyRadius = 7.85;
      public static final double pulsePerRevElevator = 1440;
      public static final double elevatorGearRatio = 1.0 / 2.0;
      public static final double pulleyPulseRatio = pulsePerRevElevator * elevatorGearRatio;
      public static final double ELEVATOR_DIST_TICK = (Math.PI * 2 * pulleyRadius) / pulleyPulseRatio;


}

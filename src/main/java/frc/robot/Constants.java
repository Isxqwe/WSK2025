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

}

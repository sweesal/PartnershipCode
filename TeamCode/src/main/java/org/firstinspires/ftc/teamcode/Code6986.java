package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp (name = "chassis")
public class Code6986 extends OpMode {
    private DcMotorEx leftfront;
    private DcMotorEx rightfront;
    private DcMotorEx leftback;
    private DcMotorEx rightback;
    double max;
    double[] speed;
    //创建一个数组
    private DcMotor intake;
    private DcMotor leftin;
    private DcMotor rightin;

    private DcMotor armHex;
    double intakeOutput;
    boolean reverse = false;
    boolean IT = false;
    int AT = 1;
    ElapsedTime Duration = new ElapsedTime();
    ElapsedTime IntakeT = new ElapsedTime();
    ElapsedTime ArmT = new ElapsedTime();

    private DigitalChannel encoder0;
    private DigitalChannel encoder1;
    @Override
    public void init() {
        Duration.reset();

        //drive train
        leftfront = hardwareMap.get(DcMotorEx.class,"LF");
        rightfront = hardwareMap.get(DcMotorEx.class,"RF");
        leftback = hardwareMap.get(DcMotorEx.class,"LB");
        rightback = hardwareMap.get(DcMotorEx.class,"RB");
        leftfront.setDirection(DcMotorEx.Direction.FORWARD);
        rightfront.setDirection(DcMotorEx.Direction.REVERSE);
        leftback.setDirection(DcMotorEx.Direction.FORWARD);
        rightback.setDirection(DcMotorEx.Direction.REVERSE);
        //elevator
        intake = hardwareMap.get(DcMotor.class, "intake");
        armHex = hardwareMap.get(DcMotor.class, "arm");

        //intake
        leftin = hardwareMap.get (DcMotor.class, "LI");
        rightin = hardwareMap.get(DcMotor.class, "RI");
        leftin.setDirection(DcMotorSimple.Direction.FORWARD);
        rightin.setDirection(DcMotorSimple.Direction.REVERSE);

        encoder0 = hardwareMap.get(DigitalChannel.class, "encoder0");
        encoder1 = hardwareMap.get(DigitalChannel.class, "encoder1");
        encoder0.setMode(DigitalChannel.Mode.INPUT);
        encoder1.setMode(DigitalChannel.Mode.INPUT);
    }

    @Override
    public void loop() {

        double value1 = gamepad1.left_stick_x;
        double value2 = gamepad1.right_stick_x;
        double value3 = -gamepad1.left_stick_y;

        if(Math.abs(value1)<0.1) value1=0;
        if(Math.abs(value2)<0.1) value2=0;
        if(Math.abs(value3)<0.1) value3=0;
        //避免误触rr
        //deadzone: FRC默认0.05

        if (gamepad1.right_bumper)
        {
            value1=value1*0.5;
            value2=value2*0.5;
            value3=value3*0.5;
        }
        //如果gamepad1.right_bumper被按下，车速会减低成原来的一半，适合在一定范围内微调

        if (gamepad1.left_bumper && Duration.time()>0.5)
        {
            reverse = !reverse;
            Duration.reset();
        }
        /*Duration.time()>0.5：代码执行的速度比操作手按下按键的速度更快
        所以可能会出现操作手按按键的时候代码已经执行了好几次，已经重复reverse这个过程好几次了
        为了避免这个问题，使用Duration.time()>0.5，保证在0.5s之后才会再执行这个代码
        而那个时候，操作手已经不再按着这个按键了，所以不会出现这个问题
        如果gamepad1.left_bumper被按下，左右前后会反过来，适合操作手在倒着开车时使用
         */
        //！非

        if(reverse)
        {
            //value1不需要取反，因为当机器反过来开的时候，顺时针转依然是顺时针转，不需要reverse
            value2 = -value2;
            value3 = -value3;
        }

        if (gamepad1.a && IntakeT.time()>0.5){
            IT = ! IT;
            IntakeT.reset();
        }

        if (IT){
            leftin.setPower(1);
            rightin.setPower(1);
        }
        else{
            leftin.setPower(0);
            rightin.setPower(0);
        }

        if (gamepad1.x){
            armHex.setPower(AT);
        }
        else{
            armHex.setPower(0);
        }
        if (gamepad1.y && ArmT.time()>0.5){
            AT = -AT;
            ArmT.reset();
        }
        double[]speed = {
                (value1+value2+value3),
                (-value1-value2+value3),
                (-value1+value2+value3),
                (value1-value2+value3)
        };

        max = Math.abs(value1+value2+value3);


        for (int i=0;i<4;i++)
        {
            if(max < Math.abs(speed[i]))
            {
                max=Math.abs(speed[i]);
            }
        }
        if (max> 1.0)
        {
            for(int i = 0; i<speed.length; i++) speed[i]/=max;

        }

         leftfront.setPower((value1+value2+value3)/max);
         rightfront.setPower((-value1-value2+value3)/max);
         leftback.setPower((-value1+value2+value3)/max);
         rightback.setPower((value1-value2+value3)/max);

        intakeOutput = Range.clip(
                gamepad1.left_trigger - gamepad1.right_trigger, -1, 1);
        intake.setPower(intakeOutput);



//        telemetry.addData("velocity",  "velocity %7d :%7d :%7d :%7d",
//                leftfront.getVelocity(),
//                rightfront.getVelocity(),
//                leftback.getVelocity(),
//                rightback.getVelocity());
        telemetry.addData("encoder1State", encoder0.getState());
        telemetry.addData("encoder2State", encoder1.getState());
        telemetry.update();
    }
}
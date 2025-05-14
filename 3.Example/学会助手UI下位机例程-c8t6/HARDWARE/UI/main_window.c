#include "main_window.h"
#include "phone_ui.h"
#include "delay.h"
/*
@在此文件中构建界面,也可仿照此文件另外构建UI文件
@UI色彩值为RGB8888，透明度、红R、绿G、蓝B各占8位 例：0x12345678,12为透明度，23为R，56为G，78为B
@在样式结构体中设置完全透明不要使用0，将透明度置零即可，比如(UI_TRANSPARENT)
@Gravity变量在Phone_UI.h中查看，可以使用或运算
*/

TextView_TypeDef main_tv,main_rx_tv;

Button_TypeDef main_bt;

EditText_TypeDef main_et;
	
Switch_TypeDef main_sw;

SeekBar_TypeDef main_sb,main_sb2;

LineChart_TypeDef main_lc,main_lc2;

JoyStick_TypeDef main_js,main_js2;


//显示主页
void ShowMianMeun()
{
	Window_Clear(); //清空屏幕
	Window_SetBackground(0XFFFFFFFF);	//背景设置为白色
	Window_SetSize(1000,2400,1);//设置窗口宽高
	//处理数据数量
	TextView_Init(&main_rx_tv,50,0,200,100);
	TextView_SetTextGravity(main_rx_tv,Gravity_CENTER_VERTICAL);
	TextView_SetBackgroundColor(main_rx_tv,UI_TRANSPARENT);
	
	//文本框初始化
	TextView_Init(&main_tv,50,100,900,300);
	TextView_SetStyle(main_tv,TextViewStyle);
	TextView_SetText(main_tv,"文本框");


	//文本输入框初始化
	EditText_Init(&main_et,50,400,900,150);
//	EditText_SetStyle(main_et,EditTextStyle);
	EditText_SetHint(main_et,"输入框");
	
	//按钮初始化
	Button_Init(&main_bt,50,550,260,150);
//	Button_SetStyle(main_bt,ButtonStyle);
	Button_SetText(main_bt,"按键");
	Button_SetTextSize(main_bt,40);
	

//开关初始化
	Switch_Init(&main_sw,800,550,150,150);
	Switch_SetChecked(main_sw,1);


	//进度条初始化
	SeekBar_Init(&main_sb,50,700,900,100);		
	SeekBar_SetColor(main_sb,0xFFFF0000);
	SeekBar_SetProgress(main_sb,50);
	SeekBar_Init(&main_sb2,50,800,900,100);	
	SeekBar_SetProgress(main_sb2,50);
	//折线图
	LineChart_Init(&main_lc,50,900,900,400);
	LineChart_SetStyle(main_lc,LineChartStyle);
	LineChart_SetLineColor(main_lc,UI_BLUE);
	
	LineChart_Init(&main_lc2,50,900,900,400);
	LineChart_SetStyle(main_lc2,LineChartStyle);
	LineChart_SetLineColor(main_lc2,UI_RED);
	LineChart_SetChartColor(main_lc2,0xFF444444);
	
	//摇杆
	JoyStick_Init(&main_js,50,1300,400,400);
	JoyStick_SetShape(main_js,1);
	JoyStick_SetColor(main_js,0xFF555555);

	JoyStick_Init(&main_js2,550,1300,400,400);
	JoyStick_SetColor(main_js2,0xFF555555);
	

}

























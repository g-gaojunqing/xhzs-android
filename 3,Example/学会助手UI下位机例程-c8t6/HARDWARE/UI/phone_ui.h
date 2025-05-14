#ifndef _PHONE_UI
#define _PHONE_UI

#include <stdint.h>

#define REAL_TIME_PROCESSING 0 //（1）实时处理（0）在主程序中处理
#define UI_RX_LEN 24   //可接收一帧数据最大数量
#define UI_BUF_LEN 1024     //非实时处理下缓冲区大小

//指令
#define UI_Window_Clear 			0x00
#define UI_Window_Background 	0x01
#define UI_Window_Size 				0x02
#define UI_Window_Orientation 0x03

#define UI_TextView_Init 				0x10
#define UI_TextView_Style 			0x11
#define UI_TextView_SetText 		0x12
#define UI_TextView_AddText 		0x13
#define UI_TextView_TextSize 		0x14
#define UI_TextView_TextColor   		0x15
#define UI_TextView_Gravity     		0x16
#define UI_TextView_BackgroundColor 0x17
#define UI_TextView_Rotation    		0x18

#define UI_Button_Init   			      0x20
#define UI_Button_Style 			      0x21
#define UI_Button_Text 		    	    0x22
#define UI_Button_Color             0x23
#define UI_Button_TextSize 			    0x24
#define UI_Button_TextColor 		    0x25
#define UI_Button_Gravity 			    0x26
#define UI_Button_BackgroundColor   0x27
#define UI_Button_Rotation			    0x28

#define UI_EditText_Init 			      0x30
#define UI_EditText_Style 					0x31
#define UI_EditText_SetText 				0x32
#define UI_EditText_SetHint 				0x33
#define UI_EditText_TextSize 		    0x34
#define UI_EditText_TextColor 		  0x35
#define UI_EditText_Gravity 		    0x36
#define UI_EditText_BackgroundColor 0x37
#define UI_EditText_Rotation		    0x38

#define UI_Switch_Init 				      0x40
#define UI_Switch_Style 			      0x41
#define UI_Switch_Checked 		      0x42
#define UI_Switch_Color 			      0x43
#define UI_Switch_BackgroundColor 	0x44
#define UI_Switch_Rotation 			    0x45

#define UI_SeekBar_Init 			      0x50
#define UI_SeekBar_Style		      	0x51
#define UI_SeekBar_Max 			      	0x52
#define UI_SeekBar_Process 			    0x53
#define UI_SeekBar_Color 			      0x54
#define UI_SeekBar_BackgroundColor 	0x55
#define UI_SeekBar_Rotation 		    0x56

#define UI_LineChart_Init 				     0x60
#define UI_LineChart_Style  			     0x61
#define UI_LineChart_AddData 			     0x62
#define UI_LineChart_Clear 			       0x63
#define UI_LineChart_XAxis 			       0x64
#define UI_LineChart_YAxis 		         0x65
#define UI_LineChart_LineColor 			   0x66
#define UI_LineChart_ChartColor 		   0x67
#define UI_LineChart_BackgroundColor   0x68

#define UI_JoyStick_Init 			      0x70
#define UI_JoyStick_Style			      0x71
#define UI_JoyStick_Color		        0x72
#define UI_JoyStick_Shape 			    0x73
#define UI_JoyStick_BackgroundColor	0x74

#define UI_DEFAULT_COLOR 	(int)0xFF6480FF	//默认颜色
#define UI_WHITE  		(int)0xFFFFFFFF	//白
#define UI_BLACK  		(int)0xFF000000	//黑
#define UI_RED     		(int)0xFFFF0000	//红
#define UI_GREEN  		(int)0xFF00FF00 //绿
#define UI_BLUE 		(int)0xFF0000FF //蓝
#define UI_CYAN   		(int)0xFF00FFFF	//青
#define UI_YELLOW  		(int)0xFFFFFF00	//黄
#define UI_MAGENTA  	(int)0xFFFF00FF //品红
#define UI_GRAY  		(int)0xFF888888 //灰
#define UI_DKGRAY  		(int)0xFF444444	//深灰
#define UI_LTGRAY 		(int)0xFFCCCCCC //浅灰
#define	UI_TRANSPARENT  (int)0x00FFFFFF //透明

//文本位置
#define	Gravity_TOP  				(int)0x00000030  //顶部
#define	Gravity_BOTTOM 				(int)0x00000050  //底部
#define	Gravity_LIFT 				(int)0x00000003  //左
#define	Gravity_RIGHT  				(int)0x00000005  //右
#define	Gravity_START  				(int)0x00800003	  //起始
#define	Gravity_END  				(int)0x00800005	  //末尾
#define	Gravity_CENTER  			(int)0x00000011  //控件中心
#define	Gravity_CENTER_VERTICAL  	(int)0x00000010  //垂直方向中心
#define	Gravity_CENTER_HORIZONTAL   (int)0x00000001  //水平方向中心

//最多储存EditText_MaxNum字节（包含'\0'）
//注意实际可接收数量一般少于UI_RX_LEN 4-5个（id前缀）
//一般等于UI_RX_LEN即可,若增加接收数量需同时修改这两个值
#define EditText_MaxNum 24

//文本框
typedef struct TextView_Struct
{ 
	uint16_t id;  //控件ID
	
	struct TextView_Struct *next;
}TextView_TypeDef;
//文本框样式
//设置为全局变量时，参数初始值为0，不赋值控件采用默认值
//参数赋值为0时，控件采用默认值
//设置为局部变量时，要对全部参数赋值
typedef struct TextViewStyle_Struct
{ 
	int textSize;	 //字体大小
	int textColor;	 //字体颜色
	int gravity; 	//文本位置
	int backgroundColor;	 //背景颜色
	int rotation; //旋转角度
}TextViewStyle_TypeDef;

//按钮
typedef struct Button_Struct
{ 
	uint16_t id;  //控件ID
	
	char isPressed;	//(=0)按钮抬起，(=1)按钮按下
	char isUpdated; //(=1)数值已更新，需手动置0
	struct Button_Struct *next;
}Button_TypeDef;
//按钮样式
//设置为全局变量时，参数初始值为0，不赋值控件采用默认值
//参数赋值为0时，控件采用默认值
//设置为局部变量时，要对全部参数赋值
typedef struct ButtonStyle_Struct
{ 	
	int color;	//颜色
	int textSize;	//字体大小
  int textColor;	//字体颜色
	int gravity;  	//文本位置
	int backgroundColor;	//背景颜色
	int rotation;  //旋转角度
}ButtonStyle_TypeDef;


//文本输入框
typedef struct EditText_Struct
{ 
	uint16_t id;  //控件ID
	
	char text[EditText_MaxNum];//输入字符串，有效字符串以'\0'结尾
	char isUpdated;//(=1)数值已更新，需手动置0
	struct EditText_Struct *next;
}EditText_TypeDef;
//文本输入框样式
//设置为全局变量时，参数初始值为0，不赋值控件采用默认值
//参数赋值为0时，控件采用默认值
//设置为局部变量时，要对全部参数赋值
typedef struct EditTextStyle_Struct
{ 	
	int textSize;	//字体大小
  int textColor;	//字体颜色
	int gravity;     //文本位置
	int backgroundColor;	 //背景颜色
	int rotation;   //旋转角度
}EditTextStyle_TypeDef;

//开关
typedef struct Switch_Struct
{ 
	uint16_t id;  //控件ID
	
	int isChecked;	//(=0)开关关闭，(=1)开关打开
	char isUpdated;//(=1)数值已更新，需手动置0
	struct Switch_Struct *next;
}Switch_TypeDef;
//开关样式
//设置为全局变量时，参数初始值为0，不赋值控件采用默认值
//参数赋值为0时，控件采用默认值
//设置为局部变量时，要对全部参数赋值
typedef struct SwitchStyle_Struct
{ 
	int color; //颜色
	int backgroundColor;	 //背景颜色
	int rotation; //旋转角度
}SwitchStyle_TypeDef;

//拖动条
typedef struct SeekBar_Struct
{ 
	uint16_t id;  //控件ID
	
	int progress;		//当前值
	char isUpdated;  //(=1)数值已更新，需手动置0
	struct SeekBar_Struct *next;
}SeekBar_TypeDef;
//拖动条样式
//设置为全局变量时，参数初始值为0，不赋值控件采用默认值
//参数赋值为0时，控件采用默认值
//设置为局部变量时，要对全部参数赋值
typedef struct SeekBarStyle_Struct
{ 	
	int max;			//最大值
	int color;      //拖动条颜色
	int backgroundColor;	 //背景颜色
	int rotation; //旋转角度
}SeekBarStyle_TypeDef;

//折线图
typedef struct LineChart_Struct
{ 
	uint16_t id;  //控件ID
	
	struct LineChart_Struct *next;
}LineChart_TypeDef;
//折线图样式
//设置为全局变量时，参数初始值为0，不赋值控件采用默认值
//参数赋值为0时，控件采用默认值
//设置为局部变量时，要对全部参数赋值
typedef struct LineChartStyle_Struct
{ 	
	int xMax;		//X轴可显示数量（0-1000）
	float yMin; 	//Y轴最小值
	float yMax;		//Y轴最大值
	int lineColor;	//折线颜色
	int chartColor;	//坐标轴颜色
	int backgroundColor;	 //背景颜色
}LineChartStyle_TypeDef;

//摇杆
typedef struct JoyStick_Struct
{ 
	uint16_t id;  //控件ID
	
	int x; //X的值（0-200）
	int y;//Y的值（0-200）
	char isUpdated;//(=1)数值已更新，需手动置0
	struct JoyStick_Struct *next;
}JoyStick_TypeDef;
//摇杆样式
//设置为全局变量时，参数初始值为0，不赋值控件采用默认值
//参数赋值为0时，控件采用默认值
//设置为局部变量时，要对全部参数赋值
typedef struct JoyStickStyle_Struct
{ 
	int color;	    //摇杆颜色
	int shape;			//形状（0 圆形，1 方形）
	int backgroundColor;	 //背景颜色
}JoyStickStyle_TypeDef;

//窗口
void Window_Clear(void);
void Window_SetBackground(int color);
void Window_SetSize(uint16_t width,uint16_t height,char fill);
void Window_SetOrientation(int orientation);

//TextView相关
void TextView_Init(TextView_TypeDef *TextView,short x,short y,short width,short height);
void TextView_SetStyle(TextView_TypeDef TextView,TextViewStyle_TypeDef Style);
void TextView_SetText(TextView_TypeDef TextView,char *text);
void TextView_AddText(TextView_TypeDef TextView,char *text);
void TextView_SetInt(TextView_TypeDef TextView,int num);
void TextView_AddInt(TextView_TypeDef TextView,int num);
void TextView_SetFloat(TextView_TypeDef TextView,float num,uint8_t len);
void TextView_AddFloat(TextView_TypeDef TextView,float num,uint8_t len);
void TextView_SetTextSize(TextView_TypeDef TextView,int size);
void TextView_SetTextColor(TextView_TypeDef TextView,int color);
void TextView_SetTextGravity(TextView_TypeDef TextView,int gravity);
void TextView_SetBackgroundColor(TextView_TypeDef TextView,int color);
void TextView_SetRotation(TextView_TypeDef TextView,int rotation);
//Button相关
void Button_Init(Button_TypeDef *Button,short x,short y,short width,short height);
void Button_SetStyle(Button_TypeDef Button,ButtonStyle_TypeDef Style);
void Button_SetText(Button_TypeDef Button,char *text);
void Button_SetColor(Button_TypeDef Button,int color);
void Button_SetTextSize(Button_TypeDef Button,int size);
void Button_SetTextColor(Button_TypeDef Button,int color);
void Button_SetBackgroundColor(Button_TypeDef Button,int color);
void Button_SetRotation(Button_TypeDef Button,int rotation);
//EditText相关
void EditText_Init(EditText_TypeDef *EditText,short x,short y,short width,short height);
void EditText_SetStyle(EditText_TypeDef EditText,EditTextStyle_TypeDef Style);
void EditText_SetText(EditText_TypeDef EditText,char *text);
void EditText_SetInt(EditText_TypeDef EditText,int num);
void EditText_SetFloat(EditText_TypeDef EditText,float num,uint8_t len);
void EditText_SetHint(EditText_TypeDef EditText,char *text);
void EditText_SetTextSize(EditText_TypeDef EditText,int size);
void EditText_SetTextColor(EditText_TypeDef EditText,int color);
void EditText_SetBackgroundColor(EditText_TypeDef EditText,int color);
void EditText_SetRotation(EditText_TypeDef EditText,int rotation);
//Switch相关
void Switch_Init(Switch_TypeDef *Switch,short x,short y,short width,short height);
void Switch_SetStyle(Switch_TypeDef Switch,SwitchStyle_TypeDef Style);
void Switch_SetChecked(Switch_TypeDef Switch,int checked);
void Switch_SetColor(Switch_TypeDef Switch,int color);
void Switch_SetBackgroundColor(Switch_TypeDef Switch,int color);
void Switch_SetRotation(Switch_TypeDef Switch,int rotation);
//SeekBar相关
void SeekBar_Init(SeekBar_TypeDef *SeekBar,short x,short y,short width,short height);
void SeekBar_SetStyle(SeekBar_TypeDef SeekBar,SeekBarStyle_TypeDef Style);
void SeekBar_SetMax(SeekBar_TypeDef SeekBar,int max);
void SeekBar_SetProgress(SeekBar_TypeDef SeekBar,int progress);
void SeekBar_SetColor(SeekBar_TypeDef SeekBar,int color);
void SeekBar_SetBackgroundColor(SeekBar_TypeDef SeekBar,int color);
void SeekBar_SetRotation(SeekBar_TypeDef SeekBar,int rotation);
//LineChart相关
void LineChart_Init(LineChart_TypeDef *LineChart,short x,short y,short width,short height);
void LineChart_SetStyle(LineChart_TypeDef LineChart,LineChartStyle_TypeDef Style);
void LineChart_AddData(LineChart_TypeDef LineChart,double data);
void LineChart_Clear(LineChart_TypeDef LineChart);
void LineChart_SetXAxis(LineChart_TypeDef LineChart,int x_max);
void LineChart_SetYAxis(LineChart_TypeDef LineChart,float y_min,float y_max);
void LineChart_SetLineColor(LineChart_TypeDef LineChart,int color);
void LineChart_SetChartColor(LineChart_TypeDef LineChart,int color);
void LineChart_SetBackgroundColor(LineChart_TypeDef LineChart,int color);
//JoyStick相关
void JoyStick_Init(JoyStick_TypeDef *JoyStick,short x,short y,short width,short height);
void JoyStick_SetStyle(JoyStick_TypeDef JoyStick,JoyStickStyle_TypeDef Style);
void JoyStick_SetColor(JoyStick_TypeDef JoyStick,int color);
void JoyStick_SetShape(JoyStick_TypeDef JoyStick,int shape);
void JoyStick_SetBackgroundColor(JoyStick_TypeDef JoyStick,int color);

//串口接收
void UI_ByteDeal(uint8_t res);  //接收一个字节处理
int UI_BufferDeal(void); //在主程序中处理函数时，缓冲区数据处理
char UI_CommandProcessor(const char *p); //一帧数据处理
#endif














#ifndef _PHONE_UI
#define _PHONE_UI

#include <stdint.h>

#define REAL_TIME_PROCESSING 0 //��1��ʵʱ����0�����������д���
#define UI_RX_LEN 24   //�ɽ���һ֡�����������
#define UI_BUF_LEN 1024     //��ʵʱ�����»�������С

//ָ��
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

#define UI_DEFAULT_COLOR 	(int)0xFF6480FF	//Ĭ����ɫ
#define UI_WHITE  		(int)0xFFFFFFFF	//��
#define UI_BLACK  		(int)0xFF000000	//��
#define UI_RED     		(int)0xFFFF0000	//��
#define UI_GREEN  		(int)0xFF00FF00 //��
#define UI_BLUE 		(int)0xFF0000FF //��
#define UI_CYAN   		(int)0xFF00FFFF	//��
#define UI_YELLOW  		(int)0xFFFFFF00	//��
#define UI_MAGENTA  	(int)0xFFFF00FF //Ʒ��
#define UI_GRAY  		(int)0xFF888888 //��
#define UI_DKGRAY  		(int)0xFF444444	//���
#define UI_LTGRAY 		(int)0xFFCCCCCC //ǳ��
#define	UI_TRANSPARENT  (int)0x00FFFFFF //͸��

//�ı�λ��
#define	Gravity_TOP  				(int)0x00000030  //����
#define	Gravity_BOTTOM 				(int)0x00000050  //�ײ�
#define	Gravity_LIFT 				(int)0x00000003  //��
#define	Gravity_RIGHT  				(int)0x00000005  //��
#define	Gravity_START  				(int)0x00800003	  //��ʼ
#define	Gravity_END  				(int)0x00800005	  //ĩβ
#define	Gravity_CENTER  			(int)0x00000011  //�ؼ�����
#define	Gravity_CENTER_VERTICAL  	(int)0x00000010  //��ֱ��������
#define	Gravity_CENTER_HORIZONTAL   (int)0x00000001  //ˮƽ��������

//��ഢ��EditText_MaxNum�ֽڣ�����'\0'��
//ע��ʵ�ʿɽ�������һ������UI_RX_LEN 4-5����idǰ׺��
//һ�����UI_RX_LEN����,�����ӽ���������ͬʱ�޸�������ֵ
#define EditText_MaxNum 24

//�ı���
typedef struct TextView_Struct
{ 
	uint16_t id;  //�ؼ�ID
	
	struct TextView_Struct *next;
}TextView_TypeDef;
//�ı�����ʽ
//����Ϊȫ�ֱ���ʱ��������ʼֵΪ0������ֵ�ؼ�����Ĭ��ֵ
//������ֵΪ0ʱ���ؼ�����Ĭ��ֵ
//����Ϊ�ֲ�����ʱ��Ҫ��ȫ��������ֵ
typedef struct TextViewStyle_Struct
{ 
	int textSize;	 //�����С
	int textColor;	 //������ɫ
	int gravity; 	//�ı�λ��
	int backgroundColor;	 //������ɫ
	int rotation; //��ת�Ƕ�
}TextViewStyle_TypeDef;

//��ť
typedef struct Button_Struct
{ 
	uint16_t id;  //�ؼ�ID
	
	char isPressed;	//(=0)��ţ̌��(=1)��ť����
	char isUpdated; //(=1)��ֵ�Ѹ��£����ֶ���0
	struct Button_Struct *next;
}Button_TypeDef;
//��ť��ʽ
//����Ϊȫ�ֱ���ʱ��������ʼֵΪ0������ֵ�ؼ�����Ĭ��ֵ
//������ֵΪ0ʱ���ؼ�����Ĭ��ֵ
//����Ϊ�ֲ�����ʱ��Ҫ��ȫ��������ֵ
typedef struct ButtonStyle_Struct
{ 	
	int color;	//��ɫ
	int textSize;	//�����С
  int textColor;	//������ɫ
	int gravity;  	//�ı�λ��
	int backgroundColor;	//������ɫ
	int rotation;  //��ת�Ƕ�
}ButtonStyle_TypeDef;


//�ı������
typedef struct EditText_Struct
{ 
	uint16_t id;  //�ؼ�ID
	
	char text[EditText_MaxNum];//�����ַ�������Ч�ַ�����'\0'��β
	char isUpdated;//(=1)��ֵ�Ѹ��£����ֶ���0
	struct EditText_Struct *next;
}EditText_TypeDef;
//�ı��������ʽ
//����Ϊȫ�ֱ���ʱ��������ʼֵΪ0������ֵ�ؼ�����Ĭ��ֵ
//������ֵΪ0ʱ���ؼ�����Ĭ��ֵ
//����Ϊ�ֲ�����ʱ��Ҫ��ȫ��������ֵ
typedef struct EditTextStyle_Struct
{ 	
	int textSize;	//�����С
  int textColor;	//������ɫ
	int gravity;     //�ı�λ��
	int backgroundColor;	 //������ɫ
	int rotation;   //��ת�Ƕ�
}EditTextStyle_TypeDef;

//����
typedef struct Switch_Struct
{ 
	uint16_t id;  //�ؼ�ID
	
	int isChecked;	//(=0)���عرգ�(=1)���ش�
	char isUpdated;//(=1)��ֵ�Ѹ��£����ֶ���0
	struct Switch_Struct *next;
}Switch_TypeDef;
//������ʽ
//����Ϊȫ�ֱ���ʱ��������ʼֵΪ0������ֵ�ؼ�����Ĭ��ֵ
//������ֵΪ0ʱ���ؼ�����Ĭ��ֵ
//����Ϊ�ֲ�����ʱ��Ҫ��ȫ��������ֵ
typedef struct SwitchStyle_Struct
{ 
	int color; //��ɫ
	int backgroundColor;	 //������ɫ
	int rotation; //��ת�Ƕ�
}SwitchStyle_TypeDef;

//�϶���
typedef struct SeekBar_Struct
{ 
	uint16_t id;  //�ؼ�ID
	
	int progress;		//��ǰֵ
	char isUpdated;  //(=1)��ֵ�Ѹ��£����ֶ���0
	struct SeekBar_Struct *next;
}SeekBar_TypeDef;
//�϶�����ʽ
//����Ϊȫ�ֱ���ʱ��������ʼֵΪ0������ֵ�ؼ�����Ĭ��ֵ
//������ֵΪ0ʱ���ؼ�����Ĭ��ֵ
//����Ϊ�ֲ�����ʱ��Ҫ��ȫ��������ֵ
typedef struct SeekBarStyle_Struct
{ 	
	int max;			//���ֵ
	int color;      //�϶�����ɫ
	int backgroundColor;	 //������ɫ
	int rotation; //��ת�Ƕ�
}SeekBarStyle_TypeDef;

//����ͼ
typedef struct LineChart_Struct
{ 
	uint16_t id;  //�ؼ�ID
	
	struct LineChart_Struct *next;
}LineChart_TypeDef;
//����ͼ��ʽ
//����Ϊȫ�ֱ���ʱ��������ʼֵΪ0������ֵ�ؼ�����Ĭ��ֵ
//������ֵΪ0ʱ���ؼ�����Ĭ��ֵ
//����Ϊ�ֲ�����ʱ��Ҫ��ȫ��������ֵ
typedef struct LineChartStyle_Struct
{ 	
	int xMax;		//X�����ʾ������0-1000��
	float yMin; 	//Y����Сֵ
	float yMax;		//Y�����ֵ
	int lineColor;	//������ɫ
	int chartColor;	//��������ɫ
	int backgroundColor;	 //������ɫ
}LineChartStyle_TypeDef;

//ҡ��
typedef struct JoyStick_Struct
{ 
	uint16_t id;  //�ؼ�ID
	
	int x; //X��ֵ��0-200��
	int y;//Y��ֵ��0-200��
	char isUpdated;//(=1)��ֵ�Ѹ��£����ֶ���0
	struct JoyStick_Struct *next;
}JoyStick_TypeDef;
//ҡ����ʽ
//����Ϊȫ�ֱ���ʱ��������ʼֵΪ0������ֵ�ؼ�����Ĭ��ֵ
//������ֵΪ0ʱ���ؼ�����Ĭ��ֵ
//����Ϊ�ֲ�����ʱ��Ҫ��ȫ��������ֵ
typedef struct JoyStickStyle_Struct
{ 
	int color;	    //ҡ����ɫ
	int shape;			//��״��0 Բ�Σ�1 ���Σ�
	int backgroundColor;	 //������ɫ
}JoyStickStyle_TypeDef;

//����
void Window_Clear(void);
void Window_SetBackground(int color);
void Window_SetSize(uint16_t width,uint16_t height,char fill);
void Window_SetOrientation(int orientation);

//TextView���
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
//Button���
void Button_Init(Button_TypeDef *Button,short x,short y,short width,short height);
void Button_SetStyle(Button_TypeDef Button,ButtonStyle_TypeDef Style);
void Button_SetText(Button_TypeDef Button,char *text);
void Button_SetColor(Button_TypeDef Button,int color);
void Button_SetTextSize(Button_TypeDef Button,int size);
void Button_SetTextColor(Button_TypeDef Button,int color);
void Button_SetBackgroundColor(Button_TypeDef Button,int color);
void Button_SetRotation(Button_TypeDef Button,int rotation);
//EditText���
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
//Switch���
void Switch_Init(Switch_TypeDef *Switch,short x,short y,short width,short height);
void Switch_SetStyle(Switch_TypeDef Switch,SwitchStyle_TypeDef Style);
void Switch_SetChecked(Switch_TypeDef Switch,int checked);
void Switch_SetColor(Switch_TypeDef Switch,int color);
void Switch_SetBackgroundColor(Switch_TypeDef Switch,int color);
void Switch_SetRotation(Switch_TypeDef Switch,int rotation);
//SeekBar���
void SeekBar_Init(SeekBar_TypeDef *SeekBar,short x,short y,short width,short height);
void SeekBar_SetStyle(SeekBar_TypeDef SeekBar,SeekBarStyle_TypeDef Style);
void SeekBar_SetMax(SeekBar_TypeDef SeekBar,int max);
void SeekBar_SetProgress(SeekBar_TypeDef SeekBar,int progress);
void SeekBar_SetColor(SeekBar_TypeDef SeekBar,int color);
void SeekBar_SetBackgroundColor(SeekBar_TypeDef SeekBar,int color);
void SeekBar_SetRotation(SeekBar_TypeDef SeekBar,int rotation);
//LineChart���
void LineChart_Init(LineChart_TypeDef *LineChart,short x,short y,short width,short height);
void LineChart_SetStyle(LineChart_TypeDef LineChart,LineChartStyle_TypeDef Style);
void LineChart_AddData(LineChart_TypeDef LineChart,double data);
void LineChart_Clear(LineChart_TypeDef LineChart);
void LineChart_SetXAxis(LineChart_TypeDef LineChart,int x_max);
void LineChart_SetYAxis(LineChart_TypeDef LineChart,float y_min,float y_max);
void LineChart_SetLineColor(LineChart_TypeDef LineChart,int color);
void LineChart_SetChartColor(LineChart_TypeDef LineChart,int color);
void LineChart_SetBackgroundColor(LineChart_TypeDef LineChart,int color);
//JoyStick���
void JoyStick_Init(JoyStick_TypeDef *JoyStick,short x,short y,short width,short height);
void JoyStick_SetStyle(JoyStick_TypeDef JoyStick,JoyStickStyle_TypeDef Style);
void JoyStick_SetColor(JoyStick_TypeDef JoyStick,int color);
void JoyStick_SetShape(JoyStick_TypeDef JoyStick,int shape);
void JoyStick_SetBackgroundColor(JoyStick_TypeDef JoyStick,int color);

//���ڽ���
void UI_ByteDeal(uint8_t res);  //����һ���ֽڴ���
int UI_BufferDeal(void); //���������д�����ʱ�����������ݴ���
char UI_CommandProcessor(const char *p); //һ֡���ݴ���
#endif














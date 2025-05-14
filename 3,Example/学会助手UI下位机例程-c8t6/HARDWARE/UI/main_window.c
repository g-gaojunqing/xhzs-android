#include "main_window.h"
#include "phone_ui.h"
#include "delay.h"
/*
@�ڴ��ļ��й�������,Ҳ�ɷ��մ��ļ����⹹��UI�ļ�
@UIɫ��ֵΪRGB8888��͸���ȡ���R����G����B��ռ8λ ����0x12345678,12Ϊ͸���ȣ�23ΪR��56ΪG��78ΪB
@����ʽ�ṹ����������ȫ͸����Ҫʹ��0����͸�������㼴�ɣ�����(UI_TRANSPARENT)
@Gravity������Phone_UI.h�в鿴������ʹ�û�����
*/

TextView_TypeDef main_tv,main_rx_tv;

Button_TypeDef main_bt;

EditText_TypeDef main_et;
	
Switch_TypeDef main_sw;

SeekBar_TypeDef main_sb,main_sb2;

LineChart_TypeDef main_lc,main_lc2;

JoyStick_TypeDef main_js,main_js2;


//��ʾ��ҳ
void ShowMianMeun()
{
	Window_Clear(); //�����Ļ
	Window_SetBackground(0XFFFFFFFF);	//��������Ϊ��ɫ
	Window_SetSize(1000,2400,1);//���ô��ڿ��
	//������������
	TextView_Init(&main_rx_tv,50,0,200,100);
	TextView_SetTextGravity(main_rx_tv,Gravity_CENTER_VERTICAL);
	TextView_SetBackgroundColor(main_rx_tv,UI_TRANSPARENT);
	
	//�ı����ʼ��
	TextView_Init(&main_tv,50,100,900,300);
	TextView_SetStyle(main_tv,TextViewStyle);
	TextView_SetText(main_tv,"�ı���");


	//�ı�������ʼ��
	EditText_Init(&main_et,50,400,900,150);
//	EditText_SetStyle(main_et,EditTextStyle);
	EditText_SetHint(main_et,"�����");
	
	//��ť��ʼ��
	Button_Init(&main_bt,50,550,260,150);
//	Button_SetStyle(main_bt,ButtonStyle);
	Button_SetText(main_bt,"����");
	Button_SetTextSize(main_bt,40);
	

//���س�ʼ��
	Switch_Init(&main_sw,800,550,150,150);
	Switch_SetChecked(main_sw,1);


	//��������ʼ��
	SeekBar_Init(&main_sb,50,700,900,100);		
	SeekBar_SetColor(main_sb,0xFFFF0000);
	SeekBar_SetProgress(main_sb,50);
	SeekBar_Init(&main_sb2,50,800,900,100);	
	SeekBar_SetProgress(main_sb2,50);
	//����ͼ
	LineChart_Init(&main_lc,50,900,900,400);
	LineChart_SetStyle(main_lc,LineChartStyle);
	LineChart_SetLineColor(main_lc,UI_BLUE);
	
	LineChart_Init(&main_lc2,50,900,900,400);
	LineChart_SetStyle(main_lc2,LineChartStyle);
	LineChart_SetLineColor(main_lc2,UI_RED);
	LineChart_SetChartColor(main_lc2,0xFF444444);
	
	//ҡ��
	JoyStick_Init(&main_js,50,1300,400,400);
	JoyStick_SetShape(main_js,1);
	JoyStick_SetColor(main_js,0xFF555555);

	JoyStick_Init(&main_js2,550,1300,400,400);
	JoyStick_SetColor(main_js2,0xFF555555);
	

}


























/**********************************************************************************************************************
 *@brief   学会助手APP UI下位机程序
 *@by      一品芝麻糕
 *@notice  与学会助手UI功能配合使用
 *				硬件：STM32F103C8T6，通信模块：蓝牙模块（建议经典蓝牙）
 *				接线：
 *				A9 ----- RX
 *				A10----- TX
 *				5V ----- 5V
 *				GND----- GND
 *@version 1.4.1
 * 
 *                                                                                                      2024-06-09
 *																									  																							 西电 嵌牛实验室
 *********************************************************************************************************************/
#include "stm32f10x.h"
#include "delay.h"
#include "key.h"
#include "led.h"
#include "usart.h"
#include "phone_ui.h"
#include "main_window.h"


int main(void)
{
	int rx_num;
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	delay_init();	   
	uart_init(115200);	 

	ShowMianMeun(); //页面刷新

	while(1)
	{
		#if !REAL_TIME_PROCESSING
			rx_num=UI_BufferDeal();
		#endif

	//	TextView_SetInt(main_rx_tv,rx_num);

		if(main_bt.isUpdated)
		{
			main_bt.isUpdated=0;
			if(main_bt.isPressed)
			{
				Button_SetColor(main_bt,UI_BLUE);
				TextView_SetText(main_tv,"按键:按下");
			}
			else
			{
				Button_SetColor(main_bt,UI_DEFAULT_COLOR);
				TextView_SetText(main_tv,"按键:抬起");
			}
		}
	  if(main_et.isUpdated)
		{
			main_et.isUpdated=0;
			TextView_SetText(main_tv,"输入框：");
			TextView_AddText(main_tv,main_et.text);
		}
		if(main_sw.isUpdated)
		{
			main_sw.isUpdated=0;
			if(main_sw.isChecked)
			{
				TextView_SetText(main_tv,"开关:打开");
			}
			else
			{
				TextView_SetText(main_tv,"开关:关闭");
			}
		}
		
		if(main_sb.isUpdated)
		{
			main_sb.isUpdated=0;
			TextView_SetText(main_tv,"拖动条：");
			TextView_AddInt(main_tv,main_sb.progress);
			SeekBar_SetProgress(main_sb2,main_sb.progress);
		}
		
		if(main_js.isUpdated)
		{
			main_js.isUpdated=0;
			LineChart_AddData(main_lc,main_js.x);
			LineChart_AddData(main_lc2,main_js.y);
		}
		if(main_js2.isUpdated)
		{
			main_js2.isUpdated=0;
			LineChart_AddData(main_lc,main_js2.x);
			LineChart_AddData(main_lc2,main_js2.y);
		}	
		delay_ms(20);
	}
}
 


















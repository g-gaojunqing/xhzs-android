
/**********************************************************************************************************************
 *@brief   学会助手安卓版本APP遥控器定时发送下位机测试程序
 *@by      一品芝麻糕
 *@notice  与学会助手APP遥控器配合使用
 *				硬件：STM32F103C8T6，通信模块：蓝牙模块（建议经典蓝牙）
 *				接线：
 *				A9 ----- RX
 *				A10----- TX
 *				5V ----- 5V
 *				GND----- GND
 *@version 1.4.0
 * 
 *                                                                                                      2024-05-11   
 *																									                                                 	西电  嵌牛实验室
 *********************************************************************************************************************/
#include "stm32f10x.h"
#include "sys.h"
#include "led.h"
#include "delay.h"
#include "usart.h"	

int main(void)
{	
	int x,y;
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	LED_Init();
	delay_init();	 
	uart_init(115200);
	
	while(1)
	{
		if(USART_RX_STA==2)
		{
			if(USART_RX_NUM==16)
			{
//				if(USART_RX_BUF[13]==0x31)
//				{
//					printf("按键D按下\r\n");
//				}
//				else
//				{
//					printf("按键D抬起\r\n");
//				}
				x=(USART_RX_BUF[0]-0x30)*100+(USART_RX_BUF[1]-0x30)*10+(USART_RX_BUF[2]-0x30);
				y=(USART_RX_BUF[3]-0x30)*100+(USART_RX_BUF[4]-0x30)*10+(USART_RX_BUF[5]-0x30);
				printf("X:%d\nY:%d\r\n",x,y);
			}
			USART_RX_STA=0;
			USART_RX_NUM=0;
		}
		
		delay_ms(20);
	}
}
 







/**********************************************************************************************************************
 *@brief   学会助手APP遥控器实时发送下位机测试程序
 *@by      一品芝麻糕
 *@notice  与学会助手遥控器配合使用
 *				硬件：STM32F103C8T6，通信模块：蓝牙模块（建议经典蓝牙）
 *				接线：
 *				A9 ----- RX
 *				A10----- TX
 *				5V ----- 5V
 *				GND----- GND
 *@version 1.4.1
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
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	LED_Init();
	delay_init();	 
	uart_init(115200);
	while(1)
	{
		//printf("遥控器测试\r\n");
		delay_ms(20);
	}
}
 









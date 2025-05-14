
/**********************************************************************************************************************
 *@brief   ѧ�����ְ�׿�汾APP��λ�����Գ���
 *@by      һƷ֥���
 *@notice  ��ѧ������APP���ʹ��
 *				Ӳ����STM32F103C8T6��ͨ��ģ�飺����ģ�飨���龭��������
 *				���ߣ�
 *				A9 ----- RX
 *				A10----- TX
 *				5V ----- 5V
 *				GND----- GND
 *@version 1.4.0
 * 
 *                                                                                                      2024-03-15   
 *																									                                                  ���� Ƕţʵ����
 *********************************************************************************************************************/
#include "stm32f10x.h"
#include "delay.h"
#include "sys.h"
#include "usart.h"	
#include "led.h"
#include "key.h"
#include "test.h"
#include "test_lib.h"

#define TEST 0

int main(void)
{
	
	delay_init();	    	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	uart_init(115200);
  KEY_Init();	
	LED_Init();
	
	Uart_SendByte(USART1,0x0D); 
	delay_ms(10);					
	Uart_SendByte(USART1,0x0A);//��һ���������׷�����ȥ,�Ȳȿ�
	
	while(1)
	{

		switch(TEST)
		{
			case 0:CameraTest();break;
			case 1:WaveTest();break;
			case 2:SpeechTest();break;
			case 3:GPSTest();break;
		}	
		delay_ms(50);
		//LED=!LED;
	}
}
 
























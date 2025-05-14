
/**********************************************************************************************************************
 *@brief   ѧ�����ְ�׿�汾APPң������ʱ������λ�����Գ���
 *@by      һƷ֥���
 *@notice  ��ѧ������APPң�������ʹ��
 *				Ӳ����STM32F103C8T6��ͨ��ģ�飺����ģ�飨���龭��������
 *				���ߣ�
 *				A9 ----- RX
 *				A10----- TX
 *				5V ----- 5V
 *				GND----- GND
 *@version 1.4.0
 * 
 *                                                                                                      2024-05-11   
 *																									                                                 	����  Ƕţʵ����
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
//					printf("����D����\r\n");
//				}
//				else
//				{
//					printf("����Ḑ��\r\n");
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
 






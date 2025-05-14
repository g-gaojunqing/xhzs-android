
/**********************************************************************************************************************
 *@brief   ѧ�����ְ�׿�汾APP����ʾ������λ�����Գ���
 *@by      һƷ֥���
 *@notice  ��ѧ������APP����ʾ�������ʹ��
 *				Ӳ����STM32F103C8T6��ͨ��ģ�飺����ģ�飨���龭��������
 *				���ߣ�
 *				A9 ----- RX
 *				A10----- TX
 *				5V ----- 5V
 *				GND----- GND
 *@version 1.4.0
 * 
 *                                                                                                      2024-04-24   
 *																									                                                 	����  Ƕţʵ����
 *********************************************************************************************************************/
#include "stm32f10x.h"
#include "sys.h"
#include "led.h"
#include "delay.h"
#include "usart.h"	
#include "math.h"
#include "string.h"




//����һ���ֽ�
void Uart_SendByte(USART_TypeDef* UARTx,u8 byte)
{
	USART_SendData(UARTx,byte);
	while((UARTx->SR&0X40)==0);//�ȴ����ͽ���
}
//��������
void Uart_SendArray(USART_TypeDef *UARTx,uint8_t *array,uint32_t num)
{
	while(num--)
	{
		Uart_SendByte(UARTx, *array);
		array++;
	}
}

/**************************************************************
 *�������ƣ�XHZS_SendWave
 *��    �飺ѧ������APP����ʾ������λ�����򣨼���ɽ�⣩
 *��    �룺wareaddr:����������ʼ��ַ waresize:�����С
 *��    ������
 *ע�������
 **************************************************************/
void XHZS_SendWave(uint8_t *waveaddr, uint16_t wavesize)
{
   uint8_t cmdf[2] = {0x03,0xFC};		//֡ͷ
   uint8_t cmdr[2] = {0xFC,0x03}; 		//֡β
   
   Uart_SendArray(USART1,cmdf,sizeof(cmdf)); 	//����֡ͷ
   Uart_SendArray(USART1,waveaddr, wavesize);	//��������
   Uart_SendArray(USART1,cmdr,sizeof(cmdr)); 	//����֡β
}
/**************************************************************
 *�������ƣ�SendWave
 *��    �飺�������ݵ�ѧ������APP����ʾ����
 *��    �룺��
 *��    ������
 *ע�������Ҫ���͵����ݸ�ֵ��Wave���鼴�ɣ�ͨ�����ζ�Ӧ
 **************************************************************/
void SendWave()
{
	float i;
	float Wave[4]={0};	//ע���������ͣ������С��Ϊͨ���������������ͨ��
	
	//���Ҳ�����
	for(i=0;i<500;i+=0.1)
	{
		Wave[0] =sin(i);
	  Wave[1] =cos(i);
	  Wave[2] =-sin(i);
	  Wave[3] =-cos(i);
	  XHZS_SendWave((uint8_t *)Wave,sizeof(Wave));
		
		delay_ms(20);
	} 
}

/**************************************************************
 *�������ƣ�OscGetFloat
 *��    �飺���ַ�תΪ������
 *��    �룺p:�ַ�����value:���ڻ�ȡ���ֵ
 *��    ����(==0)ת�ɹ���(!=0)תʧ��
 *ע���������ʾ���������ã���','Ϊ���ݷָ���
 **************************************************************/
char OscGetFloat(const char *p,float *value)
{
    int i;
    int negative_flag=1;
    int float_flag=0;
    float tmp_value=0;
    for(i=0;i<16;i++)
    {
        if(*p==',')//��⵽�ָ���������ֵ
				{
            tmp_value*=negative_flag;
						*value=tmp_value;
            return 0;
        }
        else if(*p=='-')	//��⵽����
        {
            negative_flag=-1;
        }
        else if(*p=='.')	//��⵽С����
        {
            float_flag=-1;
				}
        else if(*p>='0'&&*p<='9')
        {
            if(float_flag==0)
            {
                tmp_value=tmp_value*10+(*p-0x30);
            }
            else
            {
                tmp_value+=(*p-0x30)*pow(10,float_flag);
                float_flag--;
            }
        }
        else
				{
					return 1;
				}
				p++;
    }
    return 2;
}
/**************************************************************
 *�������ƣ�OscGetValue
 *��    �飺���ַ����л�ȡָ��������ֵ
 *��    �룺p:�ַ�������'\0'��β�����򽫳���
 *					name:ָ���������ƣ���"c12"��ȡ��1���2��������
 *					value:���ڻ�ȡ���ֵ��
 *��    ����(==0)ת�ɹ���(!=0)תʧ��
 *ע���������ʾ���������ã��ַ�������'\0'Ϊ������
 **************************************************************/
char OscGetValue(const char *p,const char *name,float *value)
{
  float tmp_value;
	int feedback;
	p=strstr(p,name);
	if(p==NULL)
	{
		return 1;
	}
	p+=4;
	feedback=OscGetFloat(p,&tmp_value);
	if(feedback==0)
	{
		*value=tmp_value;
		return 0;
	}
	return 2;
}

int main(void)
{	
	float v1=0,v2=0,v3=0,v4=0;
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	LED_Init();
	delay_init();	 
	uart_init(115200);
	
	while(1)
	{
		if(USART_RX_STA==2)
		{
			LED=!LED;
			USART_RX_BUF[USART_RX_NUM]='\0';
			OscGetValue((const char *)USART_RX_BUF,"c11",&v1);
			OscGetValue((const char *)USART_RX_BUF,"c12",&v2);
			OscGetValue((const char *)USART_RX_BUF,"c13",&v3);
			OscGetValue((const char *)USART_RX_BUF,"c14",&v4);
	
			printf("w:%.2f,%.2f,%.2f,%.2f\r\n",v1,v2,v3,v4);
			USART_RX_NUM=0;
			USART_RX_STA=0;
		}
		delay_ms(50);
	}
}
 






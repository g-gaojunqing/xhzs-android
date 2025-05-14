
/**********************************************************************************************************************
 *@brief   ѧ������APP��λ�����Գ����
 *@by      һƷ֥���
 *@notice  
 * 
 *********************************************************************************************************************/
#include "test_lib.h"
#include "stm32f10x.h"
#include "delay.h"
#include "sys.h"
#include "usart.h"	
#include "math.h"
#include "key.h"
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
//�����ַ���
void Uart_SendString(USART_TypeDef *UARTx,uint8_t *s)
{
	while(*s!=0x00)
	{
		Uart_SendByte(UARTx, *s);
		s++;
	}
}

/**************************************************************
 *�������ƣ�XHZS_SendImage
 *��    �飺ѧ������APPͼ����λ������
 *��    �룺*imgaddr:�����ַ imgsize:�����С
 *��    ������
 *ע�������
 **************************************************************/
void XHZS_SendImage(uint8_t *imgaddr, uint32_t imgsize)
{
	uint8_t cmdf[4] = {0x01, 0x09,0x08, 0x07};   	//ѧ����λ��
		
	Uart_SendArray(USART1,cmdf,sizeof(cmdf));//֡ͷ
	Uart_SendArray(USART1,imgaddr,imgsize);//����
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

float mParameters[4]={0};
//��������ʾ�������β���
//���ַ����л�ȡ����
//p:����λ����ȡ���ַ���,�ַ���ĩβ�ֶ����'\0'
void GetParameter(uint8_t *p)
{
	OscGetValue((const char *)p,"c11",&mParameters[0]);
	OscGetValue((const char *)p,"c12",&mParameters[1]);
	OscGetValue((const char *)p,"c13",&mParameters[2]);
	OscGetValue((const char *)p,"c14",&mParameters[3]);
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
	
	Wave[0] =mParameters[0];
	Wave[1] =mParameters[1];
	Wave[2] =mParameters[2];
	Wave[3] =mParameters[3];
	XHZS_SendWave((uint8_t *)Wave,sizeof(Wave));
	
//	  //���Ҳ�����
//	for(i=0;i<500;i+=0.1)
//	{
//		Wave[0] =sin(i);
//	  Wave[1] =cos(i);
//	  Wave[2] =-sin(i);
//	  Wave[3] =-cos(i);
//	  XHZS_SendWave((uint8_t *)Wave,sizeof(Wave));
//		delay_ms(20);
//	} 
}



/**************************************************************
 *�������ƣ�SendLocation
 *��    �飺���;�γ�ȵ�ѧ������APP
 *��    �룺longitude:���� latitude:γ��
 *��    ������
 *ע�������������������ʾ���ݿ��ܻ��뷢�Ͳ�ͬ
 **************************************************************/
void SendLocation(float longitude,float latitude)
{
	float Wave[2]={0};
		
	Wave[0] =longitude;
	Wave[1] =latitude;

  XHZS_SendWave((uint8_t *)Wave,sizeof(Wave));	
}
/**************************************************************
 *�������ƣ�SendXY
 *��    �飺�������굽ѧ������APP
 *��    �룺x,y
 *��    ������
 *ע�������
 **************************************************************/
void SendXY(int x,int y)
{
	int Wave[2]={0};
		
	Wave[0] =x;
	Wave[1] =y;

  XHZS_SendWave((uint8_t *)Wave,sizeof(Wave));	
}








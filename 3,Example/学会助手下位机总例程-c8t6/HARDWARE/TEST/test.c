
/**********************************************************************************************************************
 *@brief   ѧ������APP��λ�����Գ���
 *@by      GJQ
 *@notice  
 * 
 *********************************************************************************************************************/
#include "stm32f10x.h"
#include "test_lib.h"
#include "delay.h"
#include "key.h"
#include "sys.h"
#include "usart.h"	
#include "font.h"
#include "math.h"

#define Image_Height  80  //RGB:80 Gray:100 Bin:104
#define Image_Width  160	//RGB:160 Gray:100 Bin:13

//ͼ����λ������
uint8_t image[Image_Height][Image_Width];
void CameraTest()
{
	int i,j;
	for(i=0;i<Image_Height;i++)
	{
		for(j=0;j<Image_Width;j++)
		image[i][j]=Image_RGB[i][j];  
//		image[i][j]=Image_Gray[i][j];
//		image[i][j]=Image_Bin[i][j];
	}
	XHZS_SendImage(image[0],Image_Height*Image_Width);
}

//����ʾ��������
//����λ����ȡ���ݷ��ͻ���λ��
void WaveTest()
{
	printf("w:12,13,25.4564,45\r\n"); //�ַ���ģʽ�·��Ͳ���
	
	if((USART_RX_STA&0x8000)!=0)
	{
		USART_RX_BUF[USART_RX_STA&0X3FFF]='\0'; //�ַ���Ҫ��'\0'��β
		GetParameter(USART_RX_BUF);//��ȡ����
	  SendWave();//���Ͳ���
		USART_RX_STA=0;
	}	

}
//������������
void SpeechTest()
{
	if(!KEY)
	{
		delay_ms(10);
		if(!KEY)
		{
			while(!KEY);
			//���������ļ�����
//			Uart_SendByte(USART1,0x02);
//			Uart_SendByte(USART1,0x0D);
//			Uart_SendByte(USART1,0x0A);
			
			printf("������������\r\n");
		}
	}
}
//GPS����
void GPSTest()
{
	if(!KEY)
	{
		delay_ms(10);
		if(!KEY)
		{
			while(!KEY);
			SendLocation(125.314373,43.833377); //GPSģʽ����

//			Send_XY(50,0);	//����ģʽ����
		}
	}
}



























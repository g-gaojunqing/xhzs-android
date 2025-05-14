
/**********************************************************************************************************************
 *@brief   学会助手APP下位机测试程序
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

//图像上位机测试
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

//虚拟示波器测试
//从上位机获取数据发送回上位机
void WaveTest()
{
	printf("w:12,13,25.4564,45\r\n"); //字符串模式下发送波形
	
	if((USART_RX_STA&0x8000)!=0)
	{
		USART_RX_BUF[USART_RX_STA&0X3FFF]='\0'; //字符串要以'\0'结尾
		GetParameter(USART_RX_BUF);//获取数据
	  SendWave();//发送波形
		USART_RX_STA=0;
	}	

}
//语音播报测试
void SpeechTest()
{
	if(!KEY)
	{
		delay_ms(10);
		if(!KEY)
		{
			while(!KEY);
			//播放音乐文件测试
//			Uart_SendByte(USART1,0x02);
//			Uart_SendByte(USART1,0x0D);
//			Uart_SendByte(USART1,0x0A);
			
			printf("语音播报测试\r\n");
		}
	}
}
//GPS测试
void GPSTest()
{
	if(!KEY)
	{
		delay_ms(10);
		if(!KEY)
		{
			while(!KEY);
			SendLocation(125.314373,43.833377); //GPS模式测试

//			Send_XY(50,0);	//坐标模式测试
		}
	}
}



























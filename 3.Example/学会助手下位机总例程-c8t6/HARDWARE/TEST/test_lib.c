
/**********************************************************************************************************************
 *@brief   学会助手APP下位机测试程序库
 *@by      一品芝麻糕
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
//发送一个字节
void Uart_SendByte(USART_TypeDef* UARTx,u8 byte)
{
	USART_SendData(UARTx,byte);
	while((UARTx->SR&0X40)==0);//等待发送结束
}
//发送数组
void Uart_SendArray(USART_TypeDef *UARTx,uint8_t *array,uint32_t num)
{
	while(num--)
	{
		Uart_SendByte(UARTx, *array);
		array++;
	}
}
//发送字符串
void Uart_SendString(USART_TypeDef *UARTx,uint8_t *s)
{
	while(*s!=0x00)
	{
		Uart_SendByte(UARTx, *s);
		s++;
	}
}

/**************************************************************
 *函数名称：XHZS_SendImage
 *简    介：学会助手APP图像下位机程序
 *输    入：*imgaddr:数组地址 imgsize:数组大小
 *输    出：无
 *注意事项：无
 **************************************************************/
void XHZS_SendImage(uint8_t *imgaddr, uint32_t imgsize)
{
	uint8_t cmdf[4] = {0x01, 0x09,0x08, 0x07};   	//学会上位机
		
	Uart_SendArray(USART1,cmdf,sizeof(cmdf));//帧头
	Uart_SendArray(USART1,imgaddr,imgsize);//数据
}


/**************************************************************
 *函数名称：OscGetFloat
 *简    介：将字符转为浮点数
 *输    入：p:字符串；value:用于获取输出值
 *输    出：(==0)转成功；(!=0)转失败
 *注意事项：虚拟示波器调参用，以','为数据分隔符
 **************************************************************/
char OscGetFloat(const char *p,float *value)
{
    int i;
    int negative_flag=1;
    int float_flag=0;
    float tmp_value=0;
    for(i=0;i<16;i++)
    {
        if(*p==',')//检测到分隔符，返回值
				{
            tmp_value*=negative_flag;
						*value=tmp_value;
            return 0;
        }
        else if(*p=='-')	//检测到负号
        {
            negative_flag=-1;
        }
        else if(*p=='.')	//检测到小数点
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
 *函数名称：OscGetValue
 *简    介：从字符串中获取指定参数数值
 *输    入：p:字符串，以'\0'结尾，否则将出错
 *					name:指定参数名称，如"c12"获取第1组第2个参数；
 *					value:用于获取输出值；
 *输    出：(==0)转成功；(!=0)转失败
 *注意事项：虚拟示波器调参用，字符串中以'\0'为结束符
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
//用于虚拟示波器调参测试
//从字符串中获取参数
//p:从上位机获取的字符串,字符串末尾手动添加'\0'
void GetParameter(uint8_t *p)
{
	OscGetValue((const char *)p,"c11",&mParameters[0]);
	OscGetValue((const char *)p,"c12",&mParameters[1]);
	OscGetValue((const char *)p,"c13",&mParameters[2]);
	OscGetValue((const char *)p,"c14",&mParameters[3]);
}

/**************************************************************
 *函数名称：XHZS_SendWave
 *简    介：学会助手APP虚拟示波器下位机程序（兼容山外）
 *输    入：wareaddr:数据数组起始地址 waresize:数组大小
 *输    出：无
 *注意事项：无
 **************************************************************/
void XHZS_SendWave(uint8_t *waveaddr, uint16_t wavesize)
{
   uint8_t cmdf[2] = {0x03,0xFC};		//帧头
   uint8_t cmdr[2] = {0xFC,0x03}; 		//帧尾
   
   Uart_SendArray(USART1,cmdf,sizeof(cmdf)); 	//发送帧头
   Uart_SendArray(USART1,waveaddr, wavesize);	//发送数据
   Uart_SendArray(USART1,cmdr,sizeof(cmdr)); 	//发送帧尾
}
/**************************************************************
 *函数名称：SendWave
 *简    介：发送数据到学会助手APP虚拟示波器
 *输    入：无
 *输    出：无
 *注意事项：将要发送的数据赋值给Wave数组即可，通道依次对应
 **************************************************************/
void SendWave()
{
	float i;
	float Wave[4]={0};	//注意数据类型；数组大小即为通道数量，最多四条通道
	
	Wave[0] =mParameters[0];
	Wave[1] =mParameters[1];
	Wave[2] =mParameters[2];
	Wave[3] =mParameters[3];
	XHZS_SendWave((uint8_t *)Wave,sizeof(Wave));
	
//	  //正弦波测试
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
 *函数名称：SendLocation
 *简    介：发送经纬度到学会助手APP
 *输    入：longitude:经度 latitude:纬度
 *输    出：无
 *注意事项：浮点数据有误差，显示数据可能会与发送不同
 **************************************************************/
void SendLocation(float longitude,float latitude)
{
	float Wave[2]={0};
		
	Wave[0] =longitude;
	Wave[1] =latitude;

  XHZS_SendWave((uint8_t *)Wave,sizeof(Wave));	
}
/**************************************************************
 *函数名称：SendXY
 *简    介：发送坐标到学会助手APP
 *输    入：x,y
 *输    出：无
 *注意事项：无
 **************************************************************/
void SendXY(int x,int y)
{
	int Wave[2]={0};
		
	Wave[0] =x;
	Wave[1] =y;

  XHZS_SendWave((uint8_t *)Wave,sizeof(Wave));	
}








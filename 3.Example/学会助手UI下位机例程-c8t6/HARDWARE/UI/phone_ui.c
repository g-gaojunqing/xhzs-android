
/**********************************************************************************************************************
 *@brief   学会助手APP UI功能下位机底层程序
 *@by      一品芝麻糕
 *@notice  与学会助手UI功能配合使用
 *				 在MainWindow.c中创建界面
 *@time    2024-03-15
 *********************************************************************************************************************/
#include "phone_ui.h"
#include "main_window.h"
#include "usart.h" //此处为串口发送函数头文件
#include <math.h>
#include <string.h>
#include <stdlib.h>

uint8_t UI_Start[2] = {0x01,0x09};	//帧头
uint8_t UI_End[2] = {0x08,0x07}; 	//帧尾

uint16_t UI_ID=1;

TextView_TypeDef *TextViewList=NULL;
Button_TypeDef *ButtonList=NULL;
EditText_TypeDef *EditTextList=NULL;
Switch_TypeDef *SwitchList=NULL;
SeekBar_TypeDef *SeekBarList=NULL;
LineChart_TypeDef *LineChartList=NULL;
JoyStick_TypeDef *JoyStickList=NULL;

//串口发送一个字节
void UI_SendByte(unsigned char byte)
{
	Uart_SendByte(USART1,byte); //修改此处为工程中串口发送一个字节函数
}
//串口发送一个数组
void UI_SendArray(uint8_t *array,unsigned int num)
{
	while(num--)
	{
		UI_SendByte(*array);
		array++;
	}
}
//求绝对值
int  my_abs(int dat)
{
    if(dat>=0)  return dat;
    else        return -dat;
}
//求平方
uint32_t my_pow(uint8_t m,int8_t n)
{
	uint32_t result=1;	 
	while(n--)result*=m;    
	return result;
}
//串口发送一个整数
void UI_SendInt(int num)
{
	uint8_t len=10;
  int32_t absnum;	
	uint8_t t,temp;
	uint8_t enshow=0;
	absnum=my_abs(num);
	
	if(num<0) UI_SendByte('-');
	for(t=1;t<=len;t++)
	{
		temp=(absnum/my_pow(10,len-t))%10;
		if(enshow==0&&t<=len)
		{
			if(temp==0)
			{
				if(t==len) UI_SendByte('0');
				continue;
			}
			else 
			{
				enshow=1; 
			}
		}
		UI_SendByte(temp+0x30); 
	}
}

/**************************************************************
 *函数名称：Window_Clear
 *简    介：清空窗口所有控件
 *输    入：无
 *输    出：无
 *注意事项：无
 **************************************************************/
void Window_Clear()
{
	uint8_t i;
	for(i=0;i<20;i++)
	{
		UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾,结束上一帧命令
	}
	
	UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(1);                          				//数据总数
	UI_SendByte(UI_Window_Clear);                   //命令
	UI_SendByte(0);                    					//数据，凑数
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
	TextViewList=NULL;
	ButtonList=NULL;
	EditTextList=NULL;
	SwitchList=NULL;
	SeekBarList=NULL;
	LineChartList=NULL;
	JoyStickList=NULL;
	UI_ID=1;
}

/**************************************************************
 *函数名称：Window_SetBackground
 *简    介：设置窗口背景颜色
 *输    入：color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：设置窗口颜色后，壁纸将不再显示
 **************************************************************/
void Window_SetBackground(int color)
{
	UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(sizeof(color));                          //数据总数
	UI_SendByte(UI_Window_Background);                            //命令
	UI_SendArray((uint8_t*)&color,sizeof(color));        //发送数据
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：Window_SetSize
 *简    介：设置窗口宽高
 *输    入：width:窗口宽度；heigh:窗口高度；
						fill:(0)设置窗口比例与实际屏幕比例不同时不填充，比例更严格
								(1)不同也填充整个屏幕，没有黑边，强迫症福音
 *输    出：无
 *注意事项：无
 **************************************************************/
void Window_SetSize(uint16_t width,uint16_t height,char fill)
{
	UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(5);                          //数据总数
	UI_SendByte(UI_Window_Size);                            //命令
	UI_SendArray((uint8_t*)&width,sizeof(width));        //发送数据
	UI_SendArray((uint8_t*)&height,sizeof(height));
	UI_SendArray((uint8_t*)&fill,sizeof(fill));
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：AddTextViewToList
 *简    介：添加一个文本框至链表中
 *输    入：TextView:TextView控件
 *输    出：添加是否成功
 *注意事项：无
 **************************************************************/
char AddTextViewToList(TextView_TypeDef *TextView)
{
	TextView_TypeDef *pTemp=TextViewList;
	//检测控件是否已经加入列表
	while(pTemp)
	{
		if(pTemp==TextView)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	TextView->id=UI_ID++;
	//将控件加入列表
	TextView->next=TextViewList;
	TextViewList=TextView;
	return 0;
}

/**************************************************************
 *函数名称：TextView_Init
 *简    介：添加一个文本框
 *输    入：TextView:TextView控件；x：起点x坐标；y：起点y坐标
						width：控件宽；height：控件高
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_Init(TextView_TypeDef *TextView,short x,short y,short width,short height)
{
	if(AddTextViewToList(TextView))
	{
		return;
	}
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(10);     //数据总数
	UI_SendByte(UI_TextView_Init);     //发送命令
	UI_SendArray((uint8_t*)&TextView->id,2); //发送数据
	UI_SendArray((uint8_t*)&x,2); //发送数据
	UI_SendArray((uint8_t*)&y,2); //发送数据
	UI_SendArray((uint8_t*)&width,2); //发送数据
	UI_SendArray((uint8_t*)&height,2); //发送数据
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}

/**************************************************************
 *函数名称：TextView_SetStyle
 *简    介：设置文本框样式
 *输    入：TextView:TextView控件，Style:样式
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_SetStyle(TextView_TypeDef TextView,TextViewStyle_TypeDef Style)
{
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(sizeof(Style)+2);                                      //数据总数
	UI_SendByte(UI_TextView_Style);     //发送命令
	UI_SendArray((uint8_t*)&TextView.id,2); //发送id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //发送数据
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}

/**************************************************************
 *函数名称：TextView_SetText
 *简    介：文本框设置文本（原来的会被清除）
 *输    入：TextView:TextView控件 *text:要显示的文本
 *输    出：无
 *注意事项：text注意最后以0xOO结尾
 **************************************************************/
void TextView_SetText(TextView_TypeDef TextView,char *text)
{
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据量不确定，设置为零
	UI_SendByte(UI_TextView_SetText);                          //命令
	UI_SendArray((uint8_t*)&TextView.id,4);  //发送数据
  while(*text)
	{
		UI_SendByte(*text);
		text++;
	}	
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}

/**************************************************************
 *函数名称：TextView_AddText
 *简    介：文本框添加文本（原来的不清除）
 *输    入：TextView:TextView控件 *text:要显示的文本
 *输    出：无
 *注意事项：text注意最后以0xOO结尾
 **************************************************************/
void TextView_AddText(TextView_TypeDef TextView,char *text)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数,0表示无固定数量
	UI_SendByte(UI_TextView_AddText);                          //命令
	UI_SendArray((uint8_t*)&TextView.id,2);                //发送id
    while(*text)
	{
		UI_SendByte(*text);
		text++;
	}	
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}

/**************************************************************
 *函数名称：TextView_SetInt
 *简    介：文本输入框显示整数
 *输    入：TextView:TextView控件 num:整数
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_SetInt(TextView_TypeDef TextView,int num)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数
	UI_SendByte(UI_TextView_SetText);                          //命令
	UI_SendArray((uint8_t*)&TextView.id,2);                //发送id
    UI_SendInt(num);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}
/**************************************************************
 *函数名称：TextView_AddInt
 *简    介：文本输入框显示整数（原来的不清除）
 *输    入：TextView:TextView控件 num:整数
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_AddInt(TextView_TypeDef TextView,int num)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数
	UI_SendByte(UI_TextView_AddText);                          //命令
	UI_SendArray((uint8_t*)&TextView.id,2);                //发送id
    UI_SendInt(num);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}
/**************************************************************
 *函数名称：TextView_SetFloat
 *简    介：文本输入框显示浮点数
 *输    入：TextView:TextView控件 num:浮点数 len:小数点后位数
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_SetFloat(TextView_TypeDef TextView,float num,uint8_t len)
{
	uint32_t num1;
	
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数
	UI_SendByte(UI_TextView_SetText);                          //命令
	UI_SendArray((uint8_t*)&TextView.id,2);                //发送id
	if((num<0)&&(num>-1))
	UI_SendByte('-');	//补负号
	UI_SendInt((int)num);
	UI_SendByte('.');
	if(num<0) num=-num;
    num1=((uint32_t)(num*pow(10,len)));
    num1=num1%my_pow(10,len);
	UI_SendInt(num1);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}
/**************************************************************
 *函数名称：TextView_AddFloat
 *简    介：文本输入框显示浮点数（原来的不清除）
 *输    入：TextView:TextView控件 num:浮点数 len:小数点后位数
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_AddFloat(TextView_TypeDef TextView,float num,uint8_t len)
{
	uint32_t num1;
	
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数
	UI_SendByte(UI_TextView_AddText);                          //命令
	UI_SendArray((uint8_t*)&TextView.id,2);                //发送id
	if((num<0)&&(num>-1))
	UI_SendByte('-');	//补负号
	UI_SendInt((int)num);
	UI_SendByte('.');
	if(num<0) num=-num;
    num1=((uint32_t)(num*pow(10,len)));
    num1=num1%my_pow(10,len);
	UI_SendInt(num1);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}
/**************************************************************
 *函数名称：TextView_SetTextSize
 *简    介：设置文本框字体大小
 *输    入：TextView:TextView控件 size:字体大小
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_SetTextSize(TextView_TypeDef TextView,int size)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));  //发送帧头
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_TextView_TextSize);                   //命令
	UI_SendArray((uint8_t*)&TextView.id,2);				  //控件id
	UI_SendArray((uint8_t*)&size,4);         //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}

/**************************************************************
 *函数名称：TextView_SetTextColor
 *简    介：设置文本框字体颜色
 *输    入：TextView:TextView控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_SetTextColor(TextView_TypeDef TextView,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);             //数据总数
	UI_SendByte(UI_TextView_TextColor);                 //命令
	UI_SendArray((uint8_t*)&TextView.id,2);
	UI_SendArray((uint8_t*)&color,4);      //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}
/**************************************************************
 *函数名称：TextView_SetTextColor
 *简    介：设置文本框文本位置
 *输    入：TextView:TextView控件 gravity:位置，在.h文件中
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_SetTextGravity(TextView_TypeDef TextView,int gravity)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);             //数据总数
	UI_SendByte(UI_TextView_Gravity);                 //命令
	UI_SendArray((uint8_t*)&TextView.id,2);
	UI_SendArray((uint8_t*)&gravity,4);      //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}
/**************************************************************
 *函数名称：TextView_SetBackgroundColor
 *简    介：设置文本框背景颜色
 *输    入：TextView:TextView控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_SetBackgroundColor(TextView_TypeDef TextView,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_TextView_BackgroundColor);                  //命令
	UI_SendArray((uint8_t*)&TextView.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：TextView_SetRotation
 *简    介：设置文本框背景颜色
 *输    入：TextView:TextView控件 rotation:角度
 *输    出：无
 *注意事项：无
 **************************************************************/
void TextView_SetRotation(TextView_TypeDef TextView,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_TextView_Rotation);                  //命令
	UI_SendArray((uint8_t*)&TextView.id,2);				  //控件id
	UI_SendArray((uint8_t*)&rotation,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：AddButtonToList
 *简    介：添加一个文本框至链表中
 *输    入：Button:Button控件
 *输    出：添加是否成功
 *注意事项：无
 **************************************************************/
char AddButtonToList(Button_TypeDef *Button)
{
	Button_TypeDef *pTemp=ButtonList;
	//检测控件是否已经加入列表
	while(pTemp)
	{
		if(pTemp==Button)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	Button->id=UI_ID++;
	//将控件加入列表
	Button->next=ButtonList;
	ButtonList=Button;
	return 0;
}
/**************************************************************
 *函数名称：Button_Init
 *简    介：添加一个文本框
 *输    入：Button:Button控件；x：起点x坐标；y：起点y坐标
						width：控件宽；height：控件高
 *输    出：无
 *注意事项：无
 **************************************************************/
void Button_Init(Button_TypeDef *Button,short x,short y,short width,short height)
{
	if(AddButtonToList(Button))
	{
		return;
	}
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	  //发送帧头
	UI_SendByte(10);    //数据总数
	UI_SendByte(UI_Button_Init);   //发送命令
	UI_SendArray((uint8_t*)&Button->id,2); //发送数据
	UI_SendArray((uint8_t*)&x,2); //发送数据
	UI_SendArray((uint8_t*)&y,2); //发送数据
	UI_SendArray((uint8_t*)&width,2); //发送数据
	UI_SendArray((uint8_t*)&height,2); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧头
}

/**************************************************************
 *函数名称：Button_Init
 *简    介：设置文本框样式
 *输    入：Button:Button控件；Style：控件样式
 *输    出：无
 *注意事项：无
 **************************************************************/
void Button_SetStyle(Button_TypeDef Button,ButtonStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(sizeof(Style)+2);                                      //数据总数
	UI_SendByte(UI_Button_Style);     //发送命令
	UI_SendArray((uint8_t*)&Button.id,2); //发送id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}

/**************************************************************
 *函数名称：Button_SetText
 *简    介：设置按键文本
 *输    入：Button:Button控件 *text:要显示的文本
 *输    出：无
 *注意事项：使用数组时，注意最后以0x00结尾
 **************************************************************/
void Button_SetText(Button_TypeDef Button,char *text)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数
	UI_SendByte(UI_Button_Text);                          //命令
	UI_SendArray((uint8_t*)&Button.id,2);                //控件id
    while(*text)                                           //发送数据
	{
		UI_SendByte(*text);
		text++;
	}	
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}

/**************************************************************
 *函数名称：Button_SetColor
 *简    介：设置按钮颜色
 *输    入：Button:Button控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void Button_SetColor(Button_TypeDef Button,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_Button_Color);                    //命令
	UI_SendArray((uint8_t*)&Button.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}

/**************************************************************
 *函数名称：Button_SetTextSize
 *简    介：设置按钮字体大小
 *输    入：Button:Button控件 size:字体大小
 *输    出：无
 *注意事项：无
 **************************************************************/
void Button_SetTextSize(Button_TypeDef Button,int size)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));  //发送帧头
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_Button_TextSize);                   //命令
	UI_SendArray((uint8_t*)&Button.id,2);				  //控件id
	UI_SendArray((uint8_t*)&size,4);         //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}

/**************************************************************
 *函数名称：Button_SetTextColor
 *简    介：设置按钮字体颜色
 *输    入：Button:Button控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void Button_SetTextColor(Button_TypeDef Button,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);             //数据总数
	UI_SendByte(UI_Button_TextColor);                 //命令
	UI_SendArray((uint8_t*)&Button.id,2);
	UI_SendArray((uint8_t*)&color,4);      //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}
/**************************************************************
 *函数名称：Button_SetBackgroundColor
 *简    介：设置按钮背景颜色
 *输    入：Button:Button控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：跟随Button_Color的颜色，自设颜色无效
 **************************************************************/
void Button_SetBackgroundColor(Button_TypeDef Button,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_Button_BackgroundColor);                    //命令
	UI_SendArray((uint8_t*)&Button.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：Button_SetRotation
 *简    介：设置文本框角度
 *输    入：Button:Button控件 rotation:角度
 *输    出：无
 *注意事项：无
 **************************************************************/
void Button_SetRotation(Button_TypeDef Button,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_Button_Rotation);                  //命令
	UI_SendArray((uint8_t*)&Button.id,2);				  //控件id
	UI_SendArray((uint8_t*)&rotation,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}

/**************************************************************
 *函数名称：AddEditTextToList
 *简    介：添加一个文本输入框至链表中
 *输    入：EditText:EditText控件
 *输    出：添加是否成功
 *注意事项：无
 **************************************************************/
char AddEditTextToList(EditText_TypeDef *EditText)
{
	EditText_TypeDef *pTemp=EditTextList;
	//检测控件是否已经加入列表
	while(pTemp)
	{
		if(pTemp==EditText)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	EditText->id=UI_ID++;
	//将控件加入列表
	EditText->next=EditTextList;
	EditTextList=EditText;
	return 0;
}
/**************************************************************
 *函数名称：EditText_Init
 *简    介：添加一个文本框
 *输    入：EditText:EditText控件；x：起点x坐标；y：起点y坐标
						width：控件宽；height：控件高
 *输    出：无
 *注意事项：无
 **************************************************************/
void EditText_Init(EditText_TypeDef *EditText,short x,short y,short width,short height)
{
	if(AddEditTextToList(EditText))
	{
			return;
	}
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	                  //发送帧头
	UI_SendByte(10);                                  //数据总数
	UI_SendByte(UI_EditText_Init);                                              //发送命令
	UI_SendArray((uint8_t*)&EditText->id,2); //发送数据
	UI_SendArray((uint8_t*)&x,2); //发送数据
	UI_SendArray((uint8_t*)&y,2); //发送数据
	UI_SendArray((uint8_t*)&width,2); //发送数据
	UI_SendArray((uint8_t*)&height,2); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	                      //发送帧头
}

/**************************************************************
 *函数名称：EditText_SetStyle
 *简    介：设置文本输入框样式
 *输    入：EditText:EditText控件；Style：样式
 *输    出：无
 *注意事项：无
 **************************************************************/
void EditText_SetStyle(EditText_TypeDef EditText,EditTextStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(sizeof(Style)+2);                                      //数据总数
	UI_SendByte(UI_EditText_Style);     //发送命令
	UI_SendArray((uint8_t*)&EditText.id,2); //发送id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}

/**************************************************************
 *函数名称：EditText_SetText
 *简    介：设置文本输入框文本
 *输    入：EditText:EditText控件 *text:要显示的文本
 *输    出：无
 *注意事项：使用数组时，注意最后以0xOO结尾
						设置后EditText.text同步更新
 **************************************************************/
void EditText_SetText(EditText_TypeDef EditText,char *text)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数
	UI_SendByte(UI_EditText_SetText);                          //命令
	UI_SendArray((uint8_t*)&EditText.id,2); //控件id
    while(*text)                                            //发送数据
	{
		UI_SendByte(*text);
		text++;
	}	
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}

/**************************************************************
 *函数名称：EditText_SetInt
 *简    介：文本输入框显示整数
 *输    入：EditText:EditText控件 num:整数
 *输    出：无
 *注意事项：无
 **************************************************************/
void EditText_SetInt(EditText_TypeDef EditText,int num)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);   //数据总数
	UI_SendByte(UI_EditText_SetText);                          //命令
	UI_SendArray((uint8_t*)&EditText.id,2);  //发送id
    UI_SendInt(num);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));  //发送帧尾
}

/**************************************************************
 *函数名称：EditText_SetFloat
 *简    介：文本输入框显示浮点数
 *输    入：EditText:EditText控件 num:数；len:小数点后位数
 *输    出：无
 *注意事项：无
 **************************************************************/
void EditText_SetFloat(EditText_TypeDef EditText,float num,uint8_t len)
{
	uint32_t num1;
	
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数
	UI_SendByte(UI_EditText_SetText);                          //命令
	UI_SendArray((uint8_t*)&EditText.id,2);  //发送id
	if((num<0)&&(num>-1))
	UI_SendByte('-');	//补负号
	UI_SendInt((int)num);
	UI_SendByte('.');
	if(num<0) num=-num;
    num1=((uint32_t)(num*pow(10,len)));
    num1=num1%my_pow(10,len);
	UI_SendInt(num1);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));  //发送帧尾
}

/**************************************************************
 *函数名称：EditText_SetHint
 *简    介：文本输入框添加提示语
 *输    入：EditText:EditText控件 *text:要显示的提示语（字符串）
 *输    出：无
 *注意事项：text注意最后以0xOO结尾
 **************************************************************/
void EditText_SetHint(EditText_TypeDef EditText, char *text)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//发送帧头
	UI_SendByte(0);                                         //数据总数
	UI_SendByte(UI_EditText_SetHint);                          //命令
	UI_SendArray((uint8_t*)&EditText.id,2); //控件id
    while(*text)                                           //发送数据
	{
		UI_SendByte(*text);
		text++;
	}	
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //发送帧尾
}
/**************************************************************
 *函数名称：EditText_SetTextSize
 *简    介：设置文本框字体大小
 *输    入：EditText:EditText控件 size:字体大小
 *输    出：无
 *注意事项：无
 **************************************************************/
void EditText_SetTextSize(EditText_TypeDef EditText,int size)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));  //发送帧头
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_EditText_TextSize);                   //命令
	UI_SendArray((uint8_t*)&EditText.id,2);				  //控件id
	UI_SendArray((uint8_t*)&size,4);         //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}

/**************************************************************
 *函数名称：EditText_SetTextColor
 *简    介：设置文本框字体颜色
 *输    入：EditText:EditText控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void EditText_SetTextColor(EditText_TypeDef EditText,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);             //数据总数
	UI_SendByte(UI_EditText_TextColor);                 //命令
	UI_SendArray((uint8_t*)&EditText.id,2);
	UI_SendArray((uint8_t*)&color,4);      //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}
/**************************************************************
 *函数名称：EditText_SetBackgroundColor
 *简    介：设置按键背景颜色
 *输    入：EditText:EditText控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void EditText_SetBackgroundColor(EditText_TypeDef EditText,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_EditText_BackgroundColor);                    //命令
	UI_SendArray((uint8_t*)&EditText.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：EditText_SetRotation
 *简    介：设置文本框背景颜色
 *输    入：EditText:EditText控件 rotation:角度
 *输    出：无
 *注意事项：无
 **************************************************************/
void EditText_SetRotation(EditText_TypeDef EditText,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_EditText_Rotation);                  //命令
	UI_SendArray((uint8_t*)&EditText.id,2);				  //控件id
	UI_SendArray((uint8_t*)&rotation,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：AddSwitchToList
 *简    介：添加一个开关至链表中
 *输    入：Switch:Switch控件
 *输    出：添加是否成功
 *注意事项：无
 **************************************************************/
char AddSwitchToList(Switch_TypeDef *Switch)
{
	Switch_TypeDef *pTemp=SwitchList;
	//检测控件是否已经加入列表
	while(pTemp)
	{
		if(pTemp==Switch)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	Switch->id=UI_ID++;
	//将控件加入列表
	Switch->next=SwitchList;
	SwitchList=Switch;
	return 0;
}
/**************************************************************
 *函数名称：Switch_Init
 *简    介：开关初始化
 *输    入：Switch:TextView控件；x：起点x坐标；y：起点y坐标
						width：控件宽；height：控件高
 *输    出：无
 *注意事项：无
 **************************************************************/
void Switch_Init(Switch_TypeDef *Switch,short x,short y,short width,short height)
{
	if(AddSwitchToList(Switch))
	{
		return;
	}

    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	 //发送帧头
	UI_SendByte(10);   //数据总数
	UI_SendByte(UI_Switch_Init);  //发送命令
	UI_SendArray((uint8_t*)&Switch->id,2); //发送id
	UI_SendArray((uint8_t*)&x,2); //发送数据
	UI_SendArray((uint8_t*)&y,2); //发送数据
	UI_SendArray((uint8_t*)&width,2); //发送数据
	UI_SendArray((uint8_t*)&height,2); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧头
}
/**************************************************************
 *函数名称：Switch_SetStyle
 *简    介：设置文本框样式
 *输    入：Switch:Switch控件
 *输    出：无
 *注意事项：无
 **************************************************************/
void Switch_SetStyle(Switch_TypeDef Switch,SwitchStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(sizeof(Style)+2);                                      //数据总数
	UI_SendByte(UI_Switch_Style);     //发送命令
	UI_SendArray((uint8_t*)&Switch.id,2); //发送id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}
/**************************************************************
 *函数名称：Switch_SetTextColor
 *简    介：设置文本框字体颜色
 *输    入：Switch:Switch控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void Switch_SetChecked(Switch_TypeDef Switch,int checked)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);             //数据总数
	UI_SendByte(UI_Switch_Checked);                 //命令
	UI_SendArray((uint8_t*)&Switch.id,2);
	UI_SendArray((uint8_t*)&checked,4);      //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}
/**************************************************************
 *函数名称：Switch_SetTextColor
 *简    介：设置文本框字体颜色
 *输    入：Switch:Switch控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void Switch_SetColor(Switch_TypeDef Switch,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);             //数据总数
	UI_SendByte(UI_Switch_Color);                 //命令
	UI_SendArray((uint8_t*)&Switch.id,2);
	UI_SendArray((uint8_t*)&color,4);      //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}
/**************************************************************
 *函数名称：Switch_SetBackgroundColor
 *简    介：设置开关背景颜色
 *输    入：Switch:Switch控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void Switch_SetBackgroundColor(Switch_TypeDef Switch,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_Switch_BackgroundColor);                    //命令
	UI_SendArray((uint8_t*)&Switch.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：Switch_SetRotation
 *简    介：设置文本框背景颜色
 *输    入：Switch:Switch控件 rotation:角度
 *输    出：无
 *注意事项：无
 **************************************************************/
void Switch_SetRotation(Switch_TypeDef Switch,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_Switch_Rotation);                  //命令
	UI_SendArray((uint8_t*)&Switch.id,2);				  //控件id
	UI_SendArray((uint8_t*)&rotation,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}

/**************************************************************
 *函数名称：AddSeekBarToList
 *简    介：添加一个拖动条至链表中
 *输    入：SeekBar:SeekBar控件
 *输    出：添加是否成功
 *注意事项：无
 **************************************************************/
char AddSeekBarToList(SeekBar_TypeDef *SeekBar)
{
	SeekBar_TypeDef *pTemp=SeekBarList;
	//检测控件是否已经加入列表
	while(pTemp)
	{
		if(pTemp==SeekBar)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	SeekBar->id=UI_ID++;
	//将控件加入列表
	SeekBar->next=SeekBarList;
	SeekBarList=SeekBar;
	return 0;
}
/**************************************************************
 *函数名称：SeekBar_Init
 *简    介：开关初始化
 *输    入：SeekBar:SeekBar控件；x：起点x坐标；y：起点y坐标
						width：控件宽；height：控件高
 *输    出：无
 *注意事项：宽大于高横向，宽小于高竖向
 **************************************************************/
void SeekBar_Init(SeekBar_TypeDef *SeekBar,short x,short y,short width,short height)
{
	if(AddSeekBarToList(SeekBar))
	{
		return;
	}
	
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	                  //发送帧头
	UI_SendByte(10);                                  //数据总数
	UI_SendByte(UI_SeekBar_Init);                                              //发送命令
	UI_SendArray((uint8_t*)&SeekBar->id,2); //发送id
	UI_SendArray((uint8_t*)&x,2); //发送数据
	UI_SendArray((uint8_t*)&y,2); //发送数据
	UI_SendArray((uint8_t*)&width,2); //发送数据
	UI_SendArray((uint8_t*)&height,2); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	                      //发送帧头
}
/**************************************************************
 *函数名称：SeekBar_SetStyle
 *简    介：设置拖动条样式
 *输    入：SeekBar:SeekBar控件
 *输    出：无
 *注意事项：无
 **************************************************************/
void SeekBar_SetStyle(SeekBar_TypeDef SeekBar,SeekBarStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(sizeof(Style)+2);                                      //数据总数
	UI_SendByte(UI_SeekBar_Style);     //发送命令
	UI_SendArray((uint8_t*)&SeekBar.id,2); //发送id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}
/**************************************************************
 *函数名称：SeekBar_SetMax
 *简    介：设置拖动条最大值
 *输    入：SeekBar:SeekBar控件 max:拖动条最大值
 *输    出：无
 *注意事项：无
 **************************************************************/
void SeekBar_SetMax(SeekBar_TypeDef SeekBar,int max)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));  //发送帧头
	UI_SendByte(6);  //数据总数
	UI_SendByte(UI_SeekBar_Max);                         //命令
	UI_SendArray((uint8_t*)&SeekBar.id,2);	//控件id
	UI_SendArray((uint8_t*)&max,4);           //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}

/**************************************************************
 *函数名称：SeekBar_SetProcess
 *简    介：设置拖动条当前值
 *输    入：SeekBar:SeekBar控件 process:需设置当前值
 *输    出：无
 *注意事项：无
 **************************************************************/
void SeekBar_SetProgress(SeekBar_TypeDef SeekBar,int progress)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));   //发送帧头
	UI_SendByte(6);  //数据总数
	UI_SendByte(UI_SeekBar_Process);     //命令
	UI_SendArray((uint8_t*)&SeekBar.id,2);	//控件id
	UI_SendArray((uint8_t*)&progress,sizeof(progress));    //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	  //发送帧尾
}

/**************************************************************
 *函数名称：SeekBar_SetColor
 *简    介：设置拖动条颜色
 *输    入：SeekBar:SeekBar控件 color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void SeekBar_SetColor(SeekBar_TypeDef SeekBar,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);             //数据总数
	UI_SendByte(UI_SeekBar_Color);                 //命令
	UI_SendArray((uint8_t*)&SeekBar.id,2);
	UI_SendArray((uint8_t*)&color,4);      //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//发送帧尾
}
/**************************************************************
 *函数名称：SeekBar_SetBackgroundColor
 *简    介：设置拖动条背景颜色
 *输    入：SeekBar:SeekBar控件；color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void SeekBar_SetBackgroundColor(SeekBar_TypeDef SeekBar,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_SeekBar_BackgroundColor);                    //命令
	UI_SendArray((uint8_t*)&SeekBar.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：SeekBar_SetRotation
 *简    介：设置拖动条旋转角度
 *输    入：SeekBar:SeekBar控件 rotation:角度
 *输    出：无
 *注意事项：无
 **************************************************************/
void SeekBar_SetRotation(SeekBar_TypeDef SeekBar,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_SeekBar_Rotation);                  //命令
	UI_SendArray((uint8_t*)&SeekBar.id,2);				  //控件id
	UI_SendArray((uint8_t*)&rotation,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}

/**************************************************************
 *函数名称：AddLineChartToList
 *简    介：添加一个折线图至链表中
 *输    入：LineChart:LineChart控件
 *输    出：添加是否成功
 *注意事项：无
 **************************************************************/
char AddLineChartToList(LineChart_TypeDef *LineChart)
{
	LineChart_TypeDef *pTemp=LineChartList;
	//检测控件是否已经加入列表
	while(pTemp)
	{
		if(pTemp==LineChart)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	LineChart->id=UI_ID++;
	//将控件加入列表
	LineChart->next=LineChartList;
	LineChartList=LineChart;
	return 0;
}
/**************************************************************
 *函数名称：LineChart_Init
 *简    介：折线图初始化
 *输    入：LineChart:TextView控件；x：起点x坐标；y：起点y坐标
						width：控件宽；height：控件高
 *输    出：无
 *注意事项：最多显示1000个数据
 **************************************************************/
void LineChart_Init(LineChart_TypeDef *LineChart,short x,short y,short width,short height)
{
	if(AddLineChartToList(LineChart))
	{
		return;
	}
	
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(10);  //数据总数
	UI_SendByte(UI_LineChart_Init);  //发送命令
	UI_SendArray((uint8_t*)&LineChart->id,2); //发送id
	UI_SendArray((uint8_t*)&x,2); //发送数据
	UI_SendArray((uint8_t*)&y,2); //发送数据
	UI_SendArray((uint8_t*)&width,2); //发送数据
	UI_SendArray((uint8_t*)&height,2); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧头
}

/**************************************************************
 *函数名称：LineChart_SetStyle
 *简    介：设置文本框样式
 *输    入：LineChart:LineChart控件
 *输    出：无
 *注意事项：无
 **************************************************************/
void LineChart_SetStyle(LineChart_TypeDef LineChart,LineChartStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(sizeof(Style)+2);                                      //数据总数
	UI_SendByte(UI_LineChart_Style);     //发送命令
	UI_SendArray((uint8_t*)&LineChart.id,2); //发送id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}



/**************************************************************
 *函数名称：LineChart_AddData
 *简    介：在折线图中添加一个数据
 *输    入：LineChart:LineChart控件，data：数据
 *输    出：无
 *注意事项：无
 **************************************************************/
void LineChart_AddData(LineChart_TypeDef LineChart,double data)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(10);  //数据总数
	UI_SendByte(UI_LineChart_AddData);  //发送命令
	UI_SendArray((uint8_t*)&LineChart.id,2);	//控件id
	UI_SendArray((uint8_t*)&data,8); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧头
}
/**************************************************************
 *函数名称：LineChart_Clear
 *简    介：清除折线图数据
 *输    入：LineChart:LineChart控件
 *输    出：无
 *注意事项：无
 **************************************************************/
void LineChart_Clear(LineChart_TypeDef LineChart)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(2);     //数据总数
	UI_SendByte(UI_LineChart_Clear);      //命令
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //控件id
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：LineChart_SetLineXAxis
 *简    介：设置X轴显示数据数量
 *输    入：LineChart:LineChart控件，x_max：X轴显示数量
 *输    出：无
 *注意事项：无
 **************************************************************/
void LineChart_SetXAxis(LineChart_TypeDef LineChart,int x_max)
{
	UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_LineChart_XAxis);                  //命令
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //控件id
	UI_SendArray((uint8_t*)&x_max,4);        //发送数据
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：LineChart_SetLineYAxis
 *简    介：设置Y轴显示数据数量
 *输    入：LineChart:LineChart控件；
						y_min：Y轴最小值；y_max：Y轴最大值；
 *输    出：无
 *注意事项：无
 **************************************************************/
void LineChart_SetYAxis(LineChart_TypeDef LineChart,float y_min,float y_max)
{
		UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(10);    //数据总数
	UI_SendByte(UI_LineChart_YAxis);            //命令
	UI_SendArray((uint8_t*)&LineChart.id,2);	//控件id
	UI_SendArray((uint8_t*)&y_min,4);        //发送数据
	UI_SendArray((uint8_t*)&y_max,4);        //发送数据
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}

/**************************************************************
 *函数名称：LineChart_SetLineColor
 *简    介：设置折线图折线颜色
 *输    入：LineChart:LineChart控件；color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void LineChart_SetLineColor(LineChart_TypeDef LineChart,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_LineChart_LineColor);                  //命令
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：LineChart_SetChartColor
 *简    介：设置折线图坐标轴颜色
 *输    入：LineChart:LineChart控件；color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void LineChart_SetChartColor(LineChart_TypeDef LineChart,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_LineChart_ChartColor);                  //命令
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}
/**************************************************************
 *函数名称：LineChart_SetBackgroundColor
 *简    介：设置折线图背景颜色
 *输    入：LineChart:LineChart控件；color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void LineChart_SetBackgroundColor(LineChart_TypeDef LineChart,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //发送帧头	
	UI_SendByte(6);               //数据总数
	UI_SendByte(UI_LineChart_BackgroundColor);                  //命令
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //控件id
	UI_SendArray((uint8_t*)&color,4);        //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //发送帧尾
}

/**************************************************************
 *函数名称：AddJoyStickToList
 *简    介：添加一个摇杆至链表中
 *输    入：JoyStick:JoyStick控件
 *输    出：添加是否成功
 *注意事项：无
 **************************************************************/
char AddJoyStickToList(JoyStick_TypeDef *JoyStick)
{
	JoyStick_TypeDef *pTemp=JoyStickList;
	//检测控件是否已经加入列表
	while(pTemp)
	{
		if(pTemp==JoyStick)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	JoyStick->id=UI_ID++;
	//将控件加入列表
	JoyStick->next=JoyStickList;
	JoyStickList=JoyStick;
	return 0;
}
/**************************************************************
 *函数名称：JoyStick_Init
 *简    介：摇杆始化
 *输    入：JoyStick:JoyStick控件；x：起点x坐标；y：起点y坐标
						width：控件宽；height：控件高
 *输    出：无
 *注意事项：无
 **************************************************************/
void JoyStick_Init(JoyStick_TypeDef *JoyStick,short x,short y,short width,short height)
{
	if(AddJoyStickToList(JoyStick))
	{
		return;
	}
	JoyStick->x=100;
	JoyStick->y=100;
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	                  //发送帧头
	UI_SendByte(10);                                  //数据总数
	UI_SendByte(UI_JoyStick_Init);                                              //发送命令
	UI_SendArray((uint8_t *)&JoyStick->id,2); //发送id
	UI_SendArray((uint8_t*)&x,2); //发送数据
	UI_SendArray((uint8_t*)&y,2); //发送数据
	UI_SendArray((uint8_t*)&width,2); //发送数据
	UI_SendArray((uint8_t*)&height,2); //发送数据
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	                      //发送帧头
}

/**************************************************************
 *函数名称：JoyStick_SetStyle
 *简    介：设置摇杆样式
 *输    入：JoyStick:JoyStick控件；Style：摇杆样式
 *输    出：无
 *注意事项：无
 **************************************************************/
void JoyStick_SetStyle(JoyStick_TypeDef JoyStick,JoyStickStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(sizeof(Style)+2);                                      //数据总数
	UI_SendByte(UI_JoyStick_Style);     //发送命令
	UI_SendArray((uint8_t*)&JoyStick.id,2); //发送id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}
/**************************************************************
 *函数名称：JoyStick_SetColor
 *简    介：设置摇杆颜色
 *输    入：JoyStick:JoyStick控件；color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void JoyStick_SetColor(JoyStick_TypeDef JoyStick,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);                                      //数据总数
	UI_SendByte(UI_JoyStick_Color);     //发送命令
	UI_SendArray((uint8_t*)&JoyStick.id,2); //发送id
	UI_SendArray((uint8_t*)&color,4); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}
/**************************************************************
 *函数名称：JoyStick_SetStyle
 *简    介：设置摇杆样式
 *输    入：JoyStick:JoyStick控件；shape：(0)圆形，(1)正方形
 *输    出：无
 *注意事项：无
 **************************************************************/
void JoyStick_SetShape(JoyStick_TypeDef JoyStick,int shape)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);                                      //数据总数
	UI_SendByte(UI_JoyStick_Shape);     //发送命令
	UI_SendArray((uint8_t*)&JoyStick.id,2); //发送id
	UI_SendArray((uint8_t*)&shape,4); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}
/**************************************************************
 *函数名称：JoyStick_SetStyle
 *简    介：设置摇杆背景颜色
 *输    入：JoyStick:JoyStick控件；color:颜色值,例:0xFF000000
 *输    出：无
 *注意事项：无
 **************************************************************/
void JoyStick_SetBackgroundColor(JoyStick_TypeDef JoyStick,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //发送帧头
	UI_SendByte(6);                                      //数据总数
	UI_SendByte(UI_JoyStick_BackgroundColor);     //发送命令
	UI_SendArray((uint8_t*)&JoyStick.id,2); //发送id
	UI_SendArray((uint8_t*)&color,4); //发送数据
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //发送帧尾
}

uint16_t UI_RX_STA=0;       //接收状态标记	
char UI_RX_BUF[UI_RX_LEN];     //接收缓冲,最大UI_RX_LEN个字节.

#if REAL_TIME_PROCESSING
void UI_ByteDeal(uint8_t res)
{
	if((UI_RX_STA&0x8000)==0)//接收未完成
	{
		if(res==0x0A)
		{
			if(UI_RX_STA>0)
			{
				UI_RX_STA|=0x8000;
				UI_RX_BUF[(UI_RX_STA&0x3FFF)-1]='\0'; //把0x0D换成0
				UI_CommandProcessor((char *)UI_RX_BUF); //接收成功后直接解析
			  UI_RX_STA=0;
			}
		}
		else
		{
			UI_RX_BUF[UI_RX_STA]=res;
			UI_RX_STA++;
			if(UI_RX_STA>=UI_RX_LEN)
			{
				UI_RX_STA=0;//接收数据错误,重新开始接收	 
			} 
		}			 
	}   	
}

#else

uint16_t rxStart=0,rxEnd=0;
uint16_t UI_BUF_STA=0;       //接收状态标记	
char UI_BUF[UI_BUF_LEN];     //接收缓冲,最大UI_BUF_LEN个字节.
//接收一个字节
void UI_ByteDeal(uint8_t res)
{		
	if(res==0x0A)
	{
		rxEnd=UI_BUF_STA;
	}
	UI_BUF[UI_BUF_STA]=res;
	UI_BUF_STA++;
	if(UI_BUF_STA>=UI_BUF_LEN)
	{
			UI_BUF_STA=0;
	}	
	if(UI_BUF_STA==rxStart)
  {
		rxStart=UI_BUF_STA+1;
		if(rxStart>=UI_BUF_LEN)
	  {
			rxStart=0;
	  }
	}
}
//缓冲区数据处理
//接收完一帧后，不在串口中断中处理时，
//把这个放在主程序中
int UI_BufferDeal()
{
	int num,start,end;
	char res;
	int i;
	start=rxStart;
	end=rxEnd;
	if(start==end)
	{
		return 0;
	}
	if(start>end)
	{
		end+=UI_BUF_LEN;
	}
	for(i=start;i<=end;i++)
	{
		res=UI_BUF[i%UI_BUF_LEN];
		if(res==0x0A)
		{
			if(UI_RX_STA>0)
			{
				 UI_RX_BUF[UI_RX_STA-1]='\0'; //把0x0D换成0
				 UI_CommandProcessor((char *)UI_RX_BUF); 
			   UI_RX_STA=0;
			}
		}
		else
		{
			UI_RX_BUF[UI_RX_STA]=res;
			UI_RX_STA++;
			if(UI_RX_STA>=UI_RX_LEN)
			{
				UI_RX_STA=0;//接收数据错误,重新开始接收	 
			} 
		}
	}
	num=end-start; //处理数据的数量
	end%=UI_BUF_LEN;
	rxStart=end;
	return num;
}


#endif

/**************************************************************
 *函数名称：BytesDeal
 *简    介：处理接收到的一帧字符串,并赋值给控件
 *输    入：p:字符串首地址，这里指控件发出的一帧命令
 *输    出：==0解析成功;!=0解析失败
 *注意事项：p参数必须以'\0'结尾，否则解析失败
 **************************************************************/
char UI_CommandProcessor(const char *p)
{
	char type=0;
	int id=0;
	Button_TypeDef *tmpButtonList=ButtonList;
	EditText_TypeDef *tmpEditTextList=EditTextList;
	Switch_TypeDef *tmpSwitchList=SwitchList;
	SeekBar_TypeDef *tmpSeekBarList=SeekBarList;
	JoyStick_TypeDef *tmpJoyStickList=JoyStickList;
	//验证数据正确性,以及控件类型
	if(*p>'A'&&*p<'z')
	{
		type=*p;
	}
	p++;
	//控件id
	while(*p!=':')
	{
		if(*p!='\0')
		{
			id=10*id+*p-0x30;
			p++;
		}
		else
		{
			return 2;
		}
	}
	p++;
	if(type=='b')
	{
		while(tmpButtonList)
		{
			if(tmpButtonList->id==id)
			{
				tmpButtonList->isPressed=(*p-0x30);
				tmpButtonList->isUpdated=1;
				return 0;
			}
			tmpButtonList=tmpButtonList->next;
		}
	}
	else if(type=='e')
	{
		while(tmpEditTextList)
		{
			if(tmpEditTextList->id==id)
			{
				if(strlen(p)<EditText_MaxNum)
				{
						strcpy(tmpEditTextList->text,p);
						tmpEditTextList->isUpdated=1;
						return 0;
				}
				return 4;
			}
			tmpEditTextList=tmpEditTextList->next;
		}
	}
	else if(type=='s')
	{
		while(tmpSwitchList)
		{
			if(tmpSwitchList->id==id)
			{
				tmpSwitchList->isChecked=(*p-0x30);
				tmpSwitchList->isUpdated=1;
				return 0;
			}
			tmpSwitchList=tmpSwitchList->next;
		}
	}
	else if(type=='S')
	{
		while(tmpSeekBarList)
		{
			if(tmpSeekBarList->id==id)
			{
				tmpSeekBarList->progress=0;
				while(*p!='\0')
				{
					tmpSeekBarList->progress=10*tmpSeekBarList->progress+(*p-0x30);
					p++;
				}
				tmpSeekBarList->isUpdated=1;
				return 0;
			}
			tmpSeekBarList=tmpSeekBarList->next;
		}
	}
	else if(type=='j')
	{
		while(tmpJoyStickList)
		{
			if(tmpJoyStickList->id==id)
			{
				tmpJoyStickList->x=0;
				tmpJoyStickList->y=0;
				while(*p!=',')
				{
					tmpJoyStickList->x=10*tmpJoyStickList->x+(*p-0x30);
					p++;
				}
				p++;
				while(*p!='\0')
				{
					tmpJoyStickList->y=10*tmpJoyStickList->y+(*p-0x30);
					p++;
				}
				tmpJoyStickList->isUpdated=1;
				return 0;
			}
			tmpJoyStickList=tmpJoyStickList->next;
		}
	}
	return 3;
}




























/**********************************************************************************************************************
 *@brief   ѧ������APP UI������λ���ײ����
 *@by      һƷ֥���
 *@notice  ��ѧ������UI�������ʹ��
 *				 ��MainWindow.c�д�������
 *@time    2024-03-15
 *********************************************************************************************************************/
#include "phone_ui.h"
#include "main_window.h"
#include "usart.h" //�˴�Ϊ���ڷ��ͺ���ͷ�ļ�
#include <math.h>
#include <string.h>
#include <stdlib.h>

uint8_t UI_Start[2] = {0x01,0x09};	//֡ͷ
uint8_t UI_End[2] = {0x08,0x07}; 	//֡β

uint16_t UI_ID=1;

TextView_TypeDef *TextViewList=NULL;
Button_TypeDef *ButtonList=NULL;
EditText_TypeDef *EditTextList=NULL;
Switch_TypeDef *SwitchList=NULL;
SeekBar_TypeDef *SeekBarList=NULL;
LineChart_TypeDef *LineChartList=NULL;
JoyStick_TypeDef *JoyStickList=NULL;

//���ڷ���һ���ֽ�
void UI_SendByte(unsigned char byte)
{
	Uart_SendByte(USART1,byte); //�޸Ĵ˴�Ϊ�����д��ڷ���һ���ֽں���
}
//���ڷ���һ������
void UI_SendArray(uint8_t *array,unsigned int num)
{
	while(num--)
	{
		UI_SendByte(*array);
		array++;
	}
}
//�����ֵ
int  my_abs(int dat)
{
    if(dat>=0)  return dat;
    else        return -dat;
}
//��ƽ��
uint32_t my_pow(uint8_t m,int8_t n)
{
	uint32_t result=1;	 
	while(n--)result*=m;    
	return result;
}
//���ڷ���һ������
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
 *�������ƣ�Window_Clear
 *��    �飺��մ������пؼ�
 *��    �룺��
 *��    ������
 *ע�������
 **************************************************************/
void Window_Clear()
{
	uint8_t i;
	for(i=0;i<20;i++)
	{
		UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β,������һ֡����
	}
	
	UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(1);                          				//��������
	UI_SendByte(UI_Window_Clear);                   //����
	UI_SendByte(0);                    					//���ݣ�����
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
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
 *�������ƣ�Window_SetBackground
 *��    �飺���ô��ڱ�����ɫ
 *��    �룺color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע��������ô�����ɫ�󣬱�ֽ��������ʾ
 **************************************************************/
void Window_SetBackground(int color)
{
	UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(sizeof(color));                          //��������
	UI_SendByte(UI_Window_Background);                            //����
	UI_SendArray((uint8_t*)&color,sizeof(color));        //��������
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�Window_SetSize
 *��    �飺���ô��ڿ��
 *��    �룺width:���ڿ�ȣ�heigh:���ڸ߶ȣ�
						fill:(0)���ô��ڱ�����ʵ����Ļ������ͬʱ����䣬�������ϸ�
								(1)��ͬҲ���������Ļ��û�кڱߣ�ǿ��֢����
 *��    ������
 *ע�������
 **************************************************************/
void Window_SetSize(uint16_t width,uint16_t height,char fill)
{
	UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(5);                          //��������
	UI_SendByte(UI_Window_Size);                            //����
	UI_SendArray((uint8_t*)&width,sizeof(width));        //��������
	UI_SendArray((uint8_t*)&height,sizeof(height));
	UI_SendArray((uint8_t*)&fill,sizeof(fill));
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�AddTextViewToList
 *��    �飺���һ���ı�����������
 *��    �룺TextView:TextView�ؼ�
 *��    ��������Ƿ�ɹ�
 *ע�������
 **************************************************************/
char AddTextViewToList(TextView_TypeDef *TextView)
{
	TextView_TypeDef *pTemp=TextViewList;
	//���ؼ��Ƿ��Ѿ������б�
	while(pTemp)
	{
		if(pTemp==TextView)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	TextView->id=UI_ID++;
	//���ؼ������б�
	TextView->next=TextViewList;
	TextViewList=TextView;
	return 0;
}

/**************************************************************
 *�������ƣ�TextView_Init
 *��    �飺���һ���ı���
 *��    �룺TextView:TextView�ؼ���x�����x���ꣻy�����y����
						width���ؼ���height���ؼ���
 *��    ������
 *ע�������
 **************************************************************/
void TextView_Init(TextView_TypeDef *TextView,short x,short y,short width,short height)
{
	if(AddTextViewToList(TextView))
	{
		return;
	}
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(10);     //��������
	UI_SendByte(UI_TextView_Init);     //��������
	UI_SendArray((uint8_t*)&TextView->id,2); //��������
	UI_SendArray((uint8_t*)&x,2); //��������
	UI_SendArray((uint8_t*)&y,2); //��������
	UI_SendArray((uint8_t*)&width,2); //��������
	UI_SendArray((uint8_t*)&height,2); //��������
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}

/**************************************************************
 *�������ƣ�TextView_SetStyle
 *��    �飺�����ı�����ʽ
 *��    �룺TextView:TextView�ؼ���Style:��ʽ
 *��    ������
 *ע�������
 **************************************************************/
void TextView_SetStyle(TextView_TypeDef TextView,TextViewStyle_TypeDef Style)
{
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(sizeof(Style)+2);                                      //��������
	UI_SendByte(UI_TextView_Style);     //��������
	UI_SendArray((uint8_t*)&TextView.id,2); //����id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //��������
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}

/**************************************************************
 *�������ƣ�TextView_SetText
 *��    �飺�ı��������ı���ԭ���Ļᱻ�����
 *��    �룺TextView:TextView�ؼ� *text:Ҫ��ʾ���ı�
 *��    ������
 *ע�����textע�������0xOO��β
 **************************************************************/
void TextView_SetText(TextView_TypeDef TextView,char *text)
{
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������ȷ��������Ϊ��
	UI_SendByte(UI_TextView_SetText);                          //����
	UI_SendArray((uint8_t*)&TextView.id,4);  //��������
  while(*text)
	{
		UI_SendByte(*text);
		text++;
	}	
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}

/**************************************************************
 *�������ƣ�TextView_AddText
 *��    �飺�ı�������ı���ԭ���Ĳ������
 *��    �룺TextView:TextView�ؼ� *text:Ҫ��ʾ���ı�
 *��    ������
 *ע�����textע�������0xOO��β
 **************************************************************/
void TextView_AddText(TextView_TypeDef TextView,char *text)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������,0��ʾ�޹̶�����
	UI_SendByte(UI_TextView_AddText);                          //����
	UI_SendArray((uint8_t*)&TextView.id,2);                //����id
    while(*text)
	{
		UI_SendByte(*text);
		text++;
	}	
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}

/**************************************************************
 *�������ƣ�TextView_SetInt
 *��    �飺�ı��������ʾ����
 *��    �룺TextView:TextView�ؼ� num:����
 *��    ������
 *ע�������
 **************************************************************/
void TextView_SetInt(TextView_TypeDef TextView,int num)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������
	UI_SendByte(UI_TextView_SetText);                          //����
	UI_SendArray((uint8_t*)&TextView.id,2);                //����id
    UI_SendInt(num);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}
/**************************************************************
 *�������ƣ�TextView_AddInt
 *��    �飺�ı��������ʾ������ԭ���Ĳ������
 *��    �룺TextView:TextView�ؼ� num:����
 *��    ������
 *ע�������
 **************************************************************/
void TextView_AddInt(TextView_TypeDef TextView,int num)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������
	UI_SendByte(UI_TextView_AddText);                          //����
	UI_SendArray((uint8_t*)&TextView.id,2);                //����id
    UI_SendInt(num);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}
/**************************************************************
 *�������ƣ�TextView_SetFloat
 *��    �飺�ı��������ʾ������
 *��    �룺TextView:TextView�ؼ� num:������ len:С�����λ��
 *��    ������
 *ע�������
 **************************************************************/
void TextView_SetFloat(TextView_TypeDef TextView,float num,uint8_t len)
{
	uint32_t num1;
	
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������
	UI_SendByte(UI_TextView_SetText);                          //����
	UI_SendArray((uint8_t*)&TextView.id,2);                //����id
	if((num<0)&&(num>-1))
	UI_SendByte('-');	//������
	UI_SendInt((int)num);
	UI_SendByte('.');
	if(num<0) num=-num;
    num1=((uint32_t)(num*pow(10,len)));
    num1=num1%my_pow(10,len);
	UI_SendInt(num1);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}
/**************************************************************
 *�������ƣ�TextView_AddFloat
 *��    �飺�ı��������ʾ��������ԭ���Ĳ������
 *��    �룺TextView:TextView�ؼ� num:������ len:С�����λ��
 *��    ������
 *ע�������
 **************************************************************/
void TextView_AddFloat(TextView_TypeDef TextView,float num,uint8_t len)
{
	uint32_t num1;
	
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������
	UI_SendByte(UI_TextView_AddText);                          //����
	UI_SendArray((uint8_t*)&TextView.id,2);                //����id
	if((num<0)&&(num>-1))
	UI_SendByte('-');	//������
	UI_SendInt((int)num);
	UI_SendByte('.');
	if(num<0) num=-num;
    num1=((uint32_t)(num*pow(10,len)));
    num1=num1%my_pow(10,len);
	UI_SendInt(num1);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}
/**************************************************************
 *�������ƣ�TextView_SetTextSize
 *��    �飺�����ı��������С
 *��    �룺TextView:TextView�ؼ� size:�����С
 *��    ������
 *ע�������
 **************************************************************/
void TextView_SetTextSize(TextView_TypeDef TextView,int size)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));  //����֡ͷ
	UI_SendByte(6);               //��������
	UI_SendByte(UI_TextView_TextSize);                   //����
	UI_SendArray((uint8_t*)&TextView.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&size,4);         //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}

/**************************************************************
 *�������ƣ�TextView_SetTextColor
 *��    �飺�����ı���������ɫ
 *��    �룺TextView:TextView�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void TextView_SetTextColor(TextView_TypeDef TextView,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);             //��������
	UI_SendByte(UI_TextView_TextColor);                 //����
	UI_SendArray((uint8_t*)&TextView.id,2);
	UI_SendArray((uint8_t*)&color,4);      //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}
/**************************************************************
 *�������ƣ�TextView_SetTextColor
 *��    �飺�����ı����ı�λ��
 *��    �룺TextView:TextView�ؼ� gravity:λ�ã���.h�ļ���
 *��    ������
 *ע�������
 **************************************************************/
void TextView_SetTextGravity(TextView_TypeDef TextView,int gravity)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);             //��������
	UI_SendByte(UI_TextView_Gravity);                 //����
	UI_SendArray((uint8_t*)&TextView.id,2);
	UI_SendArray((uint8_t*)&gravity,4);      //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}
/**************************************************************
 *�������ƣ�TextView_SetBackgroundColor
 *��    �飺�����ı��򱳾���ɫ
 *��    �룺TextView:TextView�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void TextView_SetBackgroundColor(TextView_TypeDef TextView,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_TextView_BackgroundColor);                  //����
	UI_SendArray((uint8_t*)&TextView.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�TextView_SetRotation
 *��    �飺�����ı��򱳾���ɫ
 *��    �룺TextView:TextView�ؼ� rotation:�Ƕ�
 *��    ������
 *ע�������
 **************************************************************/
void TextView_SetRotation(TextView_TypeDef TextView,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_TextView_Rotation);                  //����
	UI_SendArray((uint8_t*)&TextView.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&rotation,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�AddButtonToList
 *��    �飺���һ���ı�����������
 *��    �룺Button:Button�ؼ�
 *��    ��������Ƿ�ɹ�
 *ע�������
 **************************************************************/
char AddButtonToList(Button_TypeDef *Button)
{
	Button_TypeDef *pTemp=ButtonList;
	//���ؼ��Ƿ��Ѿ������б�
	while(pTemp)
	{
		if(pTemp==Button)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	Button->id=UI_ID++;
	//���ؼ������б�
	Button->next=ButtonList;
	ButtonList=Button;
	return 0;
}
/**************************************************************
 *�������ƣ�Button_Init
 *��    �飺���һ���ı���
 *��    �룺Button:Button�ؼ���x�����x���ꣻy�����y����
						width���ؼ���height���ؼ���
 *��    ������
 *ע�������
 **************************************************************/
void Button_Init(Button_TypeDef *Button,short x,short y,short width,short height)
{
	if(AddButtonToList(Button))
	{
		return;
	}
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	  //����֡ͷ
	UI_SendByte(10);    //��������
	UI_SendByte(UI_Button_Init);   //��������
	UI_SendArray((uint8_t*)&Button->id,2); //��������
	UI_SendArray((uint8_t*)&x,2); //��������
	UI_SendArray((uint8_t*)&y,2); //��������
	UI_SendArray((uint8_t*)&width,2); //��������
	UI_SendArray((uint8_t*)&height,2); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡ͷ
}

/**************************************************************
 *�������ƣ�Button_Init
 *��    �飺�����ı�����ʽ
 *��    �룺Button:Button�ؼ���Style���ؼ���ʽ
 *��    ������
 *ע�������
 **************************************************************/
void Button_SetStyle(Button_TypeDef Button,ButtonStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(sizeof(Style)+2);                                      //��������
	UI_SendByte(UI_Button_Style);     //��������
	UI_SendArray((uint8_t*)&Button.id,2); //����id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}

/**************************************************************
 *�������ƣ�Button_SetText
 *��    �飺���ð����ı�
 *��    �룺Button:Button�ؼ� *text:Ҫ��ʾ���ı�
 *��    ������
 *ע�����ʹ������ʱ��ע�������0x00��β
 **************************************************************/
void Button_SetText(Button_TypeDef Button,char *text)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������
	UI_SendByte(UI_Button_Text);                          //����
	UI_SendArray((uint8_t*)&Button.id,2);                //�ؼ�id
    while(*text)                                           //��������
	{
		UI_SendByte(*text);
		text++;
	}	
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}

/**************************************************************
 *�������ƣ�Button_SetColor
 *��    �飺���ð�ť��ɫ
 *��    �룺Button:Button�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void Button_SetColor(Button_TypeDef Button,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_Button_Color);                    //����
	UI_SendArray((uint8_t*)&Button.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}

/**************************************************************
 *�������ƣ�Button_SetTextSize
 *��    �飺���ð�ť�����С
 *��    �룺Button:Button�ؼ� size:�����С
 *��    ������
 *ע�������
 **************************************************************/
void Button_SetTextSize(Button_TypeDef Button,int size)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));  //����֡ͷ
	UI_SendByte(6);               //��������
	UI_SendByte(UI_Button_TextSize);                   //����
	UI_SendArray((uint8_t*)&Button.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&size,4);         //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}

/**************************************************************
 *�������ƣ�Button_SetTextColor
 *��    �飺���ð�ť������ɫ
 *��    �룺Button:Button�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void Button_SetTextColor(Button_TypeDef Button,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);             //��������
	UI_SendByte(UI_Button_TextColor);                 //����
	UI_SendArray((uint8_t*)&Button.id,2);
	UI_SendArray((uint8_t*)&color,4);      //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}
/**************************************************************
 *�������ƣ�Button_SetBackgroundColor
 *��    �飺���ð�ť������ɫ
 *��    �룺Button:Button�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע���������Button_Color����ɫ��������ɫ��Ч
 **************************************************************/
void Button_SetBackgroundColor(Button_TypeDef Button,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_Button_BackgroundColor);                    //����
	UI_SendArray((uint8_t*)&Button.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�Button_SetRotation
 *��    �飺�����ı���Ƕ�
 *��    �룺Button:Button�ؼ� rotation:�Ƕ�
 *��    ������
 *ע�������
 **************************************************************/
void Button_SetRotation(Button_TypeDef Button,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_Button_Rotation);                  //����
	UI_SendArray((uint8_t*)&Button.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&rotation,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}

/**************************************************************
 *�������ƣ�AddEditTextToList
 *��    �飺���һ���ı��������������
 *��    �룺EditText:EditText�ؼ�
 *��    ��������Ƿ�ɹ�
 *ע�������
 **************************************************************/
char AddEditTextToList(EditText_TypeDef *EditText)
{
	EditText_TypeDef *pTemp=EditTextList;
	//���ؼ��Ƿ��Ѿ������б�
	while(pTemp)
	{
		if(pTemp==EditText)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	EditText->id=UI_ID++;
	//���ؼ������б�
	EditText->next=EditTextList;
	EditTextList=EditText;
	return 0;
}
/**************************************************************
 *�������ƣ�EditText_Init
 *��    �飺���һ���ı���
 *��    �룺EditText:EditText�ؼ���x�����x���ꣻy�����y����
						width���ؼ���height���ؼ���
 *��    ������
 *ע�������
 **************************************************************/
void EditText_Init(EditText_TypeDef *EditText,short x,short y,short width,short height)
{
	if(AddEditTextToList(EditText))
	{
			return;
	}
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	                  //����֡ͷ
	UI_SendByte(10);                                  //��������
	UI_SendByte(UI_EditText_Init);                                              //��������
	UI_SendArray((uint8_t*)&EditText->id,2); //��������
	UI_SendArray((uint8_t*)&x,2); //��������
	UI_SendArray((uint8_t*)&y,2); //��������
	UI_SendArray((uint8_t*)&width,2); //��������
	UI_SendArray((uint8_t*)&height,2); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	                      //����֡ͷ
}

/**************************************************************
 *�������ƣ�EditText_SetStyle
 *��    �飺�����ı��������ʽ
 *��    �룺EditText:EditText�ؼ���Style����ʽ
 *��    ������
 *ע�������
 **************************************************************/
void EditText_SetStyle(EditText_TypeDef EditText,EditTextStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(sizeof(Style)+2);                                      //��������
	UI_SendByte(UI_EditText_Style);     //��������
	UI_SendArray((uint8_t*)&EditText.id,2); //����id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}

/**************************************************************
 *�������ƣ�EditText_SetText
 *��    �飺�����ı�������ı�
 *��    �룺EditText:EditText�ؼ� *text:Ҫ��ʾ���ı�
 *��    ������
 *ע�����ʹ������ʱ��ע�������0xOO��β
						���ú�EditText.textͬ������
 **************************************************************/
void EditText_SetText(EditText_TypeDef EditText,char *text)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������
	UI_SendByte(UI_EditText_SetText);                          //����
	UI_SendArray((uint8_t*)&EditText.id,2); //�ؼ�id
    while(*text)                                            //��������
	{
		UI_SendByte(*text);
		text++;
	}	
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}

/**************************************************************
 *�������ƣ�EditText_SetInt
 *��    �飺�ı��������ʾ����
 *��    �룺EditText:EditText�ؼ� num:����
 *��    ������
 *ע�������
 **************************************************************/
void EditText_SetInt(EditText_TypeDef EditText,int num)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);   //��������
	UI_SendByte(UI_EditText_SetText);                          //����
	UI_SendArray((uint8_t*)&EditText.id,2);  //����id
    UI_SendInt(num);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));  //����֡β
}

/**************************************************************
 *�������ƣ�EditText_SetFloat
 *��    �飺�ı��������ʾ������
 *��    �룺EditText:EditText�ؼ� num:����len:С�����λ��
 *��    ������
 *ע�������
 **************************************************************/
void EditText_SetFloat(EditText_TypeDef EditText,float num,uint8_t len)
{
	uint32_t num1;
	
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������
	UI_SendByte(UI_EditText_SetText);                          //����
	UI_SendArray((uint8_t*)&EditText.id,2);  //����id
	if((num<0)&&(num>-1))
	UI_SendByte('-');	//������
	UI_SendInt((int)num);
	UI_SendByte('.');
	if(num<0) num=-num;
    num1=((uint32_t)(num*pow(10,len)));
    num1=num1%my_pow(10,len);
	UI_SendInt(num1);
	UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));  //����֡β
}

/**************************************************************
 *�������ƣ�EditText_SetHint
 *��    �飺�ı�����������ʾ��
 *��    �룺EditText:EditText�ؼ� *text:Ҫ��ʾ����ʾ��ַ�����
 *��    ������
 *ע�����textע�������0xOO��β
 **************************************************************/
void EditText_SetHint(EditText_TypeDef EditText, char *text)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	//����֡ͷ
	UI_SendByte(0);                                         //��������
	UI_SendByte(UI_EditText_SetHint);                          //����
	UI_SendArray((uint8_t*)&EditText.id,2); //�ؼ�id
    while(*text)                                           //��������
	{
		UI_SendByte(*text);
		text++;
	}	
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	    //����֡β
}
/**************************************************************
 *�������ƣ�EditText_SetTextSize
 *��    �飺�����ı��������С
 *��    �룺EditText:EditText�ؼ� size:�����С
 *��    ������
 *ע�������
 **************************************************************/
void EditText_SetTextSize(EditText_TypeDef EditText,int size)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));  //����֡ͷ
	UI_SendByte(6);               //��������
	UI_SendByte(UI_EditText_TextSize);                   //����
	UI_SendArray((uint8_t*)&EditText.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&size,4);         //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}

/**************************************************************
 *�������ƣ�EditText_SetTextColor
 *��    �飺�����ı���������ɫ
 *��    �룺EditText:EditText�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void EditText_SetTextColor(EditText_TypeDef EditText,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);             //��������
	UI_SendByte(UI_EditText_TextColor);                 //����
	UI_SendArray((uint8_t*)&EditText.id,2);
	UI_SendArray((uint8_t*)&color,4);      //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}
/**************************************************************
 *�������ƣ�EditText_SetBackgroundColor
 *��    �飺���ð���������ɫ
 *��    �룺EditText:EditText�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void EditText_SetBackgroundColor(EditText_TypeDef EditText,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_EditText_BackgroundColor);                    //����
	UI_SendArray((uint8_t*)&EditText.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�EditText_SetRotation
 *��    �飺�����ı��򱳾���ɫ
 *��    �룺EditText:EditText�ؼ� rotation:�Ƕ�
 *��    ������
 *ע�������
 **************************************************************/
void EditText_SetRotation(EditText_TypeDef EditText,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_EditText_Rotation);                  //����
	UI_SendArray((uint8_t*)&EditText.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&rotation,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�AddSwitchToList
 *��    �飺���һ��������������
 *��    �룺Switch:Switch�ؼ�
 *��    ��������Ƿ�ɹ�
 *ע�������
 **************************************************************/
char AddSwitchToList(Switch_TypeDef *Switch)
{
	Switch_TypeDef *pTemp=SwitchList;
	//���ؼ��Ƿ��Ѿ������б�
	while(pTemp)
	{
		if(pTemp==Switch)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	Switch->id=UI_ID++;
	//���ؼ������б�
	Switch->next=SwitchList;
	SwitchList=Switch;
	return 0;
}
/**************************************************************
 *�������ƣ�Switch_Init
 *��    �飺���س�ʼ��
 *��    �룺Switch:TextView�ؼ���x�����x���ꣻy�����y����
						width���ؼ���height���ؼ���
 *��    ������
 *ע�������
 **************************************************************/
void Switch_Init(Switch_TypeDef *Switch,short x,short y,short width,short height)
{
	if(AddSwitchToList(Switch))
	{
		return;
	}

    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	 //����֡ͷ
	UI_SendByte(10);   //��������
	UI_SendByte(UI_Switch_Init);  //��������
	UI_SendArray((uint8_t*)&Switch->id,2); //����id
	UI_SendArray((uint8_t*)&x,2); //��������
	UI_SendArray((uint8_t*)&y,2); //��������
	UI_SendArray((uint8_t*)&width,2); //��������
	UI_SendArray((uint8_t*)&height,2); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡ͷ
}
/**************************************************************
 *�������ƣ�Switch_SetStyle
 *��    �飺�����ı�����ʽ
 *��    �룺Switch:Switch�ؼ�
 *��    ������
 *ע�������
 **************************************************************/
void Switch_SetStyle(Switch_TypeDef Switch,SwitchStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(sizeof(Style)+2);                                      //��������
	UI_SendByte(UI_Switch_Style);     //��������
	UI_SendArray((uint8_t*)&Switch.id,2); //����id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}
/**************************************************************
 *�������ƣ�Switch_SetTextColor
 *��    �飺�����ı���������ɫ
 *��    �룺Switch:Switch�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void Switch_SetChecked(Switch_TypeDef Switch,int checked)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);             //��������
	UI_SendByte(UI_Switch_Checked);                 //����
	UI_SendArray((uint8_t*)&Switch.id,2);
	UI_SendArray((uint8_t*)&checked,4);      //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}
/**************************************************************
 *�������ƣ�Switch_SetTextColor
 *��    �飺�����ı���������ɫ
 *��    �룺Switch:Switch�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void Switch_SetColor(Switch_TypeDef Switch,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);             //��������
	UI_SendByte(UI_Switch_Color);                 //����
	UI_SendArray((uint8_t*)&Switch.id,2);
	UI_SendArray((uint8_t*)&color,4);      //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}
/**************************************************************
 *�������ƣ�Switch_SetBackgroundColor
 *��    �飺���ÿ��ر�����ɫ
 *��    �룺Switch:Switch�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void Switch_SetBackgroundColor(Switch_TypeDef Switch,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_Switch_BackgroundColor);                    //����
	UI_SendArray((uint8_t*)&Switch.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�Switch_SetRotation
 *��    �飺�����ı��򱳾���ɫ
 *��    �룺Switch:Switch�ؼ� rotation:�Ƕ�
 *��    ������
 *ע�������
 **************************************************************/
void Switch_SetRotation(Switch_TypeDef Switch,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_Switch_Rotation);                  //����
	UI_SendArray((uint8_t*)&Switch.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&rotation,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}

/**************************************************************
 *�������ƣ�AddSeekBarToList
 *��    �飺���һ���϶�����������
 *��    �룺SeekBar:SeekBar�ؼ�
 *��    ��������Ƿ�ɹ�
 *ע�������
 **************************************************************/
char AddSeekBarToList(SeekBar_TypeDef *SeekBar)
{
	SeekBar_TypeDef *pTemp=SeekBarList;
	//���ؼ��Ƿ��Ѿ������б�
	while(pTemp)
	{
		if(pTemp==SeekBar)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	SeekBar->id=UI_ID++;
	//���ؼ������б�
	SeekBar->next=SeekBarList;
	SeekBarList=SeekBar;
	return 0;
}
/**************************************************************
 *�������ƣ�SeekBar_Init
 *��    �飺���س�ʼ��
 *��    �룺SeekBar:SeekBar�ؼ���x�����x���ꣻy�����y����
						width���ؼ���height���ؼ���
 *��    ������
 *ע���������ڸߺ��򣬿�С�ڸ�����
 **************************************************************/
void SeekBar_Init(SeekBar_TypeDef *SeekBar,short x,short y,short width,short height)
{
	if(AddSeekBarToList(SeekBar))
	{
		return;
	}
	
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	                  //����֡ͷ
	UI_SendByte(10);                                  //��������
	UI_SendByte(UI_SeekBar_Init);                                              //��������
	UI_SendArray((uint8_t*)&SeekBar->id,2); //����id
	UI_SendArray((uint8_t*)&x,2); //��������
	UI_SendArray((uint8_t*)&y,2); //��������
	UI_SendArray((uint8_t*)&width,2); //��������
	UI_SendArray((uint8_t*)&height,2); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	                      //����֡ͷ
}
/**************************************************************
 *�������ƣ�SeekBar_SetStyle
 *��    �飺�����϶�����ʽ
 *��    �룺SeekBar:SeekBar�ؼ�
 *��    ������
 *ע�������
 **************************************************************/
void SeekBar_SetStyle(SeekBar_TypeDef SeekBar,SeekBarStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(sizeof(Style)+2);                                      //��������
	UI_SendByte(UI_SeekBar_Style);     //��������
	UI_SendArray((uint8_t*)&SeekBar.id,2); //����id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}
/**************************************************************
 *�������ƣ�SeekBar_SetMax
 *��    �飺�����϶������ֵ
 *��    �룺SeekBar:SeekBar�ؼ� max:�϶������ֵ
 *��    ������
 *ע�������
 **************************************************************/
void SeekBar_SetMax(SeekBar_TypeDef SeekBar,int max)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));  //����֡ͷ
	UI_SendByte(6);  //��������
	UI_SendByte(UI_SeekBar_Max);                         //����
	UI_SendArray((uint8_t*)&SeekBar.id,2);	//�ؼ�id
	UI_SendArray((uint8_t*)&max,4);           //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}

/**************************************************************
 *�������ƣ�SeekBar_SetProcess
 *��    �飺�����϶�����ǰֵ
 *��    �룺SeekBar:SeekBar�ؼ� process:�����õ�ǰֵ
 *��    ������
 *ע�������
 **************************************************************/
void SeekBar_SetProgress(SeekBar_TypeDef SeekBar,int progress)
{
	UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start));   //����֡ͷ
	UI_SendByte(6);  //��������
	UI_SendByte(UI_SeekBar_Process);     //����
	UI_SendArray((uint8_t*)&SeekBar.id,2);	//�ؼ�id
	UI_SendArray((uint8_t*)&progress,sizeof(progress));    //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	  //����֡β
}

/**************************************************************
 *�������ƣ�SeekBar_SetColor
 *��    �飺�����϶�����ɫ
 *��    �룺SeekBar:SeekBar�ؼ� color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void SeekBar_SetColor(SeekBar_TypeDef SeekBar,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);             //��������
	UI_SendByte(UI_SeekBar_Color);                 //����
	UI_SendArray((uint8_t*)&SeekBar.id,2);
	UI_SendArray((uint8_t*)&color,4);      //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	//����֡β
}
/**************************************************************
 *�������ƣ�SeekBar_SetBackgroundColor
 *��    �飺�����϶���������ɫ
 *��    �룺SeekBar:SeekBar�ؼ���color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void SeekBar_SetBackgroundColor(SeekBar_TypeDef SeekBar,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_SeekBar_BackgroundColor);                    //����
	UI_SendArray((uint8_t*)&SeekBar.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�SeekBar_SetRotation
 *��    �飺�����϶�����ת�Ƕ�
 *��    �룺SeekBar:SeekBar�ؼ� rotation:�Ƕ�
 *��    ������
 *ע�������
 **************************************************************/
void SeekBar_SetRotation(SeekBar_TypeDef SeekBar,int rotation)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_SeekBar_Rotation);                  //����
	UI_SendArray((uint8_t*)&SeekBar.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&rotation,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}

/**************************************************************
 *�������ƣ�AddLineChartToList
 *��    �飺���һ������ͼ��������
 *��    �룺LineChart:LineChart�ؼ�
 *��    ��������Ƿ�ɹ�
 *ע�������
 **************************************************************/
char AddLineChartToList(LineChart_TypeDef *LineChart)
{
	LineChart_TypeDef *pTemp=LineChartList;
	//���ؼ��Ƿ��Ѿ������б�
	while(pTemp)
	{
		if(pTemp==LineChart)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	LineChart->id=UI_ID++;
	//���ؼ������б�
	LineChart->next=LineChartList;
	LineChartList=LineChart;
	return 0;
}
/**************************************************************
 *�������ƣ�LineChart_Init
 *��    �飺����ͼ��ʼ��
 *��    �룺LineChart:TextView�ؼ���x�����x���ꣻy�����y����
						width���ؼ���height���ؼ���
 *��    ������
 *ע����������ʾ1000������
 **************************************************************/
void LineChart_Init(LineChart_TypeDef *LineChart,short x,short y,short width,short height)
{
	if(AddLineChartToList(LineChart))
	{
		return;
	}
	
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(10);  //��������
	UI_SendByte(UI_LineChart_Init);  //��������
	UI_SendArray((uint8_t*)&LineChart->id,2); //����id
	UI_SendArray((uint8_t*)&x,2); //��������
	UI_SendArray((uint8_t*)&y,2); //��������
	UI_SendArray((uint8_t*)&width,2); //��������
	UI_SendArray((uint8_t*)&height,2); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡ͷ
}

/**************************************************************
 *�������ƣ�LineChart_SetStyle
 *��    �飺�����ı�����ʽ
 *��    �룺LineChart:LineChart�ؼ�
 *��    ������
 *ע�������
 **************************************************************/
void LineChart_SetStyle(LineChart_TypeDef LineChart,LineChartStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(sizeof(Style)+2);                                      //��������
	UI_SendByte(UI_LineChart_Style);     //��������
	UI_SendArray((uint8_t*)&LineChart.id,2); //����id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}



/**************************************************************
 *�������ƣ�LineChart_AddData
 *��    �飺������ͼ�����һ������
 *��    �룺LineChart:LineChart�ؼ���data������
 *��    ������
 *ע�������
 **************************************************************/
void LineChart_AddData(LineChart_TypeDef LineChart,double data)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(10);  //��������
	UI_SendByte(UI_LineChart_AddData);  //��������
	UI_SendArray((uint8_t*)&LineChart.id,2);	//�ؼ�id
	UI_SendArray((uint8_t*)&data,8); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡ͷ
}
/**************************************************************
 *�������ƣ�LineChart_Clear
 *��    �飺�������ͼ����
 *��    �룺LineChart:LineChart�ؼ�
 *��    ������
 *ע�������
 **************************************************************/
void LineChart_Clear(LineChart_TypeDef LineChart)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(2);     //��������
	UI_SendByte(UI_LineChart_Clear);      //����
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //�ؼ�id
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�LineChart_SetLineXAxis
 *��    �飺����X����ʾ��������
 *��    �룺LineChart:LineChart�ؼ���x_max��X����ʾ����
 *��    ������
 *ע�������
 **************************************************************/
void LineChart_SetXAxis(LineChart_TypeDef LineChart,int x_max)
{
	UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_LineChart_XAxis);                  //����
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&x_max,4);        //��������
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�LineChart_SetLineYAxis
 *��    �飺����Y����ʾ��������
 *��    �룺LineChart:LineChart�ؼ���
						y_min��Y����Сֵ��y_max��Y�����ֵ��
 *��    ������
 *ע�������
 **************************************************************/
void LineChart_SetYAxis(LineChart_TypeDef LineChart,float y_min,float y_max)
{
		UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(10);    //��������
	UI_SendByte(UI_LineChart_YAxis);            //����
	UI_SendArray((uint8_t*)&LineChart.id,2);	//�ؼ�id
	UI_SendArray((uint8_t*)&y_min,4);        //��������
	UI_SendArray((uint8_t*)&y_max,4);        //��������
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}

/**************************************************************
 *�������ƣ�LineChart_SetLineColor
 *��    �飺��������ͼ������ɫ
 *��    �룺LineChart:LineChart�ؼ���color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void LineChart_SetLineColor(LineChart_TypeDef LineChart,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_LineChart_LineColor);                  //����
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�LineChart_SetChartColor
 *��    �飺��������ͼ��������ɫ
 *��    �룺LineChart:LineChart�ؼ���color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void LineChart_SetChartColor(LineChart_TypeDef LineChart,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_LineChart_ChartColor);                  //����
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}
/**************************************************************
 *�������ƣ�LineChart_SetBackgroundColor
 *��    �飺��������ͼ������ɫ
 *��    �룺LineChart:LineChart�ؼ���color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void LineChart_SetBackgroundColor(LineChart_TypeDef LineChart,int color)
{
    UI_SendArray((uint8_t*)UI_Start,sizeof(UI_Start));    //����֡ͷ	
	UI_SendByte(6);               //��������
	UI_SendByte(UI_LineChart_BackgroundColor);                  //����
	UI_SendArray((uint8_t*)&LineChart.id,2);				  //�ؼ�id
	UI_SendArray((uint8_t*)&color,4);        //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End));      //����֡β
}

/**************************************************************
 *�������ƣ�AddJoyStickToList
 *��    �飺���һ��ҡ����������
 *��    �룺JoyStick:JoyStick�ؼ�
 *��    ��������Ƿ�ɹ�
 *ע�������
 **************************************************************/
char AddJoyStickToList(JoyStick_TypeDef *JoyStick)
{
	JoyStick_TypeDef *pTemp=JoyStickList;
	//���ؼ��Ƿ��Ѿ������б�
	while(pTemp)
	{
		if(pTemp==JoyStick)
		{
			return 1;
		}
		pTemp=pTemp->next;
	}
	JoyStick->id=UI_ID++;
	//���ؼ������б�
	JoyStick->next=JoyStickList;
	JoyStickList=JoyStick;
	return 0;
}
/**************************************************************
 *�������ƣ�JoyStick_Init
 *��    �飺ҡ��ʼ��
 *��    �룺JoyStick:JoyStick�ؼ���x�����x���ꣻy�����y����
						width���ؼ���height���ؼ���
 *��    ������
 *ע�������
 **************************************************************/
void JoyStick_Init(JoyStick_TypeDef *JoyStick,short x,short y,short width,short height)
{
	if(AddJoyStickToList(JoyStick))
	{
		return;
	}
	JoyStick->x=100;
	JoyStick->y=100;
  UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); 	                  //����֡ͷ
	UI_SendByte(10);                                  //��������
	UI_SendByte(UI_JoyStick_Init);                                              //��������
	UI_SendArray((uint8_t *)&JoyStick->id,2); //����id
	UI_SendArray((uint8_t*)&x,2); //��������
	UI_SendArray((uint8_t*)&y,2); //��������
	UI_SendArray((uint8_t*)&width,2); //��������
	UI_SendArray((uint8_t*)&height,2); //��������
  UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); 	                      //����֡ͷ
}

/**************************************************************
 *�������ƣ�JoyStick_SetStyle
 *��    �飺����ҡ����ʽ
 *��    �룺JoyStick:JoyStick�ؼ���Style��ҡ����ʽ
 *��    ������
 *ע�������
 **************************************************************/
void JoyStick_SetStyle(JoyStick_TypeDef JoyStick,JoyStickStyle_TypeDef Style)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(sizeof(Style)+2);                                      //��������
	UI_SendByte(UI_JoyStick_Style);     //��������
	UI_SendArray((uint8_t*)&JoyStick.id,2); //����id
	UI_SendArray((uint8_t*)&Style,sizeof(Style)); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}
/**************************************************************
 *�������ƣ�JoyStick_SetColor
 *��    �飺����ҡ����ɫ
 *��    �룺JoyStick:JoyStick�ؼ���color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void JoyStick_SetColor(JoyStick_TypeDef JoyStick,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);                                      //��������
	UI_SendByte(UI_JoyStick_Color);     //��������
	UI_SendArray((uint8_t*)&JoyStick.id,2); //����id
	UI_SendArray((uint8_t*)&color,4); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}
/**************************************************************
 *�������ƣ�JoyStick_SetStyle
 *��    �飺����ҡ����ʽ
 *��    �룺JoyStick:JoyStick�ؼ���shape��(0)Բ�Σ�(1)������
 *��    ������
 *ע�������
 **************************************************************/
void JoyStick_SetShape(JoyStick_TypeDef JoyStick,int shape)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);                                      //��������
	UI_SendByte(UI_JoyStick_Shape);     //��������
	UI_SendArray((uint8_t*)&JoyStick.id,2); //����id
	UI_SendArray((uint8_t*)&shape,4); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}
/**************************************************************
 *�������ƣ�JoyStick_SetStyle
 *��    �飺����ҡ�˱�����ɫ
 *��    �룺JoyStick:JoyStick�ؼ���color:��ɫֵ,��:0xFF000000
 *��    ������
 *ע�������
 **************************************************************/
void JoyStick_SetBackgroundColor(JoyStick_TypeDef JoyStick,int color)
{
    UI_SendArray((uint8_t*)UI_Start, sizeof(UI_Start)); //����֡ͷ
	UI_SendByte(6);                                      //��������
	UI_SendByte(UI_JoyStick_BackgroundColor);     //��������
	UI_SendArray((uint8_t*)&JoyStick.id,2); //����id
	UI_SendArray((uint8_t*)&color,4); //��������
    UI_SendArray((uint8_t*)UI_End, sizeof(UI_End)); //����֡β
}

uint16_t UI_RX_STA=0;       //����״̬���	
char UI_RX_BUF[UI_RX_LEN];     //���ջ���,���UI_RX_LEN���ֽ�.

#if REAL_TIME_PROCESSING
void UI_ByteDeal(uint8_t res)
{
	if((UI_RX_STA&0x8000)==0)//����δ���
	{
		if(res==0x0A)
		{
			if(UI_RX_STA>0)
			{
				UI_RX_STA|=0x8000;
				UI_RX_BUF[(UI_RX_STA&0x3FFF)-1]='\0'; //��0x0D����0
				UI_CommandProcessor((char *)UI_RX_BUF); //���ճɹ���ֱ�ӽ���
			  UI_RX_STA=0;
			}
		}
		else
		{
			UI_RX_BUF[UI_RX_STA]=res;
			UI_RX_STA++;
			if(UI_RX_STA>=UI_RX_LEN)
			{
				UI_RX_STA=0;//�������ݴ���,���¿�ʼ����	 
			} 
		}			 
	}   	
}

#else

uint16_t rxStart=0,rxEnd=0;
uint16_t UI_BUF_STA=0;       //����״̬���	
char UI_BUF[UI_BUF_LEN];     //���ջ���,���UI_BUF_LEN���ֽ�.
//����һ���ֽ�
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
//���������ݴ���
//������һ֡�󣬲��ڴ����ж��д���ʱ��
//�����������������
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
				 UI_RX_BUF[UI_RX_STA-1]='\0'; //��0x0D����0
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
				UI_RX_STA=0;//�������ݴ���,���¿�ʼ����	 
			} 
		}
	}
	num=end-start; //�������ݵ�����
	end%=UI_BUF_LEN;
	rxStart=end;
	return num;
}


#endif

/**************************************************************
 *�������ƣ�BytesDeal
 *��    �飺������յ���һ֡�ַ���,����ֵ���ؼ�
 *��    �룺p:�ַ����׵�ַ������ָ�ؼ�������һ֡����
 *��    ����==0�����ɹ�;!=0����ʧ��
 *ע�����p����������'\0'��β���������ʧ��
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
	//��֤������ȷ��,�Լ��ؼ�����
	if(*p>'A'&&*p<'z')
	{
		type=*p;
	}
	p++;
	//�ؼ�id
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



























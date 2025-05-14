#ifndef __MAIN_WINDOW_H
#define __MAIN_WINDOW_H

#include "phone_ui.h"

extern TextView_TypeDef main_tv,main_rx_tv;

extern Button_TypeDef main_bt;

extern	EditText_TypeDef main_et;
	
extern	Switch_TypeDef main_sw;

extern	SeekBar_TypeDef main_sb,main_sb2;

extern LineChart_TypeDef main_lc,main_lc2;

extern JoyStick_TypeDef main_js,main_js2;


static const TextViewStyle_TypeDef TextViewStyle={
	.textSize=60,
	.textColor=UI_BLACK,
	.gravity=Gravity_CENTER,
	//.backgroundColor=0XFFFFFFFF,
	.rotation=0,
};

static const ButtonStyle_TypeDef ButtonStyle={
	.color=0XFF999999,
	.textSize=60,
	.textColor=UI_GREEN,
	.gravity=Gravity_CENTER,
	.rotation=0,
	//.backgroundColor=UI_GREEN,
};


static const EditTextStyle_TypeDef EditTextStyle={
	.textSize=50,
	.textColor=UI_GREEN,
	.gravity=Gravity_CENTER_VERTICAL,
//	.backgroundColor=UI_BLUE,
//	.rotation=45,
};
	
static const SwitchStyle_TypeDef SwitchStyle={
	.color=0xFFFF0000,
	.rotation=0,
//	.backgroundColor=UI_BLUE,
};

//拖动条样式
static const SeekBarStyle_TypeDef SeekBarStyle={ 	
	.max=200,
	.color=0xFF6480FF,
	.rotation=0,
//	.backgroundColor=	UI_GREEN,
};

//折线图样式
static const LineChartStyle_TypeDef LineChartStyle={ 	
	
	.xMax=50,
	.yMin=-100,
	.yMax=300,
	.lineColor=UI_BLUE,
	.chartColor=UI_GRAY,
};

static const JoyStickStyle_TypeDef JoyStickStyle={
	
	.color=UI_DEFAULT_COLOR,
	.shape=1,		
//	.backgroundColor=UI_GREEN
	
};

void ShowMianMeun(void);
void ShowMianMeun2(void);
#endif








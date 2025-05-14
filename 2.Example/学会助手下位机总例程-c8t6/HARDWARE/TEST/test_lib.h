#ifndef __TEST_LIB_H
#define __TEST_LIB_H


#include "stm32f10x.h"


void Uart_SendByte(USART_TypeDef* UARTx,u8 byte);
void Uart_SendArray(USART_TypeDef *UARTx,uint8_t *array,uint32_t num);
void Uart_SendString(USART_TypeDef *UARTx,uint8_t *s);


void XHZS_SendImage(uint8_t *imgaddr, uint32_t imgsize);
void GetParameter(uint8_t *p);
void SendWave(void);
void SendLocation(float longitude,float latitude);
void SendXY(int x,int y);


#endif

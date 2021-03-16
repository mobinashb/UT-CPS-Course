#include <Arduino.h>
#include <LiquidCrystal.h>
#include <AltSoftSerial.h>

#define BLUETOOTH_TEMPERATURE_START_CHAR '#'
#define BLUETOOTH_HUMIDITY_START_CHAR '*'
#define LUX_BOARD_BAUD_RATE 9600
#define TH_BOARD_BAUD_RATE 9600
enum BluetoothState {Waiting = 0, GetTemperature = 1, GetHumidity = 2};
// initialize the library by associating any needed LCD interface pin
// with the arduino pin number it is connected to
const int rs = 12, en = 11, d4 = 5, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);
AltSoftSerial luxAltSoftSerial;
// AltSoftSerial always uses these pins:
//
// Board          Transmit  Receive   PWM Unusable
// -----          --------  -------   ------------
// Arduino Uno        9         8         10
// Arduino Leonardo   5        13       (none)
// Arduino Mega      46        48       44, 45

void handleSerialBluetooth();
void handleLux();
void printLCD();
void runLogic();

float temperature, humidity, lux;
int bluetoothState = 0;
bool changeInSensors = false;
int wateringAmount = 0;
enum WateringUnit {Null = 0, CC = 1, Drop = 2};
int wateringUnit = Null;

void setup() {
  lcd.begin(20, 4);            // set up the LCD's number of columns and rows:
  Serial.begin(TH_BOARD_BAUD_RATE);            // start serial communication at 9600bps for bluetooth
  luxAltSoftSerial.begin(LUX_BOARD_BAUD_RATE); // start serial communication at 9600bps for lux sensor board
}

void loop() {
  // put your main code here, to run repeatedly:
  // reply only when you receive data:
  if (Serial.available() >= 5) {  // First Char + 4 Bytes Float = 5 Bytes
    handleSerialBluetooth();
  }
  if(luxAltSoftSerial.available() >= 4){  // 4 Bytes Float
    Serial.println("s");
    handleLux();
  }
  if(changeInSensors){
    runLogic();
    printLCD();
    changeInSensors = false;
  }
}

void runLogic(){
  if(humidity > 80){
    wateringAmount = 0;
    wateringUnit = Null;
  }else if(humidity < 50){
    wateringAmount = 15;
    wateringUnit = CC;
  }else{ // 50 <= humidity <= 80
    wateringUnit = Drop;
    if(temperature < 25 && lux < 600){
      wateringAmount = 10;
    }
    if(temperature < 25 && lux > 600){
      wateringAmount = 5;
    }
    if(temperature >= 25){
      wateringAmount = 10;
    }
  }
}

void handleSerialBluetooth(){
  if(bluetoothState == Waiting){
    // read the incoming byte:
    char incomingByteChar = Serial.read();
    if(incomingByteChar == BLUETOOTH_TEMPERATURE_START_CHAR)
      bluetoothState = GetTemperature;
    else if(incomingByteChar == BLUETOOTH_HUMIDITY_START_CHAR)
      bluetoothState = GetHumidity;
    else
      Serial.read(); // Data is out of order
  }
  if(bluetoothState == GetTemperature){
    float oldTemperature = temperature;
    temperature = Serial.parseFloat();
    bluetoothState = Waiting;
    if(temperature != oldTemperature)
      changeInSensors = true;
  }
  if(bluetoothState == GetHumidity){
    float oldHumidity = humidity;
    humidity = Serial.parseFloat();
    bluetoothState = Waiting;
    if(humidity != oldHumidity)
      changeInSensors = true;
  }
}

void handleLux(){
  float oldLux = lux;
  lux = luxAltSoftSerial.parseFloat();
  Serial.println(lux);
  if(lux != oldLux)
    changeInSensors = true;
}
/*
  The circuit:
 * LCD RS pin to digital pin 12
 * LCD Enable pin to digital pin 11
 * LCD D4 pin to digital pin 5
 * LCD D5 pin to digital pin 4
 * LCD D6 pin to digital pin 3
 * LCD D7 pin to digital pin 2
 * LCD R/W pin to ground
 * LCD VSS pin to ground
 * LCD VCC pin to 5V
 * 10K resistor:
 * ends to +5V and ground
 * wiper to LCD VO pin (pin 3)
 *
 * */
void printLCD(){
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.println(String(temperature).c_str());
  lcd.println(String(humidity).c_str());
  lcd.setCursor(0, 1);
  lcd.println(String(lux).c_str());
  lcd.setCursor(0, 2);
  if(wateringUnit == Null){
    lcd.println("No Watering Yet");
  }else{
    lcd.println(String(wateringAmount));
    if(wateringUnit == CC)
      lcd.println("CC");
    if(wateringUnit == Drop)
      lcd.println("Drop");
  }
}
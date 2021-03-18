#include <Arduino.h>
#include <LiquidCrystal.h>

#define TEMPERATURE_DELIMITER '*'
#define HUMIDITY_DELIMITER '$'

const int rs = 12, en = 11, d4 = 5, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal LCD(rs, en, d4, d5, d6, d7);

float humidity, temperature;
int wateringRate;
bool readNewData;

void setWateringRate();
void readSerial();
void showOnLCD();

void setup()
{
  readNewData = false;
  wateringRate = 0;
  LCD.begin(20, 4);
  Serial.begin(9600);
}

void loop()
{
  if (Serial.available() > 4) {
    readSerial();
  }

  if (readNewData) {
    setWateringRate();
    showOnLCD();
    readNewData = !readNewData;
  }
}

void setWateringRate()
{
  if (humidity > 50) {
    wateringRate = 0;
  } else if (humidity < 20) {
    wateringRate = 20;
  } else {
    if (temperature < 25) {
      wateringRate = 0;
    }
    if (temperature >= 25) {
      wateringRate = 10;
    }
  }
}

void readSerial()
{
  char curr = Serial.read();
  if (curr == TEMPERATURE_DELIMITER) {
    float temp = temperature;
    temperature = Serial.parseFloat();
    if (temperature != temp)
      readNewData = true;
  }
  else if (curr == HUMIDITY_DELIMITER) {
    float temp = humidity;
    humidity = Serial.parseFloat();
    if (humidity != temp)
      readNewData = true;
  }
  else
    Serial.read();
}

void showOnLCD()
{
  LCD.clear();
  LCD.setCursor(0, 0);
  LCD.print("Temperature: ");
  LCD.println(String(temperature).c_str());
  LCD.print("Humidity: ");
  LCD.println(String(humidity).c_str());
  LCD.setCursor(0, 1);
  if (wateringRate == 0) {
    LCD.println("No need to water.");
  } else {
    LCD.print("Watering rate: ");
    LCD.print(String(wateringRate) + " ");
    LCD.println("CC");
  }
}
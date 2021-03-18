#include <Arduino.h>
#include <LiquidCrystal.h>

#define TEMPERATURE_DELIMITER '*'
#define HUMIDITY_DELIMITER '$'

#define DC_MOTOR_PIN1 12
#define DC_MOTOR_PIN2 11

#define MOTOR_MAX_SPEED 255
int motorSpeed;
int cnt;

const int rs = 7, en = 6, d4 = 5, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal LCD(rs, en, d4, d5, d6, d7);

float humidity, temperature;
int wateringRate;
bool readNewData;

void setPWM();
void setWateringRate();
void readSerial();
void showOnLCD();

void setup()
{
  motorSpeed = 0;
  cnt = 0;
  pinMode (DC_MOTOR_PIN1, OUTPUT);
  pinMode (DC_MOTOR_PIN2, OUTPUT);
  readNewData = false;
  wateringRate = 0;
  LCD.begin(20, 4);
  Serial.begin(9600);
}

void loop()
{
  cnt = (cnt + 1) % MOTOR_MAX_SPEED;

  if (cnt < motorSpeed) {
    digitalWrite(DC_MOTOR_PIN1, HIGH);
    digitalWrite(DC_MOTOR_PIN2, LOW);
  } else {
    digitalWrite(DC_MOTOR_PIN1, LOW);
    digitalWrite(DC_MOTOR_PIN2, LOW);
  }

  if (Serial.available() > 4) {
    readSerial();
  }

  if (readNewData) {
    setWateringRate();
    showOnLCD();
    setPWM();
    readNewData = !readNewData;
  }
}

void setPWM()
{
  if (wateringRate == 20)
    motorSpeed = MOTOR_MAX_SPEED / 4;

  if (wateringRate == 10)
    motorSpeed = MOTOR_MAX_SPEED / 10;

  else
    motorSpeed =  0;
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
  LCD.setCursor(0, 1);
  LCD.print("Humidity: ");
  LCD.println(String(humidity).c_str());
  LCD.setCursor(0, 2);
  if (wateringRate == 0) {
    LCD.println("No need to water.");
  } else {
    LCD.print("Watering rate: ");
    LCD.print(String(wateringRate) + " ");
    LCD.println("CC");
  }
}
#include <Arduino.h>
#include <Wire.h>

#define TEMPERATURE_DELIMITER '*'
#define HUMIDITY_DELIMITER '$'
#define Addr 0x40

float temperature, humidity;

void getTemperatureFromSensor();
void getHumidityFromSensor();

void setup()
{
	Wire.begin();
	Serial.begin(9600);
}

void loop()
{
	getTemperatureFromSensor();
	getHumidityFromSensor();
	Serial.print(TEMPERATURE_DELIMITER);
	Serial.println(temperature);
	Serial.print(HUMIDITY_DELIMITER);
	Serial.println(humidity);
}

void getTemperatureFromSensor()
{
	unsigned int data[2];
	Wire.beginTransmission(Addr);
	Wire.write(0xF3);
	Wire.endTransmission();
	delay(500);
	Wire.requestFrom(Addr, 2);
	if (Wire.available() == 2) {
    data[0] = Wire.read();
		data[1] = Wire.read();
		temperature = (((data[0] * 256.0 + data[1]) * 175.72) / 65536.0) - 46.85;
	}
}

void getHumidityFromSensor()
{
	unsigned int data[2];
	Wire.beginTransmission(Addr);
	Wire.write(0xF5);
	Wire.endTransmission();
	delay(500);
	Wire.requestFrom(Addr, 2);
	if (Wire.available() == 2) {
		data[0] = Wire.read();
		data[1] = Wire.read();
		humidity = (((data[0] * 256.0 + data[1]) * 125.0) / 65536.0) - 6;
	}
}
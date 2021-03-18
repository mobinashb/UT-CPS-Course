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
}

void loop()
{
	getTemperatureFromSensor();
	getHumidityFromSensor();
}

void getTemperatureFromSensor()
{
	Wire.beginTransmission(Addr);
	Wire.write(0xF3);
	Wire.endTransmission();
	delay(500);
	Wire.requestFrom(Addr, 2);
	if (Wire.available() == 2) {
    unsigned int lsb = Wire.read();
		unsigned int msb = Wire.read();
		temperature = (((lsb * 256.0 + msb) * 175.72) / 65536.0) - 46.85;
	}
}

void getHumidityFromSensor()
{
	Wire.beginTransmission(Addr);
	Wire.write(0xF5);
	Wire.endTransmission();
	delay(500);
	Wire.requestFrom(Addr, 2);
	if (Wire.available() == 2) {
		unsigned int lsb = Wire.read();
		unsigned int msb = Wire.read();
		humidity = (((lsb * 256.0 + msb) * 125.0) / 65536.0) - 6;
	}
}
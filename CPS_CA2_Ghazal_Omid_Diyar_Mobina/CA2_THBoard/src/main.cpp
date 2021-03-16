#include <Arduino.h>
#include <Wire.h>

#define TEMPERATURE_DELIMITER '*'
#define HUMIDITY_DELIMITER '$'
#define I2CAddress 0x40

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
	Wire.beginTransmission(I2CAddress);
	Wire.write(0xF3);
	Wire.endTransmission();
	delay(500);
	Wire.requestFrom(I2CAddress, 2);
	if (Wire.available() == 2) {
    unsigned int lsb = Wire.read();
		unsigned int msb = Wire.read();
		temperature = (((lsb * 256.0 + msb) * 175.72) / 65536.0) - 46.85;
	}
}

void getHumidityFromSensor()
{
	Wire.beginTransmission(I2CAddress);
	Wire.write(0xF5);
	Wire.endTransmission();
	delay(500);
	Wire.requestFrom(I2CAddress, 2);
	if (Wire.available() == 2) {
		unsigned int lsb = Wire.read();
		unsigned int msb = Wire.read();
		humidity = (((lsb * 256.0 + msb) * 125.0) / 65536.0) - 6;
	}
}
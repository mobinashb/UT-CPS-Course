#include <Stepper.h>

#define degree 10
const int stepsNum = 360/degree;
#define STEPPER_MOTOR_PIN1 6
#define STEPPER_MOTOR_PIN2 5
#define STEPPER_MOTOR_PIN3 4
#define STEPPER_MOTOR_PIN4 3
#define CLOCKWISE_BUTTON 7
#define COUNTER_CLOCKWISE_BUTTON 8
#define STOP_BUTTON 9
bool stopStepping;
int motorDirection;

Stepper groupStepper(stepsNum, STEPPER_MOTOR_PIN1, 
STEPPER_MOTOR_PIN2, STEPPER_MOTOR_PIN3, STEPPER_MOTOR_PIN4);

void setup() {
  pinMode (STEPPER_MOTOR_PIN1, OUTPUT);
  pinMode (STEPPER_MOTOR_PIN2, OUTPUT);
  pinMode (STEPPER_MOTOR_PIN3, OUTPUT);
  pinMode (STEPPER_MOTOR_PIN4, OUTPUT);

  pinMode (CLOCKWISE_BUTTON, INPUT_PULLUP);
  pinMode (COUNTER_CLOCKWISE_BUTTON, INPUT_PULLUP);
  pinMode (STOP_BUTTON, INPUT_PULLUP);
  
  groupStepper.setSpeed(10);
  Serial.begin(9600);

  stopStepping = false;
  motorDirection = 1;
}

void loop() {
  if (!stopStepping) {
    groupStepper.step(motorDirection);
  }
  
  if (digitalRead(CLOCKWISE_BUTTON) == LOW) {
    Serial.println("Clock");
    while (digitalRead(CLOCKWISE_BUTTON) == LOW);
    stopStepping = false;
    motorDirection = 1;    
  }
  
  if (digitalRead(COUNTER_CLOCKWISE_BUTTON) == LOW) {
    Serial.println("Counter Clock");
    while (digitalRead(COUNTER_CLOCKWISE_BUTTON) == LOW);
    stopStepping = false;
    motorDirection = -1;    
  }
  
  if (digitalRead(STOP_BUTTON) == LOW) {
    Serial.println("Stop");
    while (digitalRead(STOP_BUTTON) == LOW);
    stopStepping = true;    
  }
}

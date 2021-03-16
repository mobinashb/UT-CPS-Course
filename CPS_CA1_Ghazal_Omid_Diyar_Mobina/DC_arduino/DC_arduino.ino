#define DC_MOTOR_PIN1 12
#define DC_MOTOR_PIN2 11
#define ON_OFF_BUTTON 10
#define INC_SPEED_BUTTON 9
#define DEC_SPEED_BUTTON 8
#define REVERSE_BUTTON 7
#define MAX_SPEED 255
#define MIN_SPEED 0
int motorSpeed;
int cnt;
bool motorOn;
bool clockwise;

void setup() {
  motorSpeed = MAX_SPEED;
  motorOn = true;
  clockwise = true;
  cnt = 0;
  pinMode (DC_MOTOR_PIN1, OUTPUT);
  pinMode (DC_MOTOR_PIN2, OUTPUT);
  pinMode (ON_OFF_BUTTON, INPUT_PULLUP);
  pinMode (INC_SPEED_BUTTON, INPUT_PULLUP);
  pinMode (DEC_SPEED_BUTTON, INPUT_PULLUP);
  pinMode (REVERSE_BUTTON, INPUT_PULLUP);
  Serial.begin(9600);
  Serial.println("DC Motor simulation");
}

void loop() {
  cnt = (cnt + 1) % MAX_SPEED;
  
  if (digitalRead(ON_OFF_BUTTON) == LOW) {
    Serial.println("on off");
    while (digitalRead(ON_OFF_BUTTON) == LOW);
    motorOn = !motorOn;
    Serial.println(motorOn);
  }

   if (digitalRead(INC_SPEED_BUTTON) == LOW) {
    Serial.println("inc speed");
    while (digitalRead(INC_SPEED_BUTTON) == LOW);
    motorSpeed = (motorSpeed < MAX_SPEED - 10) ? 
    (motorSpeed + 10) : MAX_SPEED;
    Serial.println(motorSpeed);
  }
  
  if (digitalRead(DEC_SPEED_BUTTON) == LOW) {
    Serial.println("dec speed");
    while (digitalRead(DEC_SPEED_BUTTON) == LOW);
    motorSpeed = (motorSpeed > MIN_SPEED + 10) ? 
    (motorSpeed - 10) : MIN_SPEED;
    Serial.println(motorSpeed);
  }
  
  if (digitalRead(REVERSE_BUTTON) == LOW) {
    Serial.println("reverse");
    while (digitalRead(REVERSE_BUTTON) == LOW);
    clockwise = !clockwise;
    Serial.println(clockwise);
  }

  if (motorOn && (cnt < motorSpeed)) {
    if (clockwise){
      digitalWrite(DC_MOTOR_PIN1, HIGH);
      digitalWrite(DC_MOTOR_PIN2, LOW);
    } else {
      digitalWrite(DC_MOTOR_PIN1, LOW);
      digitalWrite(DC_MOTOR_PIN2, HIGH);
    }
  } else {
    digitalWrite(DC_MOTOR_PIN1, LOW);
    digitalWrite(DC_MOTOR_PIN2, LOW);
  }
}

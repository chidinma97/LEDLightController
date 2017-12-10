#include <SoftwareSerial.h>
#include <stdio.h>

SoftwareSerial mySerial(2,3); //TX, RX
#define RED_PIN 9
#define GREEN_PIN 10
#define BLUE_PIN 11

String string;
char command;

void setup() {
  Serial.begin(9600);
  Serial.println("Arduino started...");

  mySerial.begin(9600);
  pinMode(RED_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(BLUE_PIN, OUTPUT);
}


void loop() {
  if(mySerial.available()){
    string = "";
    }

    
    while(mySerial.available()){
      command = ((byte)mySerial.read());
      
      if(command == ':') {
        break;
      } else { 
        string += command;
      }
      delay(1);
    }


  if (string == "OFF") {
    // Turn off LED red
    digitalWrite(RED_PIN, LOW);
    digitalWrite(GREEN_PIN, LOW);
    digitalWrite(BLUE_PIN, LOW);
  } else if (string == "ON") {
    digitalWrite(RED_PIN, HIGH);
    digitalWrite(GREEN_PIN, HIGH);
    digitalWrite(BLUE_PIN, HIGH);
  }
  
  if(string.startsWith("*")){
    String value = string.substring(1);
    if(value.startsWith("RED")){
      value = value.substring(3);
      analogWrite(RED_PIN, value.toInt());
    } else if (value.startsWith("GREEN")) {
      value = value.substring(5);
      analogWrite(GREEN_PIN, value.toInt());
    } else if (value.startsWith("BLUE")) {
      value = value.substring(4);
      analogWrite(BLUE_PIN, value.toInt());
    }
      
  }
}

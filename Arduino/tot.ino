#include <SPI.h>
#include <MFRC522.h>
#include <LiquidCrystal.h>
#include <Servo.h>
#include <MemoryFree.h>

Servo servo;

const int rs = 12, en = 11, d4 = 7, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);                  //init lcd

const int trigPin = 9;  
const int echoPin = 10;             //init ultrasonic sensor 
float duration, distance;  
int k=1;
String receivedData;
String serialInput;


const int buzzer = 13;              //int buzzer

int IRSensor = 41;            // init IR sensor 

#define SS_PIN 53
#define RST_PIN 5         // Define pins for RFID

// Use Serial1 for Bluetooth communication
#define BLUETOOTH_SERIAL Serial1

MFRC522 rfid(SS_PIN, RST_PIN);          // Instance of the MFRC522 class

void setup() {
  Serial.begin(9600);            // Serial Monitor for debugging
  Serial2.begin(115200);         // Celalalt serial pentru IR sensor
  BLUETOOTH_SERIAL.begin(9600);  // Initialize HC-05 Bluetooth module

  pinMode(IRSensor, INPUT);
  servo.attach(6);        //attaches the servo on pin A0 to the Servo object
  k=1;
  servo.write(50);        //init pozitie servo                             50-->bariera ridicata,   0-->bariera coborata
  SPI.begin();                   // Initialize SPI bus
  rfid.PCD_Init();               // Initialize MFRC522 RFID reader

  Serial.println("Place an RFID card near the reader...");

  lcd.begin(16, 2); 
  pinMode(trigPin, OUTPUT);  
	pinMode(echoPin, INPUT);
  pinMode(buzzer, OUTPUT);
 // lcd.print("Distanta este:");

}

void loop() {
  digitalWrite(trigPin, LOW);  
	delayMicroseconds(2);  
	digitalWrite(trigPin, HIGH);  
	delayMicroseconds(10);
	digitalWrite(trigPin, LOW);  
  duration = pulseIn(echoPin, HIGH); 
  distance = (duration*.0343)/2; 
  //fct_senzor_ultras(distance);
  //Serial.println(distance);
  if(distance<40)
      fct_senzor_ultras(distance);
  else
  {
    lcd.clear();
    fct_lcd_afis(0, 0, "StarLight");
  }
  //fct_lcd_afis(0, 0, "Distanta este");
  //fct_lcd_afis(0, 1, String(distance));
  //Serial.println(fct_senzor_ir());

  if (rfid.PICC_IsNewCardPresent() && rfid.PICC_ReadCardSerial()) {
    Serial.print(F("RFID Tag UID: "));
    String rfidData = "";
    for (byte i = 0; i < rfid.uid.size; i++) {
      Serial.print(rfid.uid.uidByte[i] < 0x10 ? " 0" : " ");
      Serial.print(rfid.uid.uidByte[i], HEX);
      rfidData += String(rfid.uid.uidByte[i], HEX); 
      delay(100);                     
    }                                                                    //RFID card reading
    // Send RFID data via Bluetooth
    BLUETOOTH_SERIAL.println(rfidData);
    Serial.println("Sent via Bluetooth: " + rfidData);
    
  if(rfidData=="8e4d4a73" && k==0)
  {
    servo.write(50);
    k=1;
  }
  else if(rfidData=="8e4d4a73" && k==1)
  {
    servo.write(0);
    k=0;
  }
}
   

    // Opreste RFID card
    rfid.PICC_HaltA();
  

  if (BLUETOOTH_SERIAL.available()) {
    receivedData = BLUETOOTH_SERIAL.readStringUntil('\n'); // Read data from Bluetooth
    Serial.print("Received via Bluetooth: ");
    Serial.println(receivedData);
  }
  
  if(fct_senzor_ir()==0)
  {
    servo.write(0);
    k=0;
    if(digitalRead(IRSensor)==1)
      receivedData="0";
    else
      receivedData="1";
          exit;

  }else 
  {
    if(receivedData=="1")           //daca apesi 1 pe aplicatie, servo se roteste la 0 grade
    {
      servo.write(0);
      k=0;

    }  
    else if(receivedData=="0")        //daca apesi 2 pe aplicatie, servo se roteste la 90 grade
    {
        servo.write(50);
        k=1;
    }
  }
                                        //Handle Bluetooth communication

  

  // Handle Serial input (if needed)
  if (Serial.available()) {
    serialInput = Serial.readStringUntil('\n'); // Read from Serial Monitor
    BLUETOOTH_SERIAL.println(serialInput);            // Forward to Bluetooth
    Serial.println("Sent to Bluetooth: " + serialInput);                //daca scrii in serial monitor, se trimite pe aplicatie
  }

  delay(30);
  


  String rfidData = ""; // Clear the String variable


}   //END VOID LOOP

// Routine to print RFID data in HEX format


 void printHex(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}
    
 


void fct_lcd_afis(int cursor_linie,int cursor_coloana,String text_lcd)
{
  lcd.setCursor(cursor_linie, cursor_coloana);
  lcd.print(text_lcd);
}                                       //functie pentru afisare pe lcd

void fct_senzor_ultras(int distance)
{
  if(distance>40)                    //daca masina este foarte departe sau extrem de aproape
  {
    lcd.clear();
    lcd.print("Starlight");
    delay(1000);
    return;
    
  }
  //lcd.print(distance);
  else if(distance<5)                    //daca masina e foarte aproape, se aude un sunet mai des
  {
    lcd.clear();
    fct_lcd_afis(0, 0, "Distanta este");
    fct_lcd_afis(0, 1, String(distance));
    tone(buzzer, 2000);
    delay(distance*35);
    noTone(buzzer);  
  }
  else if(distance<9 && distance>5)             //daca masina se apropie, de aude un sunet mai rar
  {
    
    lcd.clear();
    fct_lcd_afis(0, 0, "Distanta este");
    fct_lcd_afis(0, 1, String(distance));
    tone(buzzer, 2000); // Send 1KHz sound signal...
    delay(distance*100);
    noTone(buzzer); // Send 1KHz sound signal...
  }
  
}

int fct_senzor_ir()
{
  int sensorStatus = digitalRead(IRSensor);
  
  return sensorStatus;
}
# DroidPPPwn
An android frontend that uses [PPPwn_cpp](https://github.com/xfangfang/PPPwn_cpp)

It includes a GUI, PPPwn_cpp binary specifically compiled for arm-android, and stage1.bin + stage2.bin for all supported firmwares (7.00-11.00).
The `stage2.bin` file is built from the original code of [PPPwn](https://github.com/TheOfficialFloW/PPPwn), so it's only a proof-of-concept that prints `PPPwned` on the PS4, and need to be replaced to launch additional `payloads`.

## Requirements
- A recent `rooted` Android phone that supports OTG
- An OTG USB Cable or Adapter
- An USB Ethernet Adapter
- An ethernet cable

## Usage
- Download the latest release from this repository and install to your android phone.
- On your PS4: follow the instructions from the original [PPPwn](https://github.com/TheOfficialFloW/PPPwn/blob/master/README.md) to configure the ethernet connection.
- Start DroidPPPwn application and select your PS4 firmware.
- Press `Start` button on the app and simultaneously X on your controller when you're on the `Test Internet Connection` screen.
- Wait until the exploit reaches the stage4 and the message is printed on your monitor
- If exploit fails click `Start` button again to stop it, and repeat again the last step

Example run here: [https://youtu.be/SQT7AgTtrDY](https://youtu.be/SQT7AgTtrDY)


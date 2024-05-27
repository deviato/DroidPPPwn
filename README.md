# DroidPPPwn
An android frontend that uses [PPPwn_cpp](https://github.com/xfangfang/PPPwn_cpp)

- It includes a GUI, PPPwn_cpp binary specifically compiled for arm-android, and stage1.bin + stage2.bin for all supported firmwares (7.00-11.00).
- For firmwares 9.00, 10.00, 10.01, 11.00 `stage2.bin` is built from [Sistr0](https://github.com/Sistr0/PPPwn) repository, i.e. you can now load custom payloads like GoldHEN.
- For all the other firmwares `stage2.bin` is built from the original code of [PPPwn](https://github.com/TheOfficialFloW/PPPwn), so at the moment it's only a proof-of-concept that prints `PPPwned` on the PS4.
- You can now use your own `stage2.bin` placing it to your sdcard root folder through `push stage2.bin /sdcard/`

## Requirements
- A recent `rooted` Android phone that supports OTG
- An OTG USB Cable or Adapter
- An USB Ethernet Adapter
- An ethernet cable
Alternatively, if your device has an embedded ethernet port, you can use that, as in the case of Android TV boxes.

## Usage
- Download the latest release from this repository and install to your android phone.
- On your PS4: follow the instructions from the original [PPPwn](https://github.com/TheOfficialFloW/PPPwn/blob/master/README.md) to configure the ethernet connection.
- Start DroidPPPwn application and select your PS4 firmware.
- Press `Start` button on the app and simultaneously X on your controller when you're on the `Test Internet Connection` screen.
- Wait until the exploit reaches the stage4 and the message is printed on your monitor
- If exploit fails click `Start` button again to stop it, and repeat again the last step

Example run here: [https://youtu.be/SQT7AgTtrDY](https://youtu.be/SQT7AgTtrDY)

## Known Bugs
On some old armv7 devices when installing apk the content of lib folder isn't copied to the internal app folder. You can solve it by extracting the files from pppwn.jar (choosing between lib/armeabi-v7a/* and lib/arm64-v8a/*, depending on your architecture) and pushing them manually to `/data/data/it.deviato.droidpppwn/lib/` remembering to give execute permissions to `pppwn` with `chmod 755 pppwn`
I will try to fix this on the next release, moving all assets in /data/local/tmp

## Changelog
### 1.1
- Added support for 32bit arm-v7a with separated binary of pppwn
- Refactored the whole project lowering minSdk to version 19, so now it can run on Android KitKat 4.4+
- Replaced stage2.bin for supported firmwares with those ones from Sistr0 repo to allow loading payloads
- Added the possibility to use your own stage2.bin
- Other small fixes
### 1.0
- Initial release

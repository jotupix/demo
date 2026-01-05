# jotupix_demo_android

## Overview

This demo project is used to communicate with **JotuPix devices via BLE**.
It supports:

* Program sending (including large program data)
* Device configuration
* Real-time device control

The project demonstrates a complete workflow from **BLE connection → protocol packaging → data transmission → response parsing → timeout handling**.

---


## Environment
OS: Windows /Mac os

Compiler: Android Studio Koala | 2024.1.1 Patch 1

Android SDK: minSdkVersion 21 ,targetSdkVersion 35

Language: java

---

## Project Structure

```
jotupix_demo_android/
│
├── example/        # Demo application module
├── jotupix/        # Core protocol & data processing module
└── library/        # Shared libraries used by the project
```

---

## jotupix Module Description

The **jotupix** module contains all protocol, data processing, and program abstraction logic.

### 1. Core Layer

These classes handle **low-level communication and protocol logic**.

#### JotuPix

Main device communication manager.

Responsibilities:

* Sending commands
* Sending programs (with retry & timeout)
* Parsing device responses
* Managing internal send state

---

#### JProtocol

Protocol-level abstraction.

Responsibilities:

* Frame encoding / decoding
* Escape character handling
* Start / end flag processing
* Interaction with `JPacket`

---

#### JPacket

Packet construction and parsing.

Responsibilities:

* Packet framing
* Packet ID management
* Payload length & data handling

---

#### JLzss

LZSS compression implementation.

Used for:

* Compressing program data before transmission

---

#### JCrc

CRC32 utility class.

Used for:

* Program data integrity validation

---

#### JInfo

Device information model.

Features:

* Parsed asynchronously from device responses

---

#### JTick

Tick / time utility.

Used for:

* Timeout detection
* Retry logic

---

### 2. Program Layer

This layer abstracts **display programs and content types**.

#### JProgramContent

Container for multiple display contents.

---

#### JContentBase

Abstract base class for all content types.

---

#### JTextContent

Monochrome text content.

---

#### JTextDiyColorContent

DIY multi-color text content.

---

#### JTextFullColorContent

Full-color text content.

---

#### JGifContent

GIF animation content.

---

#### JColor

Color definitions and helper utilities.

---

## example Module Description

The **example** module demonstrates how to use the `jotupix` module in a real Android application.

### DeviceManager

`DeviceManager` is responsible for:

* BLE connection management
* MTU configuration
* Data sending implementation

---

### 1. JSend Interface Implementation

`DeviceManager` implements the `JSend` interface.

```java
int send(byte[] data, int length);
```

This method receives raw protocol data from `JotuPix` and sends it to the BLE device.

---

### 2. JotuPix Initialization

When initializing `DeviceManager`:

```java
/***
 * init the JotusPix instance here  ,the DeviceManager  implements JProtocol.JSend interface
 *
 */
mJotuPix = new JotuPix();
mJotuPix.init(this);
```

This binds the protocol layer to the BLE sender.

Corresponding implementation:

```java
/**
 * @param sender Interface used to send raw data through
 *               the underlying communication layer.
 * @brief Initialize the JotuPix module.
 */
public void init(JProtocol.JSend sender) {
    protocol.init(sender, this);
}
```

---

### 3. Ticker (Timeout Handling)

`JotuPix` requires a **periodic tick** to handle:

* Program send timeouts
* Retry logic
* Internal state updates

```java
public void tick(long currTick) {
    currMsTick = currTick;
    if (proSender.sendStatus) {
        if (JTick.timeAfterEq(currMsTick, proSender.timeoutTick)) {
            proSender.sendStatus = false;
            Log.i(TAG, "Program transmission timed out, ended");
            if (proSender.callback != null) {
                proSender.callback.onEvent(
                    JSendProgramCallback.SendStatus.FAIL, 0
                );
            }
        }
    }
}
```

---

### 4.Send program

* for program :
  you need to call JotuPix's function of  sendProgram  to send program
```java
 CoolLED.getInstance().getExecutorService().submit(new Runnable() {
    @Override
    public void run() {
        /***
         * Create programContent
         * set parameter for programContent
         */
        JProgramContent programContents = new JProgramContent();
        JGifContent jGifContent = new JGifContent();
        jGifContent.blendType = JContentBase.BlendType.COVER;
        jGifContent.showX = 0;
        jGifContent.showY = 0;
        jGifContent.showWidth = DeviceManager.DEVICE_COLUMN;
        jGifContent.showHeight = DeviceManager.DEVICE_ROW;

        GifProcessor.processGifFrameByFrame(CoolLED.getInstance(), animationItem.imageId, jGifContent.showWidth, jGifContent.showHeight, new GifProcessor.GifProcessCallback() {
            @Override
            public void onSuccess(byte[] gifData) {
                CLog.i(TAG, "onSuccess>>>gifData.size"+gifData.length);
                /***
                 * get the gif  image data
                 */
                jGifContent.setGifData(gifData);
                programContents.add(jGifContent);
                byte[] programData = programContents.get();
                int programSize = programData.length;
                JotuPix.ProgramInfo programInfo = new JotuPix.ProgramInfo();
                programInfo.proIndex = 0;
                programInfo.proAllNum = 1;
                programInfo.compress = JotuPix.CompressFlag.COMPRESS_FLAG_UNDO;
                JotuPix.JProgramGroupNor programGroup = new JotuPix.JProgramGroupNor();
                programGroup.playType = JotuPix.PlayType.PLAY_TYPE_CNT;
                programGroup.playParam = 0;
                programInfo.groupParam = programGroup;
                /***
                 * the AnimationFragment implements JotuPix.JSendProgramCallback interface
                 *  the  function of  "void onEvent(int sendStatus, double percent)" will receive sending data progress and status
                 */
                DeviceManager.getInstance().getJotuPix().sendProgram(programInfo, programData, programSize, AnimationFragment.this);
            }

            @Override
            public void onError(Exception e) {
                CLog.i(TAG, "onError>>>"+e.getMessage());
            }
        });
    }
});
```

* for simple command:
  you need to call JotuPix's function  like the below functions:

  sendSwitchStatus

  sendBrightness

  sendScreenFlip

  getDevInfo(JGetDevInfoCallback callback)

---

### 5. Ticker Usage in Application

In the application class (`CoolLED`), the ticker is updated every **20 ms**.

```java
public class CoolLED extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        BleManager.getInstance().init(this);
        DeviceManager.getInstance();

        // Initialize ticker
        DeviceManager.getInstance()
                .getJotuPix()
                .tick(System.currentTimeMillis());

        updateTicker();
    }

    /**
     * Update the ticker every 20 ms
     */
    public void updateTicker() {
        postDelay(() -> {
            DeviceManager.getInstance()
                    .getJotuPix()
                    .tick(System.currentTimeMillis());
            updateTicker();
        }, 20);
    }
}
```

---

## Android BLE Configuration

### Key BLE Parameters

| Parameter           | Value                                  |
| ------------------- | -------------------------------------- |
| Service UUID        | `0000fff0-0000-1000-8000-00805f9b34fb` |
| Characteristic UUID | `0000fff1-0000-1000-8000-00805f9b34fb` |
| Max MTU             | 247                                    |
| Min MTU             | 23                                     |
| Max Write Size      | 180 bytes                              |
| Min Write Size      | 20 bytes                               |
| Packet Interval     | 15 ms                                  |

---

### MTU Configuration Logic

```java
private static final int BLE_MTU_MAX_SIZE = 247;
private static final int BLE_MTU_MIN_SIZE = 23;
private static final int BLE_WRITE_CHAR_MAX_SIZE = 180;
private static final int BLE_WRITE_CHAR_MIN_SIZE = 20;

private void setMtu(final BleDevice bleDevice) {

    CoolLED.getInstance().getExecutorService().submit(() -> {
        if (bleDevice.getName() != null &&
                bleDevice.getName().equalsIgnoreCase(DeviceManager.Cool_LED_UX)) {

            BleManager.getInstance().setMtu(
                    bleDevice,
                    BLE_MTU_MAX_SIZE,
                    new BleMtuChangedCallback() {

                        @Override
                        public void onSetMTUFailure(BleException exception) {
                            BleManager.getInstance()
                                    .setSplitWriteNum(BLE_WRITE_CHAR_MIN_SIZE);
                        }

                        @Override
                        public void onMtuChanged(int mtu) {
                            BleManager.getInstance()
                                    .setSplitWriteNum(BLE_WRITE_CHAR_MAX_SIZE);
                        }
                    }
            );
        } else {
            BleManager.getInstance()
                    .setSplitWriteNum(BLE_WRITE_CHAR_MIN_SIZE);
        }
    });
}
```

---

## Notes

* **MTU and write size directly affect program transmission success**
* Ensure `tick()` is called continuously
* BLE packet interval must not be too small
* Large programs rely heavily on retry and timeout handling

---

## Summary

This demo provides a complete reference for:

* BLE-based device communication
* Custom binary protocol implementation
* Large data transmission with retry & timeout
* Modular content and program abstraction

It is recommended to study `DeviceManager`, `JotuPix`, and `JProtocol` together to fully understand the data flow.

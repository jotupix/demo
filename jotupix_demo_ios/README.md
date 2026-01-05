# JotuPix Demo User Guide

## 1. Overview
This demo is used to communicate with JotuPix devices via serial port.
It supports program sending, device configuration, and real-time control.

## 2. Environment
- OS: iOS
- Compiler: XCode
- Language: OC

## 3. Build & Run
1. Open JotupixDemo.xcodeproj
2. Build and Run

## 4. Demo Directory Description
Directory Structure

root
│
├─Demo
│    ├─Ble -> Bluetooth function
│    ├─Lib -> Custom UI controls
│    └─device -> Multi-device management
└─jotupix
    ├─core -> Core communication and data processing modules
    ├─program -> Program and content abstraction layer.
    └─utils -> tool

---

## 5. Main Module Description

### core

Core communication and data processing modules.

- **JotuPix**  
  Main device communication class.  
  Handles command sending, program transmission, parsing responses, and timeout management.

- **JProtocol**  
  Encapsulates protocol-level send/receive logic.

- **JPacket**  
  Packet framing, packet IDs, payload management.

- **JLzss**  
  LZSS compression implementation for program data.

- **JCrc**  
  CRC32 calculation utility.

- **JInfo**  
  Device information model (parsed asynchronously from device).

- **JTick**  
  Tick/time utilities for timeout detection.

---

### program

Program and content abstraction layer.

- **JProgramContent**  
  Container for multiple display contents.

- **JContentBase**  
  Abstract base class for all content types.

- **JTextContent**  
  Monochrome text content.

- **JTextDiyColorContent**  
  DIY multi-color text content.

- **JTextFullColorContent**  
  Full-color text content.

- **JGifContent**  
  GIF animation content.

- **JColor**  
  Color definitions and helpers.


/*
    Multi - device management is not implemented here; please implement it yourself if needed.
*/ 
#pragma once

#include "jotupix.h"
#include <string>
#include <memory>
#include <map>
#include <mutex>
#include "JInfo.h"

class JDeviceManager
{
public:
    static JDeviceManager& Instance()
    {
        static JDeviceManager instance;
        return instance;
    }

    // Do not copy and assign
    JDeviceManager(const JDeviceManager&) = delete;
    JDeviceManager& operator=(const JDeviceManager&) = delete;

    void CreateDevice(IJSend* pfnSender);
    void RemoveDevice();
    std::shared_ptr<JotuPix> GetDevice();

    void SetDeviceInfoCache(JInfo info);
    JInfo &GetDeviceInfoCache();

private:
    JDeviceManager() {}
    ~JDeviceManager() {}

    std::shared_ptr<JotuPix> m_psDevice = nullptr;
    JInfo m_sDevInfo;
};


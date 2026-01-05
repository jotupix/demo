#include "JDeviceManager.h"

void JDeviceManager::CreateDevice(IJSend* pfnSender)
{
    std::shared_ptr<JotuPix> dev = std::make_shared<JotuPix>();
    dev->Init(pfnSender);

    m_psDevice = dev;
}

void JDeviceManager::RemoveDevice()
{
    m_psDevice = nullptr;
}

std::shared_ptr<JotuPix> JDeviceManager::GetDevice()
{
    return m_psDevice;
}

void JDeviceManager::SetDeviceInfoCache(JInfo info)
{
    m_sDevInfo = info;
}

JInfo& JDeviceManager::GetDeviceInfoCache()
{
    return m_sDevInfo;
}
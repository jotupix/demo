#include "pch.h"
#include "CProgramBase.h"
#include "JotuPixDlg.h"
#include "JDeviceManager.h"

#include "Log.h"

#include "resource.h"

void CProgramBase::onEvent(SendStatus eStatus, uint8_t u8Percent)
{
    if (m_sendProgressDlg == nullptr)
    {
        return;
    }

    if (eStatus == IJSendProgramCallback::SendStatus::PROGRESS)
    {
        m_sendProgressDlg->UpdateProgress(u8Percent);
    }
    else if (eStatus == IJSendProgramCallback::SendStatus::COMPLETED)
    {
        m_sendProgressDlg->UpdateProgress(100);
        delete m_sendProgressDlg;
        m_sendProgressDlg = nullptr;

        AfxMessageBox(_T("Send complete!"));

        //free(m_pu8Buffer);
        //m_pu8Buffer = nullptr;
    }
    else
    {
        delete m_sendProgressDlg;
        m_sendProgressDlg = nullptr;
        AfxMessageBox(_T("Send failed!"));

        //free(m_pu8Buffer);
        //m_pu8Buffer = nullptr;
    }
}

void CProgramBase::onCancelCallbck()
{
    CancelCallback();
}

void CProgramBase::CancelCallback()
{
    std::shared_ptr<JotuPix> device = JDeviceManager::Instance().GetDevice();

    if (device != nullptr)
    {
        device->CancelSendProgram();
    }

    delete m_sendProgressDlg;
    m_sendProgressDlg = nullptr;

    Log::Printf("Cancel send program!\r\n");
}

int CProgramBase::SendProgram(CWnd* pParent, JotuPix::ProgramInfo* psProInfo, const uint8_t* pu8ProData, uint32_t u32ProDataLen)
{
    // Waiting for the last send to finish
    if (m_sendProgressDlg != nullptr)
    {
        return 0;
    }

    std::shared_ptr<JotuPix> device = JDeviceManager::Instance().GetDevice();

    m_sendProgressDlg = new CSendProgressDlg(pParent);
    m_sendProgressDlg->Create(IDD_SEND_PROGRESS, pParent);
    m_sendProgressDlg->SetCancelCallback(this);
    m_sendProgressDlg->ShowWindow(SW_SHOW);

    int nRet = device->SendProgram(psProInfo, pu8ProData, u32ProDataLen, this);
    if (nRet < 0)
    {
        delete m_sendProgressDlg;
        m_sendProgressDlg = nullptr;

        AfxMessageBox(_T("Sending failed, please check the logs!"));
    }

    return nRet;
}
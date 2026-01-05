#pragma once

#include "CSendProgressDlg.h"
#include "jotupix.h"

class CProgramBase : public IJSendProgramCallback, public ISendProgressCancelCallback
{
protected:
    CSendProgressDlg* m_sendProgressDlg = nullptr;

    void onEvent(SendStatus eStatus, uint8_t u8Percent) override;
    void onCancelCallbck() override;

    virtual void CancelCallback();
    virtual int SendProgram(CWnd* pParent, JotuPix::ProgramInfo* psProInfo, const uint8_t* pu8ProData, uint32_t u32ProDataLen);
};


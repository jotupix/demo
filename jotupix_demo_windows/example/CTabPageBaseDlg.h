#pragma once
#include <afxdialogex.h>

class CTabPageBaseDlg : public CDialogEx
{
public:
    CTabPageBaseDlg(UINT nIDTemplate, CWnd* pParent = nullptr)
        : CDialogEx(nIDTemplate, pParent)
    {}

protected:
    virtual void OnOK() override {}     // Disable Enter to trigger closing
    virtual void OnCancel() override {} // Disable Esc / Close page
};
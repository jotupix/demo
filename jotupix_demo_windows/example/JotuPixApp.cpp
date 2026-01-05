#include "pch.h"
#include "framework.h"
#include "JotuPixApp.h"
#include "JotuPixDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CJotuPixApp

BEGIN_MESSAGE_MAP(CJotuPixApp, CWinApp)
	ON_COMMAND(ID_HELP, &CWinApp::OnHelp)
END_MESSAGE_MAP()


CJotuPixApp::CJotuPixApp()
{
	// Support restarting the manager
	m_dwRestartManagerSupportFlags = AFX_RESTART_MANAGER_SUPPORT_RESTART;

	// TODO: Add the constructor code here,
	// Place all important initializations in InitInstance
}


// The only CJotuPixApp object

CJotuPixApp theApp;


// CJotuPixApp initialization

BOOL CJotuPixApp::InitInstance()
{
	// If an application running on Windows XP specifies that it wants to use ComCtl32.dll version 6 or later to enable visual mode,
	//it needs to use InitCommonControlsEx().Otherwise, the window will not be created.
	INITCOMMONCONTROLSEX InitCtrls;
	InitCtrls.dwSize = sizeof(InitCtrls);
	// Set it to include all public control classes that you want to use in the application.
	InitCtrls.dwICC = ICC_WIN95_CLASSES;
	InitCommonControlsEx(&InitCtrls);

	CWinApp::InitInstance();


	AfxEnableControlContainer();

	// Create a shell manager in case the dialog box contains
	// Any shell tree view control or shell list view control.
	CShellManager *pShellManager = new CShellManager;

	// Activate the "Windows Native" visual manager to enable themes in MFC controls.
	CMFCVisualManager::SetDefaultManager(RUNTIME_CLASS(CMFCVisualManagerWindows));

	// Standard Initialization
	// If you are not using these features and wish to reduce
	// the final executable size, you should remove the following
	// Unnecessary specific initialization routines
	// Change the registry key used to store settings
	// TODO: This string should be modified appropriately,
	// for example, to the company or organization name.
	SetRegistryKey(_T("Application Wizard Generated Local Application"));

	CJotuPixDlg dlg;
	m_pMainWnd = &dlg;
	INT_PTR nResponse = dlg.DoModal();
	if (nResponse == IDOK)
	{

	}
	else if (nResponse == IDCANCEL)
	{
		
	}
	else if (nResponse == -1)
	{
		TRACE(traceAppMsg, 0, "WARNING: Dialog creation failed and the application will terminate unexpectedly.\n");
		TRACE(traceAppMsg, 0, "WARNING: If you are using an MFC control on a dialog, you cannot #define _AFX_NO_MFC_CONTROLS_IN_DIALOGS.\n");
	}

	// Delete the shell manager created above.
	if (pShellManager != nullptr)
	{
		delete pShellManager;
	}

#if !defined(_AFXDLL) && !defined(_AFX_NO_MFC_CONTROLS_IN_DIALOGS)
	ControlBarCleanUp();
#endif

	// Since the dialog box is closed, FALSE will be returned to exit the application.
	// Instead of the message pump that starts the application.
	return FALSE;
}


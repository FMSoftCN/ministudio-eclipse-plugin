#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <minigui/common.h>
#include <minigui/minigui.h>
#include <minigui/gdi.h>
#include <minigui/window.h>

#include <mgncs/mgncs.h>

#include "resource.h"
#include "ncs-windows.h"

HPACKAGE hPackage = HPACKAGE_NULL;

#ifdef _MGNCS_INCORE_RES
extern HPACKAGE ncsLoadIncoreResPackage(void);
extern GHANDLE ncsGetIncoreEtc(void);
extern GHANDLE ncsGetIncoreAppIniInfo(void);
#endif

int DlModuleInit(void)
{
#ifdef ntStartWindowEx
	MSG Msg;
	mMainWnd *mWin;

#ifdef _MGNCS_INCORE_RES
	ncsSetEtcHandle(ncsGetIncoreEtc());
	ncsSetAppIniInfo(ncsGetIncoreAppIniInfo());

	ncsInitialize();

	hPackage = ncsLoadIncoreResPackage();
#else
	char f_package[MAX_PATH];

	ncsSetAppIniInfo(ncsLoadAppIniInfo("$(projectName)"));

	ncsInitialize();
	SetResPath("./");

	if (!ncsGetResPackage("$(projectName).res", f_package, MAX_PATH)) {
		fprintf(stderr, "Haven't find the  Resource Package File.");
		return 1;
	}

	hPackage = ncsLoadResPackage (f_package);
#endif

	if (hPackage == HPACKAGE_NULL) {
#ifdef _MGNCS_INCORE_RES
		fprintf (stderr, "Error: load in-core resource package failure.\n");
#else
		fprintf (stderr, "Error: load resource package:%s failure.\n", f_package);
#endif
		return 1;
	}

	SetDefaultWindowElementRenderer(ncsGetString(hPackage, NCSRM_SYSSTR_DEFRDR));
#ifdef _MGRM_PROCESSES
    JoinLayer(NAME_DEF_LAYER , argv[0], 0 , 0);
#endif

	mWin = ntStartWindowEx(hPackage, HWND_DESKTOP, (HICON)0, (HMENU)0, (DWORD)0);

	while(GetMessage(&Msg, mWin->hwnd))
	{
		TranslateMessage(&Msg);
		DispatchMessage(&Msg);
	}

	MainWindowThreadCleanup(mWin->hwnd);

	ncsUnloadResPackage(hPackage);

	ncsUninitialize();
	ncsUnloadAppIniInfo();
#endif

	return 0;
}


void DlModuleDeinit(void)
{
}
